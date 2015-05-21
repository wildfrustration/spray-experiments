package WebInterface

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.can.Http
import scala.concurrent.duration._

object WebInterface extends App {

  implicit val system = ActorSystem("challenge-akka-system")

  val service = system.actorOf(Props[FakeBenchmarkingActor], "fake-benchmarking")

  implicit val timeout = Timeout(5.seconds)

  IO(Http) ? Http.Bind(service, interface = "localhost", port = 8080)
}
