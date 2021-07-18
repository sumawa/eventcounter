//package com.sa.events.db
//
//import cats.data.OptionT
//import cats.effect.Bracket
//import cats.syntax.all._
//import com.sa.events.domain.meta.NameRepositoryAlgebra
//import doobie._
//import doobie.implicits._
//
///**
// * - With Doobie we write plain SQL queries.
// * - We can use something like Quill for Object DB mapping
// * - For ORM or other forms of query compilers then this may seem strange at first.
// * - But: “In data processing it seems, all roads eventually lead back to SQL!”
// * - Using the approach of using the de facto lingua franca for database access because it was made for this
// * - and so far no query compiler was able to beat hand crafted SQL in terms of performance.
// *
// * Keeping higher kinded type abstract F
// * To be able to suspend side effects, we are using implicit Sync
// *
// * @param xa A transactor for actually executing our queries.
// * @tparam F A higher kinded type which wraps the actual return value
// *           (effects like IO, Sync, Async, Bracket etc)
// */
//
//class DoobieNameRepositoryInterpreter[F[_]: Bracket[*[_], Throwable]](val xa: Transactor[F])
//  extends NameRepositoryAlgebra[F] {
//  // for actual SQL see TitleSQL
//  import TitleSQL._
//  import SQLPagination._
//
//  override def selectImmediateCoActors(nconst: String): F[List[(String, String)]] = {
//    for {
//      allT <- allTitles(nconst)
//      res <-  NameSQL.immCoActors(allT)
//        .query[(String,String)]
//        .to[List]
//        .transact(xa)
//
//    } yield res
//  }
//
//  private def allTitles(nconst: String) =
//    NameSQL.allTitles(nconst)
//      .to[List]
//      .transact(xa)
//}
//
//object DoobieNameRepositoryInterpreter {
//  def apply[F[_]: Bracket[*[_], Throwable]](
//                                             xa: Transactor[F],
//                                           ): DoobieNameRepositoryInterpreter[F] =
//    new DoobieNameRepositoryInterpreter(xa)
//
//}
//
//private object NameSQL {
//
//  def allTitles(nconst: String) = {
//        sql"""
//          SELECT tp.tconst
//          FROM title_basics tb
//          LEFT JOIN title_principals tp ON tb.tconst = tp.tconst
//          LEFT JOIN name_basics nb ON tp.nconst = nb.nconst
//          WHERE nb.nconst = $nconst AND titletype IN ('tvSeries','episode','movie')
//          GROUP BY tp.tconst"""
//          .query[String]
//  }
//
//  def immCoActors(list: List[String]) = {
//    val n = list.toNel.get
//
//    fr"""
//    SELECT DISTINCT tp.nconst, nb.primaryname
//    FROM title_principals tp
//    LEFT JOIN name_basics nb ON tp.nconst = nb.nconst
//    WHERE (tp.category = 'actor' or tp.category = 'actress')
//    AND   """ ++ Fragments.in(fr"tconst", list.toNel.get) // code IN (...)
//  }
//
//}
