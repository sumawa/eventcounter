package com.sa.events.mapreduce

import java.nio.file.Paths

/*
  Group large stream into sub streams
  https://stackoverflow.com/questions/52942137/how-to-group-large-stream-into-sub-streams
  SegmentN
  https://stackoverflow.com/questions/49708080/fs2-functional-streams-for-scala-how-to-do-groupn
 */
object StreamingApproach extends App{

  val weather = "/opt/data/spcore/weather.csv"

  def streamingApproach = {
    import cats.effect.{IO,ConcurrentEffect,ContextShift,Blocker,Clock,Timer}
    import fs2.{Stream,Pipe,io,text,Chunk}
    import cats.Eq
    import java.util.concurrent.TimeUnit
    TimeUnit.MINUTES


    import java.util.concurrent.Executors
    import scala.concurrent.{ExecutionContext,ExecutionContextExecutorService}

    val blockingUnboundThreadPool: java.util.concurrent.ExecutorService =
      Executors.newCachedThreadPool((r: Runnable) => {
        val t = new Thread(r)
        t.setName(s"blocking-ec-${t.getName()}")
        t
      })

    val blockingEc: ExecutionContextExecutorService =
      ExecutionContext.fromExecutorService(blockingUnboundThreadPool)

    implicit val ioContextShift: ContextShift[IO] =
      IO.contextShift(blockingEc)

//    Thread.sleep(10000)
    val res = Stream.resource(Blocker[IO]).flatMap { blocker =>
      val source: Stream[IO,Byte] = io
        .file
        .readAll[IO](Paths.get(weather),blocker,4096)

      def f(s: String) = {
        s.split(",")(0).split("-")(0)
      }

      def groupedEventsStream[A](stream: fs2.Stream[IO, A])
                                (implicit clock: Clock[IO], eq: Eq[Long]): fs2.Stream[IO, (Long, Chunk[(Long, A)])] =
        stream.evalMap(t => clock.realTime(TimeUnit.MINUTES).map((_, t)))
          .groupAdjacentBy(_._1)

      def groupedEventsStream1(stream: fs2.Stream[IO, String])
//                                (implicit eq: Eq[String])
                            : fs2.Stream[IO, (String, Chunk[(String, String)])] =
        stream.evalMap{t => IO((f(t),t))}
          .groupAdjacentBy(_._1)
//          .groupAdjacentBy(_.)}

      // https://devon-miller.gitbook.io/test_private_book/miscellaneous
      import scala.collection.JavaConverters._
//      val pipe: Pipe[IO,Byte,(String,Chunk[(String,String)])] = src =>
//    val pipe: Pipe[IO,Byte,(String,List[String])] = src =>
    val pipe: Pipe[IO,Byte,(String,String)] = src =>


  //         - converts them into string
        src.through (text.utf8Decode)
          // split them by lines
          .through (text.lines)
          // split lines into words that are flattened into Stream
          .flatMap {
            line =>
              println(s"line: $line")
              val key = f(line)
              val temp = line.split(",")(2)
              println(s" eval  ${(key,temp)}")

              Stream.eval(IO((key,temp)))
          }
          .groupAdjacentBy(_._1)
          .map { case (k,vs) =>
            println(s"vs ${vs.toList}")
            k -> vs.toList(0)._2
          }

//      val sink: Pipe[IO,Byte,Unit] = io.file.writeAll(Paths.get(writeTo),blocker)

//      val res = source
//        .through(pipe).compile.toList
//        .through(sink)


        source.through(pipe)
      //.evalMap(e => IO(e))
          //.groupedIO
//          .gr
//          .gro

    }.compile.toList

    res.unsafeRunSync()

  }

  println(s"streaming approach : $streamingApproach")

}
