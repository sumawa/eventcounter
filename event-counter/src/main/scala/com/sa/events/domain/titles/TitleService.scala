//package com.sa.events.domain.titles
//
//import cats.Functor
//import cats.data.{EitherT, Kleisli, ReaderT}
//import cats.effect.{ConcurrentEffect, ContextShift, IO, Timer}
//import cats.syntax.all._
//import com.sa.events.domain.meta.SearchParams
//import Title.{AppE, AppEx}
//import com.sa.events.db.DoobieTitleRepositoryInterpreter
//import fs2.{Pull, Stream}
///**
// * TitleService: Service layer, business logic for search, searchExact, topRatedGenre
// * binds with Doobie Repository for making DB calls.
// */
//class TitleService[F[_]](titleRepo: TitleRepositoryAlgebra[F])
//                        (implicit F: ConcurrentEffect[F]
//                         , timer: Timer[F]
//                         , contextShift: ContextShift[F]){
//  /**
//   * TODO: documentation
//   * Search by movie’s primary title or original title.
//   * The outcome being related information to that title, including cast and crew.
//   *
//   * @return
//   */
//  def searchAlternative: ReaderT[F,SearchParams,AppE[List[Title]]] =
//    for {
//      _ <- ReaderT.liftF(F.delay(println(s"search alternative")))
//      params <- ReaderT.ask[F,SearchParams]
//      titles <- if (params.exact) searchTitlesExact
//                else searchTitles
//    } yield (titles)
//
//  // TODO: alternate methods for better error handling and testability
//  // TODO: This (conversion from Throwable to String message) should be handled at the repository layer
//  def searchTitlesExact: ReaderT[F,SearchParams,AppE[List[Title]]] =
//    Kleisli {
//      case sp =>
//        titleRepo.searchExactWError(sp.term, sp.pageSize, sp.offset).value.map{
//            case Right(value) => Either.right(value)
//            case Left(ex) =>
//              // log here
//              Either.left[String,List[Title]](s"error!!! ${ex.getMessage}")
//        }
//    }
//
//  def searchTitles: ReaderT[F,SearchParams,AppE[List[Title]]] =
//    Kleisli {
//      case sp =>
//        titleRepo.searchWError(sp.term, sp.pageSize, sp.offset).value.map{
//          case Right(value) => Either.right(value)
//          case Left(ex) =>
//            // log here
//            Either.left[String,List[Title]](s"error!!! ${ex.getMessage}")
//        }
//    }
//
//  /**
//   * Search title by Genre, Sorted by combination of Number of Votes and ratings Descending)
//   * Results Default page size 50, determined by the offset
//   *
//   * @param genre
//   * @param limit
//   * @param start
//   * @param F
//   * @return
//   */
//  def topRatedByGenre(genre: Option[String], limit: Int, start: Int)(implicit F: ConcurrentEffect[F]): EitherT[F, String, List[Title]] = {
//    val c = for {
//      titles <- titleRepo.topRatedByGenre(genre, limit, start)
//      _ <- F.delay(println(s"TITLE SIZE: ${titles.size}"))
//    } yield (titles)
//    EitherT.right(c)
//  }
//
//}
//
//object TitleService {
//  def apply[F[_]](titleRepo: TitleRepositoryAlgebra[F])(implicit F: ConcurrentEffect[F]
//                                                        , timer: Timer[F]
//                                                        , contextShift: ContextShift[F]): TitleService[F] =
//    new TitleService[F](titleRepo)
//}
