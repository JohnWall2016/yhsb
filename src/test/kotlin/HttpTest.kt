import yhsb.net.HttpSocket

fun main() {
    HttpSocket("124.228.42.248", 80).use {
        println(it.getHttp("/"))
    }
}