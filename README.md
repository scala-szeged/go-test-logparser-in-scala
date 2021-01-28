# go test logparser in scala

It parses the output of `go test` and gives back the list of errors in the orignal order, groupped by file and grouped by message.

Please see the main function doing the above mentioned analisys.
```scala
  def main(args: Array[String]): Unit = {

    val defaultFile = "src/test/resources/fileman-go-test.log"
    val file = args.headOption.getOrElse(defaultFile)
    val input = Source.fromFile(file).bufferedReader()
    val result = parse(input)

    result foreach println

    println
    println(s"Number of failing asserts: ${result.size}")

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
```

---

Also see the simplicity of the grammar below. It parses all the information except the name of the tests. The grammar
below is mostly in Backus Naur Form, BNF for short.

If you are curious about the parsing of the name of the tests, please see the
actual [implementation](src/main/scala/log/parser/go/GoTestLogParser.scala).

```scala
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
```
