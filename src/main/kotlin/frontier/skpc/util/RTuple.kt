package frontier.skpc.util

class RTuple<out A, out B> private constructor(val head: A, val tail: B) {

    companion object {
        operator fun <A> invoke(a: A) =
            RTuple(a, Unit)

        operator fun <A, B> invoke(a: A, b: B) =
            RTuple(a, b)

        operator fun <A, B, C> invoke(a: A, b: B, c: C) =
            RTuple(a, RTuple(b, c))

        operator fun <A, B, C, D> invoke(a: A, b: B, c: C, d: D) =
            RTuple(a, RTuple(b, RTuple(c, d)))

        operator fun <A, B, C, D, E> invoke(a: A, b: B, c: C, d: D, e: E) =
            RTuple(a, RTuple(b, RTuple(c, RTuple(d, e))))

        operator fun <A, B, C, D, E, F> invoke(a: A, b: B, c: C, d: D, e: E, f: F) =
            RTuple(a, RTuple(b, RTuple(c, RTuple(d, RTuple(e, f)))))

        operator fun <A, B, C, D, E, F, G> invoke(a: A, b: B, c: C, d: D, e: E, f: F, g: G) =
            RTuple(a, RTuple(b, RTuple(c, RTuple(d, RTuple(e, RTuple(f, g))))))

        operator fun <A, B, C, D, E, F, G, H> invoke(a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H) =
            RTuple(a, RTuple(b, RTuple(c, RTuple(d, RTuple(e, RTuple(f, RTuple(g, h)))))))
    }
}

@JvmName("a")
inline operator fun <reified A> RTuple<A, *>.component1(): A =
    this.head

@JvmName("b")
inline operator fun <reified B> RTuple<*, B>.component2(): B =
    this.tail

@JvmName("ab")
inline operator fun <reified B> RTuple<*, RTuple<B, *>>.component2(): B =
    this.tail.head

@JvmName("bb")
inline operator fun <reified C> RTuple<*, RTuple<*, C>>.component3(): C =
    this.tail.tail

@JvmName("bba")
inline operator fun <reified C> RTuple<*, RTuple<*, RTuple<C, *>>>.component3(): C =
    this.tail.tail.head

@JvmName("bbb")
inline operator fun <reified D> RTuple<*, RTuple<*, RTuple<*, D>>>.component4(): D =
    this.tail.tail.tail

@JvmName("bbba")
inline operator fun <reified D> RTuple<*, RTuple<*, RTuple<*, RTuple<D, *>>>>.component4(): D =
    this.tail.tail.tail.head

@JvmName("bbbb")
inline operator fun <reified E> RTuple<*, RTuple<*, RTuple<*, RTuple<*, E>>>>.component5(): E =
    this.tail.tail.tail.tail

@JvmName("bbbba")
inline operator fun <reified E> RTuple<*, RTuple<*, RTuple<*, RTuple<*, RTuple<E, *>>>>>.component5(): E =
    this.tail.tail.tail.tail.head

@JvmName("bbbbb")
inline operator fun <reified F> RTuple<*, RTuple<*, RTuple<*, RTuple<*, RTuple<*, F>>>>>.component6(): F =
    this.tail.tail.tail.tail.tail

@JvmName("bbbbba")
inline operator fun <reified F> RTuple<*, RTuple<*, RTuple<*, RTuple<*, RTuple<*, RTuple<F, *>>>>>>.component6(): F =
    this.tail.tail.tail.tail.tail.head

@JvmName("bbbbbb")
inline operator fun <reified G> RTuple<*, RTuple<*, RTuple<*, RTuple<*, RTuple<*, RTuple<*, G>>>>>>.component7(): G =
    this.tail.tail.tail.tail.tail.tail

@JvmName("bbbbbba")
inline operator fun <reified G> RTuple<*, RTuple<*, RTuple<*, RTuple<*, RTuple<*, RTuple<*, RTuple<G, *>>>>>>>.component7(): G =
    this.tail.tail.tail.tail.tail.tail.head

@JvmName("bbbbbbb")
inline operator fun <reified H> RTuple<*, RTuple<*, RTuple<*, RTuple<*, RTuple<*, RTuple<*, RTuple<*, H>>>>>>>.component8(): H =
    this.tail.tail.tail.tail.tail.tail.tail

@JvmName("bbbbbbba")
inline operator fun <reified H> RTuple<*, RTuple<*, RTuple<*, RTuple<*, RTuple<*, RTuple<*, RTuple<*, RTuple<H, *>>>>>>>>.component8(): H =
    this.tail.tail.tail.tail.tail.tail.tail.head

@JvmName("bbbbbbbb")
inline operator fun <reified I> RTuple<*, RTuple<*, RTuple<*, RTuple<*, RTuple<*, RTuple<*, RTuple<*, RTuple<*, I>>>>>>>>.component9(): I =
    this.tail.tail.tail.tail.tail.tail.tail.tail

inline fun <T, R> RTuple<T, *>.map(block: (T) -> R): RTuple<R, *> =
    RTuple(block(this.head), this.tail)