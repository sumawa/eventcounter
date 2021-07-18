package scratch

import scala.annotation.tailrec

object Vertisant extends App{


//  val input  =  [1,[2,[0.4]]]
  //  val input  =  [1,3,[2,[0.4]]]

//  sealed trait List[+T]
//
//  case object  EmptyList extends List[Nothing]
//  case class Cons[T](h: T, tail: Cons[T]) extends List[T]
//
//  sealed trait Tup[A,B]
//  case class ComplexTup[A,B](tup: (A,(A,(B)))) extends Tup[A,B]
//
//  val inputList = List(1, List(2, List(0.3)))

//  def funcAdd(list: List[Any])= list match {
//    case ((x: Int) :: tail) => x + funcAdd(tail)
//    case ()
//  }

//  sealed trait TP[A]
//  case class ComplexTp[A: Any](comp: ComplexTp[Seq[A]])
//
//  val testInput: ComplexTup[Int,Double] = ComplexTup(1,(2,(0.4)))

//  val input = Array(Array)
//  val Output =  3.4

  val inputList = List(1, List(2, List(0.3)))

  sealed trait ComplexColl[T]

//  case object EmptyColl extends ComplexColl[Nothing]
  case class Simple[T](v: T) extends ComplexColl[T]
  case class Coll[T](vlist: List[ComplexColl[T]]) extends ComplexColl[T]

  //   val inputList = List(1, List(2, List(0.4)))

  val simp1  = Simple[Double](0.4)
  val simp2 = Simple[Double](2)
  val simp3 = Simple[Double](1)

  val collex = Coll[Double](List(simp3,Coll(List(simp2,Coll(List(simp1))))))

  import cats.Monoid

//  sealed  trait Trampoline[T]
//  case class Req

  import cats.Eval

  def sumComplex[A: Monoid](complexColl: ComplexColl[A]) = {
    val m = implicitly[Monoid[A]]

//    @tailrec
    def go(comp: ComplexColl[A], acc: A): A = comp match {
      case Simple(v) => Monoid[A].combine(v,acc)
      case Coll(xs) =>
        xs match {
          case (h :: tail) =>
            Monoid[A].combine(go(h,acc),go(Coll(tail),acc))
          case Nil => acc
        }
    }
    go(complexColl,Monoid[A].empty)
  }
  println(s"sumComplex($collex) : ${sumComplex(collex)}")

  //  def sumMon[A: Monoid](xs: List[A]) = {
  //    val m = implicitly[Monoid[A]]
  //    xs.foldLeft(m.empty){ (acc,x) =>
  //      Monoid[A].combine(acc,x)
  //    }
  //  }

  //  println(s"sumMon(List(1,2,3)): ${sumMon(List(1,2,3))}")

//  object ComplexColl{
//    def addFunc[T: Monoid](complexColl: ComplexColl[T]): T = complexColl match {
//      case (Coll(h :: tail)) =>
//        addFunc(h) addFunc(Coll(tail))
//      case Simple(v) => v
//    }
//  }

//  val coll1 = Coll(simp2)
//  val compList = List(simp1)
//  val compList2 = List(simp2, )
}
