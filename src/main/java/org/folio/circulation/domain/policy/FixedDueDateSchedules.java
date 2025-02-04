package org.folio.circulation.domain.policy;

import static org.folio.circulation.support.StreamToListMapper.toList;
import static org.folio.circulation.support.ValidationErrorFailure.failedValidation;
import static org.folio.circulation.support.json.JsonObjectArrayPropertyFetcher.toStream;
import static org.folio.circulation.support.json.JsonPropertyFetcher.getProperty;
import static org.folio.circulation.support.utils.DateFormatUtil.parseJodaDateTime;
import static org.folio.circulation.support.utils.DateTimeUtil.atEndOfDay;
import static org.folio.circulation.support.utils.DateTimeUtil.isBeforeMillis;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.folio.circulation.support.http.server.ValidationError;
import org.folio.circulation.support.results.Result;
import org.folio.circulation.support.utils.DateTimeUtil;
import org.joda.time.DateTime;

import io.vertx.core.json.JsonObject;

public class FixedDueDateSchedules {
  private final List<JsonObject> schedules;
  private final String id;

  FixedDueDateSchedules(String id, List<JsonObject> schedules) {
    this.id = id;
    this.schedules = schedules;
  }

  public static FixedDueDateSchedules from(JsonObject representation) {
    if (representation == null) {
      return new NoFixedDueDateSchedules();
    } else {
      return new FixedDueDateSchedules(getProperty(representation, "id"),
        toList(toStream(representation, "schedules")));
    }
  }

  public Optional<DateTime> findDueDateFor(DateTime date) {
    return findScheduleFor(date).map(this::getDueDate);
  }

  private Optional<JsonObject> findScheduleFor(DateTime date) {
    return schedules
      .stream()
      .filter(isWithin(date))
      .findFirst();
  }

  private Predicate<? super JsonObject> isWithin(DateTime date) {
    return schedule -> {
      DateTime from = parseJodaDateTime(schedule.getString("from"));
      DateTime to = atEndOfDay(parseJodaDateTime(schedule.getString("to")));

      return DateTimeUtil.isWithinMillis(date, from, to);
    };
  }

  private DateTime getDueDate(JsonObject schedule) {
    return DateTime.parse(schedule.getString("due"));
  }

  public boolean isEmpty() {
    return schedules.isEmpty();
  }

  Result<DateTime> truncateDueDate(
    DateTime dueDate,
    DateTime loanDate,
    Supplier<ValidationError> noApplicableScheduleError) {

    return findDueDateFor(loanDate)
      .map(limit -> earliest(dueDate, limit))
      .map(Result::succeeded)
      .orElseGet(() -> failedValidation(noApplicableScheduleError.get()));
  }

  private DateTime earliest(DateTime rollingDueDate, DateTime limit) {
    return isBeforeMillis(limit, rollingDueDate)
      ? limit
      : rollingDueDate;
  }

  public String getId() {
    return id;
  }
}
