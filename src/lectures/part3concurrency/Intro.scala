package lectures.part3concurrency

object Intro extends App {
  // JVM threads
  /*
  interface Runnable {
    public void run()
  }
   */
//  val aThread = new Thread(() => println("I'm running in parallel"))
//  aThread.start() // gives the signal to the JVM to start a JVM thread
//  // create a JVM thread => which runs on top of OS thread
//  // start() => run the JVM thread
//  // call a runnable.run() does not do anything in parallel with the current thread
//  aThread.join() // blocks until aThread finishes running
//
//  val threadHello = new Thread(() => (1 to 5).foreach(_ => println("hello")))
//  val threadGoodbye = new Thread(() => (1 to 5).foreach(_ => println("goodbye")))
//  threadHello.start()
//  threadGoodbye.start()
  // different runs produce different results!
  // threads scheduling depends on the OS and JVM

  // executors
  // val pool = Executors.newFixedThreadPool(10)
  // pool.execute(() => println("something in the thread pool"))

  // pool.execute(() => {
  //   Thread.sleep(1000)
  //   println("done after 1 second")
  // })

  // pool.execute(() => {
  //   Thread.sleep(1000)
  //   println("almost done")
  //   Thread.sleep(1000)
  //   println("done after 2 seconds")
  // })

  // pool.shutdown() // no more actions can be submitted to the pool
  // pool.execute(() => println("should not appear")) // throws an exception in the calling thread

  // pool.shutdownNow()
  // println(pool.isShutdown) // true

  //   def runInParallel = {
  //     var x = 0
  //
  //     val thread1 = new Thread(() => x = 1)
  //     val thread2 = new Thread(() => x = 2)
  //
  //     thread1.start()
  //     thread2.start()
  //     println(x)
  //   }
  //
  //   for (_ <- 1 to 100) runInParallel

  // race condition! The above runInParallel function is not thread safe

//  class BankAccount(var amount: Int) {
//    override def toString: String = "" + amount
//  }
//
//  def buy(account: BankAccount, thing: String, price: Int) = {
//    account.amount -= price
////     println("I've bought " + thing)
////     println("my account is now " + account)
//  }
//
//  for (_ <- 1 to 1000) {
//    val account = new BankAccount(50000)
//    val thread1 = new Thread(() => buy(account, "shoes", 3000))
//    val thread2 = new Thread(() => buy(account, "iphone12", 4000))
//
//    thread1.start()
//    thread2.start()
//    Thread.sleep(10)
//    if (account.amount != 43000) println("AHA: " + account.amount)
//  }

  /*
  Thread1 (shoes) : 50000
    - account = 50000 - 3000 = 47000
  Thread2 (iphone) : 50000
    - account = 50000 - 4000 = 46000 overwrites the memory of account.amount
    And indeed sometimes we get 46000 and sometimes 47000 which is not good at all
   */

  // option #1: use synchronized()
  // no two threads can interfere with each other
//  def buySafe(account: BankAccount, thing: String, price: Int) =
//    account.synchronized {
//      // no two threads can evaluate this at the same time
//      account.amount -= price
//      println("I've bought " + thing)
//      println("my account is now " + account)
//    }

  // option #2: use @volatile
  // Note that @volatile protects only against reads and writes of the variable, so concurrent single operations are safe
  // The -= operator is not a single operation: read, compute, write. So it is not safe to use @volatile
//  @volatile var amount = 50000

  /**
   * Exercises
   *
   * 1) Construct 50 "inception" threads
   *    Thread1 -> thread2 -> thread3 -> ...
   *    println("hello from thread #3")
   *
   *    in REVERSE ORDER
   *
   */

    // exercise 1
  def inceptionThreads(maxThreads: Int, i: Int = 1): Thread =
    new Thread(() => {
          if (i < maxThreads) {
            val newThread = inceptionThreads(maxThreads, i + 1)
            newThread.start()
            newThread.join()
          }
          println(s"Hello from thread $i")
        })

  inceptionThreads(50).start()

  // exercise 2
  var x = 0
  val threads = (1 to 100).map(_ => new Thread(() => x += 1))
  threads.foreach(_.start())

  /**
   * 1) What is the biggest value possible for x? 100, quite straightforward
   * 2) What is the smallest value possible for x? 1, the threads work in parallel and the last one to write the value. So in theory, all the thread works together and in a very rare case, all
   * the thread overwrite the value of x to 1 at the same time, so 99 threads lag behind and the last one to write the value is 100
   */

  // exercise 3 sleep fallacy
  var message = ""
  val awesomeThread = new Thread(() => {
    Thread.sleep(1000)
    message = "Scala is awesome"
  })

  message = "Scala sucks"
  awesomeThread.start()
  Thread.sleep(2000)
  println(message)
  // What is the value of message? Almost always "Scala is awesome"

  /*
  What happened?
  - the main thread writes the "Scala sucks" message
  - the awesome thread starts
  - the main thread sleeps for 2 seconds
  - awesome thread finishes and writes the "Scala is awesome" message
  - the main thread wakes up and prints the message
   */

  // How to fix it? The best choice until now is using the join method

}
