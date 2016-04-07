package scalera

import shapeless.Poly1

import scala.collection.GenTraversable
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.Try

import scala.language.higherKinds

object Polymorphic extends App {

  //  The logic ...

  object values extends Poly1 {

    implicit def genTraversableCase[T,C[_]](implicit ev: C[T] => GenTraversable[T]) =
      at[C[T]](_.toStream)

    implicit def tryCase[T,C[_]](implicit ev: C[T] => Try[T]) =
      at[C[T]](_.toOption.toStream)

    implicit def futureCase[T,C[_]](implicit ev: C[T] => Future[T], atMost: Duration = Duration.Inf) =
      at[C[T]](f => Try(Await.result(f,atMost)).toOption.toStream)

  }

  //  Let's try it!

  import scala.concurrent.ExecutionContext.Implicits.global

  case class User(name: String, age: Int)

  val result: Stream[_] = for {
    v1 <- values(List(1,2,3))
    v2 <- values(Set("hi","bye"))
    v3 <- values(Option(true))
    v4 <- values(Try(2.0))
    v5 <- values(Future(User("john",15)))
  } yield (v1,v2,v3,v4,v5)

  result.foreach(println)

}
