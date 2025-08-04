package com.lmntal.zx.importer;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImporterErrorListener extends BaseErrorListener {
  private final List<String> errors = new ArrayList<>();

  @Override
  public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
      String msg, RecognitionException e) {
    errors.add(String.format("line %d:%d %s", line, charPositionInLine, msg));
  }

  public List<String> getErrors() {
    return Collections.unmodifiableList(errors);
  }
}
