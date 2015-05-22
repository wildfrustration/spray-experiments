package WebInterface

import Persistence.AllTimeStats
import akka.actor._
import akka.util.Timeout
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.pattern.ask
import com.typesafe.config._

object Logger {

  implicit val timeout = Timeout(1.second)

  implicit val system = ActorSystem("Logging-System")

  val logging = system.actorOf(Props[LoggingActor], name = "Logging-Actor")  // the local actor

  //this is not cluster safe
  def inMemory(ms: Long): Unit = { logging ! FakeLatency(ms); Unit}

}

class LoggingActor extends Actor {

  val conf = ConfigFactory.load()

  val persistenceIp = conf.getString("persistence.ip")

  implicit val timeout = Timeout(1.second)

  val persistence = context.actorSelection(s"akka.tcp://persistence-system@$persistenceIp:2552/user/persistence-actor")

  val view = context.actorSelection(s"akka.tcp://persistence-system@$persistenceIp:2552/user/view-actor")

  def receive = {

    case r @ FakeLatency(ms) => {
      println("sending to persistence: " + r)
      persistence ! r
    }

    case r @ "stats" => {
      (view ? r).mapTo[AllTimeStats].map( stats =>
        sender() ! stats
      )
    }

    case _ => Unit

  }
}