package de.ukaachen.gpm.core.procedures;

import de.ukaachen.gpm.core.model.rule_network.initial_facts.network_facts.NetworkFacts;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.Getter;
import org.kie.api.event.rule.DebugRuleRuntimeEventListener;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieSession;
import org.kie.internal.conf.ConstraintJittingThresholdOption;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.utils.KieHelper;

/**
 * Initializing a KieSession takes some time (depending on which facts need to be inserted around
 * 100-150ms). Since we cannot re-use KieSessions because we need a freshly initialized state with
 * a different knowledge base every time, we need to initialize a fresh KieSession for every
 * request.
 * In order to not spend 100-150ms for initializing a KieSession every time, we eagerly
 * initialize them within this provider. And using this provider, a freshly initialized KieSession
 * with the networkFacts already inserted can be obtained in no time.
 */
public class KieSessionProvider {

  private final DebugRuleRuntimeEventListener debugRuleRuntimeEventListener;

  @Getter
  private final NetworkFacts networkFacts;
  private final BlockingQueue<KieSession> initializedKieSessions;

  private static final int NO_OF_CACHED_KIE_SESSIONS = 10;


  public KieSessionProvider(NetworkFacts networkFacts,
      final DebugRuleRuntimeEventListener debugRuleRuntimeEventListener) {
    this.networkFacts = networkFacts;
    this.debugRuleRuntimeEventListener = debugRuleRuntimeEventListener;
    this.initializedKieSessions = new LinkedBlockingQueue<>(NO_OF_CACHED_KIE_SESSIONS);

    final Thread eagerInitializationThread = new Thread(() -> {
      while (true) {
        try {
          initializedKieSessions.put(createNewKieSession());
        } catch (final InterruptedException e) {
          e.printStackTrace();
        }
      }
    });

    eagerInitializationThread.setDaemon(true);
    eagerInitializationThread.setPriority(Thread.MIN_PRIORITY);
    eagerInitializationThread.start();
  }

  public KieSession provideKieSession() {
    final KieSession kSession;
    try {
      kSession = initializedKieSessions.take();
    } catch (final InterruptedException e) {
      // Should not happen, but if it does, just initialize a new kieSession instead of picking
      // One of the eagerly initialized ones..
      e.printStackTrace();
      return createNewKieSession();
    }

    return kSession;
  }

  private KieSession createNewKieSession() {
    final Resource resource = ResourceFactory.newClassPathResource(
        "de/ukaachen/gpm/core/procedures/graph-rules.drl");
    return createNewKieSessionForResource(networkFacts, resource);
  }

  private KieSession createNewKieSessionForResource(
      final NetworkFacts networkFacts,
      final Resource resource) {

    final KieHelper helper = new KieHelper();
    helper.addResource(resource);
    final KieSession kSession =
        helper.build(ConstraintJittingThresholdOption.get(-1)).newKieSession();

    networkFacts.getVertices().forEach(kSession::insert);
    networkFacts.getEdges().forEach(kSession::insert);

    if (debugRuleRuntimeEventListener != null) {
      kSession.addEventListener(debugRuleRuntimeEventListener);
    }

    return kSession;
  }
}
