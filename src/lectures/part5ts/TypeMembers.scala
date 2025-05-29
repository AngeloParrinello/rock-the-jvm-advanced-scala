package lectures.part5ts

object TypeMembers extends App {

    class Animal
    class Dog extends Animal
    class Cat extends Animal

    class AnimalCollection {
      type AnimalType // abstract type member
      type BoundedAnimal <: Animal // abstract type member bounded to Animal. The actual implementation of this must be a subclass of Animal
      type SuperBoundedAnimal >: Dog <: Animal // abstract type member bounded
      type AnimalC = Cat // alias
    }

  val ac = new AnimalCollection
  // val dog: ac.AnimalType = ??? // this is an abstract type, so we can't instantiate it
  // val cat: ac.BoundedAnimal = new Cat // this will not compile

  val pup: ac.SuperBoundedAnimal = new Dog // this will compile
  // but type aliases are fine
  val cat: ac.AnimalC = new Cat // this will compile

  // type aliases work outside the class
  type CatAlias = Cat
  val anotherCat: CatAlias = new Cat

  // type aliases often used in practice to avoid name collisions importing different libraries

  // abstract type members are sometimes used in type classes
  // ALTERNATIVE TO GENERIC CLASSES
  trait MyList {
    type T
    def add(element: T): MyList
  }

  class NonEmptyList(value: Int) extends MyList {
    override type T = Int
    override def add(element: Int): MyList = ???
  }

  // .type (dot type) is a type member that is the type of the instance itself
  type CatsType = cat.type
  val newCat: CatsType = cat
  // but I can only do associations and now instantiation
  // val newerCat: CatsType = new Cat // this will not compile

  /*
    Exercise - enforce a type to be applicable to SOME TYPES only
   */
    trait MList {
      type A
      def head: A
      def tail: MList
    }

    trait ApplicableToNumbers {
      type A <: Number
    }

  // Should not compile
//  class CustomList(hd: String, tl: CustomList) extends MList {
//    override type A = String
//    override def head: String = hd
//    override def tail: CustomList = tl
//  }

  // Should compile
  class IntList(hd: Int, tl: IntList) extends MList {
    override type A = Int
    override def head: Int = hd
    override def tail: IntList = tl
  }

//  trait MListNumbers {
//    type A <: Number
//    def head: A
//    def tail: MListNumbers
//  }
//
//  // should compile
//  class CustomListNumbers(hd: Int, tl: CustomListNumbers) extends MListNumbers {
//    override type A = Int
//    override def head: Int = hd
//    override def tail: CustomListNumbers = tl
//  }
//
//  class CustomListNumbers2(hd: String, tl: CustomListNumbers2) extends MListNumbers {
//    override type A = String
//    override def head: String = hd
//    override def tail: CustomListNumbers2 = tl
//  }

}
