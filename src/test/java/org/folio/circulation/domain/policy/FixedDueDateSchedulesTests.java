package org.folio.circulation.domain.policy;

import io.vertx.core.json.JsonObject;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class FixedDueDateSchedulesTests {

  @Test
  public void shouldHaveNoSchedulesWhenPropertyMissingInJSON() {
    final FixedDueDateSchedules schedules = FixedDueDateSchedules.from(new JsonObject());

    assertThat(schedules.isEmpty(), is(true));
  }
}
