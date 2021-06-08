package de.ukaachen.gulinav.rest;

import de.ukaachen.gpm.core.GuidelineProcessorRepository;
import de.ukaachen.gpm.core.model.patient.GuidelineItem;
import de.ukaachen.gpm.core.model.patient.PatientEvaluator;
import de.ukaachen.gpm.core.model.patient.SimplePatient;
import de.ukaachen.gpm.core.model.patient_evaluators.MedicalKnowledgeModuleEvaluator;
import de.ukaachen.gpm.mks.model.Code;
import de.ukaachen.gpm.mks.model.PatientProperty;
import de.ukaachen.gulinav.rest.model.EvaluationPayload;
import de.ukaachen.gulinav.rest.model.GuidelineEvaluationResponse;
import de.ukaachen.gulinav.rest.model.GuidelineItemPayload;
import de.ukaachen.gulinav.rest.model.GuidelinePayload;
import de.ukaachen.gulinav.rest.model.PatientPayload;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration
@RequestMapping("/rest")
public class GuidelineEvaluator {

  @Autowired
  private GuidelineProcessorRepository guidelineProcessorRepository;


  @RequestMapping(value = "/getDemoGuideline", method = RequestMethod.GET, produces = "text/xml")
  String getDemoGuideline() {
    return guidelineProcessorRepository.getOriginalGuideline("demo");
  }

  @RequestMapping(value = "/guideline", method = RequestMethod.POST)
  String guideline(@RequestBody final GuidelinePayload guidelinePayload) {

    final InputStream guidelineStream =
        new ByteArrayInputStream(guidelinePayload.getGuideline().getBytes(StandardCharsets.UTF_8));
    guidelineProcessorRepository.addGuideline(guidelinePayload.getId(), guidelineStream);

    return String.format("Guideline that was provided %s", guidelinePayload.getGuideline());
  }

  @RequestMapping(value = "/evaluatePatient", method = RequestMethod.POST)
  GuidelineEvaluationResponse evaluatePatient(
      @RequestBody final EvaluationPayload evaluationPayload) {
    final String guidelineId = evaluationPayload.getGuidelineId();
    final SimplePatient simplePatient = this.createSimplePatient(evaluationPayload);

    final List<GuidelineItem> guidelineItems = guidelineProcessorRepository
        .process(guidelineId, simplePatient);

    final List<GuidelineItemPayload> resultItems = guidelineItems
        .stream()
        .map(GuidelineItemPayload::new)
        .collect(Collectors.toList());

    return new GuidelineEvaluationResponse(resultItems);
  }

  private SimplePatient createSimplePatient(final EvaluationPayload payload) {
    final SimplePatient result = new SimplePatient();

    final Map<String, Code> codeMapping = this.guidelineProcessorRepository
        .getCodeMappingByGPMProcessor().get(payload.getGuidelineId());
    if (codeMapping == null) {
      throw new IllegalArgumentException(String.format("Unknown guideline: %s",
          payload.getGuidelineId()));
    }
    final Map<Code, String> revCodeMapping = new HashMap<>();

    codeMapping.keySet().forEach(key -> revCodeMapping.put(codeMapping.get(key), key));

    if(payload.getPatientPayload() == null) {
      throw new IllegalArgumentException("No patientPayload provided in request.");
    }

    for (final PatientPayload patientData : payload.getPatientPayload()) {
      if(patientData.getCode() == null) {
        throw new IllegalArgumentException("Malformed payload provided in request. No code "
            + "provided for at least one payload item.");
      }
      final Code theCode = new Code(patientData.getCode());
      String hr = revCodeMapping.get(theCode);
      if (hr == null) {
        hr = UUID.randomUUID().toString();
      }
      final List<PatientProperty> patientProperties = patientData.getValues()
          .stream()
          .map(val -> PatientProperty.withValue(val.getValue()).withTimestamp(val.getTime())
              .create())
          .collect(Collectors.toList());

      result.addProperties(hr, theCode, patientProperties);
    }

    final PatientEvaluator mkmEvaluator = new MedicalKnowledgeModuleEvaluator();
    result.getCodeSystemToEvaluator().put("MKM", mkmEvaluator);

    return result;
  }
}
