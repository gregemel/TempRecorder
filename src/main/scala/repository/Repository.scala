package repository

import _root_.model.Temperature
import akka.Done
import controller.Controller.system
import org.mongodb.scala._

import scala.concurrent.{ExecutionContextExecutor, Future}

object Repository {
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  var temperatureHistory: List[Temperature] = Nil

  def fetchTemperature(itemId: Long): Future[Option[Temperature]] = Future {
    println(s"g3 get temperature ($itemId) from in-memory db")
    temperatureHistory.find(o => o.temp == itemId)
  }

  def saveTemperature(temp: Temperature): Future[Done] = {
    temp match {
      case Temperature(temp.location, temp.dateTime, temp.temp) =>
        println(s"p3 saving valid temperature record: ($temp)")
        writeTemperature(temp)
        temperatureHistory = temp :: temperatureHistory
      case _            =>
        println(s"p3 somtheing worng...($temp)\n")
    }
    Future { Done }
  }

  def writeTemperature(temp: Temperature): Unit = {
    //todo: pull mongo settings from config
    MongoClient("mongodb://localhost:27017")
      .getDatabase("temperatureLog")
      .getCollection("temps")
      .insertOne(makeDoc(temp))
      .subscribe(new Observer[Completed] {
        override def onNext(result: Completed): Unit = println(s"Inserted! ($result)")
        override def onError(e: Throwable): Unit = println("Failed!")
        override def onComplete(): Unit = println(s"Completed ($temp)")
      })
  }

  def makeDoc(temp: Temperature): Document = {
    Document(
      "name" -> "MongoDB",
      "type" -> "database",
      "count" -> 1,
      "info" -> Document(
        "location" -> temp.location,
        "dateTime" -> temp.dateTime,
        "temp" -> temp.temp))
  }
}