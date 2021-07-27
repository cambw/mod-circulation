package org.folio.circulation.domain.anonymization.service;

import static org.folio.circulation.domain.anonymization.LoanAnonymizationRecords.CAN_BE_ANONYMIZED_KEY;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.folio.circulation.Clock;
import org.folio.circulation.domain.Loan;
import org.folio.circulation.domain.anonymization.checkers.AnonymizationChecker;
import org.folio.circulation.domain.anonymization.checkers.AnonymizeLoansImmediatelyChecker;
import org.folio.circulation.domain.anonymization.checkers.AnonymizeLoansWithFeeFinesImmediatelyChecker;
import org.folio.circulation.domain.anonymization.checkers.FeesAndFinesClosePeriodChecker;
import org.folio.circulation.domain.anonymization.checkers.LoanClosePeriodChecker;
import org.folio.circulation.domain.anonymization.checkers.NeverAnonymizeLoansChecker;
import org.folio.circulation.domain.anonymization.checkers.NeverAnonymizeLoansWithFeeFinesChecker;
import org.folio.circulation.domain.anonymization.checkers.NoAssociatedFeesAndFinesChecker;
import org.folio.circulation.domain.anonymization.config.ClosingType;
import org.folio.circulation.domain.anonymization.config.LoanAnonymizationConfiguration;

public class AnonymizationCheckersService {
  private final LoanAnonymizationConfiguration config;
  private final Clock clock;

  private final AnonymizationChecker manualChecker;
  private AnonymizationChecker feesAndFinesCheckersFromLoanHistory;
  private AnonymizationChecker closedLoansCheckersFromLoanHistory;

  public static AnonymizationCheckersService manual(Clock clock) {
    return new AnonymizationCheckersService(null, clock,
      getManualAnonymizationChecker());
  }

  public static AnonymizationCheckersService scheduled(LoanAnonymizationConfiguration config, Clock clock) {
    return new AnonymizationCheckersService(config, clock, null);
  }

  private AnonymizationCheckersService(LoanAnonymizationConfiguration config,
    Clock clock, AnonymizationChecker manualChecker) {
    this.config = config;
    this.clock = clock;
    this.manualChecker = manualChecker;

    if ( config != null) {
      feesAndFinesCheckersFromLoanHistory = getFeesAndFinesCheckersFromLoanHistory();
      closedLoansCheckersFromLoanHistory = getClosedLoansCheckersFromLoanHistory();
    }
  }

  public boolean neverAnonymizeLoans() {
    // Without config, this cannot be determined
    if (config == null) {
      return false;
    }

    return config.getLoanClosingType() == ClosingType.NEVER &&
      !config.treatLoansWithFeesAndFinesDifferently();
  }

  public Map<String, Set<String>> segregateLoans(Collection<Loan> loans) {
    return loans.stream()
      .collect(Collectors.groupingBy(applyCheckersForLoanAndLoanHistoryConfig(),
        Collectors.mapping(Loan::getId, Collectors.toSet())));
  }

  private Function<Loan, String> applyCheckersForLoanAndLoanHistoryConfig() {
    return loan -> {
      AnonymizationChecker checker;
      if (config == null) {
        checker = manualChecker;
      } else if (loan.hasAssociatedFeesAndFines() && config.treatLoansWithFeesAndFinesDifferently()) {
        checker = feesAndFinesCheckersFromLoanHistory;
      } else {
        checker = closedLoansCheckersFromLoanHistory;
      }

      if (!checker.canBeAnonymized(loan)) {
        return checker.getReason();
      } else {
        return CAN_BE_ANONYMIZED_KEY;
      }
    };
  }

  private static AnonymizationChecker getManualAnonymizationChecker() {
    return new NoAssociatedFeesAndFinesChecker();
  }

  private AnonymizationChecker getClosedLoansCheckersFromLoanHistory() {
    AnonymizationChecker checker = null;

    switch (config.getLoanClosingType()) {
      case IMMEDIATELY:
        checker = new AnonymizeLoansImmediatelyChecker();
        break;
      case INTERVAL:
        checker = new LoanClosePeriodChecker(config.getLoanClosePeriod(), clock);
        break;
      case UNKNOWN:
      case NEVER:
        checker = new NeverAnonymizeLoansChecker();
    }

    return checker;
  }

  private AnonymizationChecker getFeesAndFinesCheckersFromLoanHistory() {
    AnonymizationChecker checker = null;

    switch (config.getFeesAndFinesClosingType()) {
      case IMMEDIATELY:
        checker = new AnonymizeLoansWithFeeFinesImmediatelyChecker();
        break;
      case INTERVAL:
        checker = new FeesAndFinesClosePeriodChecker(
          config.getFeeFineClosePeriod(), clock);
        break;
      case UNKNOWN:
      case NEVER:
        checker = new NeverAnonymizeLoansWithFeeFinesChecker();
    }

    return checker;
  }
}
