import java.lang.Exception
import java.util.*
import kotlin.reflect.*

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

object Single

fun Single.describe() {
    println("The single object")
}

fun extensionTest() {
    Single.describe()
}

fun functionTest() {
    fun add(x: Int, y: Int): Int {
        return x + y
    }

    val add1 = fun(x: Int, y: Int): Int {
        return x + y
    }
    val add2 = { x: Int, y: Int -> x + y }
    add(y = 2, x = 1)
    /*
    error:
    add1(y = 2, x = 1)
    add2(y = 2, x = 1)
    */
}

class Person(name: String, age: Int) {
    private var _age = age
    val name = name
    var age get() = _age
        set(value) {
            if (value < 0) throw Exception("Age can't less than 0")
            _age = value
        }

    constructor() : this("", 0) {
    }

    override fun toString(): String {
        return "Person: $name, $age"
    }
}

fun classTest() {

    println(Person("John", 21))
    println(Person())
}

fun randomBool(): Boolean = Random().nextInt() % 2 == 1

fun controlFlowTest() {
    val c = if (randomBool()) 'b' else null
    println(c)

    run {
        println("begin run")
        end@ while(true) {
            println("begin while")
            when {
                randomBool() -> {
                    println("break")
                    break@end
                }
                else -> println("continue")
            }
            println("end while")
        }
        println("end run")
    }
}

fun iterateMethods(any: Any) = sequence {
    when (any) {
        is KClass<*> -> {
            any.javaObjectType.methods
        }
        else -> {
            any.javaClass.methods
        }
    }.map {
        it.name
    }.sorted().forEach {
        yield(it)
    }
}

fun iterateTest() {
    println("-".repeat(30))
    for (m in iterateMethods(Int::class)) println(m)
    println("-".repeat(30))
    for (m in iterateMethods(1)) println(m)
}

fun main() {
//    genericTest()
//    extensionTest()
    controlFlowTest()
//    iterateTest()
//    classTest()
}