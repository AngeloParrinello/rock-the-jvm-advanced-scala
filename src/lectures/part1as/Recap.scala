package lectures.part1as

object Recap extends App {

  val aCondition: Boolean = false
  val aConditionedVal = if (aCondition) 42 else 65

  // instructions vs expressions
  // instructions = something you tell the computer to do (like a print statement) --> imperative
  // expressions = something that has a value or a type (like a function) --> functional
  // compiler can infer types, and in this case infers the type of aCodeBlock to be Int
  val aCodeBlock = {
    if (aCondition) 54
    // the value of a code block is the value of the last expression
    56
  }

  // Unit = void
  val theUnit = println("Hello, Scala")

  // functions
  def aFunction(x: Int): Int = x + 1

  // recursion: stack and tail
  @scala.annotation.tailrec
  def factorial(n: Int, acc: Int): Int =
    if (n <= 0) acc
    else factorial(n - 1, n * acc)

  // OOP
  class Animal
  // inheritance
  class Dog extends Animal
  // OO polymorphism by subtyping
  val aDog: Animal = new Dog // polymorphism

  // abstract classes
  // this is a class that cannot be instantiated
  // it can have both abstract and non-abstract members
  // has methods that define a common behavior for all its subclasses
  trait Carnivore {
    def eat(animal: Animal): Unit
  }

  class Crocodile extends Animal with Carnivore {
    override def eat(animal: Animal): Unit = println("Crunch!")
  }

  // method notations
  val aCroc = new Crocodile
  aCroc.eat(aDog)
  aCroc eat aDog // natural language

  // anonymous classes
  // just for this use case
  val aCarnivore = new Carnivore {
    override def eat(animal: Animal): Unit = println("Roar!")
  }
  // under the hood the compiler creates a new class that extends Carnivore

  // generics
  // the + here signals that MyList is covariant
  abstract class MyList[+A] // variance and variance problems in THIS course

  // singletons and companions
  object MyList
  // the pair of (abstract or not) class and object are called companions objects

  // case classes
  case class Person(name: String, age: Int)

  // exceptions
  val throwsException: Nothing = throw new RuntimeException // Nothing
  val aPotentialFailure = try {
    throw new RuntimeException("I'm innocent, I swear!") // Nothing
  } catch {
    case e: Exception => "I caught an exception!"
  } finally {
    // side effects
    println("Some logs")
  }

  // functional programming
  val incrementer = new Function1[Int, Int] {
    override def apply(v1: Int): Int = v1 + 1
  }

  val incremented = incrementer(42)
  val anonymousIncrementer = (x: Int) => x + 1
  // map, flatMap, filter
  val processedList = List(1, 2, 3).map(anonymousIncrementer) // map is a higher order function
  // flatMap is a higher order function that takes a function that returns a list and concatenates all the lists

  // for-comprehensions
  val aList = List(1, 2, 3)
  val flatMapped: Seq[Int] = aList.flatMap { x =>
    List(x, x + 1)
  }
  val flatMapped2 = for {
    x <- aList
  } yield List(x, x + 1)

  val pairs = for {
    num <- List(1, 2, 3)
    char <- List('a', 'b', 'c')
  } yield num + "-" + char

  // Scala collections: Seqs, Arrays, Lists, Vectors, Maps, Tuples
  val aMap = Map(
    "Daniel" -> 789,
    "Jess" -> 555
  )

  // "collections": Options, Try
  val anOption = Some(2)
  val aTry = scala.util.Try {
    throw new RuntimeException
  }

  // pattern matching
  val x = 2
  // like a switch in other languages
  val order = x match {
    case 1 => "first"
    case 2 => "second"
    case 3 => "third"
    case _ => x + "th"
  }

  val bob = Person("Bob", 22)
  val greeting = bob match {
    case Person(n, _) => s"Hi, my name is $n"
    case _ => "I don't know my name"
  }

}
