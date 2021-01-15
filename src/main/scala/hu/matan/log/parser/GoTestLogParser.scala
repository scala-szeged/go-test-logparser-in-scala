package hu.matan.log.parser

import scala.collection.immutable.List
import scala.io.Source
import scala.util.parsing.combinator.JavaTokenParsers

object GoTestLogParser extends JavaTokenParsers {

  def main(args: Array[String]): Unit = {

    val defaultFile = "src/test/resources/fileman-go-test.log"
    val file = args.headOption.getOrElse(defaultFile)
    val input = Source.fromFile(file).bufferedReader()
    val result = parse(input)

    result foreach println

    println
    println(result.size)

    println
    result.groupBy(_.file).map {
      case (file, list) => (file, list.map(_.line))
    }.foreach(println)

    println
    result.filterNot(_.message.startsWith("expected")).groupBy(_.message).map {
      case (message, list) => (message, list.map(et => s"${et.file}.go:${et.line}"))
    }.toList.sortBy(_._1).reverse.foreach {
      case (message, fileLineList) =>
        println
        println(message)
        fileLineList.distinct.foreach(println)
    }
  }

  override protected val whiteSpace = """[ \t]+""".r

  def fileContent = repsep(line, endOfLine) ^^ {
    list => list.collect { case et: ErrorTrace => et }
  }

  def line = errorWithTrace | notErrorTrace | emptyLine

  def errorWithTrace = errorTrace ~ endOfLine ~ error ~ endOfLine ~ notErrorTrace ^^ {
    case et ~ _ ~ e ~ _ ~ m => et.copy(error = e, message = m)
  }

  def errorTrace = "Error Trace:" ~> ident ~ ".go:" ~ decimalNumber ^^ {
    case file ~ _ ~ line => ErrorTrace(file, line.toInt, null, null)
  }

  def error = "Error:" ~> "[^ ]([^ :]| )*".r <~ ":"


  def notErrorTrace = ".+".r

  def emptyLine = ""

  def endOfLine = "\n"

  def parse(source: java.io.Reader): List[ErrorTrace] = parseAll(fileContent, source) match {
    case Success(expression, _) => expression
    case NoSuccess(err, next) => throw new IllegalArgumentException("failed to parse " +
      "(line " + next.pos.line + ", column " + next.pos.column + "):\n" +
      err + "\n" + next.pos.longString)
  }

  def parseChunk[T](p: Parser[T], in: java.lang.CharSequence): T = parseAll(p, in) match {
    case Success(expression, _) => expression
    case f: NoSuccess => throw new IllegalArgumentException(f.msg)
  }
}

case class ErrorTrace(file: String, line: Int, error: String, message: String)