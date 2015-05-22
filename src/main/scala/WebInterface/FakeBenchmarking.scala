package WebInterface

import akka.actor.Actor
import akka.util.Timeout
import akka.pattern._
import spray.routing._
import spray.http._
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport.sprayJsonMarshaller
import spray.httpx.SprayJsonSupport.sprayJsonUnmarshaller

import Persistence.AllTimeStats

case class FakeLatency(ms: Long)

class FakeBenchmarkingActor extends Actor with FakeBenchmarking {

  def actorRefFactory = context

  def receive = runRoute(myRoute)

}

object JsonImplicits extends DefaultJsonProtocol {

  implicit val FakeLatencyFormat = jsonFormat1(FakeLatency)

  implicit val AllTimeStatsFormat = jsonFormat2(AllTimeStats)

}

trait FakeBenchmarking extends HttpService {

  implicit val timeout = Timeout(5.seconds)

  import JsonImplicits._

  /*
    test non-blocking
    http post http://localhost:8080 ms:=5000 & http post http://localhost:8080 ms:=1000 & http post http://localhost:8080 ms:=3000
  */

  val myRoute =
    path("") {
      post {
        benchmark(Logger.write) {
          entity(as[FakeLatency]) { latency =>
            respondWithMediaType(MediaTypes.`application/json`){
              complete {
                println("sleep for: " + latency.ms)
                FakeLongOperations.inTheFuture(latency.ms).map { result =>
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
        respondWithMediaType(MediaTypes.`application/json`){
          complete {
            (Logger.logging ? "stats").mapTo[AllTimeStats]
          }
        }
      }
    }

  def benchmark(logger: (Long) => Unit): Directive0 = mapRequestContext { context =>
    val begin = System.currentTimeMillis
    context.withHttpResponseMapped { response =>
      Future { logger(System.currentTimeMillis - begin) }
      response
    }
  }
}