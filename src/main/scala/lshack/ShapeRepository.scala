package lshack

import scala.concurrent.Future

import lshack.Hm.EncryptionKeyId
import lshack.Values.{ Encryptable, State, Store, View }
import slick.driver.H2Driver.api._
import slick.lifted.Tag

case class ShapeEntity[S <: State](id: Int, `type`: String, size: String, color: String, encryptionKey: Option[EncryptionKeyId])

object Hm {

  case class EncryptionKeyId(value: Long) extends AnyVal
  case class CipherKey(bytes: Array[Byte])

  trait HasEncryptionKey {
    val encryptionKey: Option[EncryptionKeyId]
  }
  trait AsyncKmsSingleIdEncryptable[V[S <: State] <: HasEncryptionKey] extends Encryptable[V, AsyncKmsService, Future] {
    def encryptWithCipher[S <: View](v: V[S], cipher: CipherKey): V[Store]
    def encrypt[S <: View](v: V[S], service: AsyncKmsService): Future[V[Store]] = {
      v.encryptionKey match {
        case Some(encryptionKeyId) =>
          service.getKey(encryptionKeyId).map { cipher =>
            encryptWithCipher(v, cipher)
          }(service.executor)
        case None =>
          // TODO: Figure out how to properly convert this to a V[Store]
          Future.successful(v.asInstanceOf[V[Store]])
      }

    }
    def decryptWithCipher[S <: Store](v: V[S], cipher: CipherKey): V[View]
    def decrypt[S <: Store](v: V[S], service: AsyncKmsService): Future[V[View]] = {
      v.encryptionKey match {
        case Some(encryptionKeyId) =>
          service.getKey(encryptionKeyId).map { cipher =>
            decryptWithCipher(v, cipher)
          }(service.executor)
        case None =>
          // TODO: Figure out how to properly convert this to a V[View]
          Future.successful(v.asInstanceOf[V[View]])
      }
    }
  }
}

object ShapeEntity {
  type EncryptedShapeEntity = ShapeEntity[Store]
  type DecryptedShapeEntity = ShapeEntity[View]
  implicit val encryptable: Encryptable[ShapeEntity, AsyncKmsService, Future] = new Encryptable[ShapeEntity, AsyncKmsService, Future] {
    def encrypt[S <: View](shape: ShapeEntity[S], service: AsyncKmsService): Future[ShapeEntity[Store]] = {
      shape.encryptionKey match {
        case Some(encryptionKey) =>
          service.getKey(encryptionKey).map { cipher =>
            shape.copy[Store](
              // Encrypt things using 'cipher'
              size = shape.size + cipher,
              color = shape.color + cipher
            )
          }(service.executor)
        case None => Future successful shape.copy[Store]()
      }
    }
    def decrypt[S <: Store](shape: ShapeEntity[S], service: AsyncKmsService): Future[ShapeEntity[View]] = {
      shape.encryptionKey match {
        case Some(encryptionKey) =>
          service.getKey(encryptionKey).map { key =>
            shape.copy[View](
              // Decrypt things using 'cipher'
            )
          }(service.executor)
        case None => Future successful shape.copy[View]()
      }
    }
  }
}

object ShapeRepository {
  class Shapes(tag: Tag) extends Table[ShapeEntity[Store]](tag, "shape") {

    implicit val encryptionKeyTypedType = MappedColumnType.base[EncryptionKeyId, Long](
      eki => eki.value,
      EncryptionKeyId.apply(_)
    )

    def id = column[Int]("shapeid", O.PrimaryKey)
    def `type` = column[String]("type")
    def size = column[String]("size")
    def color = column[String]("color")
    def encryptionKeyId = column[EncryptionKeyId]("keyid")

    val to: ((Int, String, String, String, Option[EncryptionKeyId])) => ShapeEntity[Store] = (ShapeEntity.apply[Store] _).tupled
    val from: (ShapeEntity[Store]) => Option[(Int, String, String, String, Option[EncryptionKeyId])] = ShapeEntity.unapply[Store]

    def * = (id, `type`, size, color, encryptionKeyId.?) <> (to, from)
  }

  object shapes extends TableQuery[Shapes](new Shapes(_)) {

    def getShapesWithSizes(sizes: Set[String]) = {
      shapes.filter(_.size inSet sizes).result
    }

  }

}
