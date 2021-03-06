package blueeyes.core.service

import test.BlueEyesServiceSpecification
import blueeyes.json.JsonAST._
import blueeyes.core.http.HttpStatus
import blueeyes.core.http.HttpStatusCodes._
import blueeyes.core.http.MimeTypes._

class ServerHealthMonitorServiceSpec extends BlueEyesServiceSpecification with ServerHealthMonitorService{
   "Server Health Monitor Service" should{
    "get server health" in {
      val f = service.get[JValue]("/blueeyes/server/health")
      f.value must eventually(beSomething)

      val response = f.value.get

      response.status  mustEqual(HttpStatus(OK))
      val content = response.content.get

      content \ "runtime" must notEq(JNothing)
      content \ "memory" must notEq(JNothing)
      content \ "threads" must notEq(JNothing)
      content \ "operatingSystem" must notEq(JNothing)
      content \ "server" \ "hostName" must notEq(JNothing)
      content \ "server" \ "port" must notEq(JNothing)
      content \ "server" \ "sslPort" must notEq(JNothing)
    }
  }
}
