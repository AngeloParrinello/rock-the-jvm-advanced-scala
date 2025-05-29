package lectures.part3concurrency

import lectures.part1as.DarkSugars.Mutable

import scala.collection.mutable

object ThreadCommunication extends App {
  /*
  the producer-consumer problem
  producer -> [ x ] -> consumer
   */
  class SimpleContainer {
    private var value: Int = 0

    def isEmpty: Boolean = value == 0

    def get: Int = {
      val result = value
      value = 0
      result
    }

    def set(newValue: Int): Unit = value = newValue
  }

  def naiveProducerConsumer(): Unit = {
    val container = new SimpleContainer

    val consumer = new Thread(() => {
      println("[consumer] waiting...")
      while (container.isEmpty) {
        println("[consumer] actively waiting...")
      }

      println("[consumer] I have consumed " + container.get)
    })

    val producer = new Thread(() => {
      println("[producer] computing...")
      Thread.sleep(500)
      val value = 42
      println("[producer] I have produced, after long work, the value " + value)
      container.set(value)
    })

    consumer.start()
    producer.start()
  }

  // naiveProducerConsumer()
  // in a busy waiting mode, the consumer is actively waiting for the producer to produce a value
  // this is not efficient because the consumer is consuming CPU cycles

  // by now we now that using synchronized locks a data structure and allows only one thread to access it
  // making the operations atomic
  // this synchronization is done by the monitor which tracks which thread has locked the object
  // but only AnyRef can be synchronized

  // waiting on an object monitor will suspend you (the thread) indefinitely until another thread notifies you
  // or notifies all threads waiting on the same object monitor
  // and waiting it also releases the lock on the object monitor
  // when allowed to proceed again, it will lock the object monitor again and will continue from where it left off
  // with the notify method, it will notify one of the threads waiting on the object monitor
  // so it will wake up one of the threads waiting on the object monitor, WITHOUT knowing which thread will be woken up
  // it will finish the execution and after that, it will release the lock on the object monitor and will wake up the
  // next thread waiting on the object monitor defined by the JVM scheduler

  // wait and notify
  def smartProducerConsumer(): Unit = {
    val container = new SimpleContainer

    val consumer = new Thread(() => {
      println("[consumer] waiting...")
      container.synchronized {
        container.wait()
      }

      // container must have some value, because the only one that can wake me up is the producer
      println("[consumer] I have consumed " + container.get)
    })

    val producer = new Thread(() => {
      println("[producer] hard at work...")
      Thread.sleep(2000)
      val value = 42

      container.synchronized {
        println("[producer] I'm producing " + value)
        container.set(value)
        container.notify()
      }
    })

    consumer.start()
    producer.start()
  }

  // smartProducerConsumer()

  // multiple consumers and producers
  /*
  producer -> [ ? ? ? ] -> consumer
  So in this case we have a buffer with multiple values

  so both the producer and the consumer will wait until the buffer is empty or full

   */

  def prodConsLargeBuffer(): Unit = {
    val buffer: mutable.Queue[Int] = new mutable.Queue[Int]
    val capacity = 3

    val consumer = new Thread(() => {
      val random = new scala.util.Random()
      while (true) {
        buffer.synchronized {
          if (buffer.isEmpty) {
            println("[consumer] buffer empty, waiting...")
            buffer.wait()
          }

          // there must be at least ONE value in the buffer
          val x = buffer.dequeue()
          println("[consumer] consumed " + x)

          // hey producer, there's empty space available
          buffer.notify()
        }
        Thread.sleep(random.nextInt(500))
      }
    })

    def producer = new Thread(() => {
      val random = new scala.util.Random()
      var i = 0
      while (true) {
        buffer.synchronized {
          if (buffer.size == capacity) {
            println("[producer] buffer is full, waiting...")
            buffer.wait()
          }

          // there must be at least ONE EMPTY SPACE in the buffer
          println("[producer] producing " + i)
          buffer.enqueue(i)

          // hey consumer, new food for you!
          buffer.notify()
          i += 1
        }
        Thread.sleep(random.nextInt(500)) // change the sleep values to see how the consumer and producer interact
      }
    })

    consumer.start()
    producer.start()
  }

  prodConsLargeBuffer()

  /*
    Prod-cons, level 3

      producer1 -> [ ? ? ?] -> consumer1
      producer2 ----^     ^---- consumer2
   */

  class Consumer(id: Int, buffer: mutable.Queue[Int]) extends Thread {
    override def run(): Unit = {
      val random = new scala.util.Random()
      while (true) {
        buffer.synchronized {
          /*
            producer produces value, two Cons are waiting
            notifies one consumer, notifies one buffer
            notifies the other consumer
           */
          while (buffer.isEmpty) {
            println(s"[consumer $id] buffer empty, waiting...")
            buffer.wait()
          }

          // there must be at least ONE value in the buffer
          val x = buffer.dequeue() // OOPS! Problem!
          println(s"[consumer $id] consumed " + x)

          buffer.notify()
        }
        Thread.sleep(random.nextInt(500))
      }
    }
  }

  class Producer(id: Int, buffer: mutable.Queue[Int], capacity: Int) extends Thread {
    val random = new scala.util.Random()
    var i = 0
    while (true) {
      buffer.synchronized {
        while (buffer.size == capacity) {
          println(s"[producer $id] buffer is full, waiting...")
          buffer.wait()
        }

        // there must be at least ONE EMPTY SPACE in the buffer
        println(s"[producer $id] producing " + i)
        buffer.enqueue(i)

        // hey consumer, new food for you!
        buffer.notify()
        i += 1
      }
      Thread.sleep(random.nextInt(500)) // change the sleep values to see how the consumer and producer interact
    }
  }

  def multiProdCons(nConsumers: Int, nProducers: Int): Unit = {
    val buffer: mutable.Queue[Int] = new mutable.Queue[Int]
    val capacity = 3

    (1 to nConsumers).foreach(new Consumer(_, buffer).start())
    (1 to nProducers).foreach(new Producer(_, buffer, capacity).start())
  }

  //multiProdCons(3, 3)

  /*
  Exercises.
  1) Think of an example where notifyAll acts in  a different way than notify?
  2) Create a deadlock
  3) create a livelock
   */

  def testNotifyAll(): Unit = {
    val bell = new Object
    (1 to 10).foreach(i => new Thread(()=> {
      bell.synchronized {
        println(s"[thread $i] waiting...")
        bell.wait()
        println(s"[thread $i] hooray!")
      }
    }).start())

    new Thread(() => {
      Thread.sleep(2000)
      println("[announcer] rock n roll!")
      bell.synchronized {
        bell.notifyAll()
      }

    }).start()

  }

  testNotifyAll()


}