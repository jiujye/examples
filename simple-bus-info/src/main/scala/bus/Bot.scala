package bus


import akka.actor.Actor

import scala.concurrent.duration._
import akka.util.Timeout
import bus.BusInfoWorker.{BusInfo, Start}

/**
  * Created by arthur on 2016/5/20.
  */
case class BotStart()

class Bot extends Actor{

  implicit val ec = scala.concurrent.ExecutionContext.global
  implicit val timeout = Timeout(15.seconds)
  val buses =  Map("236" -> "10711", "262" -> "10961" , "208" -> "15112" , "251" -> "10712" , "226" -> "11245")

  def receive = initial

  val initial: Receive = {
    case BotStart =>

      buses.keys.foreach(
        x => {
          val a = context.system.actorOf(BusInfoWorker.props(x , buses(x)),"bi_"+ x)
          a ! Start
        }
      )

      context.become(handleBusInfo)
  }

  def handleBusInfo: Receive = {
    case result : BusInfo =>
      result.info.onComplete( x => x match {
        case scala.util.Success(busInfo) =>
          println("Bus Number : " + result.num)
          println("======Bus Info=======")
          println(busInfo)
          println("=======End======")

        case _ => println("error")
      })

  }


}
