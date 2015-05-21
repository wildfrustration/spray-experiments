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
      persist(r){ event =>
        context.system.eventStream.publish(event)
      }
    }

    

  }

  val receiveRecover: Receive = {
    case _ => //ignored there is no state
  }

}