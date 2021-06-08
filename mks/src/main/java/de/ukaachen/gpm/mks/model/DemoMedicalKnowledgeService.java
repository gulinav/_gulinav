package de.ukaachen.gpm.mks.model;

import de.ukaachen.gpm.mks.mk_modules.FeverModule;
import de.ukaachen.gpm.mks.mk_modules.IndependentBloodSampleModule;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public class DemoMedicalKnowledgeService {

  @Getter
  private final Map<String, MedicalKnowledgeModule<TernaryEvaluationResponse>> modules;


  public DemoMedicalKnowledgeService() {
    this.modules = new HashMap<>();
    this.modules.put("FEVER", new FeverModule());
    this.modules.put("SUFFICIENTLY_INDEPENDENT", new IndependentBloodSampleModule());
  }
}
