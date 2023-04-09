package com.twitter.algebird
import com.twitter.algebird.macros.*
import java.nio.file.Files
import java.nio.file.Path
import org.scalacheck.Gen
import org.scalacheck.*
object Debug:
  case class It(i: Int, j: Int, that: That) derives Roller, Cuber
  case class That(k: String, l: Int, c: Double)
  case class Foo(i: Int, d: Double, f: Float)
  inline given Semigroup[That] = caseclass.semigroup[That]

  def it() = {
    val it = It(1, 2, That("3", 4, 2d))
    val that = It(5, 6, That("7", 8, 2d))
    given inst: Semigroup[It] = caseclass.semigroup[It]
    given ev: Monoid[It] = caseclass.monoid[It]
    given evg: Group[Foo] = caseclass.group[Foo]
    given evr: Ring[Foo] = caseclass.ring[Foo]
    given Gen[String] = Gen.const("String")
    given Gen[Int] = Gen.const(1)
    given Gen[Double] = Gen.const(1d)
    //given a : Gen[That] = ArbitraryCaseClassMacro.gen[That]
    given b : Gen[It] = ArbitraryCaseClassMacro.gen[It]

    val f0 = Foo(1, 2, 3)
    val f1 = Foo(4, 5, 6)
    println(summon[Cuber[It]].apply(it))
    println(summon[Roller[It]].apply(it))
    println(inst.plus(it, that))
    println(ev.zero)
    println(evg.negate(f0))
    println(evg.minus(f0, f1))
    println(evr.one)
    println(evr.times(f0, f1))
    //println(a.suchThat(_ => true).sample)
    println(b.suchThat(_ => true).sample)

  }
  @main
  def run =
    it()
