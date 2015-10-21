import java.io.{File, FileWriter, PrintWriter}
import java.net.{HttpURLConnection, URL}

import scala.io.Source

/**
 * Created by LY on 2015/10/21.
 */
class OSChina {
  private final  val  httpLink="http://www.oschina.net/news"
  private final val  resultFile="OSChinaFile.txt"

  //正则表达式
  //<h2><a href="http://my.oschina.net/xxiaobian/blog/519629" target="_blank">OSChina 周三乱弹 —— 老鸟程序员的小技巧</a></h2>
  val pattern_h2="""<h2>(.*?)</h2>""".r
  //href="http://my.oschina.net/xxiaobian/blog/519629"
  val pattern_href="""href\s*=\s*["](.+?)["]""".r
  //>OSChina 周三乱弹 —— 老鸟程序员的小技巧<
  val pattern_text=""">(.*?)<""".r
  //http://my.oschina.net/xxiaobian/blog/519629
  val pattern_link="""["](.+?)["]""".r

  //连接到网页
  def connectionToURL(url:String)={
    val conUrl=new URL(url) //初始化URL
    val connection=conUrl.openConnection().asInstanceOf[HttpURLConnection]  //建立连接
    connection.setRequestMethod("GET") //设置请求方法
    connection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 5.1; rv:5.0) Gecko/20100101 Firefox/5.0")

    //判断是否响应成功
    if(connection.getResponseCode==200){
        //获取网页内容
      getHtmlContent(url,connection)
    }
  }

  def getHtmlContent(url:String,connection:HttpURLConnection)={
    val out=new FileWriter(new File(resultFile))  //初始化输出文件
    val in=Source.fromInputStream(connection.getInputStream,"utf-8")

    for(line <- in.getLines()){
      out.write(line)
    }

    out.close()
  }

  //下载网页
  def downloadUrlFile(url: String, file: String) = {
    val pw = new PrintWriter(file)
    pw.write(Source.fromURL(url, "utf-8").mkString)

    pw.close()
  }

  //解析网页
  def parseHtmlContent = {
    //首先将html网页的内容变成字符串，方便处理
    val linesString=Source.fromFile(resultFile,"utf-8").mkString

    //匹配<h2><\h2>
    val h2Content=pattern_h2.findAllIn(linesString).mkString(",").split(",")
    //遍历数组
    for(i <- 0.to(h2Content.length - 1)){
      var linkUrl=getNewsLinkUrl(pattern_href.findAllIn(h2Content(i)).mkString(",").split(",")(0))
      var newsContent=getNewsContent(pattern_text.findAllIn(h2Content(i)).mkString(",").split(",").filter(_.length > 5)(0))

      print(s"$linkUrl:$newsContent\n")
    }
  }

  //获取新闻资讯的url连接
  def  getNewsLinkUrl(url:String):String={
      val preLinkUrl=pattern_link.findAllIn(url).mkString
      val newsLinkUrl:String=preLinkUrl.substring(1,preLinkUrl.length-1)

      if(!(newsLinkUrl.startsWith("http://www.oschina.net"))){
        "http://www.oschina.net"+newsLinkUrl
      }else
        newsLinkUrl
  }

  //获取新闻资讯的内容概况
  def getNewsContent(original:String):String={
    val newsContent=original.substring(1,original.length-1)
    newsContent
  }
}

object OSChina extends App{

  val oschina=new OSChina()  //实例化对象
  oschina.connectionToURL("http://www.oschina.net/news") //链接到URL
  oschina.parseHtmlContent  //解析
}
