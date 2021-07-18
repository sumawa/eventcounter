//package com.sa.sp.db
//
//class Example1Test extends  AnyFlatSpec with Matchers with BeforeAndAfterAll {
//  private var postgres: EmbeddedPostgres = _
//  private var transactor: Transactor[IO] = _
//  implicit private val ioContextShift: ContextShift[IO] =
//    IO.contextShift(ExecutionContext.global)
//
//  override protected def beforeAll(): Unit = {
//    super.beforeAll()
//    postgres = EmbeddedPostgres.builder().start()
//    transactor = Transactor.fromDriverManager[IO](
//      "org.postgresql.Driver",
//      postgres.getJdbcUrl("postgres", "postgres"),
//      "postgres",
//      "postgres",
//      Blocker.liftExecutionContext(ExecutionContext.global)
//    )
//    sql"CREATE TABLE user_points (user_id uuid PRIMARY KEY, points int NOT NULL)"
//      .update.run
//      .transact(transactor)
//      .unsafeRunSync()
//  }
//
//  import Example1._
//
//  it should "add 1 point" in {
//    // given
//    val userId = UUID.randomUUID()
//    addUserWithPoints(userId, 15)
//
//    // when
//    new Points(DefaultDao).increase(userId).transact(transactor).unsafeRunSync()
//
//    // then
//    readUserPoints(userId) shouldBe 16
//  }
//
//  private def addUserWithPoints(userId: UUID, points: Int): Unit = {
//    sql"INSERT INTO user_points(user_id, points) VALUES ($userId, $points)"
//      .update.run.transact(transactor).unsafeRunSync()
//  }
//
//  private def readUserPoints(userId: UUID): Int = {
//    sql"SELECT points FROM user_points WHERE user_id = $userId"
//      .query[Int].unique.transact(transactor).unsafeRunSync()
//  }
//
//  override protected def afterAll(): Unit = {
//    postgres.close()
//    super.afterAll()
//  }
//}
