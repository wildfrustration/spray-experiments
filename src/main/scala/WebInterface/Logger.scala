package WebInterface

import Persistence.AllTimeStats
import akka.actor._
import akka.util.Timeout
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.pattern.ask

object Logger {

  implicit val timeout = Timeout(1.second)

  implicit val system = ActorSystem("Logging-System")

  val logging = system.actorOf(Props[LoggingActor], name = "Logging-Actor")  // the local actor

  //this is not cluster safe
  def inMemory(ms: Long): Unit = { logging ! FakeLatency(ms); Unit}

}

class LoggingActor extends Actor {

  implicit val timeout = Timeout(1.second)

  val persistence = context.actorSelection("akka.tcp://persistence-system@192.168.1.103:2552/user/persistence-actor")

  val view = context.actorSelection("akka.tcp://persistence-system@192.168.1.103:2552/user/view-actor")

  def receive = {

    case r @ FakeLatency(ms) => {
      println("sending to persistence: " + r)
      persistence ! r
    }

    case r @ "stats" => {
      sender() ! Await.result((view ? r).mapTo[AllTimeStats], timeout.duration)
    }

    case _ => Unit

  }
}