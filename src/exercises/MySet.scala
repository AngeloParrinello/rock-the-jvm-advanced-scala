package exercises

trait MySet[A] extends (A => Boolean) {
  // Implement a functional set
  override def apply(v1: A): Boolean =
    contains(v1)
  def contains(elem: A): Boolean
  def +(elem: A): MySet[A]
  def ++(anotherSet: MySet[A]): MySet[A] // concatenation, union

  def map[B](f: A => B): MySet[B]
  def flatMap[B](f: A => MySet[B]): MySet[B]
  def filter(predicate: A => Boolean): MySet[A]
  def foreach(f: A => Unit): Unit

  /*
  Exercise 2:
  - removing an element
  - intersection with another set
  - difference with another set
   */

  def -(elem: A): MySet[A]
  def &(anotherSet: MySet[A]): MySet[A] // intersection
  def --(anotherSet: MySet[A]): MySet[A] // difference
  // Exercise 3
  // implement unary_! = negation of a set
  // example s = [1, 2, 3] => !s = [4, 5, 6, 7, ...]
  // s(2) => false
  // !s(2) => true
  // this could lead to a infinite collection
  def unary_! : MySet[A] // negation of a set

}

case class EmptySet[A]() extends MySet[A] {
  override def contains(elem: A): Boolean = false

  override def +(elem: A): MySet[A] = new NonEmptySet[A](elem, this)

  override def ++(anotherSet: MySet[A]): MySet[A] = anotherSet

  override def map[B](f: A => B): MySet[B] = new EmptySet[B]

  override def flatMap[B](f: A => MySet[B]): MySet[B] = new EmptySet[B]

  override def filter(predicate: A => Boolean): MySet[A] = this

  override def foreach(f: A => Unit): Unit = ()

  override def -(elem: A): MySet[A] = this

  override def &(anotherSet: MySet[A]): MySet[A] = this

  override def --(anotherSet: MySet[A]): MySet[A] = this

  // what is the opposite of an empty set? A set with all the elements!
  override def unary_! : MySet[A] = new PropertyBasedSet[A](_ => true)
}

case class NonEmptySet[A](head: A, tail: MySet[A]) extends MySet[A] {

  override def contains(elem: A): Boolean = elem == head || tail.contains(elem)

  override def +(elem: A): MySet[A] = if (this.contains(elem)) this else NonEmptySet[A](elem, this)

  /*
    [1 2 3] ++ [4 5]
    [2 3] ++ [4 5] + 1
    [3] ++ [4 5] + 1 + 2
    [] ++ [4 5] + 1 + 2 + 3
    [4 5] + 1 + 2 + 3
    [4 5 1 2 3]
   */
  override def ++(anotherSet: MySet[A]): MySet[A] = tail ++ anotherSet + head

  override def map[B](f: A => B): MySet[B] = tail.map(f) + f(head)

  override def flatMap[B](f: A => MySet[B]): MySet[B] = f(head) ++ tail.flatMap(f)

  override def filter(predicate: A => Boolean): MySet[A] = if(predicate(head)) tail.filter(predicate) + head else tail.filter(predicate)

  override def foreach(f: A => Unit): Unit = {
    f(head)
    tail.foreach(f)
  }

  override def -(elem: A): MySet[A] = {
    if (this contains elem) {
      if (head == elem) tail
      else tail - elem + head
    } else this
  }

  // intersection = !filtering
  override def &(anotherSet: MySet[A]): MySet[A] = {
    /*
    Intersecting set A with set B
    means all elements in A that are also in B
    so we can filter the elements in A that are also in B
     */
    // filter(x => anotherSet.contains(x))
    // and we can refactor this ...
    // remember that MySet extends A => Boolean! Because MySet is a function
    // filter(anotherSet.contains)
    // and finally we can refactor this ...
    filter(anotherSet)
  }

  override def --(anotherSet: MySet[A]): MySet[A] = {
    /*
    Difference of set A with set B
    similar to intersection but we filter the elements in A that are not in B
     */
    // filter(x => !anotherSet.contains(x))
    // same refactoring as above but this time we cannot negate the function
    // filter(!anotherSet.contains(_)) // here to use !anotherSet we have to define a uniray operator
    // after implementing unary_!
    filter(!anotherSet)
  }

  // new operator
  override def unary_! : MySet[A] = new PropertyBasedSet[A](x => !this.contains(x))
}

// all elements of type A which satisfy a property
// { x in A | property(x) }
// this will take a property and return a set
class PropertyBasedSet[A](property: A => Boolean) extends MySet[A] {
  override def contains(elem: A): Boolean = property(elem)

  // {x in A | property(x) } + element = { x in A | property(x) || x == element }
  override def +(elem: A): MySet[A] = new PropertyBasedSet[A](x => property(x) || x == elem)

  // {x in A | property(x) } ++ set = { x in A | property(x) || set contains x }
  override def ++(anotherSet: MySet[A]): MySet[A] = new PropertyBasedSet[A](x => property(x) || anotherSet(x))

  /*
  Why we cannot implement map, flatMap, foreach?
  Because, we won't know if the set is finite or infinite
  all integers => (_ % 3) => [0 1 2]
  if you map a infinite set into a finite set
  but the problem is once we are into that set [0 1 2] we won't knw if the set is finite or not
  and we cannot test if the element is in the set or not
   */
  override def map[B](f: A => B): MySet[B] = politelyFail

  override def flatMap[B](f: A => MySet[B]): MySet[B] = politelyFail

  override def filter(predicate: A => Boolean): MySet[A] = new PropertyBasedSet[A](x => property(x) && predicate(x))

  override def foreach(f: A => Unit): Unit = politelyFail

  override def -(elem: A): MySet[A] = filter(_ != elem)

  override def &(anotherSet: MySet[A]): MySet[A] = filter(anotherSet)

  override def --(anotherSet: MySet[A]): MySet[A] = filter(!anotherSet)

  override def unary_! : MySet[A] = new PropertyBasedSet[A](x => !property(x))

  private def politelyFail: Nothing = throw new IllegalArgumentException("Really deep rabbit hole!")
}

object MySet {
  /*
    val s = MySet(1, 2, 3) = buildSet(seq(1, 2, 3), [])
    = buildSet(seq(2, 3), [] + 1)
    = buildSet(seq(3), [1] + 2)
    = buildSet(seq(), [1, 2] + 3)
    = [1, 2, 3]
   */
  def apply[A](values: A*): MySet[A] = {
    @scala.annotation.tailrec
    def buildSet(valSeq: Seq[A], acc: MySet[A]): MySet[A] = {
      if(valSeq.isEmpty) acc
      else buildSet(valSeq.tail, acc + valSeq.head)
    }
    buildSet(values, EmptySet[A]())
  }



}

object MySetPlayground extends App {
  val s = MySet(1, 2, 3, 4)
  s + 5 foreach println
  println("----")
  s + 5 ++ MySet(-1, -2) + 3 foreach println
  println("----")
  s + 5 ++ MySet(-1, -2) + 3 flatMap (x => MySet(x, x * 10)) foreach println
  println("----")
  s + 5 ++ MySet(-1, -2) + 3 flatMap (x => MySet(x, x * 10)) filter (_ % 2 == 0) foreach println
  println("----")
  val negative = !s // s.unary_! = all the naturals not equal to 1, 2, 3, 4
  println(negative(2)) // false, 2 is not in the negative set
  println(negative(5)) // true, 5 is in the negative set
  println("----")
  val negativeEven = negative.filter(_ % 2 == 0)
  println(negativeEven(5)) // false, 5 is not in the negative set
  println(negativeEven(4)) // false, 4 is not even
  println(negativeEven(2)) // false, 2 is not in the negative set and is even
  println("----")
  val negativeEven5 = negativeEven + 5
  println(negativeEven5(5)) // true, 5 is in the negative set
  println(negativeEven5(4)) // false, 4 is not even
  println(negativeEven5(2)) // false, 2 is not in the negative set and is even
  println(negativeEven5(6)) // true, 6 is even
  println("----")
}
