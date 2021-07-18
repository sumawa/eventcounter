//package com.sa.events.db
//
//import java.sql.SQLException
//
//import cats.data.{EitherT, NonEmptyList}
//import cats.effect.Bracket
//import cats.syntax.all._
//import com.sa.events.domain.titles.{Title, TitleRepositoryAlgebra}
//import com.sa.events.domain.titles.Title
//import doobie._
//import doobie.implicits._
//import doobie.syntax._
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
//class DoobieTitleRepositoryInterpreter[F[_]: Bracket[*[_], Throwable]](val xa: Transactor[F])
//  extends TitleRepositoryAlgebra[F] {
//  // for actual SQL see TitleSQL
//  import TitleSQL._
//  import SQLPagination._
//
//  override def search(pattern: String, pageSize: Int = 50, offset: Int = 0): F[List[Title]] =
//    paginate(pageSize,offset)(TitleSQL.search(pattern))
//      .to[List]
//      .transact(xa)
//
//  override def searchExact(pattern: String, pageSize: Int = 50, offset: Int = 0): F[List[Title]] =
//    paginate(pageSize,offset)(TitleSQL.searchExact(pattern))
//      .to[List]
//      .transact(xa)
//
//  override def topRatedByGenre(genre: Option[String], pageSize: Int = 50, offset: Int = 0): F[List[Title]] =
//    paginate(pageSize,offset)(TitleSQL.topRatedByGenre(genre))
//      .to[List]
//      .transact(xa)
//
//  import cats.MonadError
//  import cats.ApplicativeError
//
//  override def searchExactWError(pattern: String, pageSize: Int, offset: Int): EitherT[F,SQLException, List[Title]] = {
//    val res = paginate(pageSize, offset)(TitleSQL.searchExact(pattern))
//      .to[List]
//      .transact(xa)
//      .attemptSql
//    EitherT(res)
//  }
//
//  override def searchWError(pattern: String, pageSize: Int, offset: Int): EitherT[F,SQLException, List[Title]] =
//  {
//      val res = paginate(pageSize, offset)(TitleSQL.search(pattern))
//        .to[List]
//        .transact(xa)
//        .attemptSql
//      EitherT(res)
//  }
//}
//
//object DoobieTitleRepositoryInterpreter {
//  def apply[F[_]: Bracket[*[_], Throwable]](
//                                             xa: Transactor[F],
//                                           ): DoobieTitleRepositoryInterpreter[F] =
//    new DoobieTitleRepositoryInterpreter(xa)
//
//}
//
//private object TitleSQL {
//
//  def searchExact(pattern: String): Query0[Title] = {
//    val searchTerm = s"%$pattern%"
//    println(s"EXACT SEARCH TERM : $searchTerm")
//    sql"""
//    SELECT tb.tconst, tb.primarytitle, tb.originaltitle, tb.titletype, tb.genres, tr.averagerating, tb.startyear, tr.numvotes, array_agg(concat(nb.primaryname,'-',tp.category)) as castandcrew
//    FROM title_basics tb
//    LEFT JOIN title_principals tp ON tb.tconst = tp.tconst
//    LEFT JOIN name_basics nb ON tp.nconst = nb.nconst
//    LEFT JOIN title_ratings tr ON tb.tconst = tr.tconst
//    WHERE ((tb.primarytitle LIKE $searchTerm) or (tb.originaltitle LIKE $searchTerm)) AND (tr.averagerating IS NOT NULL AND tr.numvotes IS NOT NULL)
//    GROUP BY tb.tconst, tr.averagerating, tr.numvotes
//    ORDER BY (tr.numvotes,tr.averagerating) desc
//    """.queryWithLogHandler[Title](LogHandler.jdkLogHandler)
//  }
//
//  def search(pattern: String): Query0[Title] = {
//    val searchTerm = s"%$pattern%".replaceAll("\\s+","%")
//    println(s"GENERAL SEARCH TERM : $searchTerm")
//    sql"""
//    SELECT tb.tconst, tb.primarytitle, tb.originaltitle, tb.titletype, tb.genres, tb.startyear, tr.averagerating, tr.numvotes, array_agg(concat(nb.primaryname,'-',tp.category)) as castandcrew
//    FROM title_basics tb
//    LEFT JOIN title_principals tp ON tb.tconst = tp.tconst
//    LEFT JOIN name_basics nb ON tp.nconst = nb.nconst
//    LEFT JOIN title_ratings tr ON tb.tconst = tr.tconst
//    WHERE ((tb.primarytitle ILIKE $searchTerm) or (tb.originaltitle ILIKE $searchTerm)) AND (tr.averagerating IS NOT NULL AND tr.numvotes IS NOT NULL)
//    GROUP BY tb.tconst, tr.averagerating, tr.numvotes
//    ORDER BY (tr.numvotes,tr.averagerating) desc
//    """.queryWithLogHandler[Title](LogHandler.jdkLogHandler)
//  }
//
//  def topRatedByGenre(genre: Option[String]): Query0[Title] = {
//    val searchTerm = s"%$genre%"
//    println(s"SEARCH TERM : $searchTerm")
//
//    def qWithGenre(g: String) = sql"""
//    SELECT tb.tconst, tb.primarytitle, tb.originaltitle, tb.titletype, tb.genres, tb.startyear, tr.averagerating, tr.numvotes, '' as castandcrew
//    FROM title_basics tb
//    LEFT JOIN title_ratings tr ON tb.tconst = tr.tconst
//    WHERE tb.genres ILIKE $g AND tb.titletype = 'movie' AND (tr.averagerating IS NOT NULL AND tr.numvotes IS NOT NULL)
//    ORDER BY (tr.numvotes,tr.averagerating) desc
//  """.queryWithLogHandler[Title](LogHandler.jdkLogHandler)
//
//    def qWithoutGenre() = sql"""
//    SELECT tb.tconst, tb.primarytitle, tb.originaltitle, tb.titletype, tb.genres, tb.startyear, tr.averagerating, tr.numvotes, '' as castandcrew
//    FROM title_basics tb
//    LEFT JOIN title_ratings tr ON tb.tconst = tr.tconst
//    WHERE tb.titletype = 'movie' AND (tr.averagerating IS NOT NULL AND tr.numvotes IS NOT NULL)
//    ORDER BY (tr.numvotes,tr.averagerating) desc
//  """.queryWithLogHandler[Title](LogHandler.jdkLogHandler)
//
//    genre.fold(qWithoutGenre())(g => qWithGenre(s"%$g%"))
//  }
//}
