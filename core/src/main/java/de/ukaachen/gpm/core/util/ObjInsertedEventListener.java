package de.ukaachen.gpm.core.util;

import lombok.extern.log4j.Log4j2;
import org.kie.api.event.rule.DebugRuleRuntimeEventListener;
import org.kie.api.event.rule.ObjectDeletedEvent;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.event.rule.ObjectUpdatedEvent;

@Log4j2
public class ObjInsertedEventListener extends DebugRuleRuntimeEventListener {

  @Override
  public void objectInserted(final ObjectInsertedEvent event) {
    log.info("Object inserted: " + event.getObject());
  }

  @Override
  public void objectDeleted(final ObjectDeletedEvent event) {
    // No operation
  }

  @Override
  public void objectUpdated(final ObjectUpdatedEvent event) {
    // No operation
  }
}
