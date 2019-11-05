import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import com.beust.klaxon.json

class JPerson(
    @Json(name = "aac01") val name: String,
    val sex: Int
)

class JClass(val name: String) {
    val people = mutableListOf<JPerson>()

    fun add(name: String, sex: Int) = people.add(JPerson(name, sex))
}

fun main() {
    println(
        Klaxon().toJsonString(JClass("first class").apply {
            add("John", 1)
            add("Rose", 0)
        })
    )
}