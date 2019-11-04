package yhsb.net

import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.net.Socket
import java.nio.charset.Charset

class HttpSocket(private val ip: String, private val port: Int, private val charset: Charset = Charsets.UTF_8) :
    Closeable {
    private val socket = Socket(ip, port)
    private val ostream = socket.getOutputStream()
    private val istream = socket.getInputStream()

    val url = "$ip:$port"

    fun write(bytes: ByteArray) = ostream.write(bytes)

    fun write(string: String) = ostream.write(string.toByteArray(charset))

    private fun read() = istream.read()

    private fun read(len: Int): ByteArray? {
        val r = read()
        if (r == -1) return null
        return ByteArrayOutputStream(len).use {
            it.write(r)
            for (i in 2..len) {
                val r = read()
                if (r == -1) break
                it.write(r)
            }
            it.toByteArray()
        }
    }

    private fun readLine(): String {
        return ByteArrayOutputStream().use {
            end@ while (true) {
                when (val c = read()) {
                    -1 -> break@end
                    0x0D -> // \r
                        when (val n = read()) {
                            -1 -> {
                                it.write(c)
                                break@end
                            }
                            0x0A -> // \n
                                break@end
                            else -> {
                                it.write(c)
                                it.write(n)
                            }
                        }
                    else -> it.write(c)
                }
            }
            String(it.toByteArray(), charset)
        }
    }

    private fun readHttpHeader(): HttpHeader {
        val header = HttpHeader()
        while (true) {
            val line = readLine()
            if (line == "") break;
            val i = line.indexOf(':')
            if (i > 0) {
                val key = line.substring(0, i).trim()
                val value = line.substring(i + 1).trim()
                header.add(key, value)
            }
        }
        return header
    }

    fun readHttpBody(header: HttpHeader? = null): String {
        return ByteArrayOutputStream().use {
            val header = header ?: readHttpHeader()
            if (header["Transfer-Encoding"]?.contains("chunked") == true) {
                while (true) {
                    val len = readLine().toInt(16)
                    if (len <= 0) {
                        readLine()
                        break
                    }
                    it.write(read(len) ?: throw Exception("The read length is short"))
                    readLine()
                }
            } else {
                val length = header["Content-Length"]?.get(0)
                if (length != null) {
                    val len = length.toInt(10)
                    it.write(read(len) ?: throw Exception("The read length is short"))
                } else {
                    throw Exception("Unsupported transfer mode")
                }
            }
            String(it.toByteArray(), charset)
        }
    }

    override fun close() {
        socket.use {
            ostream.use {
                istream.use { }
            }
        }
    }
}

class HttpHeader {
    private val headers = mutableMapOf<String, MutableList<String>>()

    fun add(name: String, value: String) {
        val key = name.toLowerCase()
        if (!headers.containsKey(key)) {
            headers[key] = mutableListOf()
        }
        headers[key]?.add(value)
    }

    operator fun set(key: String, values: MutableList<String>) {
        headers[key.toLowerCase()] = values
    }

    operator fun get(key: String) = headers[key]
}

class HttpRequest {

}