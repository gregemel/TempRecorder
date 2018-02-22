package repository

import _root_.model.Temperature
import akka.Done
import controller.TemperatureEndpoint.system
import org.mongodb.scala._

import scala.concurrent.{ExecutionContextExecutor, Future}
//import org.mongodb.scala.connection.ClusterSettings

//// To directly connect to the default server localhost on port 27017
//val mongoClient: MongoClient = MongoClient()
//
//// Use a Connection String
//val mongoClient: MongoClient = MongoClient("mongodb://localhost")
//
//// or provide custom MongoClientSettings
//
//
//



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
        TemperatureRepository.saveTempurature(temp)
        temperatureHistory = temp :: temperatureHistory
      case _            =>
        println(s"p3 somtheing worng...($temp)\n")
        temperatureHistory
    }
    Future { Done }
  }




//  val clusterSettings: ClusterSettings = ClusterSettings
//                                          .builder()
//                                          .hosts(List(new ServerAddress("localhost")).asJava)
//                                          .build()
//  val settings: MongoClientSettings = MongoClientSettings
//                                          .builder()
//                                          .clusterSettings(clusterSettings)
//                                          .build()
//  val mongoClient: MongoClient = MongoClient(settings)

  def saveTempurature(temp: Temperature): Unit = {

    val mongoClient: MongoClient = MongoClient()
    //val mongoClient: MongoClient = MongoClient("mongodb://localhost")

    val database: MongoDatabase = mongoClient.getDatabase("tempuratureLog")

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
