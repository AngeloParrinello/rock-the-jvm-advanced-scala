package lectures.part5ts

object StructuralTypes extends App {

  // structural types
  type JavaClosable = java.io.Closeable

  class HipsterCloseable {
    def close(): Unit = println("yeah yeah I'm closing")
  }

  // let's say we want to implement a method that accepts a closeable both from Java and Scala
  // def closeQuietly(closeable: JavaClosable OR HipsterCloseable): Unit = closeable.close() // would be nice to have a union type

  type UnifiedCloseable = {
    def close(): Unit
  } // STRUCTURAL TYPE

  def closeQuietly(unifiedCloseable: UnifiedCloseable): Unit = unifiedCloseable.close()

  closeQuietly(new JavaClosable {
    override def close(): Unit = println("Java closes")
  })

  closeQuietly(new HipsterCloseable)

  // TYPE REFINEMENTS
  // this is a new syntax! We're saying that we want to refine the type of the JavaClosable
  // in other words an AdvancedCloseable is a JavaClosable PLUS an additional method called closeSilently
  // we're enriching the JavaClosable type
  type AdvancedCloseable = JavaClosable {
    def closeSilently(): Unit
  }

  class AdvancedJavaCloseable extends JavaClosable {
    override def close(): Unit = println("Java closes")

    def closeSilently(): Unit = println("Java closes silently")
  }

  def closeShh(advCloseable: AdvancedCloseable): Unit = advCloseable.closeSilently()
  /*
  The compiler here says: Ok AdvancedCloseable originates from a JavaClosable, so it must have a close method
  and it must have a closeSilently method. The AdvancedJavaCloseable has both methods, so it is a valid
   */
  closeShh(new AdvancedJavaCloseable)

  // but if I define a new Hipster class with closeSilently method:
  class HipsterCloseable2 {
    def close(): Unit = println("yeah yeah I'm closing")

    def closeSilently(): Unit = println("not making noise")
  }
  // I could not use it in the closeShh method because it is not a JavaClosable
  // because it does not originate from JavaClosable
  // closeShh(new HipsterCloseable2) // this will not compile


  // using structural types as standalone types
  def altClose(closeable: {def close(): Unit}): Unit = closeable.close()
  // in other words this thing here { def close(): Unit } is its own type!!

  // let's look at the structural types in the context of the compiler type-checking => duck typing
  // similar in spirit to the dynamic typing in Python
  type SoundMaker = {
    def makeSound(): Unit
  }

  class Dog {
    def makeSound(): Unit = println("bark")
  }

  class Car {
    def makeSound(): Unit = println("vroom")
  }

  // from a structural type perspective, the Dog and Car are SoundMakers
  // so from the compiler point of view, I can see
  val dog: SoundMaker = new Dog
  val car: SoundMaker = new Car

  // static duck typing
  /*
  The compiler is fine on this as long as the types on the right hand side are conformed to the stcture defined on
  the left hand side. This is called static duck typing. Javascript and Python are familiar with this concept,
  used typically in the dynamic languages. But in Scala, we can use this concept in a static way, because the compiler
  is smart enough to check the structure of the types on the right hand side and see if they conform to the structure.
  The scala compiler do the Duck-test (i.e. if something sounds like a duck, flies like a duck, looks like a duck, then we
  can consider it as a duck) at compile time
   */

  // but with a big caveat: the structural types in general are based on reflection, that's how the scala compiler
  // is able to guarantee at compile time that the structure of the types on the right hand side conform to the structure
  // because at runtime, the program will inspect the presents of the makesound method and it will invoke at runtime
  // so they are not as efficient as the nominal types

  /*
  Exercise
   */
   trait CBL[+T] {
     def head: T
     def tail: CBL[T]
   }

  class Human {
    def head: Brain = new Brain
  }

  class Brain {
    override def toString: String = "BRAINZ!"
  }

  def f[T](somethingWithAHead: {def head: T}): Unit = println(somethingWithAHead.head)

  /*
  f is compatible with a CBL and with a Human? Yes, because the Human has a head method
   */

  case object CBNil extends CBL[Nothing] {
    def head: Nothing = ???
    def tail: CBL[Nothing] = ???
  }

  case class CBCons[T](head: T, tail: CBL[T]) extends CBL[T]

  f(CBCons(2, CBNil))
  f(new Human) // But what is the type T??!?! It is Brain! And the compiler is smart enough to infer that

  // second exercise
  object HeadEqualizer {
    type Headable[T] = {def head: T}

    def ===[T](a: Headable[T], b: Headable[T]): Boolean = a.head == b.head
  }

  /*
  is compatible with a CBL and with a Human? Yes, because the Human has a head method
   */

  val brainzList = CBCons(new Brain, CBNil)

  HeadEqualizer.===(brainzList, new Human) // this will compile because the Human has a head method
  // problem: we can say
  val stringsList = CBCons("brainz", CBNil)
  HeadEqualizer.===(stringsList, new Human) // in this case the first argument has a T type of String and the second argument
  // has a T type of Brain, but the compiler will not complain
  // but this is a problem due to the reflection, because it will discover at runtime that the types are not the same
  // SO BE VERY CAREFUL!!!!


}