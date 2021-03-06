import Stream.{cons, empty}

sealed trait Stream[+A] {
  def headOption: Option[A] = this match {
    case Empty => None
    case Cons(h, t) => Some(h())
  }

  //5.1 Write a function to convert a Stream to a List
  def toList: List[A] = this match {
    case Empty => None
    case Cons(h, t) => h() :: t().toList
  }

  //5.2 Write the function take(n) for returning the first n elements of a Stream, and drop(n) for dropping the first n elements of a Stream.
  def take(n: Int): Stream[A] = this match {
    case Empty => Empty
    case Cons(h, t) if n > 0 => Cons(h, () => t.take(n - 1))
    case _ => Empty
  }

  def drop(n: Int): Stream[A] = this match {
    case Empty => Empty
    case Cons(_, t) if n > 0 => t().drop(n - 1)
    case Cons(_, _) if n <= 0 => this
  }

  //5.3 Write the function takeWhile for returning all starting elements of a Stream that match the given predicate
  def takeWhile(p: A => Boolean): Stream[A] = this match {
    case Cons(h: A, t: Stream[A]) => if (p(h))
    case Empty => Empty
  }

  def foldRight[B](z: => B)(f: (A, => B) => B): B = this match {
    case Cons(h, t) => f(h(), t().foldRight(z)(f))
    case _ => z
  }

  def exists(p: A => Boolean): Boolean = this match {
    case Cons(h, t) => p(h()) || t().exists(p)
    case _ => false
  }

  //5.4 Implement forAll, which checks that all elements in the Stream match a given predicate.
  def forAll(p: A => Boolean): Boolean  = this match {
    case Cons(h, t) => if (p(h())) t().forAll(p) else false
    case Empty => true
  }

  //5.5 Use foldRight to implement takeWhile
  def takeWhileFoldRight(p: A => Boolean): Stream[A] =
    foldRight(Stream.empty[A]) { (a, b) =>if (p(a)) Stream.cons(a, b) else b
    }

  //5.6 Implement headOption using foldRight
  def headOptionFoldRight: Option[A] =
    foldRight(None: Option[A])(h, _) => Some(h))

  //5.7 Implement map, filter, append, and flatMap using foldRight.
  def mapfoldRight[B](f: A => B): Stream[B] =
    foldRight(Stream.empty[B]) { (a, bs) =>
      Stream.cons(f(a), bs)
    }

  def filterFoldRight(f: A => Boolean): Stream[A] =
    foldRight(empty[B])((h, t) => if (f(h)) cons(h, t) else t)

  def append[B >: A](s: => Stream[B]): Stream[B] =
    foldRight(s)((h, t) => cons(h, t))

  def flatMapFoldRight[B](f: A => Stream[B]): Stream[B] =
    foldRight(empty[B])((h, t) => f(h) append t)
}
case object Empty extends Stream[Nothing]
case class Cons[+A](h: () => A, t: () => Stream[A]) extends Stream[A]

object Stream {
  def cons[A] (hd: => A, tl: => Stream[A]): Stream[A] = {
    lazy val head = hd
    lazy val tail = tl
    Cons(() => head, () => tail)
  }
  def empty[A]: Stream[A] = Empty

  def apply[A](as: A*): Stream[A] =
    if (as.isEmpty) empty else cons(as.head, apply(as.tail: _*))

  //5.8 Generalize ones slightly to the function constant, which returns an infinite Stream of a given value
  def constant[A](a: A): Stream[A] = {
    lazy val tail: Stream[A] = Cons(() => a, () => tail)
    tail
  }

  //5.9 Write a function that generates an infinite stream of integers, starting from n, then n + 1, n + 2, and so on.
  def from(n: Int): Stream[Int] =
    cons(n, from(n + 1))

  //5.10 Write a function fibs that generates the infinite stream of Fibonacci numbers
  val fibs = {
    def go(f0: Int, f1: Int): Stream[Int] =
      cons(f0, go(f1, f0 + f1))

    go(0, 1)
  }

  //5.11 Write a more general stream-building function called unfold
  def unfold[A, S](z: S)(f: S => Option[(A, S)]): Stream[A] =
    f(z) match {
      case Some((h, s)) => cons(h, unfold(s)(f))
      case None => empty
    }

  //5.12 Write fibs, from, constant, and ones in terms of unfold
  def fibsUnfold: Stream[Int] =
    unfold((0, 1)) {
      case (a, b) =>
        val output = a
        val nextA = b
        val nextB = a + b
        Some((output, (nextA, nextB)))
    }

  def fromUnfold(n: Int) =
    unfold(n)(n => Some((n, n + 1)))

  def constantUnfold[A](a: A) =
    unfold(a)(_ => Some((a, a)))

  val onesUnfold = unfold(1)(_ => Some((1, 1)))

  //5.13 Use unfold to implement map, take, takeWhile, zipWith and zipAll.
  def mapUnfold[B](f: A => B): Stream[B] =
    Stream.unfold(this) {
      case Cons(h, t) =>
        val output: B = f(h())
        val nextState: Stream[A] = t()
        Some((output, nextState))
      case Empty = None
    }

  def takeUnfold = ???

  def takeWhileUnfold = ???

  def zipWithUnfold = ???

  def zipAll[B](s2: Stream[B]): Stream[(Option[A], Option[B])] = ???

  def zipAllUnfold = ???

  //5.14 Implement startsWith using functions you've written
  def startsWith[A](s: Stream[A]): Boolean = ???

  //5.15 Implement tails using unfold.
  def tails: Stream[Stream[A]] = ???

  def hasSubsequence[A](s: Stream[A]): Boolean =
    tails exists (_ startsWith s)

  //5.16 Generalize tails to the function scanRight
  def tailsScanRight = ???

}