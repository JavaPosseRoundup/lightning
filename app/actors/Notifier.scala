/**
 *
 */
package actors

import akka.actor._

/**
 * Sends notifications
 */
class Notifier extends Actor {
  import Notifier._
  
  def receive: Receive = {
    case Notify => println("Get back to work!")
    case _ => ???
  }
}
object Notifier {
  case object Notify
}