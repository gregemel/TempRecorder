package repository

import _root_.model.Temperature
import akka.Done
import controller.TemperatureEndpoint.system
import org.mongodb.scala._

import scala.concurrent.{ExecutionContextExecutor, Future}

object TemperatureRepository {
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
        TemperatureRepository.writeTemperature(temp)
        temperatureHistory = temp :: temperatureHistory
      case _            =>
        println(s"p3 somtheing worng...($temp)\n")
        temperatureHistory
    }
    Future { Done }
  }

  def writeTemperature(temp: Temperature): Unit = {

    val mongoClient: MongoClient = MongoClient("mongodb://localhost:27017")
    val database: MongoDatabase = mongoClient.getDatabase("temperatureLog")
    val collection: MongoCollection[Document] = database.getCollection("temps")

    val doc: Document = Document("name" -> "MongoDB", "type" -> "database",
      "count" -> 1, "info" -> Document("location" -> "basement", "dateTime" -> "2018-02-16T22:12:00", "temp" -> 72))

    val observable: Observable[Completed] = collection.insertOne(doc)

    observable.subscribe(new Observer[Completed] {
      override def onNext(result: Completed): Unit = println("Inserted")
      override def onError(e: Throwable): Unit = println("Failed")
      override def onComplete(): Unit = println("Completed")
    })
  }
}