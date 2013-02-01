package net.xorf.simplex

import javax.servlet.http._
import collection.mutable
import collection.JavaConversions._
import collection.mutable.ArrayBuffer
import util.matching.Regex
import util.matching.Regex.Match

class Framework extends HttpServlet {
  implicit def toReq(urlData: Seq[String], sr: HttpServletRequest): Req = {
    new Req(
      sr.getRequestURI,
      urlData,
      Framework.qstrToMap(sr.getQueryString),
      Framework.extractHeaders(sr), "")
  }

  override def doGet(req: HttpServletRequest, resp: HttpServletResponse) {
    val out = resp.getWriter
    resp.setContentType("text/html")
    out.write("pathInfo: %s<br/>".format(req.getPathInfo))
    out.write("servletPath: %s<br/>".format(req.getServletPath))
    out.write("contextPath: %s<br/>".format(req.getContextPath))
    out.write("pathTranslated: %s<br/>".format(req.getPathTranslated))
    out.write("requestURI: %s<br/>".format(req.getRequestURI))

    for (h <- Framework.procs) {
      val urlMatch = h._1.findFirstMatchIn(req.getRequestURI)
      val p = urlMatch match {
        case Some(m) => {
          val output = h._2(toReq(m.subgroups, req))
          println("processing: " + h._1, m.subgroups)
          out.write(output)
          true
        }
        case None => false
      }

    }
    out.flush()
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

  def paramToTuple(p: String) = {
    p.split('=') match {
      case Array(k, v) => (k.toString, v.toString)
      case single => (single, "")
    }
  }

  def qstrToMap(qs: String) = qs.split('&').map(p => paramToTuple(p)).toMap

  def extractHeaders(req: HttpServletRequest) = {
    req.getHeaderNames.map(h => (h.toString, req.getHeaders(h.toString).map(hv => hv.toString).toSeq)).toMap
  }

  def ^^(s: String) (r: RequestProcessor) = {
    procs += (s.r -> r)
    r
  }

  ^^("/echo(.*)") {
    req: Req => {
      req.query("message")
    }
  }
}

