package com.twitter.algebird.macros
import scala.compiletime.summonInline
import scala.deriving.Mirror
import scala.quoted.Expr
import scala.quoted.Quotes
import scala.quoted.Type
import scala.runtime.Tuples

trait Roller[I]:
  type K
  def apply(in: I): TraversableOnce[K]

object Roller extends MacroHelper:
  implicit inline def roller[T]: Roller[T] = ${ deriveRollerImpl[T] }
  inline def derived[T]: Roller[T] = ${ deriveRollerImpl[T] }
  def deriveRollerImpl[T: Type](using q: Quotes): Expr[Roller[T]] =
    import q.reflect.*
    val tname = TypeRepr.of[T].typeSymbol.name
    val ev: Expr[Mirror.Of[T]] = Expr.summon[Mirror.Of[T]] match
      case None =>
        report.errorAndAbort(s"unable to derive Roller instance for ${tname}")
      case Some(expr) => expr
    ev match
      case '{
            $m: Mirror.ProductOf[T] { type MirroredElemTypes = elementTypes }
          } =>
        val caseclassFields = TypeRepr.of[T].typeSymbol.caseFields
        if caseclassFields.length > 22 then
          report.errorAndAbort(
            s"Cannot create Roller for $tname because it has more than 22 parameters."
          )

        val tupleNTyped = genTupleNTyped(caseclassFields)
        val somes: List[Symbol] = genSomes(caseclassFields)
        val idents = Expr.ofList(somes.map(sym => Ident(sym.termRef).asExpr))

        val arity = Expr(caseclassFields.length)

        def options(i: Expr[Int], index: Expr[Int]): Expr[Seq[Option[Any]]] =
          '{
            (1 to ${ arity }).map(index =>
              if (((1 << { index - 1 }) & ${ i }) == 0) then None
              else ${ idents }(index - 1).asInstanceOf[Option[Any]]
            )
          }
        def blc(e: Expr[T]): Expr[Any] =
          Block(
            somesStmt[T](somes, caseclassFields, e),
            '{
              (0 to ${ arity }).map { i =>
                val args = (1 to ${ arity }).map { index =>
                  if (index <= i) ${ idents }(index - 1) else None
                }
                Tuple.fromArray(args.toArray)
              }

            }.asTerm
          ).asExpr
        tupleNTyped.tpe.widen.asType match
          case '[t] =>
            '{

              new Roller[T]:
                type K = t
                override def apply(in: T): TraversableOnce[K] =
                  ${ blc('{ in }) }
                    .asInstanceOf[TraversableOnce[K]]
            }

      case '{ $m: Mirror.SumOf[T] { type MirroredElemTypes = elementTypes } } =>
        report.errorAndAbort(s"unable to derive Roller for ${tname}")
