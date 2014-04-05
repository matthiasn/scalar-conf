package example

import org.scalajs.spickling._

object Implicits {
  implicit def vectorPickler[A](implicit p: Pickler[A]) = new Pickler[Vector[A]] {
    def pickle[P](value: Vector[A])(implicit reg: PicklerRegistry, builder: PBuilder[P]): P =
      builder.makeArray(value.map(el => p.pickle(el)).toSeq: _*)
  }

  implicit def vectorUnpickler[A](implicit up: Unpickler[A]) =  new Unpickler[Vector[A]] {
    def unpickle[P](p: P)(implicit reg: PicklerRegistry, reader: PReader[P]): Vector[A] =
      (0 until reader.readArrayLength(p)).toVector.map { idx => up.unpickle(reader.readArrayElem(p, idx))
      }
  }
}
