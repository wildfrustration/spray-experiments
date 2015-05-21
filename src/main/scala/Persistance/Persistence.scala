package Persistence

import WebInterface.FakeLatency
import akka.actor.Actor.Receive
import akka.actor._
import akka.persistence._
import akka.util.Timeout
import scala.concurrent.duration._

object Persistence extends App {

  implicit val timeout = Timeout(1.second)

  implicit val persistenceSystem = ActorSystem("persistence-system")

  val persistence = persistenceSystem.actorOf(Props[ChallengePersistentActor], "persistence-actor")

}

// Members declared in akka.persistence.Recovery
class ChallengePersistentActor extends PersistentActor {

  val persistenceId = "log-writer"

  val receiveCommand: Receive = {

    case r @ FakeLatency(seconds) => {
      println("message received in writer: " + r)
      persist(r)( _ => Unit)
    }

    case _ => Unit

  }

  val receiveRecover: Receive = {
    case _ => Unit//ignored there is no state
  }

}

class ChallengeViewActor extends PersistentView {

  val persistenceId = "log-writer"

  val viewId = "log-view"

  def receive = {
    case r @ FakeLatency(seconds) => {
      println("VIEW MESSAGE RECEIVED: " + r)
    }
  }

}

