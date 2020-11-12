package org.folio.circulation.support.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.stream.Stream;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;

public class DateTimeUtil {
  private DateTimeUtil() {
    throw new UnsupportedOperationException("Do not instantiate");
  }

  public static ZonedDateTime atEndOfTheDay(ZonedDateTime dateTime) {
    return dateTime
      .withHour(23)
      .withMinute(59)
      .withSecond(59);
  }

  public static DateTime atEndOfTheDay(DateTime dateTime) {
    return dateTime.withTime(23, 59, 59, 0);
  }

  public static ZonedDateTime toJavaDateTime(DateTime dateTime) {
    return Instant.ofEpochMilli(dateTime.getMillis())
      .atZone(ZoneId.of(dateTime.getZone().getID()));
  }

  public static DateTime mostRecentDate(DateTime... dates) {
    return Stream.of(dates)
      .filter(Objects::nonNull)
      .max(DateTime::compareTo)
      .orElse(null);
  }

  public static LocalDate jodaToJavaLocalDate(org.joda.time.LocalDate date) {
    if (date == null) {
      return null;
    }

    return LocalDate.of(date.year().get(), date.monthOfYear().get(), date.dayOfMonth().get());
  }

  public static org.joda.time.LocalDate javaToJodaLocalDate(LocalDate date) {
    if (date == null) {
      return null;
    }

    return new org.joda.time.LocalDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
  }

  public static org.joda.time.DateTime javaToJodaDateTime(LocalDate date, LocalTime time,
    DateTimeZone zone) {

    if (date == null) {
      return null;
    }

    return javaToJodaLocalDate(date).toDateTime(time, zone);
  }

  public static org.joda.time.LocalTime endOfDay() {
    return new LocalTime(23, 59, 59);
  }
}
