package lectures.part4implicits

object ImplicitsIntro extends App {
  // the arrow here is an implicit method
  val pair = "Daniel" -> "555"

  case class Person(name: String) {
    def greet: String = s"Hi, my name is $name"
  }

  implicit def fromStringToPerson(string: String): Person = Person(string)

  // Why this works? Because the compiler is looking for an implicit conversion from String to Person
  println("Peter".greet) // println(fromStringToPerson("Peter").greet)

  class A {
    def greet: Int = 2
  }

  // implicit def fromStringToA(string: String): A = new A // then suddenly, the compiler will look for an implicit conversion from String to A
  // but it will not compile because the compiler will not know which implicit conversion to use
  // because there are two implicit conversions from String to A and from String to Person

  // implicit parameters
  def increment(x: Int)(implicit amount: Int): Int = x + amount

  implicit val defaultAmount = 10

  increment(2) // the compiler will look for an implicit value of type Int, and will find the defaultAmount
  // NOT the same as default arguments
}
