package log.parser.go

import scala.io.Source
import scala.util.matching.Regex
import scala.util.parsing.combinator.JavaTokenParsers


sealed trait Entity

case class Section(testID: List[String], entites: List[Entity], duration: Double) extends Entity

case class ErrorTrace(file: String, line: Int, error: String, message: String) extends Entity

case class ErrorTraceInSection(file: String, line: Int, error: String, message: String, section: Section)


object GoTestLogParser extends JavaTokenParsers {

  def main(args: Array[String]): Unit = {

    val defaultFile = "src/test/resources/fileman-go-test.log"
    val file = args.headOption.getOrElse(defaultFile)
    val input = Source.fromFile(file).bufferedReader()
    val r = parse(input)

    def loop(parent: Section)(acc: List[ErrorTraceInSection], entity: Entity): List[ErrorTraceInSection] = entity match {
      case section: Section => section.entites.foldLeft(acc)(loop(section))
      case head: ErrorTrace => ErrorTraceInSection(head.file, head.line, head.error, head.message, parent) :: acc
    }

    val result = r.foldLeft(List.empty[ErrorTraceInSection])(loop(null))

    println
    println(s"Number of failing asserts: ${result.size}")

    println
    result.groupBy(_.file).map {
      case (file, list) => (file, list.map(_.line).distinct)
    }.foreach(println)

    println
    result.filterNot(_.message.startsWith("expected")).groupBy(_.message).map {
      case (message, list) =>
        (
          message,
          list
            .sortBy(et => f"${et.file}:${et.line}%07d")
            .map(et => s"${et.file}:${et.line} ${et.section.testID.mkString("/")}")
        )
    }.toList.sortBy(_._1).reverse.foreach {
      case (message, fileLineList) =>
        println
        println(message)
        fileLineList.distinct.foreach(println)
    }

    // Todo: 2 word-clouds about what is failed and what is succeeded
  }

  override protected val whiteSpace: Regex = """[ \t]+""".r
  val fail = "--- FAIL:"
  val pass = "--- PASS:"


  def failingAsserts: Parser[List[Entity]] = repsep(line, endOfLine) ^^ { list =>
    list.collect {
      case et: ErrorTrace => et
      case section: Section => section
    }
  }

  override def ident: Parser[String] = "[a-zA-Z0-9_#',.=$-]+".r


  def line: Parser[Any] = section | errorWithTrace | notErrorTrace | emptyLine


  def section = header("=== RUN") ~ failingAsserts ~ footer ^^ {
    case testID ~ parts ~ footer => Section(testID, parts, footer._2)
  }

  // header("=== RUN" ) // === RUN   Test_ModelSuite/TestFindUnscheduledTaskByTags
  def header(tag: String) = tag ~> repsep(ident, "/") <~ endOfLine

  def footer: Parser[(List[String], Double)] = {
    footer(fail) | footer(pass)
  }

  // footer("--- FAIL:") // --- FAIL: Test_ModelSuite/TestFindUnscheduledTaskByTags (0.09s)
  // footer("--- PASS:") // --- PASS: TestRemoteAccessSuite/TestRenderScriptToTempFile (0.05s)
  def footer(tag: String) = tag ~> repsep(ident, "/") ~ "(" ~ floatingPointNumber <~ "s)" ^^ {
    case list ~ _ ~ seconds => (list, seconds.toDouble)
  }

  def errorWithTrace: Parser[ErrorTrace] = errorTrace ~ endOfLine ~ error ~ endOfLine ~ notErrorTrace ^^ {
    case et ~ _ ~ e ~ _ ~ m => et.copy(error = e, message = m)
  }

  def errorTrace: Parser[ErrorTrace] = "Error Trace:" ~> ident ~ ":" ~ decimalNumber ^^ {
    case file ~ _ ~ line => ErrorTrace(file, line.toInt, null, null)
  }

  def error: Parser[String] = "Error:" ~> "[^ ]([^ :]| )*".r <~ ":"

  def notErrorTrace = ".+".r ^? { case line if !line.contains(fail) && !line.contains(pass) => line }

  def emptyLine = ""

  def endOfLine = "\n"

  def parse(source: java.io.Reader): List[Entity] = parseAll(failingAsserts, source) match {
    case Success(expression, _) => expression
    case NoSuccess(err, next) => throw new IllegalArgumentException("failed to parse " +
      "(line " + next.pos.line + ", column " + next.pos.column + "):\n" +
      err + "\n" + next.pos.longString)
  }

  def parse(source: String): List[Entity] = parseAll(failingAsserts, source) match {
    case Success(expression, _) => expression
    case f: NoSuccess => throw new IllegalArgumentException(f.msg)
  }

  def parseChunk[T](p: Parser[T], in: java.lang.CharSequence): T = parseAll(p, in) match {
    case Success(expression, _) => expression
    case f: NoSuccess => throw new IllegalArgumentException(f.msg)
  }
}
