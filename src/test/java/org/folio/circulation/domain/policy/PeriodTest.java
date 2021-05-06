package org.folio.circulation.domain.policy;

import static java.time.ZonedDateTime.now;
import static java.time.ZoneOffset.UTC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class PeriodTest {

  @Test
  @Parameters( {
    "Minutes | 6  | 6",
    "Hours   | 5  | 300",
    "Days    | 4  | 5760",
    "Weeks   | 3  | 30240",
    "Months  | 2  | 89280"
  })
  public void toMinutes(String interval, Integer duration, int expectedResult) {
    assertEquals(expectedResult, Period.from(duration, interval).toMinutes());
  }

  @Test
  public void toMinutesWithNullInterval() {
    Period period = Period.from(10, null);
    assertEquals(0, period.toMinutes());
  }

  @Test
  public void toMinutesWithNullDuration() {
    Period period = Period.from(null, "Minutes");
    assertEquals(0, period.toMinutes());
  }

  @Test
  public void toMinutesWithUnknownInterval() {
    Period period = Period.from(10, "Unknown interval");
    assertEquals(0, period.toMinutes());
  }

  @Test
  @Parameters( {
    "Minutes, 5",
    "Hours, 23",
    "Days, 14",
    "Weeks, 3",
    "Months, 10"
  })
  public void hasPassedSinceDateTillNowWhenNowAfterTheDate(String interval, int duration) {
    Period period = Period.from(duration, interval);
    ZonedDateTime startDate = period.minusDate(now(UTC)).minusSeconds(1);

    assertTrue(period.hasPassedSinceDateTillNow(startDate));
    assertFalse(period.hasNotPassedSinceDateTillNow(startDate));
  }

  @Test
  @Parameters( {
    "Minutes, 55",
    "Hours, 32",
    "Days, 65",
    "Weeks, 7",
    "Months, 23"
  })
  public void hasPassedSinceDateTillNowWhenNowIsTheDate(String interval, int duration) {
    Period period = Period.from(duration, interval);
    ZonedDateTime startDate = period.minusDate(now(UTC));

    assertTrue(period.hasPassedSinceDateTillNow(startDate));
    assertFalse(period.hasNotPassedSinceDateTillNow(startDate));
  }

  @Test
  @Parameters( {
    "Minutes, 33",
    "Hours, 65",
    "Days, 9",
    "Weeks, 12",
    "Months, 3"
  })
  public void hasPassedSinceDateTillNowIsFalse(String interval, int duration) {
    Period period = Period.from(duration, interval);
    ZonedDateTime startDate = now(UTC);

    assertFalse(period.hasPassedSinceDateTillNow(startDate));
    assertTrue(period.hasNotPassedSinceDateTillNow(startDate));
  }

  @Test
  @Parameters( {
    "Minutes, 12",
    "Hours, 87",
    "Days, 98",
    "Weeks, 23",
    "Months, 4"
  })
  public void hasNotPassedSinceDateTillNow(String interval, int duration) {
    Period period = Period.from(duration, interval);
    ZonedDateTime startDate = period.plusDate(now(UTC));

    assertTrue(period.hasNotPassedSinceDateTillNow(startDate));
    assertFalse(period.hasPassedSinceDateTillNow(startDate));
  }

  @Test
  @Parameters( {
    "Minutes, 4",
    "Hours, 7",
    "Days, 8",
    "Weeks, 3",
    "Months, 9"
  })
  public void hasNotPassedSinceDateTillNowIsFalseWhenPassed(String interval, int duration) {
    Period period = Period.from(duration, interval);
    ZonedDateTime startDate = period.minusDate(now(UTC)).minusSeconds(1);

    assertFalse(period.hasNotPassedSinceDateTillNow(startDate));
    assertTrue(period.hasPassedSinceDateTillNow(startDate));
  }

  @Test
  @Parameters( {
    "Minutes, 43",
    "Hours, 65",
    "Days, 87",
    "Weeks, 12",
    "Months, 3"
  })
  public void isEqualToDateTillNow(String interval, int duration) {
    Period period = Period.from(duration, interval);
    ZonedDateTime startDate = period.minusDate(now(UTC));

    assertTrue(period.isEqualToDateTillNow(startDate)
      // Sometimes there is difference in mss
      // additional check to make the test stable
      || period.hasPassedSinceDateTillNow(startDate));
  }
}
