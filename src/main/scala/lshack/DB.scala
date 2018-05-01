package lshack

import slick.driver.H2Driver.api._

object DB {
  lazy val db = Database.forConfig("h2mem1")
}
