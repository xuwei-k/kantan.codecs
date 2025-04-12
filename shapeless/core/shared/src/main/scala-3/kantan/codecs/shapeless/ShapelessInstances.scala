/*
 * Copyright 2016 Nicolas Rinaudo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kantan.codecs.shapeless

import kantan.codecs.Decoder
import kantan.codecs.Encoder
import kantan.codecs.error.IsError
import kantan.codecs.`export`.DerivedDecoder
import kantan.codecs.`export`.DerivedEncoder
import scala.deriving.Mirror
import scala.compiletime.erasedValue
import scala.compiletime.summonInline

/** Provides `Codec` instances for case classes and sum types.
 *
 * The purpose of this package is to let concrete `Codec` implementations (such as kantan.csv or kantan.regex) focus on
 * providing instances for `HList` and `Coproduct`. Once such instances exist, this package will take care of the
 * transformation from and to case classes and sum types.
 *
 * Additionally, instances derived that way will be inserted with a sane precedence in the implicit resolution
 * mechanism. This means, for example, that they will not override bespoke `Option` or `Either` instances.
 */
trait ShapelessInstances {
  inline def deriveEncoderLoop[E, D <: Tuple, T]: List[Encoder[E, ?, T]] =
    inline erasedValue[D] match {
      case _: EmptyTuple =>
        Nil
      case _: (t *: ts) =>
        summonInline[Encoder[E, t, T]] :: deriveEncoderLoop[E, ts, T]
    }

  inline given caseClassEncoder[E, D <: Product, T](using a: Mirror.ProductOf[D]): DerivedEncoder[E, D, T] =
    productEncoder[E, D, T](
      deriveEncoderLoop[E, a.MirroredElemTypes, T],
      a
    )

  final def productEncoder[E, D <: Product, T](values: List[Encoder[E, ?, T]], a: Mirror.ProductOf[D]): DerivedEncoder[E, D, T] = {
    DerivedEncoder.from[E, D, T]{ (s: D) =>
      s.productIterator.zipWithIndex.map(
        (x, i) =>
          values(i).asInstanceOf[Encoder[E, Any, T]].encode(x)
      ).toSeq
    }
  }


  given caseClassDecoder[E, D, F, T](using a: Mirror.ProductOf[D]): DerivedDecoder[E, D, F, T] =
    sys.error("")

  given sumTypeEncoder[E, D, T](using a: Mirror.SumOf[D]): DerivedEncoder[E, D, T] =
    sys.error("")

  given sumTypeDecoder[E, D, F, T](using a: Mirror.SumOf[D]): DerivedDecoder[E, D, F, T] =
    sys.error("")

}