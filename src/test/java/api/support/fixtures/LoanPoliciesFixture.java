package api.support.fixtures;

import static java.time.ZoneOffset.UTC;
import static org.folio.circulation.support.json.JsonPropertyFetcher.getProperty;

import java.time.ZonedDateTime;

import org.folio.circulation.domain.policy.Period;

import api.support.builders.FixedDueDateSchedule;
import api.support.builders.FixedDueDateSchedulesBuilder;
import api.support.builders.LoanPolicyBuilder;
import api.support.http.IndividualResource;
import api.support.http.ResourceClient;
import io.vertx.core.json.JsonObject;

public class LoanPoliciesFixture {
  private final RecordCreator loanPolicyRecordCreator;
  private final RecordCreator fixedDueDateScheduleRecordCreator;

  public LoanPoliciesFixture(
    ResourceClient loanPoliciesClient,
    ResourceClient fixedDueDateScheduleClient) {

    loanPolicyRecordCreator = new RecordCreator(loanPoliciesClient,
      reason -> getProperty(reason, "name"));

    fixedDueDateScheduleRecordCreator = new RecordCreator(
      fixedDueDateScheduleClient, schedule -> getProperty(schedule, "name"));
  }

  public void cleanUp() {
    loanPolicyRecordCreator.cleanUp();
    fixedDueDateScheduleRecordCreator.cleanUp();
  }

  public void delete(IndividualResource record) {
    loanPolicyRecordCreator.delete(record);
  }

  public IndividualResource createExampleFixedDueDateSchedule() {
    final int currentYear = ZonedDateTime.now(UTC).getYear();

    FixedDueDateSchedulesBuilder fixedDueDateSchedule =
      new FixedDueDateSchedulesBuilder()
        .withName("Example Fixed Due Date Schedule")
        .withDescription("Example Fixed Due Date Schedule")
        .addSchedule(new FixedDueDateSchedule(
          ZonedDateTime.of(currentYear, 1, 1, 0, 0, 0, 0, UTC),
          ZonedDateTime.of(currentYear, 12, 31, 23, 59, 59, 0, UTC),
          ZonedDateTime.of(currentYear, 12, 31, 23, 59, 59, 0, UTC)));

    return createSchedule(fixedDueDateSchedule);
  }

  public IndividualResource create(LoanPolicyBuilder builder) {
    return loanPolicyRecordCreator.createIfAbsent(builder);
  }

  public IndividualResource create(JsonObject policy) {
    return loanPolicyRecordCreator.createIfAbsent(policy);
  }

  public IndividualResource createSchedule(FixedDueDateSchedulesBuilder builder) {
    return fixedDueDateScheduleRecordCreator.createIfAbsent(builder);
  }

  public IndividualResource canCirculateRolling() {
    JsonObject holds = new JsonObject();
    holds.put("alternateRenewalLoanPeriod", Period.weeks(3).asJson());
    holds.put("renewItemsWithRequest", true);

    final LoanPolicyBuilder canCirculateRollingPolicy = new LoanPolicyBuilder()
      .withName("Can Circulate Rolling")
      .withDescription("Can circulate item")
      .withHolds(holds)
      .rolling(Period.weeks(3))
      .unlimitedRenewals()
      .renewFromSystemDate();

    return loanPolicyRecordCreator.createIfAbsent(canCirculateRollingPolicy);
  }

  public IndividualResource canCirculateFixed() {
    JsonObject holds = new JsonObject();
    holds.put("alternateRenewalLoanPeriod", Period.weeks(3).asJson());
    holds.put("renewItemsWithRequest", true);

    LoanPolicyBuilder canCirculateFixedLoanPolicy = new LoanPolicyBuilder()
      .withName("Can Circulate Fixed")
      .withHolds(holds)
      .withDescription("Can circulate item")
      .fixed(createExampleFixedDueDateSchedule().getId());

    return loanPolicyRecordCreator.createIfAbsent(canCirculateFixedLoanPolicy);
  }
}
