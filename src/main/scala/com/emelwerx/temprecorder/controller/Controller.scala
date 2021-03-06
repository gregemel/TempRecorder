package com.emelwerx.temprecorder.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import scala.concurrent.{ExecutionContextExecutor, Future}
import com.emelwerx.temprecorder.handler.Events.{getTemperature, postTemperature}

object Controller {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher


  def bindServerEndpoint: Future[Http.ServerBinding] = {
    val route: Route =
      post {
        path("record-temp") {
          postTemperature
        }
      } ~
      get {
        pathPrefix("temperature" / LongNumber) { id =>
          getTemperature(id)
        }
      }

    //todo: pull endpoint settings from config
    val address = "localhost"
    val port = 8080
    val bindingFuture = Http().bindAndHandle(route, address, port)
    println(s"server endpoint bound to http://$address:$port")
    bindingFuture
  }

  def unbindServerEndpoint(bindingFuture: Future[Http.ServerBinding]): Unit = {
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ ⇒ sayGoodbye) // and shutdown when done
  }

  private def sayGoodbye = {
    println("\ngoodbye\n")
    system.terminate()
  }
}