package org.folio.circulation.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.UUID;

import org.folio.circulation.support.ClockManager;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import api.support.builders.ProxyRelationshipBuilder;

public class ProxyRelationshipTests {

  @ParameterizedTest
  @ValueSource(strings = {
    "false",
    "true"
  })
  public void shouldBeActiveWhenActiveAndDoesNotExpire(boolean useMetaObject) {
    final ProxyRelationship relationship = new ProxyRelationship(
      new ProxyRelationshipBuilder()
        .proxy(UUID.randomUUID())
        .sponsor(UUID.randomUUID())
        .active()
        .doesNotExpire()
        .useMetaObject(useMetaObject)
        .create());

    assertThat(relationship.isActive(), is(true));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "false",
    "true"
  })
  public void shouldBeActiveWhenActiveAndNotExpired(boolean useMetaObject) {
    final ProxyRelationship relationship = new ProxyRelationship(
      new ProxyRelationshipBuilder()
        .proxy(UUID.randomUUID())
        .sponsor(UUID.randomUUID())
        .active()
        .expires(ClockManager.getZonedDateTime().plusWeeks(3))
        .useMetaObject(useMetaObject)
        .create());

    assertThat(relationship.isActive(), is(true));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "false",
    "true"
  })
  public void shouldBeInactiveWhenActiveAndExpired(boolean useMetaObject) {
    final ProxyRelationship relationship = new ProxyRelationship(
      new ProxyRelationshipBuilder()
        .proxy(UUID.randomUUID())
        .sponsor(UUID.randomUUID())
        .active()
        .expires(ClockManager.getZonedDateTime().minusMonths(2))
        .useMetaObject(useMetaObject)
        .create());

    assertThat(relationship.isActive(), is(false));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "false",
    "true"
  })
  public void shouldBeInactiveWhenInactiveAndDoesNotExpire(boolean useMetaObject) {
    final ProxyRelationship relationship = new ProxyRelationship(
      new ProxyRelationshipBuilder()
        .proxy(UUID.randomUUID())
        .sponsor(UUID.randomUUID())
        .inactive()
        .doesNotExpire()
        .useMetaObject(useMetaObject)
        .create());

    assertThat(relationship.isActive(), is(false));
  }


  @ParameterizedTest
  @ValueSource(strings = {
    "false",
    "true"
  })
  public void shouldBeInactiveWhenInactiveAndNotExpired(boolean useMetaObject) {
    final ProxyRelationship relationship = new ProxyRelationship(
      new ProxyRelationshipBuilder()
        .proxy(UUID.randomUUID())
        .sponsor(UUID.randomUUID())
        .inactive()
        .expires(ClockManager.getZonedDateTime().plusWeeks(3))
        .useMetaObject(useMetaObject)
        .create());

    assertThat(relationship.isActive(), is(false));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "false",
    "true"
  })
  public void shouldBeInactiveWhenInactiveAndExpired(boolean useMetaObject) {
    final ProxyRelationship relationship = new ProxyRelationship(
      new ProxyRelationshipBuilder()
        .proxy(UUID.randomUUID())
        .sponsor(UUID.randomUUID())
        .inactive()
        .expires(ClockManager.getZonedDateTime().minusMonths(2))
        .useMetaObject(useMetaObject)
        .create());

    assertThat(relationship.isActive(), is(false));
  }
}
