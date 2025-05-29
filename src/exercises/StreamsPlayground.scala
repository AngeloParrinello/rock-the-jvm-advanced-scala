package exercises

abstract class MyStream[+A] {
  def isEmpty: Boolean
  def head: A
  def tail: MyStream[A]

  // operations on streams
  def #::[B >: A](element: B): MyStream[B] // prepend operator
  def ++[B >: A](anotherStream: => MyStream[B]): MyStream[B] // concatenate two streams

  // classic functional methods
  def foreach(f: A => Unit): Unit
  def map[B](f: A => B): MyStream[B]
  def flatMap[B](f: A => MyStream[B]): MyStream[B]
  def filter(predicate: A => Boolean): MyStream[A]

  // two more methods
  def take(n: Int): MyStream[A] // takes the first n elements out of this stream
  def takeAsList(n: Int): List[A] = take(n).toList()
  /*
  The reverse is important because:
  [1 2 3].toList([]) =
  [2 3].toList([1]) =
  [3].toList([2 1]) =
  [].toList([3 2 1]) =
  [3 2 1] but we need to reverse it to get [1 2 3]
   */
  @scala.annotation.tailrec
  final def toList[B >: A](acc: List[B] = Nil): List[B] = {
    if (isEmpty) acc.reverse
    else tail.toList(head :: acc)
  }
}

object EmptyStream extends MyStream[Nothing] {
  def isEmpty: Boolean = true
  def head: Nothing = throw new NoSuchElementException
  def tail: MyStream[Nothing] = throw new NoSuchElementException

  def #::[B >: Nothing](element: B): MyStream[B] = new Cons(element, this)
  def ++[B >: Nothing](anotherStream: => MyStream[B]): MyStream[B] = anotherStream

  def foreach(f: Nothing => Unit): Unit = ()
  def map[B](f: Nothing => B): MyStream[B] = this
  def flatMap[B](f: Nothing => MyStream[B]): MyStream[B] = this
  def filter(predicate: Nothing => Boolean): MyStream[Nothing] = this

  def take(n: Int): MyStream[Nothing] = this
}

class Cons[+A](hd: A, tl: => MyStream[A]) extends MyStream[A] {
  def isEmpty: Boolean = false
  // override as a val to avoid recomputing the head and reuse it throughout the code
  override val head: A = hd
  // as for tail we need to use a lazy val to avoid recomputing the tail
  override lazy val tail: MyStream[A] = tl // combining lazy val with call by name parameter is called
  // call by need!

  /*
  val s = new Cons(1, EmptyStream)
  val prepended = 1 #:: s = new Cons(1, s)
  but we will still not evaluate s because it is a call by name parameter
   */
  def #::[B >: A](element: B): MyStream[B] = new Cons(element, this)
  def ++[B >: A](anotherStream: => MyStream[B]): MyStream[B] =
    new Cons(head, tail ++ anotherStream)

  def foreach(f: A => Unit): Unit = {
    f(head)
    tail.foreach(f)
  }
  /*
  s = new Cons(1, EmptyStream)
  mapped = s.map(_ + 1) = new Cons(2, s.tail.map(_ + 1))
  but the second part of the expression is not evaluated and will be evaluated on a need basis
   */
  def map[B](f: A => B): MyStream[B] = new Cons(f(head), tail.map(f)) // and still preserves lazy evaluation
  def flatMap[B](f: A => MyStream[B]): MyStream[B] =
    f(head) ++ tail.flatMap(f) // concatenating the head with the rest of the stream
  def filter(predicate: A => Boolean): MyStream[A] =
    if (predicate(head)) new Cons(head, tail.filter(predicate))
    else tail.filter(predicate) // preserves lazy evaluation


  def take(n: Int): MyStream[A] =
    if (n <= 0) EmptyStream
    else if (n == 1) new Cons(head, EmptyStream)
    else new Cons(head, tail.take(n - 1))
}

object MyStream {
  // will generate a stream based on a starting element and a generator function
  // the generator function will generate the next element based on the previous element
  def from[A](start: A)(generator: A => A): MyStream[A] =
    new Cons(start, MyStream.from(generator(start))(generator))
}

object StreamsPlayground extends App {

  val naturals = MyStream.from(1)(_ + 1)
  println(naturals.head)
  println(naturals.tail.head)
  println(naturals.tail.tail.head)

  val startFrom0 = 0 #:: naturals // naturals.#::(0)
  println(startFrom0.head)

  startFrom0.take(10000).foreach(println)

  // map, flatMap, filter
  println(startFrom0.map(_ * 2).take(100).toList())
  println(startFrom0.flatMap(x => new Cons(x, new Cons(x + 1, EmptyStream))).take(10).toList())
  // we hve to put a .take(10) because otherwise I'll receive a stack overflow error, since the computer will try to
  // filter all the elements less than 10 but it is impossible to define that in an infinite stream! So, it will
  // analyze all the stream until an error occurs
  println(startFrom0.filter(_ < 10).take(10).toList())

  // Exercises on streams
  // 1 - stream of Fibonacci numbers
  // 2 - stream of prime number with Eratosthenes' sieve
  /*
  [ 2 3 4 ... ]
  filter out all numbers divisible by 2
  [ 2 3 5 7 9 11 ... ]
  filter out all numbers divisible by 3
  [ 2 3 5 7 11 13 17 ... ]
  filter out all numbers divisible by 5
  [ 2 3 5 7 11 13 17 19 23 29 ... ]
  ...
   */
  // We need BigInt because the numbers will grow very fast and with Int we would see negative numbers
  def fibonacci(first: BigInt, second: BigInt): MyStream[BigInt] =
    new Cons(first, fibonacci(second, first + second))

  println(fibonacci(1, 1).take(100).toList())

  /*
  [ 2 3 4 5 6 7 8 9 10 11 12 13 14 15 ... ] to filter out all numbers divisible by 2
  [ 2 eratosthenes applied to (numbers filtered by n % 2 != 0) ]
  [ 2 3 5 7 9 11 13 15 ... ] to filter out all numbers divisible by 3
  [ 2 3 eratosthenes applied to (numbers filtered by n % 3 != 0) ]
  [ 2 3 5 7 11 13 17 19 23 25 ... ] to filter out all numbers divisible by 5
  [ 2 3 5 eratosthenes applied to (numbers filtered by n % 5 != 0) ]
  ...
   */
  def eratosthenes(numbers: MyStream[Int]): MyStream[Int] =
    if (numbers.isEmpty) numbers
    else new Cons(numbers.head, eratosthenes(numbers.tail.filter(_ % numbers.head != 0)))

  println(eratosthenes(MyStream.from(2)(_ + 1)).take(100).toList())

}
