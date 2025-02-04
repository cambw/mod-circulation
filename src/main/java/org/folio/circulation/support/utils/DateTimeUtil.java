package org.folio.circulation.support.utils;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.MILLIS;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.stream.Stream;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * A utility for centralizing common date time operations.
 * <p>
 * JavaTime tends to have granularity down to the nanoseconds but most of this
 * project expects granularity down to the milliseconds.
 * Many of the methods provided ensure precision is against milliseconds, often
 * truncated to the milliseconds.
 * Some methods may provide different precision as noted.
 * <p>
 * The zoned date time could have a higher precision than milliseconds.
 * This makes comparison to an ISO formatted date time using milliseconds
 * excessively precise and brittle
 * This was discovered when using JDK 13.0.1 instead of JDK 1.8.0_202-b08.
 * <p>
 * Be careful with the differences of withZoneSameInstant() vs
 * withZoneSameLocal().
 * <p>
 * The "SameInstant" version changes the time zone so that the representation
 * changes but the milliseconds since epoch does not change.
 * <p>
 * The "SameLocal" version changes the milliseconds since epoch and only changes
 * the time zone without changing any values.
 */
public class DateTimeUtil {
  private DateTimeUtil() {
    throw new UnsupportedOperationException("Do not instantiate");
  }

  /**
   * Given a dateTime, normalize it.
   * <p>
   * For compatibility with JodaTime, when value is null, then a now() call
   * via ClockUtil is used.
   *
   * @param dateTime The dateTime to normalize.
   * @return The provided dateTime or if dateTime is null then ClockUtil.getZonedDateTime().
   */
  public static ZonedDateTime defaultToNow(ZonedDateTime dateTime) {
    if (dateTime == null) {
      return ClockUtil.getZonedDateTime();
    }

    return dateTime;
  }

  /**
   * Given an offset dateTime, normalize it.
   * <p>
   * For compatibility with JodaTime, when value is null, then a now() call
   * via ClockUtil is used.
   *
   * @param dateTime The dateTime to normalize.
   * @return The provided dateTime or if dateTime is null then ClockUtil.getZonedDateTime().
   */
  public static OffsetDateTime defaultToNow(OffsetDateTime dateTime) {
    if (dateTime == null) {
      return ClockUtil.getOffsetDateTime();
    }

    return dateTime;
  }

  /**
   * Given a local dateTime, normalize it.
   * <p>
   * For compatibility with JodaTime, when value is null, then a now() call
   * via ClockUtil is used.
   *
   * @param dateTime The dateTime to normalize.
   * @return The provided dateTime or if dateTime is null then ClockUtil.getZonedDateTime().
   */
  public static LocalDateTime defaultToNow(LocalDateTime dateTime) {
    if (dateTime == null) {
      return ClockUtil.getLocalDateTime();
    }

    return dateTime;
  }

  /**
   * Given a date, normalize it.
   * <p>
   * For compatibility with JodaTime, when value is null, then a now() call
   * via ClockUtil is used.
   *
   * @param date The date to normalize.
   * @return The provided date or if date is null then ClockUtil.getZonedDate().
   */
  public static LocalDate defaultToNow(LocalDate date) {
    if (date == null) {
      return ClockUtil.getLocalDate();
    }

    return date;
  }

  /**
   * Given a dateTime, normalize it.
   * <p>
   * For compatibility with JodaTime, when value is null, then a now() call
   * via ClockUtil is used.
   *
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   *
   * @param dateTime The dateTime to normalize.
   * @return The provided dateTime or if dateTime is null then ClockUtil.getZonedDateTime().
   */
  public static DateTime defaultToNow(DateTime dateTime) {
    if (dateTime == null) {
      return ClockUtil.getDateTime();
    }

    return dateTime;
  }

  /**
   * Given a dateTime, normalize it.
   * <p>
   * For compatibility with JodaTime, when value is null, then a now() call
   * via ClockUtil is used.
   *
   * @param dateTime The dateTime to normalize.
   * @return The provided dateTime or if dateTime is null then ClockUtil.getZonedDateTime().
   */
  public static Instant defaultToNow(Instant dateTime) {
    if (dateTime == null) {
      return ClockUtil.getZonedDateTime().toInstant();
    }

    return dateTime;
  }

  /**
   * Given a dateTime, normalize it.
   * <p>
   * For compatibility with JodaTime, when value is null, then a now() call
   * via ClockUtil is used.
   *
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   *
   * @param dateTime The dateTime to normalize.
   * @return The provided dateTime or if dateTime is null then ClockUtil.getZonedDateTime().
   */
  public static org.joda.time.LocalDateTime defaultToNow(org.joda.time.LocalDateTime dateTime) {
    if (dateTime == null) {
      return ClockUtil.getDateTime().toLocalDateTime();
    }

    return dateTime;
  }

  /**
   * A stub-like function for normalizing the LocalDate.
   * <p>
   * For compatibility with JodaTime, when value is null, then a now() call
   * via ClockUtil is used.
   *
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   */
  public static org.joda.time.LocalDate defaultToNow(org.joda.time.LocalDate date) {
    if (date == null) {
      return ClockUtil.getJodaLocalDate();
    }

    return date;
  }

  /**
   * Given a time, normalize it.
   * <p>
   * For compatibility with JodaTime, when value is null, then a now() call
   * via ClockUtil is used.
   *
   * @param time The time to normalize.
   * @return The provided date or if time is null then ClockUtil.getLocalTime().
   */
  public static LocalTime defaultToNow(LocalTime time) {
    if (time == null) {
      return ClockUtil.getLocalTime();
    }

    return time;
  }

  /**
   * Given a time, normalize it.
   * <p>
   * For compatibility with JodaTime, when value is null, then a now() call
   * via ClockUtil is used.
   *
   * @param time The time to normalize.
   * @return The provided date or if time is null then ClockUtil.getLocalTime().
   */
  public static org.joda.time.LocalTime defaultToNow(org.joda.time.LocalTime time) {
    if (time == null) {
      return ClockUtil.getJodaLocalTime();
    }

    return time;
  }

  /**
   * Given a dateTime, normalize it.
   * <p>
   * For compatibility with JodaTime, when value is null, then a now() call
   * via ClockUtil is used.
   *
   * @param dateTime The dateTime to normalize.
   * @return The provided dateTime or if dateTime is null then ClockUtil.getZonedDateTime().
   */
  public static org.joda.time.Instant defaultToNow(org.joda.time.Instant dateTime) {
    if (dateTime == null) {
      return org.joda.time.Instant
        .ofEpochMilli(ClockUtil.getZonedDateTime().toInstant().toEpochMilli());
    }

    return dateTime;
  }

  /**
   * Given a time zone id, normalize it.
   * <p>
   * For compatibility with JodaTime, when value is null, then a now() call
   * via ClockUtil is used.
   *
   * @param zone The time zone to normalize.
   * @return The provided time zone or if zone is null a default time zone.
   */
  public static ZoneId defaultToClockZone(ZoneId zone) {
    if (zone == null) {
      return ClockUtil.getZoneId();
    }

    return zone;
  }

  /**
   * Given a time zone offset, normalize it.
   * <p>
   * For compatibility with JodaTime, when value is null, then a now() call
   * via ClockUtil is used.
   *
   * @param zone The time zone to normalize.
   * @return The provided time zone or if zone is null a default time zone.
   */
  public static ZoneOffset defaultToClockZone(ZoneOffset zone) {
    if (zone == null) {
      return ClockUtil.getZoneOffset();
    }

    return zone;
  }

  /**
   * A stub-like function for normalizing the DateTimeZone.
   * <p>
   * The normalization is for making JavaTime backward compatible with
   * JodaTime behavior. Therefore, this does nothing.
   *
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   */
  public static DateTimeZone defaultToClockZone(DateTimeZone zone) {
    if (zone == null) {
      return ClockUtil.jodaTimezone();
    }

    return zone;
  }

  /**
   * Get the first second of the year.
   * <p>
   * This operates in the time zone specified by dateTime.
   * <p>
   * This will truncate to seconds.
   *
   * @param dateTime The dateTime to convert.
   * @return The converted dateTime.
   */
  public static ZonedDateTime atStartOfYear(ZonedDateTime dateTime) {
    return atStartOfYear(dateTime, dateTime.getZone());
  }

  /**
   * Get the first second of the year for the given time zone.
   * <p>
   * This will truncate to seconds.
   *
   * @param dateTime The dateTime to convert.
   * @param zone The time zone to use.
   * @return The converted dateTime.
   */
  public static ZonedDateTime atStartOfYear(ZonedDateTime dateTime, ZoneId zone) {
    return atStartOfDay(dateTime
      .withZoneSameInstant(zone)
      .withDayOfYear(1));
  }

  /**
   * Get the last second of the year.
   * <p>
   * This operates in the time zone specified by dateTime.
   * <p>
   * This will truncate to seconds.
   *
   * @param dateTime The dateTime to convert.
   * @return The converted dateTime.
   */
  public static ZonedDateTime atEndOfYear(ZonedDateTime dateTime) {
    return atEndOfYear(dateTime, dateTime.getZone());
  }

  /**
   * Get the last second of the year for the given time zone.
   * <p>
   * This will truncate to seconds.
   *
   * @param dateTime The dateTime to convert.
   * @param zone The time zone to use.
   * @return The converted dateTime.
   */
  public static ZonedDateTime atEndOfYear(ZonedDateTime dateTime, ZoneId zone) {
    return atEndOfDay(dateTime
      .withZoneSameInstant(zone)
      .withDayOfYear(1)
      .plusYears(1)
      .minusDays(1));
  }

  /**
   * Get the first second of the year for the given time zone.
   * <p>
   * This will truncate to seconds.
   *
   * @param LocalDate The localDate to convert.
   * @param zone The time zone to use.
   * @return The converted localDate.
   */
  public static ZonedDateTime atStartOfYear(LocalDate localDate, ZoneId zone) {
    return atStartOfDay(localDate.withDayOfYear(1), zone);
  }

  /**
   * Get the last second of the year for the given time zone.
   * <p>
   * This operates in the time zone specified by dateTime.
   * <p>
   * This will truncate to seconds.
   *
   * @param dateTime The localDate to convert.
   * @param zone The time zone to use.
   * @return The converted localDate.
   */
  public static ZonedDateTime atEndOfYear(LocalDate localDate, ZoneId zone) {
    return atEndOfDay(localDate
      .withDayOfYear(localDate
        .lengthOfYear()),
      zone);
  }

  /**
   * Get the first second of the year.
   * <p>
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   *
   * @param dateTime The dateTime to convert.
   * @return The converted dateTime.
   */
  public static DateTime atStartOfYear(DateTime dateTime) {
    return atStartOfYear(dateTime, dateTime.getZone());
  }

  /**
   * Get the first second of the year.
   * <p>
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   *
   * @param dateTime The dateTime to convert.
   * @param zone The time zone to use.
   * @return The converted dateTime.
   */
  public static DateTime atStartOfYear(DateTime dateTime, DateTimeZone zone) {
    final DateTime zonedDateTime = dateTime.withZone(zone);

    return atStartOfDay(zonedDateTime
      .withDayOfYear(dateTime
        .dayOfYear()
        .getMinimumValue()));
  }

  /**
   * Get the last second of the year.
   * <p>
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   *
   * @param dateTime The dateTime to convert.
   * @return The converted dateTime.
   */
  public static DateTime atEndOfYear(DateTime dateTime) {
    return atEndOfYear(dateTime, dateTime.getZone());
  }

  /**
   * Get the last second of the year.
   * <p>
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   *
   * @param dateTime The dateTime to convert.
   * @param zone The time zone to use.
   * @return The converted dateTime.
   */
  public static DateTime atEndOfYear(DateTime dateTime, DateTimeZone zone) {
    final DateTime zonedDateTime = dateTime.withZone(zone);

    return atEndOfDay(zonedDateTime
      .withDayOfYear(zonedDateTime
        .dayOfYear()
        .getMaximumValue()));
  }

  /**
   * Get the first second of the year.
   * <p>
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   *
   * @param LocalDate The localDate to convert.
   * @param zone The time zone to use.
   * @return The converted localDate.
   */
  public static DateTime atStartOfYear(org.joda.time.LocalDate localDate, DateTimeZone zone) {
    return atStartOfDay(localDate, zone)
      .withDayOfYear(localDate
        .dayOfYear()
        .getMinimumValue());
  }

  /**
   * Get the last second of the year.
   * <p>
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   *
   * @param dateTime The localDate to convert.
   * @param zone The time zone to use.
   * @return The converted localDate.
   */
  public static DateTime atEndOfYear(org.joda.time.LocalDate localDate, DateTimeZone zone) {
    return atEndOfDay(localDate, zone)
      .withDayOfYear(localDate
        .dayOfYear()
        .getMaximumValue());
  }

  /**
   * Get the first second of the month.
   * <p>
   * This operates in the time zone specified by dateTime.
   * <p>
   * This will truncate to seconds.
   *
   * @param dateTime The dateTime to convert.
   * @return The converted dateTime.
   */
  public static ZonedDateTime atStartOfMonth(ZonedDateTime dateTime) {
    return atStartOfMonth(dateTime, dateTime.getZone());
  }

  /**
   * Get the first second of the month for the given time zone.
   * <p>
   * This will truncate to seconds.
   *
   * @param dateTime The dateTime to convert.
   * @param zone The time zone to use.
   * @return The converted dateTime.
   */
  public static ZonedDateTime atStartOfMonth(ZonedDateTime dateTime, ZoneId zone) {
    return atStartOfDay(dateTime
      .withZoneSameInstant(zone)
      .withDayOfMonth(1));
  }

  /**
   * Get the last second of the month.
   * <p>
   * This operates in the time zone specified by dateTime.
   * <p>
   * This will truncate to seconds.
   *
   * @param dateTime The dateTime to convert.
   * @return The converted dateTime.
   */
  public static ZonedDateTime atEndOfMonth(ZonedDateTime dateTime) {
    return atEndOfMonth(dateTime, dateTime.getZone());
  }

  /**
   * Get the last second of the month for the given time zone.
   * <p>
   * This will truncate to seconds.
   *
   * @param dateTime The dateTime to convert.
   * @param zone The time zone to use.
   * @return The converted dateTime.
   */
  public static ZonedDateTime atEndOfMonth(ZonedDateTime dateTime, ZoneId zone) {
    return atEndOfDay(dateTime
      .withZoneSameInstant(zone)
      .withDayOfMonth(1)
      .plusMonths(1)
      .minusDays(1));
  }

  /**
   * Get the first second of the month for the given time zone.
   * <p>
   * This will truncate to seconds.
   *
   * @param LocalDate The localDate to convert.
   * @param zone The time zone to use.
   * @return The converted localDate.
   */
  public static ZonedDateTime atStartOfMonth(LocalDate localDate, ZoneId zone) {
    return atStartOfDay(localDate.withDayOfMonth(1), zone);
  }

  /**
   * Get the last second of the month for the given time zone.
   * <p>
   * This will truncate to seconds.
   *
   * @param dateTime The localDate to convert.
   * @param zone The time zone to use.
   * @return The converted localDate.
   */
  public static ZonedDateTime atEndOfMonth(LocalDate localDate, ZoneId zone) {
    return atEndOfDay(localDate
      .withDayOfMonth(localDate
        .lengthOfMonth()),
      zone);
  }

  /**
   * Get the first second of the month.
   * <p>
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   *
   * @param dateTime The dateTime to convert.
   * @return The converted dateTime.
   */
  public static DateTime atStartOfMonth(DateTime dateTime) {
    return atStartOfMonth(dateTime, dateTime.getZone());
  }

  /**
   * Get the first second of the month.
   * <p>
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   *
   * @param dateTime The dateTime to convert.
   * @param zone The time zone to use.
   * @return The converted dateTime.
   */
  public static DateTime atStartOfMonth(DateTime dateTime, DateTimeZone zone) {
    final DateTime zonedDateTme = dateTime.withZone(zone);

    return atStartOfDay(zonedDateTme
      .withDayOfMonth(zonedDateTme
        .dayOfMonth()
        .getMinimumValue()));
  }

  /**
   * Get the last second of the month.
   * <p>
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   *
   * @param dateTime The dateTime to convert.
   * @return The converted dateTime.
   */
  public static DateTime atEndOfMonth(DateTime dateTime) {
    return atEndOfMonth(dateTime, dateTime.getZone());
  }

  /**
   * Get the last second of the month.
   * <p>
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   *
   * @param dateTime The dateTime to convert.
   * @param zone The time zone to use.
   * @return The converted dateTime.
   */
  public static DateTime atEndOfMonth(DateTime dateTime, DateTimeZone zone) {
    final DateTime zonedDateTme = dateTime.withZone(zone);

    return atEndOfDay(zonedDateTme
      .withDayOfMonth(zonedDateTme
        .dayOfMonth()
        .getMaximumValue()));
  }

  /**
   * Get the first second of the month.
   * <p>
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   *
   * @param LocalDate The localDate to convert.
   * @param zone The time zone to use.
   * @return The converted localDate.
   */
  public static DateTime atStartOfMonth(org.joda.time.LocalDate localDate, DateTimeZone zone) {
    return atStartOfDay(localDate, zone)
      .withDayOfMonth(localDate
        .dayOfMonth()
        .getMinimumValue());
  }

  /**
   * Get the last second of the month.
   *
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   *
   * @param dateTime The localDate to convert.
   * @param zone The time zone to use.
   * @return The converted localDate.
   */
  public static DateTime atEndOfMonth(org.joda.time.LocalDate localDate, DateTimeZone zone) {
    return atEndOfDay(localDate, zone)
      .withDayOfMonth(localDate
        .dayOfMonth()
        .getMaximumValue());
  }

  /**
   * Get the first second of the day.
   * <p>
   * This operates in the time zone specified by dateTime.
   * <p>
   * This will truncate to seconds.
   *
   * @param dateTime The dateTime to convert.
   * @return The converted dateTime.
   */
  public static ZonedDateTime atStartOfDay(ZonedDateTime dateTime) {
    return atStartOfDay(dateTime, dateTime.getZone());
  }

  /**
   * Get the first second of the day for the given time zone.
   * <p>
   * This will truncate to seconds.
   *
   * @param dateTime The dateTime to convert.
   * @param zone The time zone to use.
   * @return The converted dateTime.
   */
  public static ZonedDateTime atStartOfDay(ZonedDateTime dateTime, ZoneId zone) {
    return dateTime
      .withZoneSameInstant(zone)
      .withHour(0)
      .withMinute(0)
      .withSecond(0)
      .truncatedTo(ChronoUnit.SECONDS);
  }

  /**
   * Get the last second of the day.
   * <p>
   * This operates in the time zone specified by dateTime.
   * <p>
   * This will truncate to seconds.
   *
   * @param dateTime The dateTime to convert.
   * @return The converted dateTime.
   */
  public static ZonedDateTime atEndOfDay(ZonedDateTime dateTime) {
    return atEndOfDay(dateTime, dateTime.getZone());
  }

  /**
   * Get the last second of the day for the given time zone.
   * <p>
   * This will truncate to seconds.
   *
   * @param dateTime The dateTime to convert.
   * @param zone The time zone to use.
   * @return The converted dateTime.
   */
  public static ZonedDateTime atEndOfDay(ZonedDateTime dateTime, ZoneId zone) {
    return atStartOfDay(dateTime, zone)
      .plusDays(1)
      .minusSeconds(1)
      .truncatedTo(ChronoUnit.SECONDS);
  }

  /**
   * Get the first second of the day for the given time zone.
   * <p>
   * This will truncate to seconds.
   *
   * @param LocalDate The localDate to convert.
   * @param zone The time zone to use.
   * @return The converted localDate.
   */
  public static ZonedDateTime atStartOfDay(LocalDate localDate, ZoneId zone) {
    return localDate
      .atStartOfDay(zone)
      .truncatedTo(ChronoUnit.SECONDS);
  }

  /**
   * Get the last second of the day for the given time zone.
   * <p>
   * This will truncate to seconds.
   *
   * @param dateTime The localDate to convert.
   * @param zone The time zone to use.
   * @return The converted localDate.
   */
  public static ZonedDateTime atEndOfDay(LocalDate localDate, ZoneId zone) {
    return atStartOfDay(localDate, zone)
      .plusDays(1)
      .minusSeconds(1)
      .truncatedTo(ChronoUnit.SECONDS);
  }

  /**
   * Get the first second of the day.
   * <p>
   * This operates in the time zone specified by dateTime.
   *
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   *
   * @param dateTime The dateTime to convert.
   * @return The converted dateTime.
   */
  public static DateTime atStartOfDay(DateTime dateTime) {
    return atStartOfDay(dateTime, dateTime.getZone());
  }

  /**
   * Get the first second of the day for the given time zone.
   * <p>
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   *
   * @param dateTime The dateTime to convert.
   * @param zone The time zone to use.
   * @return The converted dateTime.
   */
  public static DateTime atStartOfDay(DateTime dateTime, DateTimeZone zone) {
    return dateTime
      .withZone(zone)
      .withTimeAtStartOfDay()
      .withMillisOfSecond(0);
  }

  /**
   * Get the last second of the day.
   * <p>
   * This operates in the time zone specified by dateTime.
   * <p>
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   *
   * @param dateTime The dateTime to convert.
   * @return The converted dateTime.
   */
  public static DateTime atEndOfDay(DateTime dateTime) {
    return atEndOfDay(dateTime, dateTime.getZone());
  }

  /**
   * Get the last second of the day for the given time zone.
   * <p>
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   *
   * @param dateTime The dateTime to convert.
   * @param zone The time zone to use.
   * @return The converted dateTime.
   */
  public static DateTime atEndOfDay(DateTime dateTime, DateTimeZone zone) {
    return atStartOfDay(dateTime, zone)
      .plusDays(1)
      .minusSeconds(1)
      .withMillisOfSecond(0);
  }

  /**
   * Get the first second of the day for the given time zone.
   * <p>
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   *
   * @param LocalDate The localDate to convert.
   * @param zone The time zone to use.
   * @return The converted localDate.
   */
  public static DateTime atStartOfDay(org.joda.time.LocalDate localDate, DateTimeZone zone) {
    return localDate
      .toDateTimeAtStartOfDay(zone)
      .withMillisOfSecond(0);
  }

  /**
   * Get the last second of the day for the given time zone.
   * <p>
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   *
   * @param dateTime The localDate to convert.
   * @param zone The time zone to use.
   * @return The converted localDate.
   */
  public static DateTime atEndOfDay(org.joda.time.LocalDate localDate, DateTimeZone zone) {
    return atStartOfDay(localDate, zone)
      .plusDays(1)
      .minusSeconds(1)
      .withMillisOfSecond(0);
  }

  /**
   * Finds the most recent dateTime from a series of dateTimes.
   *
   * @param dates A series of dateTimes.
   * @return The dateTime that is most recent or NULL if no valid dateTimes
   * provided.
   */
  public static ZonedDateTime mostRecentDate(ZonedDateTime... dates) {
    return Stream.of(dates)
      .filter(Objects::nonNull)
      .max(ZonedDateTime::compareTo)
      .orElse(null);
  }

  /**
   * Finds the most recent dateTime from a series of dateTimes.
   *
   * @param dates A series of dateTimes.
   * @return The dateTime that is most recent or NULL if no valid dateTimes
   * provided.
   */
  public static DateTime mostRecentDate(DateTime... dates) {
    return Stream.of(dates)
      .filter(Objects::nonNull)
      .max(DateTime::compareTo)
      .orElse(null);
  }

  /**
   * Convert from DateTime to ZonedDateTime.
   * <p>
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   *
   * @param dateTime The JodaTime DateTime to convert from.
   * @return The converted dateTime.
   */
  public static ZonedDateTime toZonedDateTime(DateTime dateTime) {
    return java.time.Instant.ofEpochMilli(dateTime.getMillis())
      .atZone(ZoneId.of(dateTime.getZone().getID()));
  }

  /**
   * Convert the JodaTime DateTime to JavaTime OffsetDateTime.
   * <p>
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   *
   * @param dateTime The JodaTime DateTime to convert from.
   * @return The converted dateTime.
   */
  public static OffsetDateTime toOffsetDateTime(DateTime dateTime) {
    final var instant = java.time.Instant.ofEpochMilli(dateTime.getMillis());

    return OffsetDateTime.ofInstant(instant,
      ZoneId.of(dateTime.getZone().getID()));
  }

  /**
   * Check if the the left date is before the right date in milliseconds.
   * <p>
   * The isBefore()/isAfter() methods tend to work with nanoseconds and cannot
   * be used safely for millisecond comparisons.
   *
   * @param left the date to compare on the left.
   * @param right the date to compare on the right.
   * @return true if left is before right and false otherwise.
   */
  public static boolean isBeforeMillis(ZonedDateTime left, ZonedDateTime right) {
    return defaultToNow(left).toInstant().toEpochMilli() <
      defaultToNow(right).toInstant().toEpochMilli();
  }

  /**
   * Check if the the left date is before the right date in milliseconds.
   * <p>
   * The isBefore()/isAfter() methods tend to work with nanoseconds and cannot
   * be used safely for millisecond comparisons.
   *
   * @param left the date to compare on the left.
   * @param right the date to compare on the right.
   * @return true if left is before right and false otherwise.
   */
  public static boolean isBeforeMillis(LocalDateTime left, LocalDateTime right) {
    return defaultToNow(left).toInstant(UTC).toEpochMilli() <
      defaultToNow(right).toInstant(UTC).toEpochMilli();
  }

  /**
   * Check if the the left date is before the right date in milliseconds.
   * <p>
   * The isBefore()/isAfter() methods tend to work with nanoseconds and cannot
   * be used safely for millisecond comparisons.
   * <p>
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   *
   * @param left the date to compare on the left.
   * @param right the date to compare on the right.
   * @return true if left is before right and false otherwise.
   */
  public static boolean isBeforeMillis(DateTime left, DateTime right) {
    return defaultToNow(left).toInstant().getMillis() <
      defaultToNow(right).toInstant().getMillis();
  }

  /**
   * Check if the the left date is before the right date in milliseconds.
   * <p>
   * The isBefore()/isAfter() methods tend to work with nanoseconds and cannot
   * be used safely for millisecond comparisons.
   * <p>
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   *
   * @param left the date to compare on the left.
   * @param right the date to compare on the right.
   * @return true if left is before right and false otherwise.
   */
  public static boolean isBeforeMillis(org.joda.time.LocalDateTime left, org.joda.time.LocalDateTime right) {
    return defaultToNow(left).toDateTime(DateTimeZone.UTC).getMillis() <
      defaultToNow(right).toDateTime(DateTimeZone.UTC).getMillis();
  }

  /**
   * Check if the the left time is before the right time in milliseconds.
   * <p>
   * The isBefore()/isAfter() methods tend to work with nanoseconds and cannot
   * be used safely for millisecond comparisons.
   *
   * @param left the time to compare on the left.
   * @param right the time to compare on the right.
   * @return true if left is before right and false otherwise.
   */
  public static boolean isBeforeMillis(LocalTime left, LocalTime right) {
    return defaultToNow(left).truncatedTo(MILLIS)
      .isBefore(defaultToNow(right).truncatedTo(MILLIS));
  }

  /**
   * Check if the the left time is before the right time in milliseconds.
   * <p>
   * The isBefore()/isAfter() methods tend to work with nanoseconds and cannot
   * be used safely for millisecond comparisons.
   *
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   *
   * @param left the time to compare on the left.
   * @param right the time to compare on the right.
   * @return true if left is before right and false otherwise.
   */
  public static boolean isBeforeMillis(org.joda.time.LocalTime left, org.joda.time.LocalTime right) {
    return defaultToNow(left).isBefore(defaultToNow(right));
  }

  /**
   * Check if the the left date is after the right date in milliseconds.
   * <p>
   * The isBefore()/isAfter() methods tend to work with nanoseconds and cannot
   * be used safely for millisecond comparisons.
   *
   * @param left the date to compare on the left.
   * @param right the date to compare on the right.
   * @return true if left is before right and false otherwise.
   */
  public static boolean isAfterMillis(ZonedDateTime left, ZonedDateTime right) {
    return defaultToNow(left).toInstant().toEpochMilli() >
      defaultToNow(right).toInstant().toEpochMilli();
  }

  /**
   * Check if the the left date is after the right date in milliseconds.
   * <p>
   * The isBefore()/isAfter() methods tend to work with nanoseconds and cannot
   * be used safely for millisecond comparisons.
   *
   * @param left the date to compare on the left.
   * @param right the date to compare on the right.
   * @return true if left is before right and false otherwise.
   */
  public static boolean isAfterMillis(LocalDateTime left, LocalDateTime right) {
    return defaultToNow(left).toInstant(UTC).toEpochMilli() >
      defaultToNow(right).toInstant(UTC).toEpochMilli();
  }

  /**
   * Check if the the left date is after the right date in milliseconds.
   * <p>
   * The isBefore()/isAfter() methods tend to work with nanoseconds and cannot
   * be used safely for millisecond comparisons.
   * <p>
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   *
   * @param left the date to compare on the left.
   * @param right the date to compare on the right.
   * @return true if left is before right and false otherwise.
   */
  public static boolean isAfterMillis(DateTime left, DateTime right) {
    return defaultToNow(left).toInstant().getMillis() >
      defaultToNow(right).toInstant().getMillis();
  }

  /**
   * Check if the the left date is after the right date in milliseconds.
   * <p>
   * The isBefore()/isAfter() methods tend to work with nanoseconds and cannot
   * be used safely for millisecond comparisons.
   * <p>
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   *
   * @param left the date to compare on the left.
   * @param right the date to compare on the right.
   * @return true if left is before right and false otherwise.
   */
  public static boolean isAfterMillis(org.joda.time.LocalDateTime left, org.joda.time.LocalDateTime right) {
    return defaultToNow(left).toDateTime(DateTimeZone.UTC).getMillis() >
      defaultToNow(right).toDateTime(DateTimeZone.UTC).getMillis();
  }

  /**
   * Check if the the left time is after the right time in milliseconds.
   * <p>
   * The isBefore()/isAfter() methods tend to work with nanoseconds and cannot
   * be used safely for millisecond comparisons.
   *
   * @param left the time to compare on the left.
   * @param right the time to compare on the right.
   * @return true if left is before right and false otherwise.
   */
  public static boolean isAfterMillis(LocalTime left, LocalTime right) {
    return defaultToNow(left).truncatedTo(MILLIS)
      .isAfter(defaultToNow(right).truncatedTo(MILLIS));
  }
  /**
   * Check if the the left time is after the right time in milliseconds.
   * <p>
   * The isBefore()/isAfter() methods tend to work with nanoseconds and cannot
   * be used safely for millisecond comparisons.
   * <p>
   * TODO: Remove this after migrating from JodaTime to JavaTime.
   *
   * @param left the time to compare on the left.
   * @param right the time to compare on the right.
   * @return true if left is before right and false otherwise.
   */
  public static boolean isAfterMillis(org.joda.time.LocalTime left, org.joda.time.LocalTime right) {
    return defaultToNow(left).isAfter(defaultToNow(right));
  }

  /**
   * Check if the the left date is the same as the right date in milliseconds.
   * <p>
   * The equalTo() methods tend to work with nanoseconds and cannot be used
   * safely for millisecond comparisons.
   *
   * @param left the date to compare on the left.
   * @param right the date to compare on the right.
   * @return true if left is before right and false otherwise.
   */
  public static boolean isSameMillis(ZonedDateTime left, ZonedDateTime right) {
    return defaultToNow(left).toInstant().toEpochMilli() ==
      defaultToNow(right).toInstant().toEpochMilli();
  }

  /**
   * Check if the the left date is the same as the right date in milliseconds.
   * <p>
   * The equalTo() methods tend to work with nanoseconds and cannot be used
   * safely for millisecond comparisons.
   *
   * @param left the date to compare on the left.
   * @param right the date to compare on the right.
   * @return true if left is before right and false otherwise.
   */
  public static boolean isSameMillis(LocalDateTime left, LocalDateTime right) {
    return defaultToNow(left).toInstant(UTC).toEpochMilli() ==
      defaultToNow(right).toInstant(UTC).toEpochMilli();
  }

  /**
   * Check if the the left time is the same as the right time in milliseconds.
   * <p>
   * The equalTo() methods tend to work with nanoseconds and cannot be used
   * safely for millisecond comparisons.
   *
   * @param left the date to compare on the left.
   * @param right the date to compare on the right.
   * @return true if left is before right and false otherwise.
   */
  public static boolean isSameMillis(LocalTime left, LocalTime right) {
    return defaultToNow(left).truncatedTo(MILLIS).toNanoOfDay() ==
      defaultToNow(right).truncatedTo(MILLIS).toNanoOfDay();
  }

  /**
   * Check if the the left date is the same as the right date in milliseconds.
   * <p>
   * The equalTo() methods tend to work with nanoseconds and cannot be used
   * safely for millisecond comparisons.
   * <p>
   * TODO: Remove this once JodaTime is fully converted to JavaTime.
   *
   * @param left the date to compare on the left.
   * @param right the date to compare on the right.
   * @return true if left is before right and false otherwise.
   */
  public static boolean isSameMillis(DateTime left, DateTime right) {
    return defaultToNow(left).toInstant().getMillis() ==
      defaultToNow(right).toInstant().getMillis();
  }

  /**
   * Check if the the left instant is the same as the right instant in milliseconds.
   * <p>
   * The equalTo() methods tend to work with nanoseconds and cannot be used
   * safely for millisecond comparisons.
   *
   * @param left the date to compare on the left.
   * @param right the date to compare on the right.
   * @return true if left is before right and false otherwise.
   */
  public static boolean isSameMillis(Instant left, Instant right) {
    return defaultToNow(left).toEpochMilli() == defaultToNow(right).toEpochMilli();
  }

  /**
   * Check if the the left instant is the same as the right instant in milliseconds.
   * <p>
   * The equalTo() methods tend to work with nanoseconds and cannot be used
   * safely for millisecond comparisons.
   *
   * TODO: Remove this once JodaTime is fully converted to JavaTime.
   *
   * @param left the date to compare on the left.
   * @param right the date to compare on the right.
   * @return true if left is before right and false otherwise.
   */
  public static boolean isSameMillis(org.joda.time.Instant left, org.joda.time.Instant right) {
    return defaultToNow(left).getMillis() == defaultToNow(right).getMillis();
  }

  /**
   * Check if the date is within the first and last dates, exclusively.
   * <p>
   * The isBefore()/isAfter() methods tend to work with nanoseconds and cannot
   * be used safely for millisecond comparisons.
   *
   * @param date the date to check if is within.
   * @param first the date representing the beginning.
   * @param last the date representing the end.
   * @return true if date is within and false otherwise.
   */
  public static boolean isWithinMillis(ZonedDateTime date, ZonedDateTime first, ZonedDateTime last) {
    return isBeforeMillis(date, last) && isAfterMillis(date, first);
  }

  /**
   * Check if the date is within the first and last dates, exclusively.
   * <p>
   * The isBefore()/isAfter() methods tend to work with nanoseconds and cannot
   * be used safely for millisecond comparisons.
   *
   * @param date the date to check if is within.
   * @param first the date representing the beginning.
   * @param last the date representing the end.
   * @return true if date is within and false otherwise.
   */
  public static boolean isWithinMillis(LocalDateTime date, LocalDateTime first, LocalDateTime last) {
    return isBeforeMillis(date, last) && isAfterMillis(date, first);
  }

  /**
   * Check if the date is within the first and last dates, exclusively.
   * <p>
   * The isBefore()/isAfter() methods tend to work with nanoseconds and cannot
   * be used safely for millisecond comparisons.
   * <p>
   * TODO: Remove this once JodaTime is fully converted to JavaTime.
   *
   * @param time the time to check if is within.
   * @param first the time representing the beginning.
   * @param last the time representing the end.
   * @return true if time is within and false otherwise.
   */
  public static boolean isWithinMillis(LocalTime time, LocalTime first, LocalTime last) {
    return isBeforeMillis(time, last) && isAfterMillis(time, first);
  }

  /**
   * Check if the date is within the first and last dates, exclusively.
   * <p>
   * The isBefore()/isAfter() methods tend to work with nanoseconds and cannot
   * be used safely for millisecond comparisons.
   * <p>
   * TODO: Remove this once JodaTime is fully converted to JavaTime.
   *
   * @param date the date to check if is within.
   * @param first the date representing the beginning.
   * @param last the date representing the end.
   * @return true if date is within and false otherwise.
   */
  public static boolean isWithinMillis(DateTime date, DateTime first, DateTime last) {
    return isBeforeMillis(date, last) && isAfterMillis(date, first);
  }

  /**
   * Check if the date is within the first and last dates, exclusively.
   * <p>
   * The isBefore()/isAfter() methods tend to work with nanoseconds and cannot
   * be used safely for millisecond comparisons.
   * <p>
   * TODO: Remove this once JodaTime is fully converted to JavaTime.
   *
   * @param date the date to check if is within.
   * @param first the date representing the beginning.
   * @param last the date representing the end.
   * @return true if date is within and false otherwise.
   */
  public static boolean isWithinMillis(org.joda.time.LocalDateTime date, org.joda.time.LocalDateTime first, org.joda.time.LocalDateTime last) {
    return isBeforeMillis(date, last) && isAfterMillis(date, first);
  }

  /**
   * Check if the date is within the first and last dates, exclusively.
   * <p>
   * The isBefore()/isAfter() methods tend to work with nanoseconds and cannot
   * be used safely for millisecond comparisons.
   * <p>
   * TODO: Remove this once JodaTime is fully converted to JavaTime.
   *
   * @param time the time to check if is within.
   * @param first the time representing the beginning.
   * @param last the time representing the end.
   * @return true if time is within and false otherwise.
   */
  public static boolean isWithinMillis(org.joda.time.LocalTime time, org.joda.time.LocalTime first, org.joda.time.LocalTime last) {
    return isBeforeMillis(time, last) && isAfterMillis(time, first);
  }

  /**
   * Compare the the left date with the right date in milliseconds.
   * <p>
   * The compareTo() method states that it compares to millis but this appears
   * to not be the case. Instead, this takes the approach of directly
   * converting to epoch millis to guarantee positioning and granularity before
   * comparing.
   *
   * @param left the date to compare on the left.
   * @param right the date to compare on the right.
   * @return true if left is before right and false otherwise.
   */
  public static int compareToMillis(ZonedDateTime left, ZonedDateTime right) {
    if (defaultToNow(left).toInstant().toEpochMilli()
      == defaultToNow(right).toInstant().toEpochMilli()) {
      return 0;
    }

    if (defaultToNow(left).toInstant().toEpochMilli()
      < defaultToNow(right).toInstant().toEpochMilli()) {
      return -1;
    }

    return 1;
  }

  /**
   * Compare the the left time with the right time in milliseconds.
   *
   * @param left the time to compare on the left.
   * @param right the time to compare on the right.
   * @return true if left is before right and false otherwise.
   */
  public static int compareToMillis(LocalTime left, LocalTime right) {
    return defaultToNow(left).truncatedTo(MILLIS)
      .compareTo(defaultToNow(right).truncatedTo(MILLIS));
  }

  /**
   * Compare the the left date with the right date in milliseconds.
   * <p>
   * The compareTo() method states that it compares to millis but this appears
   * to not be the case. Instead, this takes the approach of directly
   * converting to epoch millis to guarantee positioning and granularity before
   * comparing.
   * <p>
   * TODO: Remove this once JodaTime is fully converted to JavaTime.
   *
   * @param left the date to compare on the left.
   * @param right the date to compare on the right.
   * @return true if left is before right and false otherwise.
   */
  public static int compareToMillis(DateTime left, DateTime right) {
    if (defaultToNow(left).toInstant().getMillis()
      == defaultToNow(right).toInstant().getMillis()) {
      return 0;
    }

    if (defaultToNow(left).toInstant().getMillis()
      < defaultToNow(right).toInstant().getMillis()) {
      return -1;
    }

    return 1;
  }

  /**
   * Compare the the left time with the right time in milliseconds.
   * <p>
   * TODO: Remove this once JodaTime is fully converted to JavaTime.
   *
   * @param left the time to compare on the left.
   * @param right the time to compare on the right.
   * @return true if left is before right and false otherwise.
   */
  public static int compareToMillis(org.joda.time.LocalTime left, org.joda.time.LocalTime right) {
    return defaultToNow(left).compareTo(defaultToNow(right));
  }

  /**
   * Get the number of milliseconds between two date times.
   * <p>
   * If "begin" is greater than "end", then milliseconds is set to 0.
   *
   * @param begin The inclusive begin time.
   * @param end The exclusive end time.
   *
   * @return The number of milliseconds between the inclusive begin and the
   * exclusive end.
   */
  public static long millisBetween(ZonedDateTime begin, ZonedDateTime end) {
    if (isBeforeMillis(defaultToNow(begin), defaultToNow(end))) {
      return defaultToNow(end).toInstant().toEpochMilli()
        - defaultToNow(begin).toInstant().toEpochMilli();
    }

    return 0L;
  }

  /**
   * Get the number of milliseconds between two date times.
   * <p>
   * If "begin" is greater than "end", then milliseconds is set to 0.
   * <p>
   * LocalDateTime does not have the time zone so be sure that these dates are
   * both representative of the same time zone.
   *
   * @param begin The inclusive begin time.
   * @param end The exclusive end time.
   * @return The number of milliseconds between the inclusive begin and the
   * exclusive end.
   */
  public static long millisBetween(LocalDateTime begin, LocalDateTime end) {
    final ZonedDateTime zonedBegin = ZonedDateTime.of(defaultToNow(begin), UTC);
    final ZonedDateTime zonedEnd = ZonedDateTime.of(defaultToNow(end), UTC);

    return millisBetween(zonedBegin, zonedEnd);
  }

  /**
   * Get the number of milliseconds between two date times.
   * <p>
   * If "begin" is greater than "end", then milliseconds is set to 0.
   * <p>
   * LocalDateTime does not have the time zone so be sure that these dates are
   * both representative of the same time zone.
   *
   * @param begin The inclusive begin time.
   * @param end The exclusive end time.
   * @return The number of milliseconds between the inclusive begin and the
   * exclusive end.
   */
  public static long millisBetween(long begin, long end) {
    if (begin < end) {
      return end - begin;
    }

    return 0L;
  }

}