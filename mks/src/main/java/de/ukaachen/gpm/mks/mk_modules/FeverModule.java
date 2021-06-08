package de.ukaachen.gpm.mks.mk_modules;

import de.ukaachen.gpm.mks.model.MedicalKnowledgeContext;
import de.ukaachen.gpm.mks.model.MedicalKnowledgeModule;
import de.ukaachen.gpm.mks.model.PatientProperty;
import de.ukaachen.gpm.mks.model.TernaryEvaluationResponse;
import java.time.Duration;
import java.util.List;

/**
 * Demo Module that pretends to determine whether a person has fever is as easy as a threshold
 * comparison of the body temperature (which is, of course, not true).
 */
public class FeverModule implements MedicalKnowledgeModule<TernaryEvaluationResponse> {

  private static final String BODY_TEMPERATURE = "LOINC:8310-5";

  @Override
  public TernaryEvaluationResponse evaluate(final MedicalKnowledgeContext context) {
    List<PatientProperty> bodyTemps = context.provideValue(BODY_TEMPERATURE, Duration.ofDays(3));
    final double maxTemp = bodyTemps.stream()
        .map(PatientProperty::getValue)
        .mapToDouble(temp -> {
          if (!(temp instanceof Double)) {
            if (temp instanceof String) {
              try {
                return Double.parseDouble((String)temp);
              } catch(Exception e) {
                throw new IllegalArgumentException("Given context provides non-double values as body "
                    + "temperatures");
              }
            }
          } else {
            return (Double) temp;
          }
          // Cannot happen, but the compiler needs it
          throw new AssertionError();
        })
        .max().orElse(Double.NaN);

    if (Double.isNaN(maxTemp)) {
      return null;
    }
    if (maxTemp < 37.4d) {
      return TernaryEvaluationResponse.FALSE;
    } else {
      return TernaryEvaluationResponse.TRUE;
    }
  }
}
