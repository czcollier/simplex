package net.xorf.simplex.testapp

import net.xorf.simplex.Framework

class TestApp extends Framework {

  ^^("/echo(.*)") {
    req =>  req.query.get("message") match {
      case Some(s) => s
      case None => "no message"
    }
  }
  ^^("/mystuff/(([0-9]{2})+)") {
    req => "<br/>...echoed" + req.urlParams.mkString("|")
  }
}
