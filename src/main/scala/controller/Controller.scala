package controller

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import handler.Events

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object Controller {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  def main(args: Array[String]) {

    val route: Route =
      get {
        pathPrefix("temperature" / LongNumber) { id =>
          Events.routeGetTemp(id)
        }
      } ~
        post {
          path("record-temp") {
            Events.routePostTemp
          }
        }

    //todo: pull endpoint settings from config
    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...\n")

    StdIn.readLine() // wait until the return key is pressed
    println("shutting down...")

    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ ⇒ completeService) // and shutdown when done

    println("...not yet...")
  }

  private def completeService = {
    println("now!")
    system.terminate()
  }
}