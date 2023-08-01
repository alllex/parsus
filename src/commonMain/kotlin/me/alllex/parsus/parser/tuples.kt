package me.alllex.parsus.parser


data class Tuple2<out T1, out T2>(val t1: T1, val t2: T2) {
    val first: T1 get() = t1
    val second: T2 get() = t2
    fun toPair(): Pair<T1, T2> = Pair(t1, t2)
    override fun toString(): String = "($t1, $t2)"
}

fun <T, T1 : T, T2 : T> Tuple2<T1, T2>.toList(): List<T> = listOf(t1, t2)


data class Tuple3<out T1, out T2, out T3>(val t1: T1, val t2: T2, val t3: T3) {
    override fun toString(): String = "($t1, $t2, $t3)"
}

fun <T, T1 : T, T2 : T, T3 : T> Tuple3<T1, T2, T3>.toList(): List<T> = listOf(t1, t2, t3)


data class Tuple4<out T1, out T2, out T3, out T4>(val t1: T1, val t2: T2, val t3: T3, val t4: T4) {
    override fun toString(): String = "($t1, $t2, $t3, $t4)"
}

fun <T, T1 : T, T2 : T, T3 : T, T4 : T> Tuple4<T1, T2, T3, T4>.toList(): List<T> = listOf(t1, t2, t3, t4)


data class Tuple5<out T1, out T2, out T3, out T4, out T5>(val t1: T1, val t2: T2, val t3: T3, val t4: T4, val t5: T5) {
    override fun toString(): String = "($t1, $t2, $t3, $t4, $t5)"
}

fun <T, T1 : T, T2 : T, T3 : T, T4 : T, T5 : T> Tuple5<T1, T2, T3, T4, T5>.toList(): List<T> =
    listOf(t1, t2, t3, t4, t5)


data class Tuple6<out T1, out T2, out T3, out T4, out T5, out T6>(val t1: T1, val t2: T2, val t3: T3, val t4: T4, val t5: T5, val t6: T6) {
    override fun toString(): String = "($t1, $t2, $t3, $t4, $t5, $t6)"
}

fun <T, T1 : T, T2 : T, T3 : T, T4 : T, T5 : T, T6 : T> Tuple6<T1, T2, T3, T4, T5, T6>.toList(): List<T> =
    listOf(t1, t2, t3, t4, t5, t6)


data class Tuple7<out T1, out T2, out T3, out T4, out T5, out T6, out T7>(val t1: T1, val t2: T2, val t3: T3, val t4: T4, val t5: T5, val t6: T6, val t7: T7) {
    override fun toString(): String = "($t1, $t2, $t3, $t4, $t5, $t6, $t7)"
}

fun <T, T1 : T, T2 : T, T3 : T, T4 : T, T5 : T, T6 : T, T7 : T> Tuple7<T1, T2, T3, T4, T5, T6, T7>.toList(): List<T> =
    listOf(t1, t2, t3, t4, t5, t6, t7)
