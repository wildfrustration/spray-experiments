package WebInterface

import akka.actor._

object Logger {

  implicit val system = ActorSystem("Logging-System")
  val logging = system.actorOf(Props[LoggingActor], name = "Logging-Actor")  // the local actor

  //this is not cluster safe
  def inMemory(ms: Long): Unit = { logging ! FakeLatency(ms); Unit}

}

class LoggingActor extends Actor {

  val persistence = context.actorSelection("akka.tcp://persistence-system@127.0.0.1:2552/persistence-actor")

  def receive = {

    case r @ FakeLatency(ms) => {
      println("sending to persistence")
      persistence ! r
    }

    case _ => Unit

  }
}