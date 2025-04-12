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

package kantan.codecs.shapeless.laws

import org.scalacheck.{Arbitrary, Cogen}

sealed trait Or[+A, +B] extends Product with Serializable
final case class Left[A](a: A) extends Or[A, Nothing]
final case class Right[B](b: B) extends Or[Nothing, B]

object Or {
  implicit def arbitrary[A: Arbitrary, B: Arbitrary]: Arbitrary[Or[A, B]] =
    Arbitrary(
      Arbitrary
        .arbitrary[Boolean]
        .flatMap(x =>
          if(x) {
            Arbitrary.arbitrary[A].map(Left.apply)
          } else {
            Arbitrary.arbitrary[B].map(Right.apply)
          }
        )
    )

  implicit def cogen[A: Cogen, B: Cogen]: Cogen[Or[A, B]] =
    implicitly[Cogen[Either[A, B]]].contramap {
      case Left(a) =>
        scala.Left(a)
      case Right(a) =>
        scala.Right(a)
    }
}
