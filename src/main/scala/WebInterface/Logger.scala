package WebInterface

import akka.actor._
import akka.util.Timeout
import scala.concurrent.duration._

object Logger {

  implicit val timeout = Timeout(1.second)

  implicit val system = ActorSystem("Logging-System")

  val logging = system.actorOf(Props[LoggingActor], name = "Logging-Actor")  // the local actor

  //this is not cluster safe
  def inMemory(latency: Long): Unit = { logging ! FakeLatency(latency / 1000); Unit}

}

class LoggingActor extends Actor {

  val persistence = context.actorSelection("akka.tcp://persistence-system@192.168.1.103:2552/user/persistence-actor")

  def receive = {

    case r @ FakeLatency(seconds) => {
      println("sending to persistence: " + r)
      persistence ! r
    }

    case _ => Unit

  }
}