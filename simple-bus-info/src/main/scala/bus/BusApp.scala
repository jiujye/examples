package bus

import akka.actor.{ActorSystem, Props}

/**
  * Created by arthur on 2016/5/20.
  */
object BusApp extends App {

  val system = ActorSystem("bus-info")
  val b = system.actorOf(Props[Bot], "bot")
  b! BotStart

}
