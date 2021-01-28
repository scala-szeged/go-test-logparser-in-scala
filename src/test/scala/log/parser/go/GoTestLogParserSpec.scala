package log.parser.go

import org.specs2.mutable._

class GoTestLogParserSpec extends Specification {

  val example5 =
    "=== RUN TestRemoteAccessSuite\n" +
      "--- PASS: TestRemoteAccessSuite (0.05s)\n" +
      "=== RUN Test_ActionSuite\n" +
      "--- PASS: Test_ActionSuite (0.07s)"
  val result5 = List(
    Section(List("TestRemoteAccessSuite"), List(), 0.05),
    Section(List("Test_ActionSuite"), List(), 0.07)
  )

  example5 should (s"parsed as $result5" in {
    val result = GoTestLogParser.parseChunk(
      GoTestLogParser.failingAsserts,
      example5)

    result must be_===(result5)
  })


  val example1: String =
    """        	Error Trace:	task_schedule_utils_test.go:177
      |        	Error:      	Received unexpected error:
      |        	            	pq: column tasks.base does not exist""".stripMargin

  val result1: ErrorTrace = ErrorTrace(
    file = "task_schedule_utils_test.go",
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

  val example3 = "--- PASS: Test_ActionSuite (0.05s)"
  val result3 = (List("Test_ActionSuite"), 0.05)

  example3 should (s"parsed as $result3" in {
    val result = GoTestLogParser.parseChunk(GoTestLogParser.footer("--- PASS:"), example3)

    result must be_===(result3)
  })

  val example4 =
    "=== RUN Test_ActionSuite\n" +
      "--- PASS: Test_ActionSuite (0.05s)"
  val result4 = Section(List("Test_ActionSuite"), List(), 0.05)

  example4 should (s"parsed as $result4" in {
    val result = GoTestLogParser.parseChunk(GoTestLogParser.section, example4)

    result must be_===(result4)
  })
}
