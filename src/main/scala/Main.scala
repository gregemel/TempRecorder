import com.emelwerx.temprecorder.controller.Controller.{bindServerEndpoint, unbindServerEndpoint}

import scala.io.StdIn

object Main {

  def main(args: Array[String]): Unit = {
    val bindingFuture = bindServerEndpoint
    println(s"\nPress RETURN to stop...\n")

    StdIn.readLine() // wait until the return key is pressed
    println("shutting down...")

    unbindServerEndpoint(bindingFuture)
    println("...not yet...")
  }
}