import org.junit.Test;
import static org.junit.Assert.*;

import logic.VariableList;
import language.execution.Statement;
import language.execution.If;
import language.parser.InputReader;
import language.parser.ParserException;

public class ParseStatementTest {
  @Test
  public void testReadPrint() throws ParserException {
    VariableList vars = new VariableList();
    Statement stat = InputReader.readStatement("print(\"Hello world!\")", vars);
    assertTrue(stat.toString().equals("print(\"Hello world!\")"));
  }

  @Test
  public void testReadEmptyPrintln() throws ParserException {
    VariableList vars = new VariableList();
    Statement stat = InputReader.readStatement("println()", vars);
    assertTrue(stat.toString().equals("print(\"\\n\")"));
  }

  @Test
  public void testReadMultiPrint() throws ParserException {
    VariableList vars = new VariableList();
    Statement stat = InputReader.readStatement("println(\"bing\", i, 1)", vars);
    assertTrue(stat.toString().equals("print(\"bing\", i, 1, \"\\n\")"));
  }

  @Test
  public void testSimpleIf() throws ParserException {
    VariableList vars = new VariableList();
    Statement stat = InputReader.readStatement("if i > 0 then println()", vars);
    assertTrue(stat.toString().equals("if 0 < i then print(\"\\n\")"));
  }

  @Test
  public void testIfOrder() throws ParserException {
    VariableList vars = new VariableList();
    Statement stat = InputReader.readStatement(
      "if i > 0 then if j != 0 then print(i) else print(j)", vars);
    Statement t = ((If)stat).queryThenStatement();
    Statement e = ((If)stat).queryElseStatement();
    assertTrue(t.toString().equals("if j ≠ 0 then print(i)\nelse print(j)"));
    assertTrue(e == null);
  }

  @Test
  public void testIfThenElse() throws ParserException {
    VariableList vars = new VariableList();
    Statement stat = InputReader.readStatement("if i = j then println() else print(\"x\")", vars);
    assertTrue(stat.toString().equals("if i = j then print(\"\\n\")\nelse print(\"x\")"));
  }

  @Test
  public void testFor() throws ParserException {
    VariableList vars = new VariableList();
    Statement stat = InputReader.readStatement("for i := 1 to k + 1 do println(i)", vars);
    assertTrue(stat.toString().equals("for i := 1 to k+1 do print(i, \"\\n\")"));
  }

  @Test
  public void testBlock() throws ParserException {
    VariableList vars = new VariableList();
    Statement stat = InputReader.readStatement(
      "{ print(i) print(j) if i < 0 then {println() } else println() println(\"Hello\") }", vars);
    assertTrue(stat.toString().equals("{\n" +
      "  print(i)\n" +
      "  print(j)\n" +
      "  if i < 0 then {\n" +
      "    print(\"\\n\")\n" +
      "  }\n" +
      "  else print(\"\\n\")\n" +
      "  print(\"Hello\", \"\\n\")\n" +
      "}"));
  }

  @Test
  public void testComplex() throws ParserException {
    VariableList vars = new VariableList();
    InputReader.readDeclarationFromString("declare queen[x,y] :: Bool for x ∈ {1..8}, y ∈ {1..8}",
                                          vars);
    String txt =
      "for y := 1 to 8 do {\n" +
      "  for x := 1 to 8 do {\n" +
      "    if queen[x,y] then print(\"Q\")\n" +
      "    else print(\" \")\n" +
      "  }\n" +
      "  print(\"\\n\")\n" +
      "}";
    Statement stat = InputReader.readStatement(txt, vars);
    assertTrue(stat.toString().equals(txt));
  }
}

