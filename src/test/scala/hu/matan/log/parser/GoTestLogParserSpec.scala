package hu.matan.log.parser

import org.specs2.mutable._

class GoTestLogParserSpec extends Specification {

  val example1: String =
    """        	Error Trace:	task_schedule_utils_test.go:177
      |        	Error:      	Received unexpected error:
      |        	            	pq: column tasks.base does not exist""".stripMargin

  val result1: ErrorTrace = ErrorTrace(
    file = "task_schedule_utils_test",
    line = 177,
    error = "Received unexpected error",
    message = "pq: column tasks.base does not exist"
  )

  example1 should (s"parsed as $result1" in {
    val result = GoTestLogParser.parseChunk(GoTestLogParser.errorWithTrace, example1)

    result must be_===(result1)
  })

  val example2 = "GOROOT=/usr/local/Cellar/go@1.14/1.14.13/libexec #gosetup"
  val result2 = "GOROOT=/usr/local/Cellar/go@1.14/1.14.13/libexec #gosetup"

  example2 should (s"parsed as $result2" in {
    val result = GoTestLogParser.parseChunk(GoTestLogParser.notErrorTrace, example2)

    result must be_===(result2)
  })
}
