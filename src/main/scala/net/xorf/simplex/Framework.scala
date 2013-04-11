package net.xorf.simplex

import javax.servlet.http._
import collection.JavaConversions._
import collection.mutable.ArrayBuffer
import util.matching.Regex
import util.control.Breaks._

class Framework extends HttpServlet {
  implicit def toReq(urlData: Seq[String], sr: HttpServletRequest): Req = {

    def paramToTuple(p: String) = {
      p.split('=') match {
        case Array(k, v) => (k.toString, v.toString)
        case single => (single.toString, "")
      }
    }

    def qstrToMap(qs: Option[String]): Map[String, String] = qs match {
      case Some(s) => s.split('&').map(p => paramToTuple(p)).toMap
      case None => Map()
    }

    def extractHeaders(req: HttpServletRequest) = {
      req.getHeaderNames.map(h => (h.toString, req.getHeaders(h.toString).map(hv => hv.toString).toSeq)).toMap
    }

    new Req(
      sr.getRequestURI,
      urlData,
      qstrToMap(Option(sr.getQueryString)),
      extractHeaders(sr), "")
  }

  override def doGet(req: HttpServletRequest, resp: HttpServletResponse) {
    val out = resp.getWriter
    resp.setContentType("text/html")

    breakable {
      for (h <- Framework.procs) {
        val urlMatch = h._1.findFirstMatchIn(req.getRequestURI)

        val processed = urlMatch match {
          case Some(m) => {
            val output = h._2(toReq(m.subgroups, req))
            println("processing: " + h._1, m.subgroups)
            out.write(output)
            true
          }
          case None => false
        }

        if (processed) break()
      }
    }
    out.flush()
  }

  def ^^(s: String) (r: Framework.RequestProcessor) = {
    Framework.procs += (s.r -> r)
    r
  }
}

class Req(
  val path: String,
  val urlParams: Seq[String],
  val query: Map[String, String],
  val headers: Map[String, Seq[String]],
  val data: String)

object Framework {
  type RequestProcessor = (Req) => String

  val procs = new ArrayBuffer[(Regex, RequestProcessor)]

}

