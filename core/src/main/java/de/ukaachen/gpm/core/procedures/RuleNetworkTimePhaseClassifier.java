package de.ukaachen.gpm.core.procedures;


import de.ukaachen.gpm.core.model.patient.GPMPatient;
import de.ukaachen.gpm.core.model.patient.TimePhase;
import de.ukaachen.gpm.core.model.rule_network.Vertex;
import de.ukaachen.gpm.core.model.rule_network.VertexType;
import de.ukaachen.gpm.core.model.rule_network.inferences.VertexTimePhase;
import de.ukaachen.gpm.core.model.rule_network.initial_facts.patient_facts.PatientFacts;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.joda.time.Duration;
import org.kie.api.runtime.KieSession;

@Log4j2
public class RuleNetworkTimePhaseClassifier {

  private final KieSessionProvider kieSessionProvider;
  private final DroolsFactsHandler droolsFactsHandler;
  private final Map<String, Vertex> idToVertex;

  public RuleNetworkTimePhaseClassifier(
      final KieSessionProvider kieSessionProvider,
      final DroolsFactsHandler droolsFactsHandler) {
    this.kieSessionProvider = kieSessionProvider;
    this.droolsFactsHandler = droolsFactsHandler;

    this.idToVertex = new HashMap<>();
    this.droolsFactsHandler.getRuleNetwork().getVertices()
        .forEach(v -> idToVertex.put(v.getId(), v));
  }


  Map<Vertex, TimePhase> classify(final GPMPatient patient) {
    final long startTime = System.currentTimeMillis();

    final long before = System.currentTimeMillis();
    final KieSession kSession = kieSessionProvider.provideKieSession();
    final long after = System.currentTimeMillis();
    final Duration duration = new Duration(before, after);
    log.info("Obtaining kieSession took " + duration.getMillis() + " milliseconds.");

    final PatientFacts patientFacts = droolsFactsHandler.createPatientFacts(patient);

    patientFacts.getFinishedVertices().forEach(kSession::insert);
    patientFacts.getSatisfiedConditions().forEach(kSession::insert);
    patientFacts.getPotentiallySatisfiedConditions().forEach(kSession::insert);
    final long ruleStartTime = System.currentTimeMillis();
    kSession.fireAllRules();
    final long ruleEndTime = System.currentTimeMillis();
    final long ruleDuration = ruleEndTime - ruleStartTime;
    log.info("Executing Drools rules took " + ruleDuration + " milliseconds.");

    final Map<Vertex, TimePhase> result = new HashMap<>();
    kSession.getFactHandles(object -> object instanceof VertexTimePhase)
        .stream()
        .map(kSession::getObject)
        .map(obj -> (VertexTimePhase) obj)
        .map(vtp -> {
          if (vtp.getVertex().getType() != VertexType.ACTIVITY) {
            return new VertexTimePhase(vtp.getVertex(), TimePhase.NOT_CLASSIFIABLE);
          } else {
            return vtp;
          }
        })
        .forEach(vtp -> result.put(idToVertex.get(vtp.getVertex().getId()), vtp.getTimePhase()));

    kSession.dispose();

    final long endTime = System.currentTimeMillis();
    final long classificationDurationInMillis = new Duration(startTime, endTime).getMillis();
    log.info("Classification took " + classificationDurationInMillis + "milliseconds");
    return result;
  }
}
