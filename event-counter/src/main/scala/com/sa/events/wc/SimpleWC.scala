package com.sa.events.wc

import java.sql.SQLException

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

object SimpleWC {

  case class EventData(event_type: String, data: String, timestamp: Long)

  object EventData{
    type EventE[T] = Either[String,T]
    type EventEx[T] = Either[SQLException,T]
    implicit val decode: Decoder[EventData] = deriveDecoder[EventData]
    implicit val encode: Encoder[EventData] = deriveEncoder[EventData]
  }

  def main(args: Array[String]): Unit = {
    // Create local Streaming Context with batch interval of 10 second
    val conf = new SparkConf().setMaster("local[*]").setAppName("SimpleWC")
    val sc = new SparkContext(conf)
    sc.setLogLevel("WARN")

    val streamingContext = new StreamingContext(sc,Seconds(20))

    // Create a DStream that will connect to hostname:port like localhost:9999
    val lines = streamingContext.socketTextStream("localhost",9999)

    println(s"lines: $lines")
    // Split each line in batch into words

//    val words1 = lines.flatMap { l =>
//      println(s"LINEEEEEE: $l")
//      l
////      l.split(" ")
//    }
//    words1.print()

    val words = lines.flatMap { l =>
      println(s"LINESSSSS: $l")
      l.split(" ")
    }

    // Count each word in batch
    val pairs = words.map(word => (word,1))
//    val wordCounts = pairs.reduceByKey(_ + _)
    val wordCounts = pairs.reduceByKey((x,y) => x + y)

    // print the elements of each RDD generated in this stream to the console.
    wordCounts.print()

    // start the computation
    streamingContext.start()

    // Wait for computation to terminate
    streamingContext.awaitTermination()
  }
}
