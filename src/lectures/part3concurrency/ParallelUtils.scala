package lectures.part3concurrency

// starting from scala 2.13, the parallel collections need to be added manually
// through also the sbt build file but there is not in this project
// import scala.collection.parallel.CollectionConverters._

object ParallelUtils extends App {

  // 1. parallel collections

//  val parList = List(1, 2, 3).par // handled my multiple threads at the same time
//
//  val aParVector = ParVector[Int](1, 2, 3)
//
//  /*
//  * Seq
//  * Vector
//  * Array
//  * Map - Hash, Trie
//  * Set - Hash, Trie
//   */
//
//  def measure[T](operation: => T): Long = {
//    val time = System.currentTimeMillis()
//    operation
//    System.currentTimeMillis() - time
//  }
//
//  val list = (1 to 100000).toList // if we decrease the number of elements,
//  // the parallel time will be higher, why?
//  // because the parallel collections are not efficient for small collections
//
//  // That becasue the parallel collection works on the map-reduce model!
//  // The overhead of creating the threads and managing them is higher than the actual computation
//  // steps:
//  // 1. split the elements into chunks - Splitter
//  // 2. operation
//  // 3. recombine - Combiner
//
//
//  val serialTime = measure {
//    list.map(_ + 1)
//  }
//  val parallelTime = measure {
//    list.par.map(_ + 1)
//  }
//
//  println("serial time: " + serialTime)
//  println("parallel time: " + parallelTime)
//
//  // map, flatMap, filter, foreach, reduce, fold
//
//  println(List(1, 2, 3).reduce(_ - _)) // 1 - 2 - 3 = -4
//  println(List(1, 2, 3).par.reduce(_ - _)) // ??? = 2
//  // the operation is not associative, so the result is non-deterministic!
//
//  // second problem with parallel collections, is that sometimes you need synchronization
//  var sum = 0
//  List(1, 2, 3).par.foreach(sum += _)
//  println(sum) // 6 ... BUT THIS IS NOT GUARANTEED! race conditions!
//
//  // configuring
//  aParVector.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(2)) // 2 threads, the number of thread that are going to manage the parallel collection
//  /*
//  * alternatives
//  * - ThreadPoolTaskSupport - deprecated
//  * - ExecutionContextTaskSupport(EC) - use an ExecutionContext, the same for the Future's API
//  *
//   */
//
//  aParVector.tasksupport = new TaskSupport {
//    // VERY LOW CHANCE TO BE USED, MOST OF THE TIMES YOU WILL USE THE FORK JOIN POOL OR THE EXECUTION CONTEXT
//    override def execute[R, Tp](fjtask: Task[R, Tp]): () => R = ??? // scehdule a thread to run in parallel
//
//    override def executeAndWaitResult[R, Tp](task: Task[R, Tp]): R = ??? // run the task and wait for the result
//
//    override def parallelismLevel: Int = ??? // number of threads to use
//
//    override val environment: AnyRef = ??? // the environment to run the task, the manager that runs the threads
//  }
//
//  val aParMap = parList.map(_ * 2)
//
//  val aParFlatMap = parList.flatMap(x => List(x, x * 2))
//
//  val aParFilter = parList.filter(_ % 2 == 0)

  // these operations are parallel

//  println(aParMap)
//  println(aParFlatMap)
//  println(aParFilter)

  // 2. atomic operations and references
  // atomic: thread-safe data structures, an atomic operation is an operation that is executed as a single unit of work without the possibility of interference from other operations
  // atomic reference: thread-safe reference

  import java.util.concurrent.atomic.AtomicReference

  val atomic = new AtomicReference[Int](2)

  val currentValue = atomic.get() // thread-safe read
  atomic.set(4) // thread-safe write

  atomic.compareAndSet(38, 56) // if the value is 38, then set it to 56, reference equality

  atomic.updateAndGet(_ + 1) // thread-safe function run
  atomic.getAndUpdate(_ + 1) // thread-safe function run

  atomic.accumulateAndGet(12, _ + _) // thread-safe accumulation
  atomic.getAndAccumulate(12, _ + _) // thread-safe accumulation

  val oldValue = atomic.getAndSet(5) // thread-safe combo

  val newValue = atomic.getAndUpdate(_ + 1) // thread-safe function run

  println(currentValue)
  println(oldValue)
  println(newValue)

  // if you have multiple threads working with the same atomic reference, you

}
