package test

import java.time.LocalDateTime

import model.db.table.{MutableLevel, MutableUser}
import org.json4s.JsonAST.{JNothing, JObject}
object DBUtil {

  def dummyUser(): MutableUser = {
    val user = new MutableUser
    user.email = ""
    user.passwordHash = ""
    user.active = true
    user.rating = None
    user.verbose = None
    user.jsB = JObject(List.empty)
    user.js = JObject(List.empty)
    user.lastlogin = LocalDateTime.now()
    user
  }

  def dummyLevel(): MutableLevel = {
    val level = new MutableLevel
    level.jsB = "{}"
    level.js = "{}"
    level.userid = 0
    level.level = 0
    level.score = 0
    level.complete = false
    level.createdat = LocalDateTime.now()
    level
  }

}
