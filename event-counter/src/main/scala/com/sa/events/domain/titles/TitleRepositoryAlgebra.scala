//package com.sa.events.domain.titles
//
//import java.sql.SQLException
//
//import cats.data.EitherT
///*
//  trait exposing functions for interacting with DB Repo
// */
//trait TitleRepositoryAlgebra[F[_]] {
//
//  /**
//   * TODO: Documentation
//   *
//   * @param pattern
//   * @param pageSize
//   * @param offset
//   * @return
//   */
//  def search(pattern: String, pageSize: Int, offset: Int): F[List[Title]]
//
//  /**
//   *
//   * @param pattern
//   * @param pageSize
//   * @param offset
//   * @return
//   */
//  def searchExact(pattern: String, pageSize: Int, offset: Int): F[List[Title]]
//
//  /**
//   *
//   * @param genre
//   * @param pageSize
//   * @param offset
//   * @return
//   */
//  def topRatedByGenre(genre: Option[String], pageSize: Int, offset: Int): F[List[Title]]
//
////  def searchWError(pattern: String, pageSize: Int = 50, offset: Int = 0): F[Either[String,List[Title]]]
////  def searchExactWError(pattern: String, pageSize: Int = 50, offset: Int = 0): F[Either[String,List[Title]]]
//
////  def searchWError(pattern: String, pageSize: Int = 50, offset: Int = 0): EitherT[F,String,List[Title]]
//  def searchWError(pattern: String, pageSize: Int, offset: Int): EitherT[F,SQLException, List[Title]]
//  def searchExactWError(pattern: String, pageSize: Int = 50, offset: Int = 0): EitherT[F,SQLException, List[Title]]
////  def searchExactWError(pattern: String, pageSize: Int = 50, offset: Int = 0): EitherT[F,String,List[Title]]
//}
