package lectures.part4implicits

object TypeClasses extends App {

  // we are backenders for a social network
  trait HTMLWritable {
    def toHTML: String
  }

  case class User(name: String, age: Int, email: String) extends HTMLWritable {
    override def toHTML: String = s"<div>$name ($age yo) <a href=$email/> </div>"
  }

  User("John", 32, "john@rockthejvm.com").toHTML

  /* disadavantages
    1 - for the types WE write (User, Post, Message) we can define toHTML only once. So for the other types
    for instance the java standard dates or whatever, we would need to define a convverter which is almost never beautiful
    2 - ONE implementation out of quite a number
   */

  // option 2 - pattern matching
//  object HTMLSerializerPM {
//    def serializeToHTML(value: Any) = value match {
//      case User(n, a, e) =>
//      case java.util.Date =>
//      case _ =>
//    }
//  }

  /*
  disadavantages
    1 - lost type safety
    2 - need to modify the code every time
    3 - still ONE implementation
   */

  // option 3 - type classes
  trait HTMLSerializer[T] {
    def serialize(value: T): String
  }

//  object UserSerializer extends HTMLSerializer[User] {
//    override def serialize(user: User): String = s"<div>${user.name} (${user.age} yo) <a href=${user.email}/> </div>"
//  }
  val john = User("John", 32, "john@rockthejvm.com")
  println(UserSerializer.serialize(john))

  // 1 we can define serializers for other types
  import java.util.Date
  object DateSerializer extends HTMLSerializer[Date] {
    override def serialize(date: Date): String = s"<div>${date.toString}</div>"
  }

  // 2 we can define multiple serializers for a single type
  object PartialUserSerializer extends HTMLSerializer[User] {
    override def serialize(user: User): String = s"<div>${user.name}</div>"
  }

  // this is called TYPE CLASS (the trait), whereas the objects are called TYPE CLASS INSTANCES
  // the static type checking controls at compile time that we passed in a function the correct type
  // the type class extends this concept and allows us to define a function that can be applied to a type
  // it is applied to a type in order to belong to a specific type class

//  object Equal {
//    def apply[T](a: T, b: T)(implicit equalizer: Equal[T]): Boolean = equalizer.apply(a, b)
//  }

  // THE POWER OF IMPLICITS IN THE TYPE CLASS
  // PART 2
  object HTMLSerializer {
    def serialize[T](value: T)(implicit serializer: HTMLSerializer[T]): String = {
      serializer.serialize(value)
    }

    // even better design is to have inside the base serializer (a simple factory method, the apply)
    // that returns the serializer, that returns the correct serializer and in this way we can access the other methods
    // of the serializer
    def apply[T](implicit serializer: HTMLSerializer[T]) = serializer
  }

  implicit object IntSerializer extends HTMLSerializer[Int] {
    override def serialize(value: Int): String = s"<div style='color: blue;'>$value</div>"
  }

  println(HTMLSerializer.serialize(42)(IntSerializer)) // we need to declare the serializer, but why not make the compiler
  // do this for us?
  // making the compiler do this for us is called IMPLICIT RESOLUTION
  // make the object implicit
  println(HTMLSerializer.serialize(42)) // the compiler will look for the implicit object of type HTMLSerializer[Int]

  implicit object UserSerializer extends HTMLSerializer[User] {
    override def serialize(user: User): String = s"<div>${user.name} (${user.age} yo) <a href=${user.email}/> </div>"
  }

  println(HTMLSerializer.serialize(john)) // the compiler searches for the implicit object of type HTMLSerializer[User]

  // with the apply we can say...
  // access to the entire type class interface
  println(HTMLSerializer[User].serialize(john))


  //let's re-define the Type Class
  trait MyTypeClassTemplate2[T] {
    def action(value: T): String
  }

  //let's re-define the Type Class Instances
  object MyTypeClassTemplate2 {
    def apply[T](implicit instance: MyTypeClassTemplate2[T]) = instance
  }

  // AD-HOC polymorphism, the example above is a form of polymorphism
  // simple classes polymorphism does not allow us to define a function that can be applied to a type
  // the compiler takes care of invoking the correct method for the correct type, the correct type class instances

  // Lesson 3 Type Classes
  implicit class HTMLEnrichment[T](value: T) {
    // note that the 'implicit' keyword allows me to explicitly pass the serializer but also to use the implicit one so
    // without defining it explicitly during the invocation (see below the examples)
    def toHTML(implicit serializer: HTMLSerializer[T]): String = {
      serializer.serialize(value)
    }
  }

  // println(john.toHTML(UserSerializer)) // convert to HTML with the serializer for User // re-written by the compiler as
  // println(new HTMLEnrichment[User](john).toHTML(UserSerializer))
  println(john.toHTML)
  /*
    - extend to new types
    - choose implementation (either by importing the implicit object or by passing it explicitly)
    - super expressive
   */

  println(2.toHTML) // because of the implicit class, the compiler will look for the implicit object of type HTMLSerializer[Int]

  // println(john.toHTML(PartialUserSerializer))

  // The type classes pattern is composed by three main components:
  /*
    - type class itself --- HTMLSerializer[T] { .. }
    - type class instances (some of which are implicit) --- UserSerializer, IntSerializer
    - conversion with implicit classes --- HTMLEnrichment
   */

  // context bounds
  def htmlBoilerplate[T](content: T)(implicit serializer: HTMLSerializer[T]): String = { // this signature is quite verbose and heavy, isn't it?
    s"<html><body>${content.toHTML(serializer)}</body></html>"
  }

  // i can rename it as...
  // in the method body, I can't use the serializer because, there is the context bound (the [T : HTMLSerializer]),
  // which is telling the compiler to inject here an implicit parameter of type HTMLSerializer of type T.
  // the disantavege is that I can't use the serializer in the method body
  def htmlSugar[T : HTMLSerializer](content: T): String = { // this is a context bound, it is a syntactic sugar
    val serializer = implicitly[HTMLSerializer[T]]
    s"<html><body>${content.toHTML(serializer)}</body></html>"
  }

  // the final step is .. implicitly!
  // the implicitly method is a method that is defined in the Predef object, which is imported by default in every Scala
  // program, so I can use it without importing anything
  def htmlSugar2[T : HTMLSerializer](content: T): String = { // this is a context bound, it is a syntactic sugar
    val serializer = implicitly[HTMLSerializer[T]]
    // use the serializer
    s"<html><body>${content.toHTML(serializer)}</body></html>"
  }

  case class Permissions(mask: String)
  implicit val defaultPermissions: Permissions = Permissions("0744")

  // in some other part of the code, I want to know what is the default permission, even is implicitly defined
  val standardPerms = implicitly[Permissions]

  /*
  Type classes takeaways
  - The type class is nothing but a trait which takes a type parameter and just defines some operations on that type
  trait MyTypeClassTemplate[T] {
    def action(value: T): String
  }
  - the second step is to define the type class instances, which are objects that implement the type class (often implicit)
  implicit object MyTypeClassInstance extends MyTypeClassTemplate[Int] {..}
  - Then we have two options to use the type class:
    - the first one is to define a companion object for the type class, which has an apply method that returns the type class instance,
    which will allow to use a type class instance without us knowing that is there
    object MyTypeClassTemplate {
      def apply[T](implicit instance: MyTypeClassTemplate[T]) = instance
    }
    MyTypeClassTemplate[Int].action(42)
    - the second way is to go a pimp-my-library pattern, which is to define an implicit class that enriches the type we want to
    that also takes an implicit parameter of the type class
    implicit class MyTypeClassEnrichment[T](value: T) {
      def myAction(implicit myTypeClassInstance: MyTypeClassTemplate[T]): String = myTypeClassInstance.action(value)
    }
    42.myAction
    - type class pattern is a way to achieve ad-hoc polymorphism
    - type classes are a powerful tool in Scala
    - you can define type classes and type class instances with implicits
    - you can use context bounds to simplify type class instances
    - you can use implicits to pass type class instances as implicit parameters
   */
}
