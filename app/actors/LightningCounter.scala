package actors

import akka.actor.Actor

/**
 * Counts and tracks lightning strikes.
 */
class LightningCounter extends Actor {
  def safeState(strikes: Seq[Long]): Receive = {
    case _ => ???
  }
  def hazardState(strikes: Seq[Long]): Receive = {
    case _ => ???
  }
  
  def receive = safeState(Nil)
}
object LightningCounter {
  case object RegisterStrike
  case object CheckState
  case class CurrentStrikes(strikes: Seq[Long])
}