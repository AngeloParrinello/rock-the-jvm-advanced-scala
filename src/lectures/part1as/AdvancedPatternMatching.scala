package lectures.part1as

object AdvancedPatternMatching extends App {

  val numbers = List(1)
  val description = numbers match {
    case head :: Nil => s"The only element is $head."
    case _ =>
  }
  println(description)

  /*
    - constants
    - wildcards
    - case classes
    - tuples
    - some special magic like above
   */

  // for some reason we cannot make this class a case class but we want to use it in pattern matching
  class Person(val name: String, val age: Int)

  // how to do that? We start by creating a companion object
  // in this special case we could rename this companion object (to for example PersonPattern) but then
  // we should also rename the name in the pattern matching from Person(n, a) to PersonPattern(n, a)
  // is this really necessary? I don't think so, but there is this trick that we can use
  // because what we are doing here is called extractor and the compiler will look for an unapply method
  object Person {
    // having declared this method, we can now use it in pattern matching
    // because the compiler can split the Person object into its name and age
    def unapply(person: Person): Option[(String, Int)] = {
      if (person.age < 21) None
      else Some((person.name, person.age))
    }

    // we can define multiple unapply methods
    def unapply(age: Int): Option[String] = {
      Some(if (age < 21) "minor" else "major")
    }
  }

  val bob = new Person("bob", 25)
  val greeting = bob match {
    case Person(n, a) => s"Hi, my name is $n and I am $a years old."
    case _ => "I am not sure what to say."
  }
  println(greeting)

  val legalStatus = bob.age match {
    // quite unclear because it seems that there is a Person with a apply method that takes a status
    case Person(status) => s"My legal status is $status."
  }

  println(legalStatus)

  /*
    Exercise.
   */

  val n: Int = 45
  val mathProperty = n match {
    case x if x < 10 => "single digit"
    case x if x % 2 == 0 => "an even number"
    case _ => "no property"
  }

  println(mathProperty)

  // let's try to use this in a pattern matching
  object even {
    def unapply(arg: Int): Boolean = arg % 2 == 0
  }

  object singleDigit {
    def unapply(arg: Int): Boolean = arg < 10
  }

  // a quick way to define a test for pattern matching is to define a singleton object with an unapply method
  // that test a certain condition
  val n2: Int = 8
  val mathProperty2 = n2 match {
    case singleDigit() => "single digit"
    case even() => "an even number"
    case _ => "no property"
  }

  println(mathProperty2)

  // (our own) infix
  case class Or[A, B](a: A, b: B) // Either in Scala, similar to Option
  val either = Or(2, "two")
  val humanDescription = either match {
    // case Or(number, string) => s"$number is written as $string" // normal way but we can also do...
    case number Or string => s"$number is written as $string"
  }
  println(humanDescription)

  // decomposing sequences
  val vararg = numbers match {
    case List(1, _*) => "starting with 1" // _* is a vararg pattern, this needs unapplySeq
  }
  println(vararg)

  // unapply sequence
  abstract class MyList[+A] {
    def head: A = ???
    def tail: MyList[A] = ???
  }
  case object Empty extends MyList[Nothing]
  case class Cons[+A](override val head: A, override val tail: MyList[A]) extends MyList[A]

  object MyList {
    def unapplySeq[A](list: MyList[A]): Option[Seq[A]] = {
      if (list == Empty) Some(Seq.empty)
      else unapplySeq(list.tail).map(list.head +: _)
    }
  }

  val myList: MyList[Int] = Cons(1, Cons(2, Cons(3, Empty)))
  val decomposed = myList match {
    // this is possible because we have defined an unapplySeq method in the companion object
    // usually, I'd need the unapply method to do this, but since I use _* I need unapplySeq
    case MyList(1, 2, _*) => "starting with 1, 2"
    case _ => "something else"
  }

  // custom return types for unapply
  // let's start this example defining a wrapper with these two methods
  // isEmpty: Boolean, get: something
  // really really rare to see this in practice

  abstract class Wrapper[T] {
    def isEmpty: Boolean
    def get: T
  }

  object PersonWrapper {
    def unapply(person: Person): Wrapper[String] = new Wrapper[String] {
      def isEmpty: Boolean = false
      def get: String = person.name
    }
  }

  println(bob match {
    case PersonWrapper(n) => s"This person's name is $n."
    case _ => "An alien."
  })

  // Takeaways
  /*
  - we can define our own patterns --> we take the object we want to decompose, we create a specific unapply and then the results will be as an Option
  - infix patterns --> we can define our own infix patterns
  - unapplySeq --> we can define our own unapplySeq for pattern matching on varargs
  - custom return types for unapply --> we can define our own return types for unapply
   */

}
