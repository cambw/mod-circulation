package org.folio.circulation.rules.cache;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.folio.circulation.support.results.Result.ofAsync;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.concurrent.CompletableFuture;

import org.folio.circulation.rules.Drools;
import org.folio.circulation.rules.Text2Drools;
import org.folio.circulation.support.CollectionResourceClient;
import org.folio.circulation.support.results.Result;
import org.slf4j.Logger;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.vertx.core.json.JsonObject;

public final class CirculationRulesCache {
  private static final Logger log = getLogger(CirculationRulesCache.class);
  private static final CirculationRulesCache instance = new CirculationRulesCache();
  /** after this time the rules get loaded before executing the circulation rules engine */
  private static final long MAX_AGE_IN_MILLISECONDS = 5000;
  private final AsyncCache<String, Result<Rules>> rulesCache;

  public static CirculationRulesCache getInstance() {
    return instance;
  }

  private CirculationRulesCache() {
    rulesCache = Caffeine.newBuilder()
      .expireAfterWrite(MAX_AGE_IN_MILLISECONDS, MILLISECONDS)
      .buildAsync();
  }

  /**
   * Completely drop the cache. This enforces rebuilding the drools rules
   * even when the circulation rules haven't changed.
   */
  public void dropCache() {
    rulesCache.synchronous().cleanUp();
  }

  /**
   * Enforce reload of the tenant's circulation rules.
   * This doesn't rebuild the drools rules if the circulation rules haven't changed.
   * @param tenantId  id of the tenant
   */
  public void clearCache(String tenantId) {
    rulesCache.synchronous().invalidate(tenantId);
  }

  private CompletableFuture<Result<Rules>> loadRules(
    CollectionResourceClient circulationRulesClient) {

    return circulationRulesClient.get()
      .thenCompose(r -> r.after(response -> {
        Rules rules = new Rules();

        JsonObject circulationRules = new JsonObject(response.getBody());

        if (log.isDebugEnabled()) {
          log.debug("circulationRules = {}", circulationRules.encodePrettily());
        }

        String rulesAsText = circulationRules.getString("rulesAsText");

        if (rulesAsText == null) {
          throw new NullPointerException("rulesAsText");
        }

        rules.rulesAsText = rulesAsText;
        rules.rulesAsDrools = Text2Drools.convert(rules.rulesAsText);
        log.debug("rulesAsDrools = {}", rules.rulesAsDrools);
        rules.drools = new Drools(rules.rulesAsDrools);

        return ofAsync(() -> rules);
      }));
  }

  public CompletableFuture<Result<Drools>> getDrools(String tenantId,
    CollectionResourceClient circulationRulesClient) {

    return rulesCache.get(tenantId, (t, e) -> loadRules(circulationRulesClient))
      .thenCompose(r -> r.after(updatedRules -> ofAsync(() -> updatedRules.drools)));
  }

  private static class Rules {
    private volatile String rulesAsText = "";
    private volatile String rulesAsDrools = "";
    private volatile Drools drools;
  }
}
