package com.sa.events.tcp

import cats.effect.{Blocker, Bracket, ConcurrentEffect, ContextShift, Timer}
import com.sa.events.domain.eventdata.EventData
import fs2.io.tcp.Socket
import io.circe.parser.decode

/*
 - java.nio.channels and java.nio.channels.Selector libraries.
 - channels represent connections to entities that
  - are capable of performing I/O operations, such as files and sockets
  - defines selectors, for multiplexed, non-blocking I/O operations.
 - selector may be created by invoking the open method of this class,
    which will use the system’s default selector provider to create a new selector.

  java.nio
    - defines buffers which are containers for the data
    - Charsets and their encoders and decoders
    - Channels which represents connection to entities capable of I/O
    - Selectors and selector keys, which together with channels defines
      multiplexed non-blocking I/O facility

  - Create CrunchifyNIOClient.java which tries to connect to server on port 1111
  - Create ArrayList with 5 company names
  - Iterate through ArrayList and send each companyName to server
  - Close connection after task finish
 */
object EventCounterDaemon1 {

  import fs2._
  import fs2.io.tcp.SocketGroup

  import scala.concurrent.duration._

  // https://github.com/typelevel/fs2/issues/1300


  import java.net.InetSocketAddress

  def tcpStream[F[_]](socket: Socket[F])
                     (implicit F: ConcurrentEffect[F]
                      , timer: Timer[F]
                      , contextShift: ContextShift[F]): Stream[F, Option[Map[String,Int]]] = {

    val outcome = for {
      chunks <- Stream.eval{
        println(s"READING CHUNKS")
        socket.read(4096)
      }
      strList <- Stream.eval{F.delay(chunks.map(_.toList.map(_.toChar).mkString("")))}
    }yield {
      val r = strList.map { str =>
        str.split("\n").toList
          .flatMap { s =>
            val decodedEventData = decode[EventData](s)
            decodedEventData.toOption
          }
      }
      println(s" GOT EVENT DATA SIZE ${r.map(_.size)}")
//      println(s"R: $r")
      val stringListMapOpt = r.map{p => p.groupBy(_.event_type)}
      val wc = stringListMapOpt.map(_.map(entry => entry._1 -> entry._2.size))
      println(s"WC Map: $wc")
      wc
    }
    outcome
  }

  def execute[F[_]](blocker: Blocker)
                             (implicit F: ConcurrentEffect[F]
                    , timer: Timer[F]
                    , contextShift: ContextShift[F]) = {
    SocketGroup[F](blocker).use { sg =>
      sg.client[F](new InetSocketAddress("localhost", 9999)
        ,true,256 * 1024,256 * 1024
        ,true).use(init(_).compile.drain)
    }
  }

  def init[F[_]](socket: Socket[F])
                (implicit F: ConcurrentEffect[F]
                 , timer: Timer[F]
                 , cs: ContextShift[F])= for {
    serverAddr <- Stream.eval(
      F.delay(new InetSocketAddress("localhost", 9999))
    )
    _ <- Stream.eval(F.delay(println(s"IN INIT")))
    res <- Stream.awakeEvery[F](10 seconds) >> tcpStream(socket)
  } yield ()



  //  /**
//   * This methods translates the data from an inputstream (say, from a socket)
//   * to '\n' delimited strings and returns an iterator to access the strings.
//   */
//  def bytesToLines(inputStream: InputStream): Iterator[String] = {
//    val dataInputStream = new BufferedReader(
//      new InputStreamReader(inputStream, StandardCharsets.UTF_8))
//    new NextIterator[String] {
//      protected override def getNext() = {
//        val nextValue = dataInputStream.readLine()
//        if (nextValue == null) {
//          finished = true
//        }
//        nextValue
//      }
//
//      protected override def close(): Unit = {
//        dataInputStream.close()
//      }
//    }
//  }
}
/** Provides a basic/boilerplate Iterator implementation. */
//private[spark] abstract class NextIterator[U] extends Iterator[U] {
//
//  private var gotNext = false
//  private var nextValue: U = _
//  private var closed = false
//  protected var finished = false
//
//  /**
//   * Method for subclasses to implement to provide the next element.
//   *
//   * If no next element is available, the subclass should set `finished`
//   * to `true` and may return any value (it will be ignored).
//   *
//   * This convention is required because `null` may be a valid value,
//   * and using `Option` seems like it might create unnecessary Some/None
//   * instances, given some iterators might be called in a tight loop.
//   *
//   * @return U, or set 'finished' when done
//   */
//  protected def getNext(): U
//
//  /**
//   * Method for subclasses to implement when all elements have been successfully
//   * iterated, and the iteration is done.
//   *
//   * <b>Note:</b> `NextIterator` cannot guarantee that `close` will be
//   * called because it has no control over what happens when an exception
//   * happens in the user code that is calling hasNext/next.
//   *
//   * Ideally you should have another try/catch, as in HadoopRDD, that
//   * ensures any resources are closed should iteration fail.
//   */
//  protected def close(): Unit
//
//  /**
//   * Calls the subclass-defined close method, but only once.
//   *
//   * Usually calling `close` multiple times should be fine, but historically
//   * there have been issues with some InputFormats throwing exceptions.
//   */
//  def closeIfNeeded(): Unit = {
//    if (!closed) {
//      // Note: it's important that we set closed = true before calling close(), since setting it
//      // afterwards would permit us to call close() multiple times if close() threw an exception.
//      closed = true
//      close()
//    }
//  }
//
//  override def hasNext: Boolean = {
//    if (!finished) {
//      if (!gotNext) {
//        nextValue = getNext()
//        if (finished) {
//          closeIfNeeded()
//        }
//        gotNext = true
//      }
//    }
//    !finished
//  }
//
//  override def next(): U = {
//    if (!hasNext) {
//      throw new NoSuchElementException("End of stream")
//    }
//    gotNext = false
//    nextValue
//  }
//}

