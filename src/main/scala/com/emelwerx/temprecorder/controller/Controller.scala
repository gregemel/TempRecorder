package com.emelwerx.temprecorder.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.emelwerx.temprecorder.handler.Events.{routeGetTemp,routePostTemp}

import scala.concurrent.{ExecutionContextExecutor, Future}

object Controller {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  def bindServerEndpoint: Future[Http.ServerBinding] = {
    val route: Route =
      get {
        pathPrefix("temperature" / LongNumber) { id =>
          routeGetTemp(id)
        }
      } ~
        post {
          path("record-temp") {
            routePostTemp
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