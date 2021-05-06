package org.folio.circulation.domain.policy.library;

import static org.folio.circulation.domain.policy.library.ClosedLibraryStrategyUtils.END_OF_A_DAY;
import static org.folio.circulation.domain.policy.library.ClosedLibraryStrategyUtils.failureForAbsentTimetable;
import static org.folio.circulation.support.results.Result.failed;
import static org.folio.circulation.support.results.Result.succeeded;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;

import org.folio.circulation.AdjacentOpeningDays;
import org.folio.circulation.domain.OpeningDay;
import org.folio.circulation.support.results.Result;

public class EndOfNextOpenDayStrategy implements ClosedLibraryStrategy {

  private final ZoneOffset zone;

  public EndOfNextOpenDayStrategy(ZoneOffset zone) {
    this.zone = zone;
  }

  @Override
  public Result<ZonedDateTime> calculateDueDate(ZonedDateTime requestedDate,
    AdjacentOpeningDays openingDays) {

    Objects.requireNonNull(openingDays);
    if (openingDays.getRequestedDay().getOpen()) {
      LocalDate date = requestedDate.toLocalDate();
      return succeeded(ZonedDateTime.of(date, END_OF_A_DAY, zone));
    }
    OpeningDay nextDay = openingDays.getNextDay();
    if (!nextDay.getOpen()) {
      return failed(failureForAbsentTimetable());
    }
    LocalDate date = nextDay.getDayWithTimeZone().toLocalDate();
    return succeeded(ZonedDateTime.of(date, END_OF_A_DAY, zone));
  }
}
