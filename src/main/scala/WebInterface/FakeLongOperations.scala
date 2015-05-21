package WebInterface

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

object FakeLongOperations {

  def inTheFuture(seconds: Int) = Future {
    blocking(Thread.sleep(seconds * 1000))
    seconds
  }
}
