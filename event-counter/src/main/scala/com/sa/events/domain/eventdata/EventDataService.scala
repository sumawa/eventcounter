package com.sa.events.domain.eventdata

import cats.effect.{ConcurrentEffect, ContextShift, Timer}

///**
// * NameServiceWithRef: Service layer, business logic for namess
// * binds with Doobie Repository for making DB calls.
// *
// * This implementation uses cats Ref
// *
//    Ref[F, A] is side-effect-ful, because,
//      + wanting to keep the property of referential transparency while sharing and mutating state.
//      + when we invoke flatMap on it twice we get two different mutable states,
//      + and this is the power of Referential Transparency.
//      + It gives way more control than having a val ref hanging around in our code and gives local reasoning.
//
//    Benefits of Referential Transparency
//    - being able to understand and build code in a compositional way.
//    - That is, understanding code by understanding individual parts
//      and putting them back together
//    - And building code by building individual parts and combining them together.
//    - This is only possible if local reasoning is guaranteed
//    - Because otherwise there will be weird interactions when we put things back together
//    - and referential transparency is defined as something that guarantees local reasoning.
//
//    Case of state sharing
//        - since the only way to share is passing things as an argument,
//        - the regions of sharing are exactly the same of our call graph
//        - so we transform an important aspect of the behaviour (“who shares this state?”)
//        - into a straightforward syntactical property (“what methods take this argument”?).
//        - This makes shared state in pure FP a lot easier to reason about than its side-effect-ful counterpart.
//
// */
//class NameServiceWithRef[F[_]](nameRepo: NameRepositoryAlgebra[F])
//                              (implicit F: ConcurrentEffect[F]
//                            , timer: Timer[F]
//                            , contextShift: ContextShift[F]){
//
//  val KEVIN_BACON_NMID = "nm0000102"
//  val DEPTH_THRESHOLD = 1000
//
//  /*
//    A wrapper class containing all mutable states.
//   */
//  case class DegreeState(queue: mutable.Queue[(IdAndName,Int)]
//                         , visitedSet: mutable.Set[String]
//                        ,var currentNameState: NameState)
//
//  /*
//    flatMap once and pass the reference as an argument!
//    We need to call flatMap once up in the call chain where we call the processes
//    to make sure they all share the same state.
//   */
//  def degreeOfSepRef(targetActor: String) = {
//    val initState = DegreeState(
//      mutable.Queue(((KEVIN_BACON_NMID,"Kevin Bacon"),0))
//      , mutable.Set(KEVIN_BACON_NMID)
//      , NameState((KEVIN_BACON_NMID,"Kevin Bacon"),0,false)
//    )
//    /*
//      - A new Ref gets created every time we flatMap (creating a Ref is side effect-ful!)
//      - and our processes will not be sharing the same state changing the behavior of program.
//     */
//    val output = Ref.of[F,DegreeState](initState)
//      .flatMap { myState =>
//        degreesOfSep(targetActor,myState).value
//      }
//    EitherT(output)
//  }
//  /**
//   * main entry point of the function that computes degree of separation
//   * for a given target Actor
//   *
//   * The entire logic is based on BFS (Breadth First Search) in Graphs
//   * The actors and immediate co-actors are treated as Nodes
//   * and direct connection between them as edges.
//   *
//   * Step 1:
//   *  Initialize queues, sets and state
//   *
//   * Step 2:
//   *  Set up a source stream using initial actor (Kevin Bacon)
//   *  continuously emitting co-actors, then each of their co-actors and so on.
//   *
//   * Step 3:
//   *  Already defined a "Pipe" above which process current actors co-actors
//   *
//   * Step 4:
//   *  Define a condition for termination which is
//   *    - check if either target actor is found
//   *    - OR queue is Empty
//   *    - OR queue reached a threshold
//   *  terminate if any of the condition is met.
//   *
//   * Step 5:
//   *  Define a sink if required
//   *
//   * Step 5:
//   *  Bind all four together  SourceStream -> Pipe -> Through Condition -> Sink
//   *
//   * @param targetActor   The target actor who's degree to be find relative to Kevin Bacon
//   * @return              Either error or a "final state" which may or may not contain answer
//   */
//  def degreesOfSep(targetActor: String, degreeStateRef: Ref[F,DegreeState]): EitherT[F,String,Option[NameState]] = {
//
//    /*
//      Create a source: Based on current item in the queue.
//      Initially queue will have only source item (Kevin Bacon)
//      Subsequently, if downstream continue to request, based on other actors added in the queue,
//      the new "DegreeState" is computed
//     */
//    val sourceActorState: Stream[F,DegreeState] = {
//      iterateEvalFromEffect(degreeStateRef.get) {
//        currState =>
//          val q = currState.queue
//          val (idAndName,l) = q.dequeue()
//          val found = ( idAndName._1 == targetActor)
//          // if target actor found clear the queue and return NameState with true
//          val nextNameState = if (found){
//            q.clear()
//            NameState(idAndName,l,found)
//          } else {
//            NameState(idAndName,l,false)
//          }
//          F.delay(DegreeState(q,currState.visitedSet,nextNameState))
//      }
//    }
//
//    // possible use in future
//    val sink: Pipe[F,NameState,Option[NameState]] = _.last
//
//    /*
//      We defined a Source that emits Actors NameState,
//      a pipe that performs a series of transformations to those ActorState
//      and a sink that takes those State do nothing.
//
//      One of the major ways we connect these elements
//      is via a method in FS2 streams called through,
//      which transforms a given stream given a pipe:
//     */
//    val finalDegreeState = sourceActorState
//      .through(pipeRef(targetActor, degreeStateRef))
//      .takeThrough (as => terminatingCondition(as,DEPTH_THRESHOLD))
//      .compile
//      .last
//
//    // return EiterT[F,String,Option[NameState]
//    EitherT.right[String](
//      for {
//        ds <- finalDegreeState
//      } yield (ds.map(_.currentNameState))
//    )
//  }
//
//  /**
//   * A helper function to
//   *  - produce a stream of effects, starting with a given "seed" effect
//   *  - and then generating each subsequent effect from the value produced by the preceding effect.
//   *
//   * @param start   A "seed" effect
//   * @param f
//   * @tparam F      The effect type
//   * @tparam A      The actual type
//   * @return        Stream[F,A]
//   */
//
//  def iterateEvalFromEffect[F[_], A](start: F[A])(f: A => F[A]): Stream[F, A] =
//    Stream.eval(start)
//      .flatMap(s => Stream.emit(s) ++
//        iterateEvalFromEffect(f(s))(f))
//
//  /*
//    The pipe is a streaming element with open input and output
//    - used as processing step to transform data gotten from source
//    - Can consist of single or multiple steps
//
//    Pipe has type Pipe[F[_],-I,+O] which is a type alias for
//    A stream transformation represented as function from stream to stream.
//
//    type Pipe[F[_], -I, +O] = Stream[F,I] => Stream[F,O]
//
//    F represents Effect, I represents input type, O represents output type
//
//    A Pipe in fs2 is a function that takes a stream of a certain type and returns
//    another stream of same or different type.
//
//    Example a simple pipe that takes a stream and converts into string
//      val iToStrPipe: Pipe[Pure, Int, String] =
//        stream => stream.map(i => s"$i str")
//
//    can be rewritten as
//      val iToStrPipe: Pipe[Pure, Int, String] =
//        _.map(_ + " str")
//   */
//
//  /**
//   * This pipe
//   *  - pops next actor from the queue
//   *  - finds all the possible co-actors from the DB
//   *  - if list of co-actors contains the target actor
//   *      return Actor's NameState with incremented level
//   *    else
//   *      add all the co-actors names in the queue
//   *      (excluding those already in the Set)
//   *
//   * @param targetActor
//   * @param degStateRef
//   * @return
//   */
//  private def pipeRef(targetActor: String, degStateRef: Ref[F,DegreeState]): Stream[F, DegreeState] => Stream[F, DegreeState] = {
//    _.evalMap { degreeState =>
////      TODO: some debug statement to be removed later, added for investigating possible parallel execution
//      for {
//        _ <- F.delay {println(s"Stage  processing Degree state by ${Thread.currentThread().getName}")}
//        nameState = degreeState.currentNameState
//        (idAndName, level) = (nameState.idAndName,nameState.deg)
//        coActors <- nameRepo.selectImmediateCoActors(idAndName._1)
//        nextState <- execNextState(coActors,targetActor,level)
//                        .runS(degreeState)
//        _ <- F.delay(println(s"ns: ${nextState.currentNameState}"))
//      }yield nextState
//    }
//  }
//
//  /*
//    TODO: Add description
//   */
//  private def execNextState(coActors: List[IdAndName]
//                        , targetActor: String
//                        , currentLevel: Int): StateT[F,DegreeState,Unit] = {
//    for {
//      currDegreeState <- StateT.get[F, DegreeState]
//      (q,v) = (currDegreeState.queue,currDegreeState.visitedSet)
//      newState = nextState(targetActor,currentLevel,q,v,coActors)
//      _ <- StateT.set {
//        currDegreeState.currentNameState = newState
//        currDegreeState
//      }
//    } yield ()
//  }
//
//  private def nextState(targetActor : String
//                       , currentLevel: Int
//                        , queue: mutable.Queue[(IdAndName,Int)]
//                       , visitedSet: mutable.Set[String]
//                        , coActors: List[IdAndName]) = {
//    val matchedTargetActor = coActors.filter(_._1 == targetActor)
//    val found = (matchedTargetActor.size > 0)
//
//    if (found) {
//      // increment the level, set found = true
//      // return given actor's  NameState and clear the Queue
//      queue.clear()
//      NameState(matchedTargetActor.head, currentLevel + 1, found)
//    } else {
//      // if target actor not found, add all the co-actors nconst (those not already added in the Set)
//      // to the queue, and add nconst to visited set so that we not accidentally process it twice later.
//      coActors.foreach { case (nconst, name) =>
//        if (!visitedSet.contains(nconst)) {
//          queue.enqueue(((nconst,name), currentLevel + 1))
//          visitedSet += nconst
//        }
//      }
//      println(s"queue SIZE = ${queue.size} and curr Level = ${currentLevel + 1} ")
//      NameState((targetActor,targetActor), currentLevel + 1, found)
//    }
//  }
//  /*
//    Terminating condition of stream
//    Terminate if any of the condition is met.
//    NameState found is True OR Queue is Empty OR Queue size reached a threshold.
//    We will match degrees to a certain level. (may be 10 or so)
//   */
//  private def terminatingCondition(degreeState: DegreeState
//                        , threshold: Int): Boolean = {
//    println(s"visitedSet size: ${degreeState.visitedSet.size} current actorState: ${degreeState.queue.size} queue Empty : ${degreeState.queue.isEmpty} ")
//    // check if either target actor is found
//    !(degreeState.currentNameState.found
//        // OR queue is Empty
//        || degreeState.queue.isEmpty
//          // OR queue reached a threshold
//          || degreeState.queue.size > threshold)
//  }
//}

object EventDataService {
//  def apply[F[_]](nameRepo: NameRepositoryAlgebra[F])
    def apply[F[_]]()
                 (implicit F: ConcurrentEffect[F]
                  , timer: Timer[F]
                  , contextShift: ContextShift[F]): EventDataService[F] =
    new EventDataService[F]()
}

/**
 * NameServiceWithRef: Service layer, business logic for namess
 * binds with Doobie Repository for making DB calls.
 *
 * This implementation uses cats Ref
 *
Ref[F, A] is side-effect-ful, because,
      + wanting to keep the property of referential transparency while sharing and mutating state.
      + when we invoke flatMap on it twice we get two different mutable states,
      + and this is the power of Referential Transparency.
      + It gives way more control than having a val ref hanging around in our code and gives local reasoning.

    Benefits of Referential Transparency
    - being able to understand and build code in a compositional way.
    - That is, understanding code by understanding individual parts
      and putting them back together
    - And building code by building individual parts and combining them together.
    - This is only possible if local reasoning is guaranteed
    - Because otherwise there will be weird interactions when we put things back together
    - and referential transparency is defined as something that guarantees local reasoning.

    Case of state sharing
        - since the only way to share is passing things as an argument,
        - the regions of sharing are exactly the same of our call graph
        - so we transform an important aspect of the behaviour (“who shares this state?”)
        - into a straightforward syntactical property (“what methods take this argument”?).
        - This makes shared state in pure FP a lot easier to reason about than its side-effect-ful counterpart.

 */
class EventDataService[F[_]]()
                              (implicit F: ConcurrentEffect[F]
                               , timer: Timer[F]
                               , contextShift: ContextShift[F]){
  def someFunc() = ???

}





