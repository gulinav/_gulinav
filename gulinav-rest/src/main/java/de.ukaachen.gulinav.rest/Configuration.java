package de.ukaachen.gulinav.rest;

import de.ukaachen.gpm.core.GuidelineProcessorRepository;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class Configuration {

  @Bean
  public GuidelineProcessorRepository guidelineProcessorRepository() {
    return new GuidelineProcessorRepository();
  }
}
