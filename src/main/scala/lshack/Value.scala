package lshack

import scala.concurrent.{ ExecutionContext, Future }
import scala.languageFeature.higherKinds
import scala.util.{ Success, Try }

import lshack.Hm.{ CipherKey, EncryptionKeyId }
import lshack.Values._

object Values {

  sealed trait State
  trait Store extends State
  trait View extends State

  trait Encryptable[V[S <: State], Dep, F[A]] {
    def encrypt[S <: View](d: V[S], dep: Dep): F[V[Store]]
    def decrypt[S <: Store](d: V[S], dep: Dep): F[V[View]]
  }

  implicit class EncryptOp[S <: View, V[S <: State], F[A], Dep](v: V[S])(implicit val ev: Encryptable[V, Dep, F], dep: Dep) {
    def encrypt: F[V[Store]] = ev.encrypt(v, dep)
  }

  implicit class DecryptOp[S <: Store, V[S <: State], F[A], Dep](v: V[S])(implicit val ev: Encryptable[V, Dep, F], dep: Dep) {
    def decrypt: F[V[View]] = ev.decrypt(v, dep)
  }

  type BasicEncryptable[V[S <: State]] = Encryptable[V, Unit, Box]
  implicit class BasicEncryptOp[S <: View, V[S <: State]](v: V[S])(implicit val ev: BasicEncryptable[V]) { def encrypt: Box[V[Store]] = ev.encrypt(v, ()) }
  implicit class BasicDecryptOp[S <: Store, V[S <: State]](v: V[S])(implicit val ev: BasicEncryptable[V]) { def decrypt: Box[V[View]] = ev.decrypt(v, ()) }
}

class Box[A](val a: A)

case class IntValue[S <: State](x: Int)

object IntValue {
  implicit val encryptable: BasicEncryptable[IntValue] = new BasicEncryptable[IntValue] {
    def encrypt[S <: View](i: IntValue[S], dep: Unit): Box[IntValue[Store]] = new Box(i.copy(x = i.x + 100))
    def decrypt[S <: Store](i: IntValue[S], dep: Unit): Box[IntValue[View]] = new Box(i.copy(x = i.x - 100))
  }
}

case class StringValue[S <: State](x: String)

object StringValue {
  implicit val encryptable: Encryptable[StringValue, String, Try] = new Encryptable[StringValue, String, Try] {
    def encrypt[S <: View](i: StringValue[S], dep: String): Try[StringValue[Store]] = {
      Success(i.copy(x = dep + i.x))
    }
    def decrypt[S <: Store](i: StringValue[S], dep: String): Try[StringValue[View]] = {
      Success(i.copy(x = dep + i.x))
    }
  }
}

trait KmsService {
  type Key
  def getKey(key: String): Key
}

trait AsyncKmsService {
  val executor: ExecutionContext
  def getKey(key: EncryptionKeyId): Future[CipherKey]
}

case class KmsEncryptedString[S <: State](value: String, key: EncryptionKeyId)

object KmsEncryptedString {

  implicit val encryptableAsync: Encryptable[KmsEncryptedString, AsyncKmsService, Future] = new Encryptable[KmsEncryptedString, AsyncKmsService, Future] {
    def encrypt[S <: View](i: KmsEncryptedString[S], dep: AsyncKmsService): Future[KmsEncryptedString[Store]] = {
      dep.getKey(i.key).map { key =>
        i.copy[Store](value = i.value + key)
      }(dep.executor)
    }
    def decrypt[S <: Store](i: KmsEncryptedString[S], dep: AsyncKmsService): Future[KmsEncryptedString[View]] = {
      dep.getKey(i.key).map { key =>
        i.copy[View]()
      }(dep.executor)
    }
  }

//  implicit val encryptable: Encryptable[KmsEncryptedString, KmsService, Try] = new Encryptable[KmsEncryptedString, KmsService, Try] {
//    def encrypt[S <: Decrypted](i: KmsEncryptedString[S], dep: AsyncKmsService): Try[KmsEncryptedString[Encrypted]] = {
//      val key = dep.getKey(i.key)
//      println("Yay I have a KmsService")
//      Success(i.copy[Encrypted](value = i.value + key))
//    }
//    def decrypt[S <: Encrypted](i: KmsEncryptedString[S], dep: AsyncKmsService): Try[KmsEncryptedString[Decrypted]] = {
//      Success(i.copy[Decrypted]())
//    }
//  }
}

