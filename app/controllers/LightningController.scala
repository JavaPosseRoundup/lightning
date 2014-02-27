package controllers

import akka.util.Timeout
import scala.concurrent.duration._
import actors.LightningCounter
import actors.LightningCounter._
import actors.Notifier
import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Akka
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.ActorRef
import akka.actor.Props
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import akka.pattern.ask
import play.api.libs.json.Json
import java.util.Date

/**
 * Lightning controller.
 */
object LightningController extends Controller {
  implicit val askTimeout = Timeout(15.seconds)
  val notifierActorRef: ActorRef =
    Akka.system.actorOf(Notifier.props("jackgene@gmail.com", "hqintmail.shopzilla.com"))
  val lightningCounterActorRef: ActorRef =
    Akka.system.actorOf(LightningCounter.props(3, 15.seconds, notifierActorRef))
  
  def registerStrike = Action.async {
    val currentStrikesFuture: Future[Any] =
      lightningCounterActorRef ? RegisterStrike(System.currentTimeMillis)
    
    currentStrikesFuture.map {
      case CurrentStrikes(strikeTimesMillis: Seq[Long]) =>
        val strikeTimes: Seq[String] = strikeTimesMillis.map {new Date(_).toString}
        Ok(Json.toJson(strikeTimes))
      case _ =>
        InternalServerError("Unknown Error")
    } recover {
      case e: Exception =>
        InternalServerError(e.toString)
    }
  }
}