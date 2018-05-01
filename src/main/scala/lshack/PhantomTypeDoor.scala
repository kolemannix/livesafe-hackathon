package lshack

import scala.languageFeature.higherKinds

object Doors {

  sealed trait Status
  trait Open extends Status
  trait Closed extends Status

  trait DoorCompanion[D[S <: Status] <: Door[S, D]] {
    def ctor[S <: Status]: D[S]
  }

  trait Door[S <: Status, DoorMaker[S <: Status] <: Door[S, DoorMaker]] {
    val companion: DoorCompanion[DoorMaker]
  }

  object Door {

    def open[S <: Closed, D[SS <: Status] <: Door[SS, D]](d: D[S]): D[Open] = {
      d.companion.ctor[Open]
    }
    def close[S <: Open, D[SS <: Status] <: Door[SS, D]](d: D[S]): D[Closed] = {
      d.companion.ctor[Closed]
    }
  }

  case class MahoganyDoor[S <: Status](size: Int) extends Door[S, MahoganyDoor] {
    override val companion = new DoorCompanion[MahoganyDoor] {
      override def ctor[SN <: Status]: MahoganyDoor[SN] = MahoganyDoor[SN](size)
    }
  }
  case class IronDoor[S <: Status]() extends Door[S, IronDoor] {
    override val companion = new DoorCompanion[IronDoor] {
      override def ctor[SN <: Status]: IronDoor[SN] = IronDoor[SN]()
    }
  }

}
