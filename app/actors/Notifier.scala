/**
 *
 */
package actors

import akka.actor._
import org.apache.commons.mail.HtmlEmail

/**
 * Sends notifications
 */
class Notifier(emailAddress: String, smtpHost: String)
    extends Actor with ActorLogging{
  import Notifier._
  
  def receive: Receive = {
    case Notify =>
      log.info("Sending notification")
      val email = new HtmlEmail
      email.setHostName(smtpHost)
      email.setFrom("lightning@javaposseroundup.com")
      email.addTo(emailAddress)
      email.setSubject("Get back to work!")
      email.setHtmlMsg("<ht>Get back to work!</h1>")
      email.send()
    case _ => ???
  }
}
object Notifier {
  case object Notify
  
  def props(emailAddress: String, smtpHost: String) =
    Props(classOf[Notifier],  emailAddress, smtpHost)
}