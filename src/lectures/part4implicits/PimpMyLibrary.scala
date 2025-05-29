package lectures.part4implicits

import scala.language.implicitConversions

object PimpMyLibrary extends App {

  // 2.isPrime is not a method of Int, but we can make it so
  implicit class RichInt(val value: Int) {
    // it must take one and only one parameter
    def isEven: Boolean = value % 2 == 0
    def sqrt: Double = Math.sqrt(value)

    // exercise method
    def times(function: () => Unit): Unit = {
      def timesAux(n: Int): Unit =
        if (n <= 0) ()
        else {
          function()
          timesAux(n - 1)
        }

      timesAux(value)
    }

    def *[T](list: List[T]): List[T] = {
      def concatenate(n: Int): List[T] =
        if (n <= 0) List()
        else concatenate(n - 1) ++ list

      concatenate(value)
    }
  }

  new RichInt(42).sqrt // we can say that but also...
  42.isEven // we can say that too, it is translated by the compiler in new RichInt(42).isEven

  // type enrichment = pimping (my library)

  1 to 10 // this is a range, but it is not a method of Int, it is a method of the RichInt class, implicitly imported by the compiler

  import scala.concurrent.duration._

  // another example
  3.seconds

  // compiler DOES NOT do multiple implicit searches, if I define another implicit class RicherInt, the compiler will not know which one to use
  //  implicit class RicherInt(val value: Int) {
  //    def isOdd: Boolean = value % 2 != 0
  //  }
  // 42.isOdd // this will not compile

  /*
    Exercise: Enrich the String class
      - asInt
      - encrypt
      - decrypt

    Keep enriching the Int class
    - times(function)
      3.times(() => ...)
      - *
      3 * 2 => 6
   */

  implicit class RichString(string: String) {
    def asInt: Int = Integer.valueOf(string) // this is a Java method, java.lang.Integer.valueOf
    def encrypt(cypherDistance: Int): String = string.map(c => (c + cypherDistance).asInstanceOf[Char])
    def decrypt(cypherDistance: Int): String = string.map(c => (c - cypherDistance).asInstanceOf[Char])
  }

  println("3".asInt + 4) // 7
  println("John".encrypt(2)) // Lqjp

  3.times(() => println("Scala rocks!")) // it will print "Scala rocks!" 3 times
  println(4 * List(1, 2)) // List(1, 2, 1, 2, 1, 2, 1, 2)

  // "3" / 4 // this will not compile because the compiler does not know how to divide a string by an integer
  // but we can do so (like in Javascript) by adding an implicit conversion!

  implicit def stringToInt(string: String): Int = Integer.valueOf(string)
  // the mechanism is the same, the compiler will look for an implicit conversion from String to Int, and it will find it
  println("6" / 2) // 3, stringToInt("6") / 2

  // but we can also say...
  // and this will be the same as
  // implicit class RichAltInt(value: Int)
  class RichAlternativeInt(value: Int)
  implicit def enrich(value: Int): RichAlternativeInt = new RichAlternativeInt(value)

  // although the implicit conversion are very powerful, they can also be dangerous, because they can make the code less readable
  // adn so they are discouraged in the Scala community

  // for example
  // DANGER ZONE
  implicit def intToBoolean(i: Int): Boolean = i == 1

  /*
  For C programmers, this is a very common pattern, but it is not recommended in Scala
    if (n) do something
    else do something else
   */

  val aConditionValue = if (3) "OK" else "Something wrong"
  println(aConditionValue) // Something wrong
  // and this is generally not true, but the takeaway here is that if there is a bug in an implicit conversion, it can be very hard to debug

  /*
  PIMP MY LIBRARY RESPONSIBLY
  Tips:
  - keep type enrichment to implicit classes and type classes
  - avoid implicit defs as much as possible
  - package implicits clearly, bring them into scope only when you need them
  - if you need conversions, make them specific
   */



}
