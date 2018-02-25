package com.emelwerx.temprecorder.repository

import java.util.concurrent.TimeUnit

import akka.Done
import com.emelwerx.temprecorder.controller.Controller.system
import com.emelwerx.temprecorder.model.Temperature
import com.emelwerx.temprecorder.repository.Helpers.ImplicitObservable
import org.mongodb.scala.{Completed, Document, MongoClient, Observable, Observer}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor, Future}

object Repository {
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  implicit class DocumentObservable[C](val observable: Observable[Document]) extends ImplicitObservable[Document] {
    override val converter: (Document) => String = (doc) => doc.toJson
  }

  def fetchTemperature(itemId: Long): Future[Option[Temperature]] = Future {
    println(s"g3 get temperature ($itemId) from in-memory db")
    readTemperature(itemId)
  }


  def readTemperature(itemId: Long): Option[Temperature] = {
    println(s"g3 get temperature ($itemId) from mongodb")
    val observer = collection.find().first()
    val something: Document = Await.result(observer.head(), Duration(10, TimeUnit.SECONDS))

    println(s"something has returned: ($something)\n")
    var op = None: Option[Temperature]

    op = Some(Temperature("loc", "date", 32))

    op
  }

  def saveTemperature(temp: Temperature): Future[Done] = {
    temp match {
      case Temperature(temp.location, temp.dateTime, temp.temp) =>
        println(s"p3 saving valid temperature record: ($temp)")
        writeTemperature(temp)
      case _            =>
        println(s"p3 somtheing worng...($temp)\n")
    }
    Future { Done }
  }

  def writeTemperature(temp: Temperature): Unit = {
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

  def updateLogSize(): Unit = {
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