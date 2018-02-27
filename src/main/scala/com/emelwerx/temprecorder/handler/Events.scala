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

  def postTemperature: Route = entity(as[Temperature]) { temp: Temperature =>
    val futureSave: Future[Done] = saveTemperature(temp)
    onComplete(futureSave) { done =>
      val msg = s"p2 ($done) temp recorded! ($temp)\n"
      print(msg)
      complete(temp)
    }
  }

  def getTemperature(temp: Long): Route = {
    println(s"g1 http GET by temperature ($temp)")
    val magnet: Future[Option[Temperature]] = fetchTemperature(temp)
    onSuccess(magnet)  {
      case Some(temperature) =>
        val msg = s"g4 yay, completed getting temperature from MongoDB ($temperature)\n"
        println(msg)
        complete(temperature)
      case None =>
        println("g5 get failed to complete\n")
        complete(StatusCodes.NotFound)
    }
  }
}