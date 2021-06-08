package de.ukaachen.gulinav.rest;

import de.ukaachen.gpm.core.GuidelineProcessorRepository;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Main {

  @Autowired
  private GuidelineProcessorRepository processorRepository;

  public static void main(final String[] args) {
    SpringApplication.run(Main.class, args);
  }

  @Bean
  public CommandLineRunner run(final ApplicationContext ctx) {
    return args -> {
      processorRepository.addGuideline(
          "demo", getClass().getResourceAsStream("/de/ukaachen/gpm/core/model/demo.bpmn"));
    };
  }

}
