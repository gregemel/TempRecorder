package hello

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.Done
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.io.StdIn
import scala.concurrent.{ExecutionContextExecutor, Future}

object TemperatureEndpoint {

  // needed to run the route
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  // needed for the future map/flatmap in the end and future in fetchItem and saveOrder
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  var tempHistory: List[Temperature] = Nil

  // domain model
  final case class Temperature(location: String, temp: Long)
  final case class TempLog(entries: List[Temperature])

  // formats for unmarshalling and marshalling
  implicit val tempFormat: RootJsonFormat[Temperature] = jsonFormat2(Temperature)

  // (fake) async database query api
  def fetchItem(itemId: Long): Future[Option[Temperature]] = Future {
    println(s"fetchItem($itemId)")
    tempHistory.find(o => o.temp == itemId)
  }
  def saveTemp(temp: Temperature): Future[Done] = {
    temp match {
      case Temperature(temp.location, temp.temp) =>
        println(s"saving...$temp")
        tempHistory = temp :: tempHistory
      case _            => tempHistory
    }
    Future { Done }
  }

  def main(args: Array[String]) {

    val route: Route =
      get {
        pathPrefix("item" / LongNumber) { id =>
          val maybeItem: Future[Option[Temperature]] = fetchItem(id)

          onSuccess(maybeItem) {
            case Some(item) =>
              println(s"item completed $item")
              complete(item)

            case None       =>
              println("item failed to complete")
              complete(StatusCodes.NotFound)
          }
        }
      } ~
        post {
          path("record-temp") {
            entity(as[Temperature]) { temp =>
              val saved: Future[Done] = saveTemp(temp)
              onComplete(saved) { done =>
                complete(s"temp recorded! $temp\nlog size=${tempHistory.size}")
              }
            }
          }
        }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ ⇒ system.terminate()) // and shutdown when done
  }
}
