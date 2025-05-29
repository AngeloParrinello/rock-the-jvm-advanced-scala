package lectures.part2afp

object LazyEvaluation extends App {

  // val x = throw new RuntimeException // This will throw an exception
  // but if I use lazy val, it will not throw an exception
  lazy val y = throw new RuntimeException // This will not throw an exception
  // lazy DELAYS the evaluation of values
  // if I actually use the value of y, it will throw an exception
  // println(y)
  lazy val z = {
    println("hello")
    42
  }
  println(z)  // this will print hello and 42
  println(z) // this will print 42
  // lazy vals are evaluated once and once only when use for the first time
  // because they are stored in memory

  // examples of implications:
  // 1. side effects
  def sideEffectCondition: Boolean = {
    println("Boo")
    true
  }
  def simpleCondition: Boolean = false

  lazy val lazyCondition = sideEffectCondition
  println(if (simpleCondition && lazyCondition) "yes" else "no")
  // What happened here? the second condition is not evaluated because the first condition is false
  // so the compiler does not evaluate the second condition and as a consequence, the side effect is not printed
  // since it is a lazy val and it is not evaluated

  // 2. in conjunction with call by name
  def byNameMethod(n: => Int): Int = n + n + n + 1
  def retrieveMagicValue = {
    // side effect or a long computation
    println("waiting")
    Thread.sleep(1000)
    42
  }

  println(byNameMethod(retrieveMagicValue)) // this will print waiting 3 times and 127
  // the retrieveMagicValue is evaluated 3 times because it is a call by name parameter
  // so it has no really sense to use a call by name in this case because it is evaluated 3 times
  // we should use lazy val instead

  def lazyMethod(n: => Int): Int = {
    lazy val t = n
    t + t + t + 1
  }
  def retrieveMagicValueWithLazy = {
    // side effect or a long computation
    println("waiting")
    Thread.sleep(1000)
    42
  }

  println(lazyMethod(retrieveMagicValueWithLazy)) // this will print waiting only once and 127
  // this is a technique to avoid the evaluation of the call by name parameter multiple times
  // we use a lazy val to store the value of the call by name parameter and then we use it
  // so the call by name parameter is evaluated only once
  // THIS IS A TECHNIQUE CALLED CALL BY NEED
  // Very useful when you want to evaluate your parameter only when you need but use the same value in the rest of the code

  // filtering with lazy vals
  def lessThan30(i: Int): Boolean = {
    println(s"$i is less than 30?")
    i < 30
  }

  def greaterThan20(i: Int): Boolean = {
      println(s"$i is greater than 20?")
      i > 20
    }

  val numbers = List(1, 25, 40, 5, 23)
  val lt30 = numbers.filter(lessThan30) // BTW this ETA expansion convert it to a function value (val). this will print 1 is less than 30? 25 is less than 30? 40 is less than 30? 5 is less than 30? 23 is less than 30?

  val gt20 = lt30.filter(greaterThan20) // this will print 1 is less than 30? 25 is less than 30? 40 is less than 30? 5 is less than 30? 23 is less than 30? 1 is greater than 20? 25 is greater than 20? 5 is greater than 20? 23 is greater than 20?
  println(gt20) // this will print List(25, 23)

  val lt30lazy = numbers.withFilter(lessThan30) // this will not print anything, .withFilter uses lazy vals under the hood
  val gt20lazy = lt30lazy.withFilter(greaterThan20) // this will not print anything
  println
  println(gt20lazy) // this won't return List(25, 23) because withFilter does not return a list, it returns a special object
  // did not evaluate the elements inside of it yet but you can use it as a collection and then, once you traverse the object,
  // it will evaluate the elements inside of it
  // scala.collection.IterableOps$WithFilter@66d33a
  gt20lazy.foreach(println) // this will print 25 and 23

  // for-comprehensions use withFilter with guards
  for {
    a <- List(1, 2, 3) if a % 2 == 0 // use lazy vals!
  } yield a + 1
  // this is equivalent to
  List(1, 2, 3).withFilter(_ % 2 == 0).map(_ + 1) // List[Int]

  // Exercise: implement a lazily evaluated, singly linked STREAM of elements.
  // naturals = MyStream.from(1)(x => x + 1) = stream of natural numbers (potentially infinite!)
  // naturals.take(100) // lazily evaluated stream of the first 100 naturals (finite stream)
  // naturals.take(100).foreach(println) // will force the evaluation of the first 100 elements
  // naturals.foreach(println) // will crash - infinite!
  // naturals.map(_ * 2) // stream of all even numbers (potentially infinite), but not crash because it is lazily evaluated

}
