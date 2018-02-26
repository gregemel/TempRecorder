package com.emelwerx.temprecorder.handler

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.emelwerx.temprecorder.model.Temperature
import com.emelwerx.temprecorder.repository.Repository.{fetchTemperature, saveTemperature}
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

object Events {

  implicit val tempFormat: RootJsonFormat[Temperature] = jsonFormat3(Temperature)

  def postTemperature: Route = {
    entity(as[Temperature]) { temp =>
      onComplete(saveTemperature(temp)) { done =>
        val msg = s"p6 ($done) temp recorded! ($temp)\n"
        print(msg)
        complete(temp)
      }
    }
  }

  def getTemperature(id: Long): Route = {
    onSuccess(fetchTemperature(id)) {
      case Some(validId) =>
        val msg = s"g5 yay, completed getting from uri ($validId)\n"
        println(msg)
        complete(validId)
      case None =>
        println("g5 get failed to complete\n")
        complete(StatusCodes.NotFound)
    }
  }
}