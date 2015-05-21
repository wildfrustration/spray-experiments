package WebInterface

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

object FakeLongOperations {

  def inTheFuture(ms: Long) = Future {
    blocking(Thread.sleep(ms))
    ms
  }
}
