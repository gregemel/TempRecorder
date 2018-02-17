package hello

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.StdIn

object TemperatureEndpoint {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  var temperatureHistory: List[Temperature] = Nil

  final case class Temperature(location: String, dateTime: String, temp: Long) {
    println(s"p1 creating a temp ($location, $temp)")
  }
  implicit val tempFormat: RootJsonFormat[Temperature] = jsonFormat3(Temperature)

  def fetchTemperature(itemId: Long): Future[Option[Temperature]] = Future {
    println(s"g3 get temperature ($itemId) from in-memory db")
    temperatureHistory.find(o => o.temp == itemId)
  }

  def saveTemperature(temp: Temperature): Future[Done] = {
    temp match {
      case Temperature(temp.location, temp.dateTime, temp.temp) =>
        println(s"p3 saving valid temperature record: ($temp)")
        temperatureHistory = temp :: temperatureHistory
      case _            =>
        println(s"p3 somtheing worng...($temp)\n")
        temperatureHistory
    }
    Future { Done }
  }

  def main(args: Array[String]) {

    val route: Route =
      get {
        pathPrefix("temperature" / LongNumber) { id =>
          println(s"g1 get $id")
          val futureMaybeTemperature: Future[Option[Temperature]] = fetchTemperature(id)
          println("g2 about to get...")
          val success = onSuccess(futureMaybeTemperature) {
            case Some(validId) =>
              val msg = s"g5 yay, completed getting ($validId)\n"
              println(msg)
              complete(msg)
            case None       =>
              println("g5 get failed to complete\n")
              complete(StatusCodes.NotFound)
          }
          println("g4 waiting for maybe...")
          success
        }
      } ~
        post {
          path("record-temp") {
            println("p0 pull the post data")
            entity(as[Temperature]) { temp =>
              println("p2 about to save")
              val saved: Future[Done] = saveTemperature(temp)
              println("p4 write a temp record")
              val doneIt = onComplete(saved) { done =>
                val msg = s"p6 ($done) temp recorded! ($temp) log size=${temperatureHistory.size}\n"
                print(msg)
                complete(msg)
              }
              print("p5 about to write new record...\n")
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
