package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import akka.pattern.ask
import akka.util.Timeout
import play.api._
import play.api.Play.current
import play.api.mvc._

object Application extends Controller {

  implicit val askTimeout = Timeout(15.seconds)
  
  def index = Action {
    Ok(views.html.index("The lightning tracker application is ready."))
  }
}