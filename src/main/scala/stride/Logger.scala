package stride

object Logger {

  //this is not cluster safe
  def inMemory(latency: Long): Unit = { /* think about it later */ println("waking from: " + latency / 1000) }

}