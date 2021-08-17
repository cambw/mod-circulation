package org.folio.circulation.domain;

import static java.time.ZoneOffset.UTC;
import static org.folio.circulation.support.json.JsonPropertyWriter.write;
import static org.junit.Assert.assertEquals;

import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import api.support.builders.ConfigRecordBuilder;
import api.support.builders.ConfigurationBuilder;
import io.vertx.core.json.JsonObject;

public class ConfigurationServiceTest {

  private static final String US_LOCALE = "en-US";
  private static final String VALUE = "value";
  private static final Integer DEFAULT_TIMEOUT_CONFIGURATION = 3;

  private static ConfigurationService service;

  @BeforeClass
  public static void before() {
    service = new ConfigurationService();
  }

  @Test
  public void testUtcTimeZone() {
    String timeZoneValue = getTimezoneValue("Z");
    JsonObject jsonObject = getJsonObject(timeZoneValue);

    assertEquals(UTC, service.findDateTimeZone(jsonObject));
  }

  @Test
  public void testEuropeTimeZone() {
    String zone = "Europe/Kiev";
    String timeZoneValue = getTimezoneValue(zone);
    JsonObject jsonObject = getJsonObject(timeZoneValue);

    assertEquals(ZoneId.of(zone), service.findDateTimeZone(jsonObject));
  }

  @Test
  public void testEmptyTimeZoneValue() {
    String timeZoneValue = getTimezoneValue("");
    JsonObject jsonObject = getJsonObject(timeZoneValue);

    assertEquals(UTC, service.findDateTimeZone(jsonObject));
  }

  @Test
  public void testEmptyJsonValue() {
    JsonObject jsonObject = getJsonObject("");

    assertEquals(UTC, service.findDateTimeZone(jsonObject));
  }

  @Test
  public void testEmptyJson() {
    JsonObject jsonObject = new JsonObject();

    assertEquals(UTC, service.findDateTimeZone(jsonObject));
  }

  @Test
  public void shouldUseConfiguredCheckoutTimeoutDurationWhenAnInteger() {
    JsonObject jsonConfig = new JsonObject()
      .put(VALUE, getJsonConfigWithCheckoutTimeoutDurationAsInteger());
    List<Configuration> records = Collections.singletonList(new Configuration(jsonConfig));

    Integer actualSessionTimeout = service.findSessionTimeout(records);

    assertEquals(actualSessionTimeout, new Integer(1));
  }

  @Test
  public void shouldUseConfiguredCheckoutTimeoutDurationWhenIsAnIntegerString() {
    JsonObject jsonConfig = new JsonObject()
      .put(VALUE, getJsonConfigWithCheckoutTimeoutDurationAsString("1"));
    List<Configuration> records = Collections.singletonList(new Configuration(jsonConfig));

    Integer actualSessionTimeout = service.findSessionTimeout(records);

    assertEquals(actualSessionTimeout, new Integer(1));
  }

  @Test
  public void shouldUseDefaultCheckoutTimeoutDurationWhenConfiguredValueIsNotAnInteger() {
    JsonObject jsonConfig = new JsonObject()
      .put(VALUE, getJsonConfigWithCheckoutTimeoutDurationAsString("test"));
    List<Configuration> records = Collections.singletonList(new Configuration(jsonConfig));

    Integer actualSessionTimeout = service.findSessionTimeout(records);

    assertEquals(DEFAULT_TIMEOUT_CONFIGURATION, actualSessionTimeout);
  }

  private JsonObject getJsonObject(String timeZoneValue) {
    ConfigRecordBuilder config = new ConfigRecordBuilder(timeZoneValue);
    return new ConfigurationBuilder(Collections.singletonList(config)).create();
  }

  private String getTimezoneValue(String timezone) {
    final JsonObject encodedValue = new JsonObject();
    write(encodedValue, "locale", US_LOCALE);
    write(encodedValue, "timezone", timezone);
    return encodedValue.toString();
  }

  private String getJsonConfigWithCheckoutTimeoutDurationAsString(String checkoutTimeoutDuration) {
    final JsonObject encodedValue = new JsonObject();
    write(encodedValue, "checkoutTimeoutDuration", checkoutTimeoutDuration);
    write(encodedValue, "checkoutTimeout", true);
    return encodedValue.toString();
  }

  private String getJsonConfigWithCheckoutTimeoutDurationAsInteger() {
    final JsonObject encodedValue = new JsonObject();
    write(encodedValue, "checkoutTimeoutDuration", 1);
    write(encodedValue, "checkoutTimeout", true);
    return encodedValue.toString();
  }
}
