package lectures.part4implicits

object OrganizingImplicits extends App {

  println(List(1, 4, 2, 3).sorted) // sorted needs an implicit ordering
  // but we do not provide it! How does it work?
  // The compiler will look for an implicit value of type Ordering[Int]
  // and it will find in scala.Predef, which is imported by default

  implicit val reverseOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _)
  println(List(1, 4, 2, 3).sorted) // List(4, 3, 2, 1)

  // if we define another implicit ordering, the compiler will not know which one to use
  // implicit val normalOrdering: Ordering[Int] = Ordering.fromLessThan(_ < _)

  /*
    Implicits (used as implicit parameters):
      - val/var
      - object
      - accessor methods = defs with no parentheses. This last one in particular is very important! If I define
      implicit def reverseOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _)
      this will be considered from the compiler as an implicit, but if I define
      implicit def reverseOrdering(): Ordering[Int] = Ordering.fromLessThan(_ > _)
      this will not be considered as an implicit!!
   */

  // Exercise
  case class Person(name: String, age: Int)

  val persons = List(
    Person("Steve", 30),
    Person("Amy", 22),
    Person("John", 66)
  )

  implicit val orderingPersons: Ordering[Person] = Ordering.fromLessThan((a, b) => a.name.compareTo(b.name) < 0)
  println(persons.sorted) // it will not compile because the compiler does not know how to sort Person objects, but if we define an implicit ordering for Person, it will work

  /*
    Implicit scope
      - normal scope = LOCAL SCOPE (highest priority, where we write code)
      - imported scope
      - companions of all types involved in the method signature. Let's take as an example the sorted method
      def sorted[B >: A](implicit ord: Ordering[B]): List[A]
        - List
        - Ordering
        - all the types involved = A or any supertype
   */
  // Having saying that, if we put the implicit ordering of the persons in an object, it will not work
//  object SomeObject {
//    implicit val orderingPersons: Ordering[Person] = Ordering.fromLessThan((a, b) => a.name.compareTo(b.name) < 0)
//  }
  // But if we put it in the Person class, it will work
//  object Person {
//    implicit val orderingPersons: Ordering[Person] = Ordering.fromLessThan((a, b) => a.name.compareTo(b.name) < 0)
//  }

  /*
  object Person {
    implicit val orderingPersons: Ordering[Person] = Ordering.fromLessThan((a, b) => a.name.compareTo(b.name) < 0)
  }
  implicit val ageOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.age < b.age)
  println(persons.sorted) // which one will be used? The compiler will use the implicit with higher priority,
  which is the one defined in the same scope, so the ageOrdering
   */

  /*
  object AlphabeticNameOrdering {
    implicit val orderingPersons: Ordering[Person] = Ordering.fromLessThan((a, b) => a.name.compareTo(b.name) < 0)
    }
  object AgeOrdering {
    implicit val ageOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.age < b.age)
  }
//  define an object for each implicit ordering and import the one you want to use, if you have multiple implicit orderings
//  that you are likely going to use the same time
  import AlphabeticNameOrdering._
  println(persons.sorted)
   */

  /*
  Best practice: When defining an implicit val
  # 1
  - If there is a single possible value for it and you can edit the code for which you are defining the implicit, define it in the companion object of the type
  # 2
  - If there are many possible values for it but a single good one and you can still edit the code for the type,
  define the good one in the companion object of the type and the others in the companion object of the type class
  # 3
  - If there are many possible values and you are going to use them a similar amount of times, define them in an object each one and import the one you want to use
   */

  /*
  Exercise

  - totalPrice = most used (50%)
  - by unit count = 25%
  - by unit price = 25%
  Organize them and make them available to use in the right order
   */
  case class Purchase(nUnits: Int, unitPrice: Double)

  // this will get by default because it is in its companion object
  object Purchase {
    implicit val totalPriceOrdering: Ordering[Purchase] = Ordering.fromLessThan((a, b) => a.nUnits * a.unitPrice < b.nUnits * b.unitPrice)
  }

  object UnitCountOrdering {
    implicit val unitCountOrdering: Ordering[Purchase] = Ordering.fromLessThan((a, b) => a.nUnits < b.nUnits)
  }

  object UnitPriceOrdering {
    implicit val unitPriceOrdering: Ordering[Purchase] = Ordering.fromLessThan((a, b) => a.unitPrice < b.unitPrice)
  }


}
