package de.ukaachen.gulinav.cli;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

public class CliUtil {

  public static final Options OPTIONS = new Options();
  public static final DefaultParser PARSER = new DefaultParser();

  static {
    OPTIONS.addOption("f", "file", true, "The path to the guideline model (BPMN file).");
  }
}
