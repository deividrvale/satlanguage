package language.parser;

import java.util.ArrayList;

/**
 * An AntlrParserException is a collection of error messages generated by lexing or parsing ome
 * user input into a parse tree.
 */
public class AntlrParserException extends ParserException {
  private int _errorCount;

  private static String collectErrors(ArrayList<String> messages) {
    String str = "";
    for (int i = 0; i < messages.size() && i < 10; i++) {
      str += messages.get(i) + "\n";
    }
    if (messages.size() > 10) str += "[Truncated; " + messages.size() + " more.]";
    return str;
  }

  public AntlrParserException(ArrayList<String> messages) {
    super(null, collectErrors(messages));
    _errorCount = messages.size();
  }

  public int getTotalErrorCount() {
    return _errorCount;
  }
}

