package lectures.part5ts

object Variance extends App {

  trait Animal
  class Dog extends Animal
  class Cat extends Animal
  class Crocodile extends Animal

  // What is variance?
  // "inheritance" - type substitution of generics

  class Cage[T] // the question is? Should I be able to pass a Cage[Cat] to a Cage[Animal]? Or in other words, should a Cage[Cat] be a Cage[Animal], and so
  // should a Cage[Cat] inherit from Cage[Animal]?

  // possible answers:
  // 1. yes - covariance
  class CCage[+T]
  val ccage: CCage[Animal] = new CCage[Cat] // yes, it is a Cage[Animal]

  // 2. no - invariance
  class ICage[T]
  //val icage: ICage[Animal] = new ICage[Cat] // no, it is not a Cage[Animal]
  // for the compiler is bad as val x: Int = "hello"
  val icage: ICage[Animal] = new ICage[Animal] // this works

  // 3. hell, no! - contravariance - the opposite of covariance
  class XCage[-T]
  val xcage: XCage[Cat] = new XCage[Animal] // yes, it is a Cage[Animal]

  class InvariantCage[T](val animal: T) // invariant

  // covariant positions
  class CovariantCage[+T](val animal: T) // COVARIANT POSITION, the generic type T is in a covariant position, and this means that
  // the compiler will only allow us to use T if it is a covariant type parameter

  // class ContravariantCage[-T](val animal: T)
  /*
  this not work because the compiler will not allow us to use T in a contravariant position
  it is like write:
  val catCage: XCage[Cat] = new XCage[Animal](new Crocodile)
   */

  // class CovariantVariableCage[+T](var animal: T) // types of vars are in CONTRAVARIANT POSITION
  /*
  this not work because the compiler will not allow us to use T in a contravariant position
  it is like write:
  val ccage: CCage[Animal] = new CCage[Cat](new Cat)
  ccage.animal = new Crocodile // it is not a Cat but we defined the animal as var
   */

  // class ContravariantVariableCage[-T](var animal: T) // also in COVARIANT POSITION
  /*
this not work because the compiler will not allow us to use T in a contravariant position
it is like write:
val catCage: XCage[Cat] = new XCage[Animal](new Cat)
catCage.animal = new Crocodile
 */

  class InvariantVariableCage[T](var animal: T) // ok

//  trait AnotherCovariantCage[+T] {
//    //def addAnimal(animal: T) // CONTRAVARIANT POSITION
//  }
  /*
  This not compile because the compiler will not allow us to use T in a contravariant position
  it is like write:
  val ccage: CCage[Animal] = new CCage[Dog]
  ccage.addAnimal(new Cat)
   */

  class AnotherContravariantCage[-T] {
    def addAnimal(animal: T) = true
  }
  val acc: AnotherContravariantCage[Cat] = new AnotherContravariantCage[Animal]
  acc.addAnimal(new Cat)
  // acc.addAnimal(new Dog) // this not works
  class Kitty extends Cat
  acc.addAnimal(new Kitty) // this works

  // how do we solve this?
  // this is a problem because we often want to create covariant collection

  class MyList[+A] {
    def add[B >: A](element: B): MyList[B] = new MyList[B] // B is a super type of A, so we force to put a super type of A, widening the type! So the arguments are in a contravariant position
  }

  val emptyList = new MyList[Kitty]
  val animals = emptyList.add(new Kitty) // this works, a list of Kitty now
  val moreAnimals = animals.add(new Cat) // this works, a list of Cat now
  val evenMoreAnimals = moreAnimals.add(new Dog) // this works, a list of Animal now

  // METHOD ARGUMENTS ARE IN CONTRAVARIANT POSITION

  // return types
  class PetShop[-T] {
    // def get(isItAPuppy: Boolean): T // METHOD RETURN TYPES ARE IN COVARIANT POSITION
    /*
    This not compile because the compiler will not allow us to use T in a contravariant position
    it is like write:
    val catShop: PetShop[Cat] = new PetShop[Animal] {
      def get(isItAPuppy: Boolean): Animal = new Cat
      }

    val dogShop: PetShop[Dog] = catShop
    val result = dogShop.get(true) // it is a Cat, an will return a Cat, but we expect a Dog
     */
    def get[S <: T](isItAPuppy: Boolean, defaultAnimal: S): S = defaultAnimal
  }

  val shop: PetShop[Dog] = new PetShop[Animal]
  // val evilCat = shop.get(true, new Cat) // the ide does not complain but the compiler does and will not compile

  class TerraNova extends Dog
  val bigFurry = shop.get(true, new TerraNova) // this works, because TerraNova is a Dog

  /*
  Big rule:
  - method arguments are in CONTRAVARIANT position
  - return types are in COVARIANT position
   */

  /*
  If for instance we analyze the standard library, the trait Function1[-T, +R] is contravariant in T (the argument type) and covariant in R
  (the return type)
   */

  /**
   * 1. Invariant, covariant, contravariant version of this API Parking[T](things: List[T]) {
   *  park(vehicle: T)
   *  impound(vehicles: List[T])
   *  checkVehicles(conditions: String): List[T]
   *  }
   *
   *  2. used someone else's API: IList[T] // invariant List
   *  3. Parking = monad!
   *  - flatMap
   *
   *
   */

  class Vehicle
  class Bike extends Vehicle
  class Car extends Vehicle

  class IList[T]

  // invariant
  class IParking[T](things: List[T]) {
    def park(vehicle: T): IParking[T] = ???
    def impound(vehicles: List[T]): IParking[T] = ???
    def checkVehicles(conditions: String): List[T] = ???
    def flatMap[S](f: T => IParking[S]): IParking[S] = ???
  }

  // covariant
  class CParking[+T](things: List[T]) {
    def park[S >: T](vehicle: S): CParking[S] = ??? // we are going to wide the type, TYPE WIDENING, the resulting type will be wider than the original type
    def impound[S >: T](vehicles: List[S]): CParking[S] = ??? // same above
    def checkVehicles(conditions: String): List[T] = ??? // we apply a condition to the list, so we are not changing the type
    def flatMap[S](f: T => CParking[S]): CParking[S] = ???
  }

  // contravariant
  class CCParking[-T](things: List[T]) {
    def park(vehicle: T): CCParking[T] = ??? // this is fine because T is contra-variant and it appears in a contra-variant position
    // def park[S <: T](vehicle: T): CCParking[T] = ??? this is correct but the use of [S <: T] is not necessary
    def impound(vehicles: List[T]): CCParking[T] = ??? // same above
    // def impound[S <: T](vehicles: List[T]): CCParking[T] = ??? this is correct but the use of [S <: T] is not necessary
    def checkVehicles[S <: T](conditions: String): List[S] = ??? // here we have to use [S <: T], because List is covariant and it follows the variance of T but T is
    // contravariant, so the List is contravariant, but appears in a covariant position
    // because we are returning a contravariant list
    // here we have to use [S <: T] because we are returning a contravariant list
    // so we have restricted the type
    def flatMap[R <: T, S](f: R => CCParking[S]): CCParking[S] = ???
    /*
    def flatMap[S](f: T => CCParking[S]): CCParking[S] = ??? // this is not gonna work because
    Contravariant type T occurs in covariant position in type T => Variance.CCParking[S] of value f
    But we said that the method arguments are in contravariant position, so the whole T => CCParking[S] must be in a
    contra-variant position. But T => CCParking[S] is a function type (is basically Function1[T, CCParking[S]]), and we know
    that function1 is contravariant in the first argument (in T, in this case) which means is in an inverse variance relationship
    in T. Which makes the position of T in a double contravariant position, which is covariant!
    And so we have to use a new type parameter R <: T, and we have to use R instead of T in the function type
     */
  }

  // exercise 2
  // used someone else's API: IList[T] // invariant List
  class ICarparking[T](things: IList[T]) {
    def park(vehicle: T): ICarparking[T] = ???
    def impound(vehicles: IList[T]): ICarparking[T] = ???
    def checkVehicles(conditions: String): IList[T] = ???
  }

  class CParking2[+T](things: IList[T]) {
    def park[S >: T](vehicle: S): CParking2[S] = ???
    def impound[S >: T](vehicles: IList[S]): CParking2[S] = ??? // because IList is invariant, why don't we remove [S >: T] and use only T instead? No! We cannot do it! Because we are
    // trying to put a wider type in a narrower type, so we have to use [S >: T]. The compiler says that we cannot use a covariant type parameter T in an invariant position! (remember
    // that the IList is invariant). So using the [S >: T] we are widening the type
    def checkVehicles[S >: T](conditions: String): IList[S] = ??? // same as above
  }

  class CCParking2[-T](things: IList[T]) {
    def park(vehicle: T): CCParking2[T] = ???
    def impound[S <: T](vehicles: IList[S]): CCParking2[T] = ??? // same reasoning as above, but reversed. We need to apply a type-restriction
    def checkVehicles[S <: T](conditions: String): IList[S] = ??? // same as above
  }



  /*
  Rule of thumb
  - use covariance (i.e. use a type S covariant) = (if you use that as a) COLLECTION OF THINGS. For example, if you use Parking as a collection of vehicles use covariance
  - use contravariance (i.e. use a type S contravariant) = (if you use that as a) GROUP OF ACTIONS. For example, if you want to use Parking as a group of actions, use contravariance

  In this mine-example, I would use a contravariant type parameter, because I want to use Parking as a group of actions, and I want to be able to park a vehicle, impound a list of vehicles, and check the vehicles
   */




}
