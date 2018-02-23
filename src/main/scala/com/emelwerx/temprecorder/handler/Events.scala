package com.emelwerx.temprecorder.handler

import akka.Done
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.emelwerx.temprecorder.model.Temperature
import com.emelwerx.temprecorder.repository.Repository.{fetchTemperature, saveTemperature}
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.concurrent.Future

object Events {

  implicit val tempFormat: RootJsonFormat[Temperature] = jsonFormat3(Temperature)

  def routePostTemp: Route = {
    println("p0 pull the post data")
    entity(as[Temperature]) { temp =>
      println("p2 about to save")
      val saved: Future[Done] = saveTemperature(temp)
      println("p4 write a temp record")
      val doneIt = onComplete(saved) { done =>
        val msg = s"p6 ($done) temp recorded! ($temp)\n"
        print(msg)
        complete(msg)
      }
      print("p5 about to write new record...\n")
      doneIt
    }
  }

  def routeGetTemp(id: Long): Route = {
    println(s"g1 get $id")
    val futureMaybeTemperature: Future[Option[Temperature]] = fetchTemperature(id)
    println("g2 about to get...")
    val success = onSuccess(futureMaybeTemperature) {
      case Some(validId) =>
        val msg = s"g5 yay, completed getting ($validId)\n"
        println(msg)
        complete(msg)
      case None =>
        println("g5 get failed to complete\n")
        complete(StatusCodes.NotFound)
    }
    println("g4 waiting for maybe...")
    success
  }
}