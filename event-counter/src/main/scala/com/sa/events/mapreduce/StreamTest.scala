package com.sa.events.mapreduce

import java.io.{File, PrintWriter}
import java.nio.file.Paths

import cats.effect.Async
import sourcecode.Macros.Chunk

object StreamTest extends App{

  def readAndWriteFileBadForHugeFiles(readFrom: String, writeTo: String) = {
    val counts = scala.collection.mutable.Map.empty[String, Int]
    val fileSource = scala.io.Source.fromFile(readFrom)
    val startTime = System.currentTimeMillis()
    try {
      fileSource
        .getLines()
        .toList
        .flatMap(_.split("\\W+"))
        .foreach { word =>
          counts += (word -> (counts.getOrElse(word, 0) + 1))
        }
    } finally {
      fileSource.close()
    }
    val fileContent = counts.foldLeft("") {
      case (accumulator, (word, count)) =>
        accumulator + s"$word = $count\n"
    }
    val writer = new PrintWriter(new File(writeTo))
    writer.write(fileContent)
    writer.close()
  }
  //  readAndWriteFileBadForHugeFiles("/opt/data/imdb/title.akas.tsv"
  //    ,"/opt/data/imdb/output")

  import cats.effect.{IO,ConcurrentEffect,ContextShift,Blocker,Timer}
  import fs2.Stream
  import fs2.{Pipe,io,text}

  import java.util.concurrent.Executors
  import scala.concurrent.{ExecutionContext,ExecutionContextExecutorService}

  val blockingUnboundThreadPool =
    Executors.newCachedThreadPool((r: Runnable) => {
      val t = new Thread(r)
      t.setName(s"blocking-ec-${t.getName()}")
      t
    })

  val blockingEc: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(blockingUnboundThreadPool)

  implicit val ioContextShift: ContextShift[IO] =
    IO.contextShift(blockingEc)

  def readAndWriteFileStreaming(readFrom: String, writeTo: String): Stream[IO,Unit] =
    Stream.resource(Blocker[IO]).flatMap { blocker =>
      /*
        Source:
          - starting point of stream or
          - entry point of our stream
          - with one open output, responsible for incrementally fetching data from
            + file
            + database
            + socket
          - Can comprise any number of internal sources and transformations wired together

         FS2 Stream model is "pull" model, means
          - "downstream" functions or parts call "upstream" functions to obtain data when needed
          - "Source" which is responsible for loading the data, loads data if and only
            if data is needed further down in the processing pipeline.
          - If error occurs downstream or exception is thrown or not handled,
            the "Source" stops loading the data and typically releases acquired resources.

            val intStream: Stream[Pure, Int] = Stream(1,2,3,4,5)

            Here source is a Stream that doesn't need a context
            Hence the Pure F type and emits Ints
       */

      val source: Stream[IO,Byte] = io
        .file
        .readAll[IO](Paths.get(readFrom),blocker,4096)
      //      val source: Stream[IO,Byte] = io.file.readAll[IO](Paths.get(readFrom),blocker,102400)
      /*
        Looking at type signature of the source we defined:
          source: Stream[IO,Byte]
        The very first parameter IO is the context of the environment type,
        and second parameter Byte is the type of data this source emits downstream.

        There must be at least one source in every stream.
       */
      var flatMapStep = 0
      var foldStep = 0
      var mapStep = 0
      val startTime = System.currentTimeMillis()
      var newStartTime = 0L
      //      println(s"source: $source")

      /* Count logic in a pipe
        - takes the stream of bytes
       */

      val pipe: Pipe[IO,Byte,Byte] = src =>
        //         - converts them into string
        src.through(text.utf8Decode)
          // split them by lines
          .through(text.lines)
          // split lines into words that are flattened into Stream
          .flatMap {
            line =>
              Stream(line.split("\\W+"): _*)
          }
          .evalMap {
            //          e => println(s"post flatMap: $e ${e.length}"); IO(e)
            e =>
              flatMapStep += 1
              //              println(s"post flatMap:  ${e.length}")
              IO(e)
          }
          // save words and number of occurrences into a Map
          .fold(Map.empty[String,Int]){ (count,word) =>
            //            println(s"fold step: ${foldStep}")
            //            foldStep += 1
            count + (word -> (count.getOrElse(word,0) + 1))
          }
          .evalMap {
            //            e => println(s"post fold $e ${e.size}"); IO(e)
            e =>
              println(s"Time for flatMap step and post fold: ${(System.currentTimeMillis - startTime)/1000} sec")
              println(s" flatMapStep = $flatMapStep post fold  ${e.size}")
              newStartTime = System.currentTimeMillis()
              IO(e)
          }
          // creates a string that represents words separated by \n
          // and their number of occurrences
          // TODO: replace append with String builder
          .map(_.foldLeft(""){
            case (accumulator,(word,count)) =>
              //              println(s"map step: ${mapStep}")
              //              mapStep += 1
              //              accumulator + s"$word = $count\n"
              //              s"$word" + s"$accumulator = $count\n"
              s"$word = $count\n"
          })
          .evalMap {
            //            e => println(s"post map $e ${e.size}"); IO(e)
            e =>
              println(s"Time for post map: ${(newStartTime - System.currentTimeMillis())/1000} sec")
              println(s"post map  ${e.size}")
              IO(e)
          }
          // transforms String into a stream of Byte
          .through(text.utf8Encode)

      val sink: Pipe[IO,Byte,Unit] = io.file.writeAll(Paths.get(writeTo),blocker)

      val source1 = io
        .file
        .readAll[IO](Paths.get(readFrom),blocker,4096)
        .chunkN(1000)
      //        .map(e => e.toList)

      source
        .through(pipe)
        .through(sink)

    }

  val hugeFile= "/opt/data/imdb/title.akas.tsv"
  val smallFile = "/opt/data/imdb/imdb_title_principals.csv"
  val smallerFile = "/opt/data/grasshopper/market_data_v2.csv"

  //  readAndWriteFileStreaming(smallFile
  //    ,"/opt/data/imdb/output").compile.drain.unsafeRunSync()

  //  readAndWriteFileStreaming(smallerFile
  //    ,"/opt/data/imdb/output").compile.drain.unsafeRunSync()

  //  readAndWriteFileStreaming(hugeFile
  //    ,"/opt/data/imdb/output").compile.drain.unsafeRunSync()

  /*
    BATCHING
      a process of grouping elements and emitting them downstream for processing.
      In fs2 these groups of elements are called Chunks.

      A "Chunk" can be thought of a a finite sequence of values that is used by fs2
      streams internally.
   */
  Stream((1 to 100): _*)
    .chunkN(10)
    .map(println)
    .compile
    .drain


  def writeToConsole[F[_]: Async](chunk: fs2.Chunk[String]): F[Unit] =
    Async[F].async { callback =>
      println(s"[thread: ${Thread.currentThread().getName}] :: Writing $chunk to socket")
      callback(Right(()))
    }

  Stream((1 to 100).map(_.toString): _*)
    .chunkN(10)
    .covary[IO]
    .parEvalMapUnordered(10)(writeToConsole[IO])
    .compile
    .drain
    .unsafeRunSync()
}
