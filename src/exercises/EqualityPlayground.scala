package exercises

import lectures.part4implicits.TypeClasses.{User, john}

object EqualityPlayground {
  /*
  Exercise
   */

  // type class
  trait Equal[T] {
    def apply(a: T, b: T): Boolean
  }

  // type class instances
  implicit object NameEquality extends Equal[User] {
    override def apply(a: User, b: User): Boolean = a.name == b.name
  }

  object FullEquality extends Equal[User] {
    override def apply(a: User, b: User): Boolean = a.name == b.name && a.email == b.email
  }

  /*
Exercise: implement the type class pattern for the Equality type class
 */

  // make the upper Equal trait implicit
  object Equal {
    def apply[T](a: T, b: T)(implicit equalizer: Equal[T]): Boolean = equalizer.apply(a, b)
  }

  val john2 = User("John", 32, "anotherjohn@jvm.rock")
  println(Equal(john, john2))

  // Exercise type classes 3
  /*
  Exercise - improve the Equal TC with an implicit conversion class
  ===(anotherValue: T)
  !==(anotherValue: T)
   */

  implicit class TypeSafeEqual[T](value: T) {
    def ===(other: T)(implicit equalizer: Equal[T]): Boolean = equalizer.apply(value, other)
    def !==(other: T)(implicit equalizer: Equal[T]): Boolean = !equalizer.apply(value, other)
  }

  println(john === john2) // john.===(john2) --> new TypeSafeEqual[User](john).===(john2) --> new TypeSafeEqual[User](john).===(john2)(NameEquality)
  /*
  THIS IS TYPE SAFE! I CAN SAY:
   */
  println(john == 43) // this will compile but with a warning
  // println(john === 43) // this will not compile at all! Because is TYPE SAFE!
  println(john !== john2)

}
