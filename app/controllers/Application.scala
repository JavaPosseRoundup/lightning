package controllers

import play.api._
import play.api.Play.current
import play.api.mvc._

object Application extends Controller {
  def index = Action {
    Ok(views.html.index("The lightning tracker application is ready."))
  }
}