
inline fun <reified T> value(): T? {
    return when (T::class) {
        Int::class -> 1 as T
        String::class -> "abc" as T
        else -> null
    }
}

fun genericTest() {
    val i: Int? = value()
    println(i)
    val s: String? = value()
    println(s)
    val b: Boolean? = value()
    println(b)
}

object Single {}

fun Single.describe() {
    println("The single object")
}

fun main() {
    genericTest()
    Single.describe()

    "abc".also {  }
}