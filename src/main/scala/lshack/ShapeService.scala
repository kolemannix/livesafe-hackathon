package lshack

import scala.concurrent.{ ExecutionContext, Future }

import lshack.ShapeRepository.shapes

/**
 * @author <a href="mailto:koleman@livesafemobile.com">Koleman Nix</a>
 */
trait ShapeService {

  implicit val ec: ExecutionContext

  import Values._

  def bySizeEncrypted(sizes: Set[String]): Future[Seq[ShapeEntity[Store]]] = {
    DB.db.run(shapes.getShapesWithSizes(sizes))
  }
  def bySizeDecrypted(sizes: Set[String])(implicit kms: AsyncKmsService): Future[Seq[ShapeEntity[View]]] = {
    bySizeEncrypted(sizes).flatMap { shapes =>
      val x: Seq[ShapeEntity[Store]] = shapes
      val y: Seq[Future[ShapeEntity[View]]] = shapes.map(_.decrypt)
      val z: Future[Seq[ShapeEntity[View]]] = Future.sequence(shapes.map(_.decrypt))
      z
    }
  }
}
