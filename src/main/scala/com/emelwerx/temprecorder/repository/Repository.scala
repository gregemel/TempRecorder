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
    println(s"g2 get temperature ($itemId) from MongoDB")
    import org.mongodb.scala.model.Filters._
    val observer = collection
      .find(equal("info.temp", itemId))
      .first()
    println("blocking...\n")
    val document: Document = Await.result(observer.head(), Duration(10, TimeUnit.SECONDS))
    println(s"g3 MongoDB returned: ($document)\n")
    Some(makeTemp(document))
  }

  def saveTemperature(temp: Temperature): Future[Done] = Future {
    collection
      .insertOne(makeDoc(temp))
      .subscribe(new Observer[Completed] {
        override def onNext(result: Completed): Unit = {
          println(s"p3 Inserted ($result) in MongoDB")
          printRecordCount()
        }
        override def onError(e: Throwable): Unit = println("p3 Failed!")
        override def onComplete(): Unit = println(s"p4 completed recording ($temp) in MongoDB")
      })
    Done
  }

  private lazy val collection = {
    //todo: pull mongo settings from config
    MongoClient("mongodb://localhost:27017")
      .getDatabase("temperatureLog")
      .getCollection("temps")
  }

  private def printRecordCount(): Unit = {
    val value: Observable[Long] = for {
      countResult <- collection.count
    } yield countResult

    value.subscribe(new Observer[Long] {
      override def onNext(result: Long): Unit = println(s"current record count=($result)")
      override def onError(e: Throwable): Unit = println("failed!")
      override def onComplete(): Unit = println("count completed!")
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