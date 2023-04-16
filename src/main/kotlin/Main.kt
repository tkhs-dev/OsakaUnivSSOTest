import org.jsoup.Connection
import org.jsoup.Jsoup
import sun.awt.CharsetString
import java.net.URLDecoder

fun main(args: Array<String>) {
    System.setProperty("http.proxyHost", "127.0.0.1");
    System.setProperty("https.proxyHost", "127.0.0.1");
    System.setProperty("http.proxyPort", "8888");
    System.setProperty("https.proxyPort", "8888");
    System.setProperty("javax.net.ssl.trustStore", "C:/Users/tokuh/FiddlerKeystore");
    System.setProperty("javax.net.ssl.trustStorePassword", "fiddler");

    println("Testing Osaka Univ SSO service!!")
    println("Program arguments: ${args.joinToString()}")

    val con1 = Jsoup.connect("https://www.cle.osaka-u.ac.jp/")
        .method(Connection.Method.GET)
        .execute()
    var cookie_cle = con1.cookies()
    val con2 = Jsoup.connect("https://www.cle.osaka-u.ac.jp/auth-saml/saml/login")
        .method(Connection.Method.GET)
        .followRedirects(false)
        .data(mutableMapOf("apId" to "_3936_1","redirectUrl" to "https://www.cle.osaka-u.ac.jp/ultra"))
        .headers(mutableMapOf(
            "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
            "Accept-Encoding" to "gzip, deflate, br",
            "Accept-Language" to "ja-JP,ja;q=0.9",
            "sec-ch-ua" to "\"Chromium\";v=\"110\", \"Not A(Brand\";v=\"24\"",
            "sec-ch-ua-mobile" to "?0",
            "sec-ch-ua-platform" to "\"Windows\"",
            "Upgrade-Insecure-Requests" to "1",
            "DNT" to "1",
            "Sec-Fetch-Site" to "same-origin",
            "Sec-Fetch-Mode" to "navigate",
            "Sec-Fetch-User" to "?1",
            "Sec-Fetch-Dest" to "document",
            "Referer" to "https://www.cle.osaka-u.ac.jp/"
    ))
        .cookies(cookie_cle.also { it.put("BbClientCalenderTimeZone","Asia/Tokyo") })
        .ignoreContentType(true)
        .execute()
    cookie_cle = con2.cookies()
    val param = URLDecoder.decode(con2.header("Location")?.replace("https://ou-idp.auth.osaka-u.ac.jp/idp/sso_redirect?",""),"UTF-8")?.split("&")
    val con3 = Jsoup.connect("https://ou-idp.auth.osaka-u.ac.jp/idp/sso_redirect")
        .headers(mutableMapOf(
            "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
            "Accept-Encoding" to "gzip, deflate, br",
            "Accept-Language" to "ja-JP,ja;q=0.9",
            "sec-ch-ua" to "\"Chromium\";v=\"110\", \"Not A(Brand\";v=\"24\"",
            "sec-ch-ua-mobile" to "?0",
            "sec-ch-ua-platform" to "\"Windows\"",
            "Upgrade-Insecure-Requests" to "1",
            "DNT" to "1",
            "Sec-Fetch-Site" to "same-site",
            "Sec-Fetch-Mode" to "navigate",
            "Sec-Fetch-User" to "?1",
            "Sec-Fetch-Dest" to "document",
            "Referer" to "https://www.cle.osaka-u.ac.jp/"
        ))
        .data(mutableMapOf(
            "SAMLRequest" to (param?.get(0)?.replace("SAMLRequest=","") ?: ""),
            "SigAlg" to "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256",
            "Signature" to (param?.get(2)?.replace("Signature=","") ?: "")))
        .execute()
    val cookie_idp = con3.cookies()
    val con4 = Jsoup.connect("https://ou-idp.auth.osaka-u.ac.jp/idp/authnPwd")
        .method(Connection.Method.GET)
        .data(mutableMapOf("USER_ID" to args[0],"USER_PASSWORD" to args[1],"CHECK_BOX" to "","IDButton" to "ログイン"))
        .cookies(cookie_idp)
        .execute()

    println(con2.header("Location")
        ?.replace("http%3A%2F%2Fwww.w3.org%2F2001%2F04%2Fxmldsig-more%23rsa-sha256","http://www.w3.org/2001/04/xmldsig-more#rsa-sha256")
        ?: "")
    println(con4.let { it.charset("SJIS") }.body())
}