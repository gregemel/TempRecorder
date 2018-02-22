package controller

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import handler.TemperatureEvents

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object TemperatureEndpoint {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  def main(args: Array[String]) {

    val route: Route =
      get {
        pathPrefix("temperature" / LongNumber) { id =>
          TemperatureEvents.routeGetTemp(id)
        }
      } ~
        post {
          path("record-temp") {
            TemperatureEvents.routePostTemp
          }
        }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...\n")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ ⇒ system.terminate()) // and shutdown when done
  }
}
