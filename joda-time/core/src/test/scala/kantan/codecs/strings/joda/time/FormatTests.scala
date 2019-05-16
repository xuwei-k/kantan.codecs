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

package kantan
package codecs
package strings
package joda
package time

import kantan.codecs.laws.SerializableLaws
import laws.discipline._, arbitrary._
import org.joda.time._
import org.joda.time.format.DateTimeFormat
import org.scalatest.EitherValues._

class FormatTests extends DisciplineSuite {

  checkAll("Format (from literal)", SerializableTests(SerializableLaws(fmt"MM, yyyy")).serializable)

  checkAll(
    "Format (from formatter)",
    SerializableTests(SerializableLaws(Format(DateTimeFormat.forPattern("MM, yyyy")))).serializable
  )

  test("Format should yield the same results as the corresponding DateTimeFormatter for LocalDateTime") {

    val formatStr = "yyyy-MM-dd'T'HH:mm:ss"

    val format    = Format.from(formatStr).right.value
    val formatter = DateTimeFormat.forPattern(formatStr)

    forAll { d: LocalDateTime =>
      val str: String = formatter.print(d)

      format.parseLocalDateTime(str).right.value should be(formatter.parseLocalDateTime(str))

    }
  }

  test("Format should yield the same results as the corresponding DateTimeFormatter for DateTime") {

    val formatStr = "yyyy-MM-dd'T'HH:mm:ss"

    val format    = Format.from(formatStr).right.value
    val formatter = DateTimeFormat.forPattern(formatStr)

    forAll { d: DateTime =>
      val str: String = formatter.print(d)

      format.parseDateTime(str).right.value should be(formatter.parseDateTime(str))
    }
  }

  test("Format should yield the same results as the corresponding DateTimeFormatter for LocalTime") {

    val formatStr = "HH:mm:ss"

    val format    = Format.from(formatStr).right.value
    val formatter = DateTimeFormat.forPattern(formatStr)

    forAll { d: LocalTime =>
      val str: String = formatter.print(d)

      format.parseLocalTime(str).right.value should be(formatter.parseLocalTime(str))

    }
  }

  test("Format should yield the same results as the corresponding DateTimeFormatter for LocalDate") {

    val formatStr = "yyyy-MM-dd"

    val format    = Format.from(formatStr).right.value
    val formatter = DateTimeFormat.forPattern(formatStr)

    forAll { d: LocalDate =>
      val str: String = formatter.print(d)

      format.parseLocalDate(str).right.value should be(formatter.parseLocalDate(str))
    }
  }

}
