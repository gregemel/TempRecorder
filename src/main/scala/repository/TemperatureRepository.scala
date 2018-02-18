package repository

import hello.TemperatureEndpoint.Temperature
import org.mongodb.scala._
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


    val doc: Document = Document("_id" -> 1, "name" -> "MongoDB", "type" -> "database",
      "count" -> 1, "info" -> Document("location" -> "basement", "dateTime" -> "2018-02-16T22:12:00", "temp" -> 72))

    val observable: Observable[Completed] = collection.insertOne(doc)

    observable.subscribe(new Observer[Completed] {

      override def onNext(result: Completed): Unit = println("Inserted")

      override def onError(e: Throwable): Unit = println("Failed")

      override def onComplete(): Unit = println("Completed")
    })

  }

}
