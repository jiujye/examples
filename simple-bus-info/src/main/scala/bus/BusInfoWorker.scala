package bus

/**
  * Created by arthur on 2016/5/20.
  */

import java.io._
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

import akka.actor.Actor.Receive
import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.persistence.PersistentActor
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import bus.BusInfoWorker._
import scala.concurrent.duration._
import scala.concurrent.Future


object BusInfoWorker {

  sealed trait Cmd
  case object Start extends Cmd
  case object Fetch extends Cmd
  case class Parse(rawInfo: Future[String]) extends Cmd

  sealed trait BusInfoEvt
  case class InfoFeteched(rawInfo: Future[String]) extends BusInfoEvt
  case class InfoParsed(info: Future[String]) extends BusInfoEvt

  case class BusInfo(num:String , info:Future[String])


  private case class BusFetchState(rawInfo : Future[String] , parseInfo : Future[String]) {

    def updated(evt: BusInfoEvt): BusFetchState = evt match {
      case InfoFeteched(r)   => copy(rawInfo = r)
      case InfoParsed(i) => copy(parseInfo = i)
    }

  }

  def props(num:String , rid: String): Props = Props(new BusInfoWorker(num , rid))

}

class BusInfoWorker(num:String , rid:String)
  extends PersistentActor with ActorLogging {

  implicit val ec = scala.concurrent.ExecutionContext.global

  override def persistenceId: String = rid
  private var state = BusFetchState(Future{""} , Future{""})

  override def receiveRecover: Receive = {
    case evt: InfoFeteched =>
      state = state.updated(evt)
      log.info("Replayed {}", evt.getClass.getSimpleName)
    case evt: InfoParsed =>
      state = state.updated(evt)
      log.info("Replayed {}", evt.getClass.getSimpleName)
    case e @ _ =>
      log.info(s"Unknown message $e received  from $sender")
  }

  override def receiveCommand: Receive = {

    case Start =>
      self ! Fetch
      context.system.scheduler.schedule(15.seconds, 15.seconds, self, Fetch)
    case Fetch =>
      val rawInfo = fetchBus(rid)
      persist( InfoFeteched(rawInfo)){
        event =>
          state.updated(event)
          self ! Parse(rawInfo)
      }
    case p : Parse =>
      val info = parseInfo(p.rawInfo)
      persist( InfoParsed(info)){
        event =>
          state.updated(event)
          context.actorSelection("/user/bot") ! BusInfo(num,info)
      }

  }

  def fetchBus(rid:String): Future[String] ={

    implicit val materializer: ActorMaterializer = ActorMaterializer()

    val connectionFlow: Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] =
      Http(context.system).outgoingConnection("e-bus.taipei.gov.tw")

    val unixTime = System.currentTimeMillis() / 1000L
    val uri2 = "/newmap/Js/RouteInfo?rid="+ rid+"&sec=0&_="+unixTime

    val responseFuture: Future[HttpResponse] =
      Source.single(HttpRequest(uri = uri2))
        .via(connectionFlow)
        .runWith(Sink.head)


    responseFuture.filter(response =>
      response.status == akka.http.scaladsl.model.StatusCodes.OK ).map(
      response  => {
        val g =  Gzip.decode(response)
        Unmarshal(g.entity).to[String]

      }
    ).flatMap(x=>x)


  }

  def parseInfo(info:Future[String]): Future[String] ={

    import net.liftweb.json._
    implicit val formats = net.liftweb.json.DefaultFormats

    info.map(busInfo => {
      pretty(render(parse(busInfo) \ "Buses"))
    })


  }

}





