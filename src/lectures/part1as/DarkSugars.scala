package lectures.part1as

import scala.reflect.runtime.universe.Try

object DarkSugars extends App {

  // syntax sugar #1: methods with single param
  def singleArgMethod(arg: Int): String = s"$arg little ducks..."
  val description = singleArgMethod {
    // this is a block of code
    42
  }

  val aTryInstance = Try { // java's try {...}
    throw new RuntimeException
  }

  // same above but for map, flatmap, etc.
  List(1, 2, 3).map { x =>
    x + 1
  }

  // syntax sugar #2: single abstract method pattern
  trait Action {
    def act(x: Int): Int
  }

  val anInstance: Action = new Action {
    override def act(x: Int): Int = x + 1
  }

  // in Java are called functional interfaces
  // just write the lambda!
  val aFunkyInstance: Action = (x: Int) => x + 1 // magic

  // example: Runnables
  val aThread = new Thread(new Runnable {
    override def run(): Unit = println("Hello, Scala")
  })

  val aSweeterThread = new Thread(() => println("Sweet, Scala!"))

  // and this thing works for any abstract class with a single abstract method

  abstract class AnAbstractType {
    def implemented: Int = 23
    def f(a: Int): Unit
  }

  val anAbstractInstance: AnAbstractType = (a: Int) => println("sweet")

  // syntax sugar #3: the :: and #:: methods are special

  val prependedList = 2 :: List(3, 4) // List(3, 4).::(2)
  // 2.::(List(3, 4)) not possible because there is no method :: in Int
  // the :: method is available in List, so is List(3, 4).::(2)
  // the scala spec allows the compiler to rewrite 2 :: List(3, 4) to List(3, 4).::(2)
  // because  the associativity of the method is defined by the last character of the method name
  // if the method ends with a colon, it's right associative
  // if the method ends with anything else, it's left associative
  // so we can write...
  val _ = 1 :: 2 :: 3 :: List(4, 5)
  val _ = List(4, 5).::(3).::(2).::(1) // equivalent

  class MyStream[T] {
    def -->:(value: T): MyStream[T] = this // actual implementation here
  }

  val myStream = 1 -->: 2 -->: 3 -->: new MyStream[Int]

  // syntax sugar #4: multi-word method naming
  // rarely used in practice
  class TeenGirl(name: String) {
    def `and then said`(gossip: String): Unit = println(s"$name said $gossip")
  }

  val lilly = new TeenGirl("Lilly")
  lilly `and then said` "Scala is so sweet!"

  // syntax sugar #5: infix types
  // rarely used in practice
  class Composite[A, B]
  val composite: Composite[Int, String] = ???
  val composite2: Int Composite String = ???

  class -->[A, B]
  val towards: Int --> String = ???

  // syntax sugar #6: update() is very special, much like apply()
  val anArray = Array(1, 2, 3)
  anArray(2) = 7 // rewritten to anArray.update(2, 7)
  // used in mutable collections
  // remember apply() and update() are special methods in Scala

  // syntax sugar #7: setters for mutable containers
  class Mutable {
    private var internalMember: Int = 0 // private for OO encapsulation
    def member: Int = internalMember // "getter"
    def member_=(value: Int): Unit = internalMember = value // "setter"
  }

  val aMutableContainer = new Mutable
  aMutableContainer.member = 42 // rewritten as aMutableContainer.member_=(42)
}
