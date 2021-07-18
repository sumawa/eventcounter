package com.sa.events.wc

import java.sql.SQLException

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

object StatefulWC {

  case class EventData(event_type: String, data: String, timestamp: Long)

  object EventData{
//    type EventE[T] = Either[String,T]
//    type EventEx[T] = Either[SQLException,T]
    implicit val decode: Decoder[EventData] = deriveDecoder[EventData]
    implicit val encode: Encoder[EventData] = deriveEncoder[EventData]
  }

  def updateFunction(newValues: Seq[Int], runningCount: Option[Int]): Option[Int] = {
    println(s"newValues: $newValues === runningCount: $runningCount")
    val newCount = runningCount.getOrElse(0) + newValues.sum
    Some(newCount)
  }

  def main(args: Array[String]): Unit = {
    // Create local Streaming Context with batch interval of 10 second
    val conf = new SparkConf().setMaster("local[*]").setAppName("StatefulWC")
    val sc = new SparkContext(conf)
    sc.setLogLevel("WARN")

    val streamingContext = new StreamingContext(sc,Seconds(20))
    streamingContext.checkpoint("chk")

    // Create a DStream that will connect to hostname:port like localhost:9999
    val lines = streamingContext.socketTextStream("localhost",9999)

    import io.circe._, io.circe.parser._
    import io.circe.parser.decode

    val words = lines.flatMap { l =>
      println(s"LINESSSSS: $l")
      val decodedEventData = decode[EventData](l)
      println(s"decodedEventData: $decodedEventData")
      l.split(" ")
    }

    // Count each word in each batch
    val pairs = words.map(word => (word,1))
//    val wordCounts = pairs.reduceByKey(_ + _)
    val wordCounts = pairs.reduceByKey((x,y) => x + y)
    wordCounts.print()
    val runningCounts = pairs.updateStateByKey[Int](updateFunction _)
    // print the elements of each RDD generated in this stream to the console.
    runningCounts.print()

    /*
      alternate
     */
    val events = lines.flatMap { l =>
      println(s"LINESSSSS: $l")
      val decodedEventData = decode[EventData](l)
      println(s"decodedEventData: $decodedEventData")
      decodedEventData.toOption
//      l.split(" ")
    }


//    val eventsCount = pairsEvent.groupByKey()
  def updateFunctionEvent(newValues: Seq[EventData], currentValue: Option[EventData]): Option[Int] = {
    println(s"newValues: $newValues === runningCount: $currentValue")
    val mp = newValues.groupBy(_.event_type)
    val newMp = mp.map{ case (k,v) => (k -> v.size)}
    println(s"new mp: $newMp")
    Some(5)
//    val newCount = currentValue.map(v => v.)
//    Some(newCount)
  }

    val pairsEvent = events.map(ev => (ev.event_type,1))
    pairsEvent.print()

//    val pairsEventGrouped = pairsEvent.groupByKey()
//    pairsEventGrouped.print()

    val runningEvents = pairsEvent.updateStateByKey[Int](updateFunction _)
    runningEvents.print()

    // start the computation
    streamingContext.start()

    // Wait for computation to terminate
    streamingContext.awaitTermination()
  }
}
