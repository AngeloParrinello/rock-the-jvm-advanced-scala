package lectures.part4implicits

import scala.language.implicitConversions

object ScalaJavaConversions extends App {

  import java.{util => ju}

  import scala.jdk.CollectionConverters._

  val javaSet: ju.Set[Int] = new ju.HashSet[Int]()
  (1 to 5).foreach(javaSet.add)

  println(javaSet)

  val scalaSet = javaSet.asScala

  /*
  Many implicit conversions are already defined in the scala.jdk.CollectionConverters package
  Iterator
  Iterable
  ju.List - collection.mutable.Buffer
  ju.Set - collection.mutable.Set
  ju.Map - collection.mutable.Map
   */

  // mutable package
  import collection.mutable._

  val numbersBuffer = ArrayBuffer[Int](1, 2, 3)
  // the java equivalent is...
  val juNumbersBuffer = numbersBuffer.asJava

  println(juNumbersBuffer.asScala eq numbersBuffer) // true, this is the exact reference .. REFERENCE EQUALITY

  val numbers = List(1, 2, 3)
  val juNumbers = numbers.asJava
  val backToScala = juNumbers.asScala

  println(backToScala eq numbers) // false, the numbers list is an immutable list, so it is a different reference
  println(backToScala == numbers) // true, the content is the same, but the reference is different

  juNumbers.add(7) // in Java this is possible, but in Scala it is not possible, so it will throw an exception

  println(backToScala) // List(1, 2, 3, 7)

  /*
  Exercise
  create a Scala-Java Optional-Option
  with .asScala
   */

  //  implicit class JavaOptionalToScala[T](value: ju.Optional[T]) {
  //    def asScala: Option[T] =
  //      if (value.isPresent) Some(value.get())
  //      else None
  //  }

  class ToScala[T](value: T) {
    def asScala: T = value
  }

  implicit def toScalaOption[T](value: ju.Optional[T]): ToScala[Option[T]] =
    new ToScala(
      if (value.isPresent) Some(value.get())
      else None
    )

  val juOptional: ju.Optional[Int] = ju.Optional.of(2)
  val scalaOption = juOptional.asScala // this is rewritten in the compiler as toScalaOption(juOptional).asScala
  // val scalaOption2 = toScalaOption(juOptional).asScala

  println(scalaOption) // Some(2)

}
