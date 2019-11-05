package yhsb.jb

import yhsb.net.HttpRequest
import yhsb.net.HttpSocket

class Session(
    host: String,
    port: Int,
    val userID: String,
    val password: String
) : HttpSocket(host, port) {
    var sessionID: String? = null
    var cxCookie: String? = null

    private fun createRequest(): HttpRequest {
        return HttpRequest(path = "/hncjb/reports/crud", method = "POST").apply {
            addHeader("Host", url)
            addHeader("Connection", "keep-alive")
            addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
            addHeader("Origin", "http://$url")
            addHeader("X-Requested-With", "XMLHttpRequest")
            addHeader(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36"
            )
            addHeader("Content-Type", "multipart/form-data;charset=UTF-8")
            addHeader("Referer", "http://$url/hncjb/pages/html/index.html")
            addHeader("Accept-Encoding", "gzip, deflate")
            addHeader("Accept-Language", "zh-CN,zh;q=0.8")

            if (sessionID != null) {
                addHeader("Cookie", "jsessionid_ylzcbp=$sessionID; cxcookie=$cxCookie")
            }
        }
    }

    private fun buildRequest(content: String): HttpRequest {
        return createRequest().apply {
            addBody(content)
        }
    }

    fun request(content: String) {
        write(buildRequest(content).toByteArray())
    }


}