package com.emelwerx.temprecorder.repository

import java.util.concurrent.TimeUnit

import akka.Done
import com.emelwerx.temprecorder.model.Temperature
import com.emelwerx.temprecorder.repository.Helpers.ImplicitObservable
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.{Completed, Document, MongoClient, Observable, Observer}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor, Future}

object Repository {
  import com.emelwerx.temprecorder.controller.Controller.system
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  implicit class DocumentObservable[C](val observable: Observable[Document]) extends ImplicitObservable[Document] {
    override val converter: (Document) => String = (doc) => doc.toJson
  }

  def fetchTemperature(itemId: Long): Future[Option[Temperature]] = Future {
    println(s"g3 get temperature ($itemId) from in-memory db")
    import org.mongodb.scala.model.Filters._
    val observer = collection.find(equal("info.temp", itemId)).first()
    val document: Document = Await.result(observer.head(), Duration(10, TimeUnit.SECONDS))
    println(s"got a document has returned: ($document)\n")
    Some(makeTemp(document))
  }

  def saveTemperature(temp: Temperature): Future[Done] = Future {
    temp match {
      case Temperature(temp.location, temp.dateTime, temp.temp) =>
        println(s"p3 saving valid temperature record: ($temp)")
        writeTemperature(temp)
      case _            =>
        println(s"p3 somtheing worng...($temp)\n")    //this never gets called.  broken json gets handled upstream
    }
    Done
  }

  private def writeTemperature(temp: Temperature): Unit = {
    collection.insertOne(makeDoc(temp))
      .subscribe(new Observer[Completed] {
        override def onNext(result: Completed): Unit = {
          println(s"Inserted! ($result)")
          updateLogSize()
        }
        override def onError(e: Throwable): Unit = println("Failed!")
        override def onComplete(): Unit = println(s"Completed ($temp)")
      })
  }

  private lazy val collection = {
    //todo: pull mongo settings from config
    MongoClient("mongodb://localhost:27017")
      .getDatabase("temperatureLog")
      .getCollection("temps")
  }

  private def updateLogSize(): Unit = {
    val insertAndCount = for {
      countResult <- collection.count()
    } yield countResult

    println(s"countResult: ($insertAndCount)")

    insertAndCount.subscribe(new Observer[Long] {
      override def onNext(result: Long): Unit = println(s"count! ($result)")
      override def onError(e: Throwable): Unit = println("failed!")
      override def onComplete(): Unit = println("completed!")
    })
  }

  private def makeDoc(temp: Temperature): Document = {
    Document(
      "name" -> "MongoDB",
      "type" -> "database",
      "count" -> 1,
      "info" -> Document(
        "location" -> temp.location,
        "dateTime" -> temp.dateTime,
        "temp" -> temp.temp))
  }

  private def makeTemp(doc: Document): Temperature = {
    val info: BsonDocument = doc.get[BsonDocument]("info").get
    Temperature(
      info.getString("location").getValue,
      info.getString("dateTime").getValue,
      info.getInt64("temp").getValue)
  }
}