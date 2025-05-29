package lectures.part3concurrency

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Random, Success, Try}
// important for futures
import scala.concurrent.ExecutionContext.Implicits.global

object FuturesAndPromises extends App {

  private def calculateTheMeaningOfLife: Int = {
    Thread.sleep(2000)
    42
  }

  private val aFuture = Future {
    calculateTheMeaningOfLife // calculates the meaning of life on ANOTHER thread
  } // the apply method of the Future needs a context where to instantiate for the threads
  // (global) which is passed by the compiler, because is implicit

  println(aFuture.value) // Option[Try[Int]] --> most of the time, in this case, will return None

  println("Waiting on the future")

  aFuture.onComplete { // since it returns a Try, onComplete will receive a Try
    case Success(meaningOfLife) => println(s"The meaning of life is $meaningOfLife")
    case Failure(e) => println(s"I have failed with $e")
  } // this is a callback, we do not know which thread actually executes this

  Thread.sleep(3000) // important because otherwise the main thread will finish before the future completes

  // mini social network
  case class Profile(id: String, name: String) {
    def poke(anotherProfile: Profile): Unit = println(s"${this.name} poked ${anotherProfile.name}")
  }

  object SocialNetwork {
    // "database"
    val names = Map(
      "fb.id.1-zuck" -> "Mark",
      "fb.id.2-bill" -> "Bill",
      "fb.id.0-dummy" -> "Dummy"
    )
    val friends = Map(
      "fb.id.1-zuck" -> "fb.id.2-bill"
    )

    val random = new Random()

    //API
    def fetchProfile(id: String): Future[Profile] = Future {
      Thread.sleep(random.nextInt(300)) // simulate fetching from the DB
      Profile(id, names(id))
    }

    def fetchBestFriend(profile: Profile): Future[Profile] = Future {
      Thread.sleep(random.nextInt(400))
      val bestFriendId = friends(profile.id)
      Profile(bestFriendId, names(bestFriendId))
    }
  }

  // client: mark to poke bill
  val mark = SocialNetwork.fetchProfile("fb.id.1-zuck")
  mark.onComplete {
    case Success(markProfile) =>
      val bill = SocialNetwork.fetchBestFriend(markProfile)
      bill.onComplete {
        case Success(billProfile) => markProfile.poke(billProfile)
        case Failure(e) => e.printStackTrace()
      }
    case Failure(e) => e.printStackTrace()
  }

  Thread.sleep(1000)

  // works but nested callbacks are ugly and unreadable and error-prone
  // we would tackle a lot of problems with this approach

  // functional composition of futures
  // map, flatMap, filter
  val nameOnTheWall = mark.map(profile => profile.name)

  val marksBestFriend = mark.flatMap(profile => SocialNetwork.fetchBestFriend(profile))

  val zucksBestFriendRestricted = marksBestFriend.filter(profile => profile.name.startsWith("Z"))

  // for-comprehensions
  for {
    mark <- SocialNetwork.fetchProfile("fb.id.1-zuck")
    bill <- SocialNetwork.fetchBestFriend(mark)
  } yield mark.poke(bill) //MUCH MORE CLEANER THAN THE NESTED CALLBACKS ABOVE

  Thread.sleep(1000)

  // fallbacks, in case the thing didn't go as expected
  val aProfileNoMatterWhat =  SocialNetwork.fetchProfile("unknown id").recover {
    case e: Throwable => Profile("fb.id.0-dummy", "Forever Alone")
  }

  val aFetchedProfileNoMatterWhat = SocialNetwork.fetchProfile("unknown id").recoverWith {
    case e: Throwable => SocialNetwork.fetchProfile("fb.id.0-dummy") // we use recoverWith to return a "safe" result
  }

  val fallBackResult = SocialNetwork.fetchProfile("unknown id").fallbackTo(SocialNetwork.fetchProfile("fb.id.0-dummy"))

  // online banking app
  case class User(name: String)
  case class Transaction(sender: String, receiver: String, amount: Double, status: String)

  object BankingApp {
    val name = "Rock the JVM banking"

    def fetchUser(name: String): Future[User] = Future {
      // simulate fetching from the DB
      Thread.sleep(500)
      User(name)
    }

    def createTransaction(user:User, merchantName: String, cost: Double): Future[Transaction] = Future {
      // simulate some processes
      Thread.sleep(1000)
      Transaction(user.name, merchantName, cost, "success")
    }

    def purchase(username: String, item: String, merchantName: String, cost: Double): String = {
      // fetch the user from the DB
      // create a transaction
      // wait for the transaction to finish
      val transactionStatusFuture = for {
        user <- fetchUser(username)
        transaction <- createTransaction(user, merchantName, cost)
      } yield transaction.status

      import scala.concurrent.Await
      import scala.concurrent.duration._
      Await.result(transactionStatusFuture, 2.seconds) // this seems a magic: the method .seconds() seems to be added to Int by magic
      // because, clearly the Int class does not have this method
      // how the heck is this possible?? This is due to the magic of pimp my library and implicit conversions pattern
      // this is blocking, we should avoid this
    }
  }

  println(BankingApp.purchase("Daniel", "iPhone 12", "rock the jvm store", 3000))

  // futures ARE the functional way to deal with concurrency and asynchronous programming

  // promises

  val promise = Promise[Int]() // "controller" over a future
  val future = promise.future // this is the future that the promise will control

  // thread 1 - "consumer"
  // the consumer knows how to handle the future
  future.onComplete {
    case Success(r) => println(s"[consumer] I've received $r")
  }

  // thread 2 - "producer"
  val producer = new Thread(() => {
    println("[producer] crunching numbers...")
    Thread.sleep(500)
    // "fulfilling" the promise
    promise.success(42)
    println("[producer] done")
  })

  // we see "crunching numbers..." and then
  // basically both the producer and the consumer are going to print their messages
  // at the same time, because the producer is a thread and the consumer is another thread
  // because thanks to the promise, we can separate the producer and the consumer
  // so as soon as the producer is done, the consumer will be notified
  // without concurrency issues
  producer.start()
  Thread.sleep(1000)

  /*
  1) fulfill a future immediately with a value
  2) inSequence(fa, fb)
  3) first(fa, fb) => new future with the first value of the two futures)
  4) last(fa, fb) => new future with the last value of the two futures)
  5) retryUntil(action: () => Future[T], condition: T => Boolean): Future[T]
   */

  // 1 - fulfill a future immediately with a value
  def fulfillImmediately[T](value: T): Future[T] = Future(value)

  // 2 - inSequence(fa, fb)
  def inSequence[A, B](first: Future[A], second:  Future[B]): Future[B] =
    first.flatMap(_ => second)

  // 3 - first(fa, fb)
  def first[A](fa: Future[A], fb: Future[A]): Future[A] = {
    val promise = Promise[A]

    /*
    def tryComplete(promise: Promise[A], result: Try[A]) = result match {
      case Success(r) => try {
        promise.success(r)
      } catch {
        case _ =>
      }
      case Failure(e) => try {
        promise.failure(e)
      } catch {
        case _ =>
      }

      fa.onComplete(result => tryComplete(promise, result))
      fb.onComplete(result => tryComplete(promise, result))
     */

    /*
    There is already a tryComplete method in the Promise class
            |
            |
            V
     */

    // The first future to complete will complete the promise
    // in this tryComplete method, we are trying to complete the promise with the result of the futures, but the first future
    // that fulfill the promise will complete the promise and this means that the other future will not be able to complete the promise
    // throwing an error, for this reason is important to use the tryComplete method of the promise
    // THIS IS A NON-DETERMINISTIC BEHAVIOR! NOT RECOMMENDED
    fa.onComplete(promise.tryComplete)
    fb.onComplete(promise.tryComplete)

    promise.future
  }

  // 4 - last(fa, fb)
  def last[A](fa: Future[A], fb: Future[A]): Future[A] = {
    // 1 promise which both futures will try to complete
    // 2 promise which the last future will complete
    val bothPromise = Promise[A]
    val lastPromise = Promise[A]

    val checkAndComplete = (result: Try[A]) =>
      if (!bothPromise.tryComplete(result))
        lastPromise.complete(result)

    fa.onComplete(checkAndComplete)
    fb.onComplete(checkAndComplete)

    // return the value of the last promise
    lastPromise.future
  }

  // 5 - retryUntil
  def retryUntil[A](action: () => Future[A], condition: A => Boolean): Future[A] =
    action()
      .filter(condition) // if the condition is met, the future will complete
      .recoverWith {
        case _ => retryUntil(action, condition) // if the condition is not met, we will retry until the condition is met
      }

  val fast = Future {
    Thread.sleep(100)
    42
  }

  val slow = Future {
    Thread.sleep(200)
    45
  }

  first(fast, slow).foreach(println)
  last(fast, slow).foreach(println)

  Thread.sleep(1000)

  val random = new Random()
  val action = () => Future {
    Thread.sleep(100)
    val nextValue = random.nextInt(100)
    println(s"generated $nextValue")
    nextValue
  }

  retryUntil(action, (x: Int) => x < 10).foreach(result => println(s"settled at $result"))

  /*
  Future[T] is a computation which will finish at some point in the future
  - non-blocking, asynchronous computation
  -map, flatMap, filter with for-comprehensions are allowed, so they are monads
  - falling back
  - blocking if necessary
   */

  /*
  Futures are immutable "read-only" objects
  Promises are "writable-once" objects containers over a future

   */
}
