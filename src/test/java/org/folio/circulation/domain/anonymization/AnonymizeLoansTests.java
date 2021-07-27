package org.folio.circulation.domain.anonymization;

import static org.folio.circulation.Clock.systemClock;
import static org.folio.circulation.support.json.JsonPropertyWriter.write;
import static org.folio.circulation.support.json.JsonPropertyWriter.writeByPath;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.UUID;

import org.folio.circulation.domain.Account;
import org.folio.circulation.domain.FeeFineAction;
import org.folio.circulation.domain.Loan;
import org.folio.circulation.domain.anonymization.config.ClosingType;
import org.folio.circulation.domain.anonymization.config.LoanAnonymizationConfiguration;
import org.folio.circulation.domain.anonymization.service.AnonymizationCheckersService;
import org.folio.circulation.support.ClockManager;
import org.folio.circulation.domain.policy.Period;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.vertx.core.json.JsonObject;

class AnonymizeLoansTests {
  @Nested
  class WhenAnonymizingAllLoansImmediately {
    private final AnonymizationCheckersService checker = checker();

    @Test
    public void anonymizeClosedLoanWithNoFees() {
      final var segregatedLoans = checker.segregateLoans(List.of(closedLoan(
        when(2021, 5, 1, 15, 11, 27))));

      assertThat(segregatedLoans.size(), is(1));
      // Partition for loans that should be annonymized
      assertThat(segregatedLoans.get("_").size(), is(1));
    }

    @Test
    public void anonymizeClosedLoanWithClosedFees() {
      final var segregatedLoans = checker.segregateLoans(List.of(closedLoanWithClosedFee(
        when(2021, 5, 11, 11, 54, 32))));

      assertThat(segregatedLoans.size(), is(1));
      // Partition for loans that should be annonymized
      assertThat(segregatedLoans.get("_").size(), is(1));
    }

    @Test
    public void doNotAnonymizeOpenLoanWithNoFees() {
      final var segregatedLoans = checker.segregateLoans(List.of(openLoan()));

      assertThat(segregatedLoans.size(), is(1));
      assertThat(segregatedLoans.get("anonymizeImmediately").size(), is(1));
    }

    @Test
    public void doNotAnonymizeClosedLoanWithOpenFees() {
      final var segregatedLoans = checker.segregateLoans(List.of(closedLoanWithOpenFee()));

      assertThat(segregatedLoans.size(), is(1));
      assertThat(segregatedLoans.get("anonymizeImmediately").size(), is(1));
    }

    @Test
    public void doNotAnonymizeOpenLoanWithOpenFees() {
      final var segregatedLoans = checker.segregateLoans(List.of(openLoanWithOpenFee()));

      assertThat(segregatedLoans.size(), is(1));
      assertThat(segregatedLoans.get("anonymizeImmediately").size(), is(1));
    }

    private AnonymizationCheckersService checker() {
      // Fee fines closing type is deliberately different to make sure that it is ignored when
      // loans with fees should not be treated differently
      return AnonymizationCheckersService.scheduled(
        new LoanAnonymizationConfiguration(ClosingType.IMMEDIATELY, ClosingType.NEVER,
          false, null, null),
        systemClock());
    }
  }

  @Nested
  class WhenAnonymizingLoansWithFeesImmediately {
    private final AnonymizationCheckersService checker = checker();

    @Test
    public void anonymizeClosedLoanWithClosedFees() {
      final var segregatedLoans = checker.segregateLoans(List.of(closedLoanWithClosedFee(
        when(2021, 5, 11, 11, 54, 32))));

      assertThat(segregatedLoans.size(), is(1));
      assertThat(segregatedLoans.get("_").size(), is(1));
    }

    @Test
    public void doNotAnonymizeClosedLoanWithOpenFees() {
      final var segregatedLoans = checker.segregateLoans(List.of(closedLoanWithOpenFee()));

      assertThat(segregatedLoans.size(), is(1));
      assertThat(segregatedLoans.get("feesAndFinesOpen").size(), is(1));
    }

    @Test
    public void doNotAnonymizeOpenLoanWithOpenFees() {
      final var segregatedLoans = checker.segregateLoans(List.of(openLoanWithOpenFee()));

      assertThat(segregatedLoans.size(), is(1));
      assertThat(segregatedLoans.get("feesAndFinesOpen").size(), is(1));
    }

    private AnonymizationCheckersService checker() {
      // General closing type is deliberately different to make sure that the
      // loans with fees closing type is definitely used
      return AnonymizationCheckersService.scheduled(
        new LoanAnonymizationConfiguration(ClosingType.NEVER, ClosingType.IMMEDIATELY,
          true, null, null), systemClock());
    }
  }

  @Nested
  class WhenNeverAnonymizingLoans {
    private final AnonymizationCheckersService checker = checker();

    @Test
    public void doNotAnonymizeLoanClosedWithNoFees() {
      final var segregatedLoans = checker.segregateLoans(List.of(closedLoan(
        when(2021, 5, 1, 15, 11, 27))));

      assertThat(segregatedLoans.size(), is(1));
      assertThat(segregatedLoans.get("neverAnonymizeLoans").size(), is(1));
    }

    @Test
    public void doNotAnonymizeOpenLoanWithNoFees() {
      final var segregatedLoans = checker.segregateLoans(List.of(openLoan()));

      assertThat(segregatedLoans.size(), is(1));
      assertThat(segregatedLoans.get("neverAnonymizeLoans").size(), is(1));
    }

    @Test
    public void doNotAnonymizeClosedLoanWithClosedFees() {
      final var segregatedLoans = checker.segregateLoans(
        List.of(closedLoanWithClosedFee(when(2021, 5, 11, 11, 54, 32))));

      assertThat(segregatedLoans.size(), is(1));
      assertThat(segregatedLoans.get("neverAnonymizeLoansWithFeesAndFines").size(),
        is(1));
    }

    @Test
    public void doNotAnonymizeClosedLoanWithOpenFees() {
      final var segregatedLoans = checker.segregateLoans(
        List.of(closedLoanWithOpenFee()));

      assertThat(segregatedLoans.size(), is(1));
      assertThat(segregatedLoans.get("neverAnonymizeLoansWithFeesAndFines").size(),
        is(1));
    }

    @Test
    public void doNotAnonymizeOpenLoanWithOpenFees() {
      final var segregatedLoans = checker.segregateLoans(
        List.of(openLoanWithOpenFee()));

      assertThat(segregatedLoans.size(), is(1));
      assertThat(segregatedLoans.get("neverAnonymizeLoansWithFeesAndFines").size(),
        is(1));
    }

    private AnonymizationCheckersService checker() {
      // Fee fines closing type is deliberately different to make sure that it is ignored when
      // loans with fees should not be treated differently
      return AnonymizationCheckersService.scheduled(
        new LoanAnonymizationConfiguration(ClosingType.NEVER, ClosingType.NEVER,
          true, null, null),
        systemClock());
    }
  }

  @Nested
  class WhenManuallyAnonymizingLoans {
    private final AnonymizationCheckersService checker = checker();

    @Test
    public void anonymizeLoanClosedWithNoFees() {
      final var segregatedLoans = checker.segregateLoans(List.of(closedLoan(
        when(2021, 5, 1, 15, 11, 27))));

      assertThat(segregatedLoans.size(), is(1));
      assertThat(segregatedLoans.get("_").size(), is(1));
    }

    @Test
    public void doNotAnonymizeOpenLoanWithNoFees() {
      final var segregatedLoans = checker.segregateLoans(List.of(openLoan()));

      assertThat(segregatedLoans.size(), is(1));
      assertThat(segregatedLoans.get("_").size(), is(1));
    }

    @Test
    public void doNotAnonymizeClosedLoanWithClosedFees() {
      final var segregatedLoans = checker.segregateLoans(
        List.of(closedLoanWithClosedFee(when(2021, 5, 11, 11, 54, 32))));

      assertThat(segregatedLoans.size(), is(1));
      assertThat(segregatedLoans.get("haveAssociatedFeesAndFines").size(),
        is(1));
    }

    @Test
    public void doNotAnonymizeClosedLoanWithOpenFees() {
      final var segregatedLoans = checker.segregateLoans(
        List.of(closedLoanWithOpenFee()));

      assertThat(segregatedLoans.size(), is(1));
      assertThat(segregatedLoans.get("haveAssociatedFeesAndFines").size(),
        is(1));
    }

    @Test
    public void doNotAnonymizeOpenLoanWithOpenFees() {
      final var segregatedLoans = checker.segregateLoans(
        List.of(openLoanWithOpenFee()));

      assertThat(segregatedLoans.size(), is(1));
      assertThat(segregatedLoans.get("haveAssociatedFeesAndFines").size(),
        is(1));
    }

    private AnonymizationCheckersService checker() {
      // Manual anonymization is triggered by providing no config
      return AnonymizationCheckersService.manual(() -> ClockManager.getClockManager().getDateTime());
    }
  }

  @Nested
  class WhenAnonymizingLoansClosedEarlier {
    private final AnonymizationCheckersService checker = checker();

    @Test
    public void anonymizeLoanClosedMoreThanOneWeekAgo() {
      final var segregatedLoans = checker.segregateLoans(List.of(closedLoan(
        when(2021, 5, 3, 10, 23, 55))));

      assertThat(segregatedLoans.size(), is(1));
      assertThat(segregatedLoans.get("_").size(), is(1));
    }

    @Test
    public void doNotAnonymizeLoanClosedLessThanOneWeekAgo() {
      final var segregatedLoans = checker.segregateLoans(List.of(closedLoan(
        when(2021, 5, 9, 7, 1, 45))));

      assertThat(segregatedLoans.size(), is(1));
      assertThat(segregatedLoans.get("loanClosedPeriodNotPassed").size(), is(1));
    }

    @Test
    public void doNotAnonymizeClosedLoanWithNoReturnDate() {
      final var segregatedLoans = checker.segregateLoans(List.of(closedLoan(null)));

      assertThat(segregatedLoans.size(), is(1));
      assertThat(segregatedLoans.get("loanClosedPeriodNotPassed").size(), is(1));
    }

    @Test
    public void doNotAnonymizeOpenLoanWithNoFees() {
      final var segregatedLoans = checker.segregateLoans(List.of(openLoan()));

      assertThat(segregatedLoans.size(), is(1));
      assertThat(segregatedLoans.get("loanClosedPeriodNotPassed").size(), is(1));
    }

    @Test
    public void anonymizeClosedLoanWithFeeClosedEarlierThanLastWeek() {
      final var segregatedLoans = checker.segregateLoans(
        List.of(closedLoanWithClosedFee(when(2021, 5, 1, 15, 11, 27))));

      assertThat(segregatedLoans.size(), is(1));
      assertThat(segregatedLoans.get("_").size(), is(1));
    }

    @Test
    public void doNotAnonymizeClosedLoanWithFeesClosedWithinTheLastWeek() {
      final var segregatedLoans = checker.segregateLoans(
        List.of(closedLoanWithClosedFee(when(2021, 5, 11, 11, 54, 32))));

      assertThat(segregatedLoans.size(), is(1));
      assertThat(segregatedLoans.get("intervalAfterFeesAndFinesCloseNotPassed").size(),
        is(1));
    }

    @Test
    public void doNotAnonymizeClosedLoanWithClosedFeeWithoutAClosedDate() {
      final var segregatedLoans = checker.segregateLoans(
        List.of(closedLoanWithClosedFee(null)));

      assertThat(segregatedLoans.size(), is(1));
      assertThat(segregatedLoans.get("intervalAfterFeesAndFinesCloseNotPassed").size(),
        is(1));
    }


    @Test
    public void doNotAnonymizeClosedLoanWithOpenFees() {
      final var segregatedLoans = checker.segregateLoans(
        List.of(closedLoanWithOpenFee()));

      assertThat(segregatedLoans.size(), is(1));
      assertThat(segregatedLoans.get("intervalAfterFeesAndFinesCloseNotPassed").size(),
        is(1));
    }

    @Test
    public void doNotAnonymizeOpenLoanWithOpenFees() {
      final var segregatedLoans = checker.segregateLoans(
        List.of(openLoanWithOpenFee()));

      assertThat(segregatedLoans.size(), is(1));
      assertThat(segregatedLoans.get("intervalAfterFeesAndFinesCloseNotPassed").size(),
        is(1));
    }

    private AnonymizationCheckersService checker() {
      // Fee fines closing type is deliberately different to make sure that it is ignored when
      // loans with fees should not be treated differently
      return AnonymizationCheckersService.scheduled(
        new LoanAnonymizationConfiguration(ClosingType.INTERVAL, ClosingType.INTERVAL,
          true, Period.weeks(1),  Period.weeks(1)),
        () -> new DateTime(2021, 5, 15, 8, 15, 43, DateTimeZone.UTC));
    }
  }

  private DateTime when(int year, int month, int day, int hour, int minute, int second) {
    return new DateTime(year, month, day, hour, minute, second, DateTimeZone.UTC);
  }

  private Loan openLoan() {
    return loan("Open", null);
  }

  private Loan openLoanWithOpenFee() {
    return loan("Open", null)
      .withAccounts(List.of(openFee()));
  }

  private Loan closedLoan(DateTime returnDate) {
    return loan("Closed", returnDate);
  }

  private Loan closedLoanWithClosedFee(DateTime feeClosedDate) {
    return loan("Closed", null)
      .withAccounts(List.of(closedFee(feeClosedDate)));
  }

  private Loan closedLoanWithOpenFee() {
    return loan("Open", null)
      .withAccounts(List.of(openFee()));
  }

  private Loan loan(String status, DateTime systemReturnDate) {
    final var json = new JsonObject();

    write(json, "id", UUID.randomUUID());
    writeByPath(json, status, "status", "name");
    write(json, "systemReturnDate", systemReturnDate);

    return Loan.from(json);
  }

  private Account openFee() {
    return fee("Open", List.of());
  }

  private Account closedFee(DateTime feeClosedDate) {
    final var json = new JsonObject();

    write(json, "balance", 0.0);
    write(json, "dateAction", feeClosedDate);

    final var closureAction = new FeeFineAction(json);

    return fee("Closed", List.of(closureAction));
  }

  private Account fee(String status, List<FeeFineAction> actions) {
    return new Account(null, null, null, null, status, null, actions, null);
  }
}
