package lectures.part5ts

object FBoundedPolymorphism extends App {
  // "hey I have a class hierarchy, how do I force a method in a supertype to accept a "current type"?
//  trait Animal {
//    def breed: List[Animal]
//  }
//
//  class Cat extends Animal {
//    override def breed: List[Animal] = ??? // List[Cat]
//  }
//
//  // and the same for Dog, Cow, etc.
//  // I want to enforce the return type to be the same as the current type
//  class Dog extends Animal {
//    override def breed: List[Animal] = ??? // List[Dog]
//  }

  // many possible solutions to this problem

  // Solution 1 - Naive
  // we can easily change the return type like:
//  trait Animal {
//    def breed: List[Animal]
//  }
//
//  class Cat extends Animal {
//    override def breed: List[Cat] = ??? // List[Cat]
//  }
//
//  // and the same for Dog, Cow, etc.
//  // I want to enforce the return type to be the same as the current type
//  class Dog extends Animal {
//    override def breed: List[Dog] = ??? // List[Dog]
//  }
//
//  // and this is fine, but what happens if I did this:
//  class Dog2 extends Animal {
//    override def breed: List[Cat] = ??? // List[Dog]
//  }
  // this is also a valid code but it's wrong!
  // so we want that the compiler do that stuff for us


  // Solution 2 - FBP (F-Bounded Polymorphism)
//  trait Animal[A <: Animal[A]] { // recursive type: F-Bounded Polymorphism
//    def breed: List[Animal[A]]
//  }
//
//  class Cat extends Animal[Cat] {
//    override def breed: List[Animal[Cat]] = ??? // List[Cat]
//  }
//
//  // and the same for Dog, Cow, etc.
//  // I want to enforce the return type to be the same as the current type
//  class Dog extends Animal[Dog] {
//    override def breed: List[Animal[Dog]] = ??? // List[Dog]
//  }
//
//  // often presents in ORM frameworks
//  // such as:
//  // trait Entity[E <: Entity[E]]
//
//  // or often used for comparison
//  class Person extends Comparable[Person] { // recursive type: F-Bounded Polymorphism
//    override def compareTo(o: Person): Int = ???
//  }
//
//  // but we can still write...
//  class Crocodile extends Animal[Dog] {
//    override def breed: List[Animal[Dog]] = ??? // but we want a List of Crocodile...
//  }

  // so the problem now is: how do I enforce that the class I'm defining and the type I'm passing are the same?

  // Solution 3 - FBP + self-types
//  trait Animal[A <: Animal[A]] { self: A => // the self-type is the current type
//    def breed: List[Animal[A]]
//  }
//
//  class Cat extends Animal[Cat] {
//    override def breed: List[Animal[Cat]] = ??? // List[Cat]
//  }
//
//  // and the same for Dog, Cow, etc.
//  // I want to enforce the return type to be the same as the current type
//  class Dog extends Animal[Dog] {
//    override def breed: List[Animal[Dog]] = ??? // List[Dog]
//  }

  // and now I cannot write
  //  class Crocodile extends Animal[Dog] {
  //    override def breed: List[Animal[Dog]] = ??? // but we want a List of Crocodile...
  //  }
  // because the compiler will say that Dog does not conform to Crocodile

  // HOWEVER, we can still write...
//  trait Fish extends Animal[Fish]
//  class Shark extends Fish {
//    override def breed: List[Animal[Fish]] = List(new Cod) // this is wrong
//  }
//  class Cod extends Fish {
//    override def breed: List[Animal[Fish]] = ???
//  }
  // and now, we found a limitation of F-Bounded Polymorphism that we cannot solve
  // BUT...

  // Solution 4 - type classes!
//  trait Animal
//  // Step 1: define the type class description
//  trait CanBreed[A] {
//    def breed(a: A): List[A]
//  }
//
//  trait CanSmell[A] {
//    def smell(a: A): String
//  }
//
//  trait CanWinAgainst[A, B] {
//    def winAgainst(a: A, b: B): Boolean
//  }
//
//  class Dog extends Animal
//  // Step 2: define type class instance
//  object Dog {
//    implicit object DogsCanBreed extends CanBreed[Dog] {
//      def breed(a: Dog): List[Dog] = List()
//    }
//    implicit object DogsCanSmell extends CanSmell[Dog] {
//      def smell(a: Dog): String = "sniff"
//    }
//    implicit object DogsCanWinAgainstCats extends CanWinAgainst[Dog, Cat] {
//      def winAgainst(a: Dog, b: Cat): Boolean = true
//    }
//  }
//  // Step 3: converting Dog to some implicit class which has a method that can receive an argument of CanBreed[Dog]
//  // as an implicit parameter
//  implicit class CanBreedOps[A](animal: A) {
//    def breed(implicit canBreed: CanBreed[A]): List[A] = canBreed.breed(animal)
//  }
//  implicit class CanSmellOps[A](animal: A) {
//    def smell(implicit canSmell: CanSmell[A]): String = canSmell.smell(animal)
//  }
//  implicit class CanWinAgainstOps[A, B](animal: A) {
//    def winAgainst(b: B)(implicit canWinAgainst: CanWinAgainst[A, B]): Boolean = canWinAgainst.winAgainst(animal, b)
//  }
//
//  val dog = new Dog
//  dog.breed // List[Dog]!
//  dog.smell // sniff
//  dog.winAgainst(new Cat) // true
//  // dog.winAgainst(new Dog) // false, and does not compile
//  /*
//  the compiler says:
//
//  new CanBreedOps[Dog](dog).breed(Dog.DogsCanBreed)
//
//  implicit value to pass to breed: Dog.DogsCanBreed
//   */
//
//  // and now the compiler yells at me if I try to do this:
//  class Cat extends Animal
//  object Cat {
//    implicit object CatsCanBreed extends CanBreed[Dog] {
//      def breed(a: Dog): List[Dog] = List()
//    }
//  }
//  val cat = new Cat
  // cat.breed // List[Dog]! but we want List[Cat]!
  // but, a good critic is that now we catch all the type errors at compile time
  // but at the same time we have to write a lot of boilerplate code
  // and split the code in many places

  // so let's try to fix this little con
  // Solution 5 - type classes with type class instances
  // by keeping in the API here (in this case the Animal) the methods that we want to use
  trait Animal[A] { // the animal A is the type class itself (pure type classes)
    def breed(a: A): List[A]
  }

  class Dog
//  object Dog {
//    implicit object DogAnimal extends Animal[Dog] {
//      def breed(a: Dog): List[Dog] = List()
//    }
//  }
object Dog {
  implicit object DogAnimal extends Animal[Dog] {
    def breed(a: Dog): List[Dog] = List()
  }
}

  implicit class AnimalOps[A](animal: A) {
    def breed(implicit animalTypeClassInstance: Animal[A]): List[A] = animalTypeClassInstance.breed(animal)
  }

  val dog = new Dog
  dog.breed

}
