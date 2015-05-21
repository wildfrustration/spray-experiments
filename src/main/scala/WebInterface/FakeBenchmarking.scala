package WebInterface

import akka.actor.Actor
import akka.util.Timeout
import spray.routing._
import spray.http._
import MediaTypes._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport.sprayJsonMarshaller
import spray.httpx.SprayJsonSupport.sprayJsonUnmarshaller

case class FakeLatency(seconds: Long)

object JsonImplicits extends DefaultJsonProtocol {
  implicit val FakeLatencyFormat = jsonFormat1(FakeLatency)
}


// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class FakeBenchmarkingActor extends Actor with FakeBenchmarking {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}

// this trait defines our service behavior independently from the service actor
trait FakeBenchmarking extends HttpService {

  implicit val timeout = Timeout(5.seconds)

  import JsonImplicits._

  /*
    test non-blocking
    http post http://localhost:8080 seconds:=5 & http post http://localhost:8080 seconds:=1 & http post http://localhost:8080 seconds:=3
  */

  val myRoute =
    path("") {
      post {
        benchmark(Logger.inMemory) {
          entity(as[FakeLatency]) { latency =>
            respondWithMediaType(MediaTypes.`application/json`){
              complete {
                println("sleep for: " + latency.seconds)
                FakeLongOperations.inTheFuture(latency.seconds).map { result =>
                  latency
                }
              }
            }
          }
        }
      }
    } ~
    path("") {
      get {
        complete {
          akka.pattern.ask(Logger.logging, "stats").map {
            case (n, latency) => {
              s"hits: $n, average: $latency"
            }
            case _ => {
              ""
            }
          }
        }
      }
    }

  def benchmark(logger: (Long) => Unit): Directive0 = mapRequestContext { context =>
    val begin = System.currentTimeMillis
    context.withHttpResponseMapped { response =>
      logger(System.currentTimeMillis - begin)
      response
    }
  }
}