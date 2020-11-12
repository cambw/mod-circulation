package org.folio.circulation.domain.policy.library;

import static java.util.Objects.requireNonNull;
import static org.folio.circulation.domain.policy.library.ClosedLibraryStrategyUtils.END_OF_A_DAY;
import static org.folio.circulation.domain.policy.library.ClosedLibraryStrategyUtils.failureForAbsentTimetable;
import static org.folio.circulation.support.results.Result.failed;
import static org.folio.circulation.support.results.Result.succeeded;
import static org.folio.circulation.support.utils.DateTimeUtil.endOfDay;
import static org.folio.circulation.support.utils.DateTimeUtil.javaToJodaDateTime;

import org.folio.circulation.AdjacentOpeningDays;
import org.folio.circulation.domain.OpeningDay;
import org.folio.circulation.support.results.Result;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class EndOfPreviousDayStrategy implements ClosedLibraryStrategy {
  private final DateTimeZone zone;

  public EndOfPreviousDayStrategy(DateTimeZone zone) {
    this.zone = zone;
  }

  @Override
  public Result<DateTime> calculateDueDate(DateTime requestedDate, AdjacentOpeningDays openingDays) {
    requireNonNull(openingDays);

    if (openingDays.getRequestedDay().getOpen()) {
      return succeeded(requestedDate.withZone(zone).withTime(END_OF_A_DAY));
    }

    OpeningDay previousDay = openingDays.getPreviousDay();

    if (!previousDay.getOpen()) {
      return failed(failureForAbsentTimetable());
    }
    return succeeded(javaToJodaDateTime(previousDay.getJavaDate(), endOfDay(), zone));
  }
}
