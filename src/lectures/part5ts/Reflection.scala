package lectures.part5ts

object Reflection extends App {

  // PART 1

  // how do I instantiate a class or a method, by just call its name dynamically at runtime?
  // this is the problem of reflection
  // the ability of the JVM of call classes, methods, fields at runtime
  // this is a very powerful tool, but it's also very dangerous

  // reflection + macros + quasiquotes = META-PROGRAMMING

  case class Person(name: String) {
    def sayMyName(): Unit = println(s"My name is $name")
  }

  // there are some steps to follow in order to invoke the scala reflection API
  // 0 - import
  import scala.reflect.runtime.{universe => ru}

  // 1 - MIRROR
  // a mirror is a reflection of the JVM instance
  val m = ru.runtimeMirror(getClass.getClassLoader) // standard way of getting your hand on the current class loader

  // 2 - create a class object
  val clazz = m.staticClass("lectures.part5ts.Reflection.Person") // creating a class object by name
  // starting getting into the reflection territory
  // this is a class symbol

  // 3 - create a reflected mirror = "can do things"
  val cm = m.reflectClass(clazz) // creating a reflected mirror
  // this is a class mirror

  // 4 - get the constructor
  val constructor = clazz.primaryConstructor.asMethod // getting the constructor
  // this is a method symbol

  // 5 - reflect the constructor
  val constructorMirror = cm.reflectConstructor(constructor) // reflecting the constructor
  // this is a method mirror

  // 6 - invoke the constructor
  val instance = constructorMirror.apply("John") // invoking the constructor

  println(instance) // Person(John)


  // Another example
  // I have an instance of a class and I want to invoke a method by name
  val p = Person("Mary") // from the wire as a serialized object
  // method name computed from somewhere else
  val methodName = "sayMyName"
  // 1 - mirror
  // 2 - reflect the instance
  val reflected = m.reflect(p)
  // 3 - method symbol
  val methodSymbol = ru.typeOf[Person].decl(ru.TermName(methodName)).asMethod
  // 4 - reflect the method
  val method = reflected.reflectMethod(methodSymbol)
  // 5 - invoke the method
  method.apply() // "My name is Mary"

  // PART 2

  // type erasure. As you know generic types are erased at runtime
  // the reason is that the JVM was designed to be backwards compatible with Java 1.0
  // and generics were introduced in Java 1.5
  // so the JVM since then has to be able to run code that was compiled with Java 1.0
  // and as a consequence, all the JVM languages have to deal with JVM type erasure pain points

  // pp 1 - differentiate types at runtime
  val numbers = List(1, 2, 3)
  numbers match {
    case listOfStrings: List[String] => println("a list of strings")
    case listOfNumbers: List[Int] => println("a list of numbers")
  }

  // and if we run this code what do we expect? "a list of strings" or "a list of numbers"?
  // we'll receive "a list of strings" because of type erasure!!
  // because at runtime the JVM erases the type information and so the JVM cannot differentiate between a List[Int] and a List[String]
  // indeed, it will match the List of Strings case because the JVM will only see a List, in other words the JVM will see at runtime this:
  /*
    numbers match {
    case listOfStrings: List => println("a list of strings")
    case listOfNumbers: List => println("a list of numbers")
  }
   */

  // pp 2 - limitations on overloads
  // the JVM cannot differentiate between the two methods below
  // def processList(list: List[Int]): Int = 43
  // def processList(list: List[String]): Int = 45
  // normally considered as an overload, but the JVM sees them as the same method
  // because the JVM erases the type information and so the JVM cannot differentiate between a List[Int] and a List[String]
  // at runtime, the JVM will see this:
  /*
    def processList(list: List): Int = 43
    def processList(list: List): Int = 45
   */

  // Scala compiler has a workaround for this problem: TypeTags
  // TypeTags are a way to carry the type information at runtime

  // 0 - import
  import ru._

  // 1 - creating a type tag "manually"
  val ttag = typeTag[Person]
  println(ttag.tpe) // lectures.part5ts.Reflection.Person
  // type tags are not super useful by themselves, but they are used as a type evidence

  class MyMap[K, V]
  // 2 - pass type tags as implicit parameters (which is the most common way to use them and not use them manually)
  def getTypeArguments[T](value: T)(implicit typeTag: TypeTag[T]): List[Type] = typeTag.tpe match {
    case TypeRef(_, _, typeArguments) => typeArguments
    case _ => List()
  }

  val myMap = new MyMap[Int, String]
  val typeArgs = getTypeArguments(myMap) //(typeTag: TypeTag[MyMap[Int, String]])
  println(typeArgs) // List(Int, String)

  // but how is it possible that the type information is not erased?
  // Because at compile time the Scala compiler will generate the type information and it will pass it around
  // and so the type information is carried around by the compiler and it is used by the reflection API
  // basically the compiler will hold this information and we can use it at runtime

  def isSubType[A, B](implicit typeTagA: TypeTag[A], typeTagB: TypeTag[B]): Boolean = {
    typeTagA.tpe <:< typeTagB.tpe // <:< is a method that checks if A is a subtype of B, and it's part of the Type API
  }

  class Animal
  class Dog extends Animal
  println(isSubType[Dog, Animal]) // true

  // Another example
  // 3 - method symbol
  val anotherMethodSymbol = typeTag[Person].tpe.decl(ru.TermName("sayMyName")).asMethod
  // 4 - reflect the method
  val sameMethod = reflected.reflectMethod(anotherMethodSymbol)
  // 5 - invoke the method
  sameMethod.apply() // "My name is Mary"

}
