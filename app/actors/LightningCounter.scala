package actors

import akka.actor.Actor
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.Props
import akka.actor.ActorRef

/**
 * Counts and tracks lightning strikes.
 */
class LightningCounter(
    val safeThresholdCount: Int = 3,
    val safetyThresholdTimeWindow: FiniteDuration = 15.minutes,
    val notifier: ActorRef)
    extends Actor {
  import LightningCounter._
  import Notifier._
  
  def safeState(strikes: Seq[Long]): Receive = {
    case RegisterStrike(timeMillis) =>
      val unexpiredStrikes = strikes.filter {
        _ > (timeMillis - safetyThresholdTimeWindow.toMillis)
      }
      val updatedStrikes = timeMillis +: unexpiredStrikes
      if (updatedStrikes.size > safeThresholdCount) {
        context.become(hazardState(updatedStrikes))
      } else {
        context.become(safeState(updatedStrikes))
      }
      sender ! CurrentStrikes(updatedStrikes)
      context.system.scheduler.scheduleOnce(
        safetyThresholdTimeWindow, self, CheckState(timeMillis + safetyThresholdTimeWindow.toMillis)
      )
    case CheckState(_) =>
      println("Checking state in safe state. No-op.")
    case _ => ???
  }
  def hazardState(strikes: Seq[Long]): Receive = {
    case RegisterStrike(timeMillis) =>
      val unexpiredStrikes = strikes.filter {
        _ > (timeMillis - safetyThresholdTimeWindow.toMillis)
      }
      val updatedStrikes = timeMillis +: unexpiredStrikes
      context.become(hazardState(updatedStrikes))
      sender ! CurrentStrikes(updatedStrikes)
      context.system.scheduler.scheduleOnce(
        safetyThresholdTimeWindow, self, CheckState(timeMillis + safetyThresholdTimeWindow.toMillis)
      )
    case CheckState(timeMillis) =>
      val unexpiredStrikes = strikes.filter {
        _ > (timeMillis - safetyThresholdTimeWindow.toMillis)
      }
      if (unexpiredStrikes.size <= safeThresholdCount) {
        notifier ! Notify
        context.become(safeState(unexpiredStrikes))
      }
    case _ => ???
  }
  
  def receive = safeState(Nil)
}
object LightningCounter {
  case class RegisterStrike(timeMillis: Long)
  case class CurrentStrikes(strikeTimesMillis: Seq[Long])
  case class CheckState(timeMillis: Long)
  
  def props(safeThresholdCount: Int, safetyThresholdTimeWindow: FiniteDuration, notifier: ActorRef) =
    Props(classOf[LightningCounter],  safeThresholdCount, safetyThresholdTimeWindow, notifier)
}