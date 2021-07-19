//package com.sa.events.tcp
//
//import java.net.InetSocketAddress
//
//import cats.effect.{Blocker, IOApp}
//import com.sa.events.domain.eventdata.EventData
//import io.circe.parser.decode
//
///*
// - java.nio.channels and java.nio.channels.Selector libraries.
// - channels represent connections to entities that
//  - are capable of performing I/O operations, such as files and sockets
//  - defines selectors, for multiplexed, non-blocking I/O operations.
// - selector may be created by invoking the open method of this class,
//    which will use the system’s default selector provider to create a new selector.
//
//  java.nio
//    - defines buffers which are containers for the data
//    - Charsets and their encoders and decoders
//    - Channels which represents connection to entities capable of I/O
//    - Selectors and selector keys, which together with channels defines
//      multiplexed non-blocking I/O facility
//
//  - Create CrunchifyNIOClient.java which tries to connect to server on port 1111
//  - Create ArrayList with 5 company names
//  - Iterate through ArrayList and send each companyName to server
//  - Close connection after task finish
// */
//object EC2 extends IOApp{
//
//  import cats.effect.{ExitCode, IO}
//  import fs2._
//  import fs2.concurrent.SignallingRef
//  import fs2.io.tcp.SocketGroup
//
//  import scala.concurrent.duration._
//
//  // https://github.com/typelevel/fs2/issues/1300
//
//
//  import java.net.InetSocketAddress
//  def tcpStream(socketGroup: SocketGroup, serverAddr: InetSocketAddress): Stream[IO, Option[List[EventData]]] = {
//
////    val serverAddr = new InetSocketAddress("localhost", 9999);
//
////    val bufferSize = 16348
//    val outcome = for {
////      serverAddr <- Stream.eval(IO(new InetSocketAddress("localhost", 9999)))
//      res <- Stream.eval{
//        IO(socketGroup.client[IO](serverAddr
//          ,true,256 * 1024,256 * 1024
//          ,true))
//      }
//      chunks <- Stream.eval{res.use { r =>
//        println(s"READING CHUNKS")
//        r.read(4096)
//      }}
//      strList <- Stream.eval{IO(chunks.map(_.toList.map(_.toChar).mkString("")))}
//    }yield {
//      val r = strList.map { str =>
//        str.split("\n").toList
//          .flatMap { s =>
//            val decodedEventData = decode[EventData](s)
//            decodedEventData.toOption
//          }
//      }
//      println(s" GOT EVENT DATA SIZE ${r.map(_.size)}")
//      println(s"R: $r")
//      r
//    }
//    outcome
//  }
//
//  def init(socketGroup: SocketGroup) = for {
//    serverAddr <- Stream.eval(IO(new InetSocketAddress("localhost", 9999)))
//    _ <- Stream.eval(IO(println(s"IN INIT")))
//    _ <- Stream.awakeEvery[IO](10 seconds) >> tcpStream(socketGroup, serverAddr)
//  } yield ()
//
//  override def run(args: List[String]): IO[ExitCode] = {
//    Blocker[IO].use { bl =>
////      server.resource.use(_ => IO.never).as(ExitCode.Success)
////      SocketGroup[IO](bl).use { tcpStream(_).compile.drain.as(ExitCode.Success) }
//      SocketGroup[IO](bl).use { init(_).compile.drain.as(ExitCode.Success) }
//    }
//  }
//
//
////  /**
////   * This methods translates the data from an inputstream (say, from a socket)
////   * to '\n' delimited strings and returns an iterator to access the strings.
////   */
////  def bytesToLines(inputStream: InputStream): Iterator[String] = {
////    val dataInputStream = new BufferedReader(
////      new InputStreamReader(inputStream, StandardCharsets.UTF_8))
////    new NextIterator[String] {
////      protected override def getNext() = {
////        val nextValue = dataInputStream.readLine()
////        if (nextValue == null) {
////          finished = true
////        }
////        nextValue
////      }
////
////      protected override def close(): Unit = {
////        dataInputStream.close()
////      }
////    }
////  }
//}
///** Provides a basic/boilerplate Iterator implementation. */
////private[spark] abstract class NextIterator[U] extends Iterator[U] {
////
////  private var gotNext = false
////  private var nextValue: U = _
////  private var closed = false
////  protected var finished = false
////
////  /**
////   * Method for subclasses to implement to provide the next element.
////   *
////   * If no next element is available, the subclass should set `finished`
////   * to `true` and may return any value (it will be ignored).
////   *
////   * This convention is required because `null` may be a valid value,
////   * and using `Option` seems like it might create unnecessary Some/None
////   * instances, given some iterators might be called in a tight loop.
////   *
////   * @return U, or set 'finished' when done
////   */
////  protected def getNext(): U
////
////  /**
////   * Method for subclasses to implement when all elements have been successfully
////   * iterated, and the iteration is done.
////   *
////   * <b>Note:</b> `NextIterator` cannot guarantee that `close` will be
////   * called because it has no control over what happens when an exception
////   * happens in the user code that is calling hasNext/next.
////   *
////   * Ideally you should have another try/catch, as in HadoopRDD, that
////   * ensures any resources are closed should iteration fail.
////   */
////  protected def close(): Unit
////
////  /**
////   * Calls the subclass-defined close method, but only once.
////   *
////   * Usually calling `close` multiple times should be fine, but historically
////   * there have been issues with some InputFormats throwing exceptions.
////   */
////  def closeIfNeeded(): Unit = {
////    if (!closed) {
////      // Note: it's important that we set closed = true before calling close(), since setting it
////      // afterwards would permit us to call close() multiple times if close() threw an exception.
////      closed = true
////      close()
////    }
////  }
////
////  override def hasNext: Boolean = {
////    if (!finished) {
////      if (!gotNext) {
////        nextValue = getNext()
////        if (finished) {
////          closeIfNeeded()
////        }
////        gotNext = true
////      }
////    }
////    !finished
////  }
////
////  override def next(): U = {
////    if (!hasNext) {
////      throw new NoSuchElementException("End of stream")
////    }
////    gotNext = false
////    nextValue
////  }
////}
//
