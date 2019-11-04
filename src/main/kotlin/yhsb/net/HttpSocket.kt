package yhsb.net

import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.net.Socket
import java.nio.charset.Charset
import kotlin.sequences.iterator

class HttpSocket(
    ip: String,
    port: Int,
    private val charset: Charset = Charsets.UTF_8
) : Closeable {
    private val socket = Socket(ip, port)
    private val ostream = socket.getOutputStream()
    private val istream = socket.getInputStream()

    private val url = "$ip:$port"

    private fun write(bytes: ByteArray) = ostream.write(bytes)

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
            // header.forEach(::println)
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

    fun getHttp(path: String, charset: Charset = Charsets.UTF_8): String {
        val request = HttpRequest(path, method = "GET", charset = charset).apply {
            addHeader("Host", url)
            addHeader("Connection", "keep-alive")
            addHeader("Cache-Control", "max-age=0")
            addHeader("Upgrade-Insecure-Requests", "1")
            addHeader(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36"
            )
            addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
            addHeader("Accept-Encoding", "gzip, deflate")
            addHeader("Accept-Language", "zh-CN,zh;q=0.9")
        }
        write(request.toByteArray())
        return readHttpBody()
    }
}

class HttpHeader : Iterable<Map.Entry<String, List<String>>> {
    private val headers = mutableMapOf<String, MutableList<String>>()

    fun add(name: String, value: String) {
        val key = name.toLowerCase()
        if (!headers.containsKey(key)) {
            headers[key] = mutableListOf()
        }
        headers[key]?.add(value)
    }

    fun addAll(header: HttpHeader) = headers.putAll(header.headers)

    operator fun set(key: String, values: MutableList<String>) {
        headers[key.toLowerCase()] = values
    }

    operator fun get(key: String) = headers[key.toLowerCase()]

    override fun iterator() = iterator {
        headers.forEach {
            yield(it)
        }
    }
}

class HttpRequest(
    private val path: String,
    private val method: String = "GET",
    private val charset: Charset = Charsets.UTF_8,
    header: HttpHeader? = null
) {
    private val header = HttpHeader().apply {
        if (header != null) addAll(header)
    }
    private val body = ByteArrayOutputStream()

    fun addHeader(key: String, value: String) = header.add(key, value)

    fun addBody(str: String) = body.write(str.toByteArray(charset))

    fun toByteArray(): ByteArray {
        val buf = ByteArrayOutputStream()
        buf.write("$method $path HTTP/1.1\r\n".toByteArray(charset))
        header.forEach { entry ->
            entry.value.forEach { value ->
                buf.write("${entry.key}: $value\r\n".toByteArray(charset))
            }
        }
        if (body.size() > 0) {
            buf.write("content-length: ${body.size()}\r\n".toByteArray(charset))
        }
        buf.write("\r\n".toByteArray(charset))
        if (body.size() > 0) {
            buf.write(body.toByteArray())
        }
        return buf.toByteArray()
    }
}