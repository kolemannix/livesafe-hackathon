package lshack

import scala.concurrent.{ ExecutionContext, Future }


/**
 * @author <a href="mailto:koleman@livesafemobile.com">Koleman Nix</a>
 */
object Main extends App {
  def doors(): Unit = {
    import lshack.Doors._

    val closedMahogany = MahoganyDoor[Closed](4)

    val openedMahogany = Door.open(closedMahogany)
    val closed = Door.close(openedMahogany)

    println("Equality works? " + (closed == closedMahogany))

    closed.companion.ctor[Closed]

    // Can't turn an Iron door to/from Mahogany door by opening/closing
    // val tryOpenToIron: IronDoor[Open] = Doors.Door.open(closed)

    // Can't close a closed door
     // val closedAgain = Doors.Door.close(closed)
     // val openedAgain = Doors.Door.open(openedMahogany)
  }

//  def values() {
//    import Values._
//    val x = IntValue[View](4)
//    val enc = x.encrypt.a.decrypt.a
//
//
//    implicit val dep: String = "foo"
//    val s = StringValue[View]("asdf")
//    s.encrypt.get.decrypt.get.encrypt.get.decrypt
//
//    implicit val kms: KmsService = new KmsService {
//      type Key = String
//      override def getKey(key: String): Key = key + "123"
//    }
//    implicit val asyncKms: AsyncKmsService = new AsyncKmsService {
//      type Key = String
//      val executor: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
//      def getKey(key: String): Future[Key] = Future successful { key + "asdf" }
//    }
//
//    val kmsString: KmsEncryptedString[View] = KmsEncryptedString[View]("asdf", "global-key")
//    println(kind[KmsEncryptedString.type])
//    kmsString.encrypt.map(x => println(x))(scala.concurrent.ExecutionContext.Implicits.global)
//    val kmsString2: KmsEncryptedString[Store] = KmsEncryptedString[Store]("asdf", "global-key")
//    kmsString2.decrypt
//
//  }
//
//  values()
}
