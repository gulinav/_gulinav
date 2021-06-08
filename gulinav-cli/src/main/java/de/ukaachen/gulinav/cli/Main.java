package de.ukaachen.gulinav.cli;

import de.ukaachen.gpm.core.GuidelineProcessorRepository;
import de.ukaachen.gpm.core.model.patient.GuidelineItem;
import de.ukaachen.gpm.core.model.patient.SimplePatient;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;


/**
 * This command-line version should be used use GuLiNav via the command-line.
 * It is not fully implemented, since it is, for now only intended as a minimal-working-example
 * of how to use GuLiNav's core module as a software library.
 */
public class Main {

  public static void main(final String[] args) throws ParseException, FileNotFoundException {
    final CommandLine cmd = CliUtil.PARSER.parse(CliUtil.OPTIONS, args);

    final String bpmnFile = cmd.getOptionValue("file");
    final InputStream bpmnIs = new FileInputStream(bpmnFile);

    // Create a new dummy repository. In the CLI version this would not be needed
    final GuidelineProcessorRepository repo = new GuidelineProcessorRepository();

    // Add the guideline to the repo under a random, yet known UUID
    final String randomUUID = UUID.randomUUID().toString();
    repo.addGuideline(randomUUID, bpmnIs);

    // TODO: Here we would need to read the patient-data from the command line or define another way
    //  to provide the patient-data to the processor. For now, we pretend we know nothing about the
    //  patient (thus usually the very first step of the guideline should be active etc.)
    final SimplePatient patientData = new SimplePatient();

    // Process the guideline
    final List<GuidelineItem> contextualizedGuideline = repo.process(randomUUID, patientData);


    // Print the result
    System.out.println("The resulting guideline-items are: ");
    System.out.println("#########################################################################");
    contextualizedGuideline.forEach(System.out::println);
  }
}
