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

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  var temperatureHistory: List[Temperature] = Nil

  final case class Temperature(location: String, temp: Long)
  implicit val tempFormat: RootJsonFormat[Temperature] = jsonFormat2(Temperature)

  def fetchTemperature(itemId: Long): Future[Option[Temperature]] = Future {
    println(s"fetchItem($itemId) from ")
    temperatureHistory.find(o => o.temp == itemId)
  }

  def saveTemperature(temp: Temperature): Future[Done] = {
    temp match {
      case Temperature(temp.location, temp.temp) =>
        println(s"saving valid temperature record: $temp\n")
        temperatureHistory = temp :: temperatureHistory
      case _            =>
        println(s"somtheing worng...$temp\n")
        temperatureHistory
    }
    Future { Done }
  }

  def main(args: Array[String]) {

    val route: Route =
      get {
        pathPrefix("item" / LongNumber) { id =>
          val futureMaybeTemperature: Future[Option[Temperature]] = fetchTemperature(id)
          val success = onSuccess(futureMaybeTemperature) {
            case Some(validId) =>
              val msg = s"yay, completed getting $validId\n"
              println(msg)
              complete(msg)
            case None       =>
              println("get failed to complete\n")
              complete(StatusCodes.NotFound)
          }
          println("waiting for maybe...\n")
          success
        }
      } ~
        post {
          path("record-temp") {
            entity(as[Temperature]) { temp =>
              val saved: Future[Done] = saveTemperature(temp)
              val doneIt = onComplete(saved) { done =>
                val msg = s"$done temp recorded! $temp\nlog size=${temperatureHistory.size}\n"
                print(msg)
                complete(msg)
              }
              print("about to write new record...\n")
              doneIt
            }
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
