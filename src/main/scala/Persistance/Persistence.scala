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

  val viewer = persistenceSystem.actorOf(Props[ChallengeViewActor], "view-actor")
}

// Members declared in akka.persistence.Recovery
class ChallengePersistentActor extends PersistentActor {

  val persistenceId = "log-writer"

  val receiveCommand: Receive = {

    case r @ FakeLatency(seconds) => {
      println("message received in writer: " + r)
      persist(r)( _ => Unit)
    }

    case _ => { println("dafuk"); Unit }

  }

  val receiveRecover: Receive = {
    case _ => Unit//ignored there is no state
  }

}

case class AllTimeStats(n: Long, latency: Long)

class ChallengeViewActor extends PersistentView {

  val persistenceId = "log-writer"

  val viewId = "log-view"

  var allTimesStats: AllTimeStats = AllTimeStats(0,0)

  def receive = {
    case r @ FakeLatency(seconds) => {
      val newN = allTimesStats.n + 1
      allTimesStats = AllTimeStats(newN, (allTimesStats.latency + seconds) / newN)
      println(allTimesStats)
    }

    case "stats" => {
      context.sender ! allTimesStats
    }

    case _ => Unit
  }

}

