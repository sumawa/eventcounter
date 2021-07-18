package com.sa.events.mapreduce

import java.nio.file.Paths

import cats.effect.Concurrent
import fs2.Pipe

import scala.io.Source

object MapReduceExmMain extends App {

  val weather = "/opt/data/spcore/weather.csv"

  def simple() = {
    val maxTempYearMap = Map[String,Int]()

//    val mp = for  {
//      line <- Source.fromFile(weather).getLines
//      myMap = Map[List,Int]()
//      arr = line.split(",")
//      (fullDate,zipCode,temperature) = (arr(0),arr(1),arr(2).toInt)
//      dateArr = fullDate.split("-")
//      (year,month,day) = (dateArr(0),dateArr(1),dateArr(2))
//      currMaxTemp = if (maxTempYearMap.contains(year)){
//        val existingTemp = maxTempYearMap(year)
//        Math.max(existingTemp,temperature)
//      }else temperature
//      updatedMap
//    } yield (myMap + (year -> temperature))

    val myMap = scala.collection.mutable.Map[String,Int]()

    Thread.sleep(20000)
    for (line <- Source.fromFile(weather).getLines) {
      val arr = line.split(",")
      val (fullDate, zipCode, temperature) = (arr(0), arr(1), arr(2).toInt)
      val dateArr = fullDate.split("-")
      val (year,month,day) = (dateArr(0),dateArr(1),dateArr(2))
      val currMaxTemp = if (maxTempYearMap.contains(year)) {
        val existingTemp = maxTempYearMap(year)
        Math.max(existingTemp, temperature)
      } else
        temperature

      myMap(year) = currMaxTemp
    }
    List
    myMap
//    val ll = List(List("abc", "abc", "cbe", "cab"), List("abc", "uob", "bse", "lse"), List("def", "abc", "oae", "pup"))
//    val mm = ll.foldLeft(Map[String, List[String]]()) { (acc, x) =>
//      if (acc.contains(x(0))) {
//        acc + (x(0) -> (acc(x(0)) ++ x))
//      } else {
//        acc + (x(0) -> x)
//      }
//    }
  }

  def groupByApproach = {

//    Thread.sleep(10000)
    /*
      The signature of groupBy function for a list is like this

        def groupBy[K](f: A => K): immutable.Map[K, Repr]

      Internally the implementation uses a mutable Map to build a map using above f: A => K function
      and then returns an immutable map

      function to derive key "year" from a given line like this

        2016-05-09,234893,34
     */
    def f(s: String) = {
      s.split(",")(0).split("-")(0)
    }
    /*
      Using the above key derivation function will result a map of year -> grouped values for the year
      For example

      Map ( ....
        , 2016 -> List(2016-05-09,234893,34, 2016-02-03,234900,8, 2016-03-07,234900,-2, 2016-08-07,234894,36)
        , 2017 -> List(2017-04-04,234900,43, 2017-02-09,234900,21, ...)
      )

     */
    Source.fromFile(weather).getLines.toList.groupBy(f)
  }

//  println(s"myMap ${simple()} \n ")
  println(s"\n groupByApproach: $groupByApproach")


}
