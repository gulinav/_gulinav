package de.ukaachen.gpm.mks.model;

public interface MedicalKnowledgeModule<T> {
  T evaluate(final MedicalKnowledgeContext context);
}
