package org.folio.circulation.domain;

import static java.util.Objects.requireNonNull;
import static org.folio.circulation.support.json.JsonObjectArrayPropertyFetcher.mapToList;
import static org.folio.circulation.support.json.JsonPropertyFetcher.getBooleanProperty;
import static org.folio.circulation.support.json.JsonPropertyFetcher.getLocalDateProperty;
import static org.folio.circulation.support.json.JsonPropertyWriter.write;
import static org.folio.circulation.support.utils.DateTimeUtil.formatDateTimeOptional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collector;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class OpeningDay {
  public static OpeningDay createClosedDay() {
    return createOpeningDay(Collections.emptyList(), null, true, false);
  }

  private static final String DATE_KEY = "date";
  private static final String ALL_DAY_KEY = "allDay";
  private static final String OPEN_KEY = "open";
  private static final String OPENING_HOUR_KEY = "openingHour";
  private static final String OPENING_DAY_KEY = "openingDay";

  private final List<OpeningHour> openingHour;
  private final LocalDate date;
  private final boolean allDay;
  private final boolean open;
  private final ZoneOffset zone;

  public static OpeningDay fromJsonByDefaultKey(JsonObject jsonObject) {
    JsonObject openingDayJson = jsonObject.getJsonObject(OPENING_DAY_KEY);

    requireNonNull(openingDayJson, "Json object cannot be null");

    return new OpeningDay(fillOpeningDay(openingDayJson),
      getLocalDateProperty(openingDayJson, DATE_KEY),
      getBooleanProperty(openingDayJson, ALL_DAY_KEY),
      getBooleanProperty(openingDayJson, OPEN_KEY), null);
  }

  public static OpeningDay fromOpeningPeriodJson(JsonObject openingPeriod, ZoneOffset zone) {
    JsonObject openingDayJson = openingPeriod.getJsonObject(OPENING_DAY_KEY);

    return createOpeningDay(fillOpeningDay(openingDayJson),
      getLocalDateProperty(openingDayJson, DATE_KEY),
      getBooleanProperty(openingDayJson, ALL_DAY_KEY),
      getBooleanProperty(openingDayJson, OPEN_KEY), zone);
  }

  public static OpeningDay createOpeningDay(List<OpeningHour> openingHour,
    LocalDate date, boolean allDay, boolean open) {

    return new OpeningDay(openingHour, date, allDay, open, null);
  }

  public static OpeningDay createOpeningDay(List<OpeningHour> openingHour,
    LocalDate date, boolean allDay, boolean open, ZoneOffset zone) {

    return new OpeningDay(openingHour, date, allDay, open, zone);
  }

  public OpeningDay(List<OpeningHour> openingHour, LocalDate date,
    boolean allDay, boolean open, ZoneOffset zone) {

    this.openingHour = openingHour;
    this.allDay = allDay;
    this.open = open;

    if (zone != null && zone != ZoneOffset.UTC) {
      this.date = ZonedDateTime.of(date, LocalTime.MIDNIGHT, zone)
        .withZoneSameInstant(ZoneOffset.UTC).toLocalDate();
      this.zone = zone;
    }
    else {
      this.date = date;
      this.zone = ZoneOffset.UTC;
    }
  }

  public LocalDate getDate() {
    return date;
  }

  public boolean getAllDay() {
    return allDay;
  }

  public boolean getOpen() {
    return open;
  }

  public List<OpeningHour> getOpeningHour() {
    return openingHour;
  }

  public ZoneOffset getZone() {
    return zone;
  }

  private JsonArray openingHourToJsonArray() {
    return openingHour.stream()
      .map(OpeningHour::toJson)
      .collect(Collector.of(JsonArray::new, JsonArray::add, JsonArray::add));
  }

  public JsonObject toJson() {
    final JsonObject json = new JsonObject();

    write(json, DATE_KEY, formatDateTimeOptional(
      ZonedDateTime.of(date, LocalTime.MIDNIGHT, ZoneOffset.UTC)));
    write(json, ALL_DAY_KEY, allDay);
    write(json, OPEN_KEY, open);
    write(json, OPENING_HOUR_KEY, openingHourToJsonArray());

    return json;
  }

  private static List<OpeningHour> fillOpeningDay(JsonObject representation) {
    return mapToList(representation, OPENING_HOUR_KEY, OpeningHour::new);
  }
}
