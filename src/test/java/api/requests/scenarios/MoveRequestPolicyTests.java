package api.requests.scenarios;

import static api.support.utl.PatronNoticeTestHelper.verifyNumberOfPublishedEvents;
import static api.support.utl.PatronNoticeTestHelper.verifyNumberOfSentNotices;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.waitAtMost;
import static org.folio.circulation.domain.representations.RequestProperties.REQUEST_TYPE;
import static org.folio.circulation.domain.representations.logs.LogEventType.NOTICE;
import static org.folio.circulation.domain.representations.logs.LogEventType.NOTICE_ERROR;
import static org.folio.circulation.support.utils.DateFormatUtil.formatDateTime;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.folio.circulation.domain.MultipleRecords;
import org.folio.circulation.domain.RequestType;
import org.folio.circulation.domain.notice.NoticeEventType;
import org.folio.circulation.domain.policy.Period;
import org.folio.circulation.support.http.client.Response;
import org.folio.circulation.support.utils.ClockUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import api.support.APITests;
import api.support.builders.LoanPolicyBuilder;
import api.support.builders.MoveRequestBuilder;
import api.support.builders.NoticeConfigurationBuilder;
import api.support.builders.NoticePolicyBuilder;
import api.support.fakes.FakeModNotify;
import api.support.fakes.FakePubSub;
import api.support.http.IndividualResource;
import io.vertx.core.json.JsonObject;

/**
 * Notes:<br>
 *  MGD = Minimum guaranteed due date<br>
 *  RD = Recall due date<br>
 */
class MoveRequestPolicyTests extends APITests {
  private static Clock clock;

  private NoticePolicyBuilder noticePolicy;

  @BeforeAll
  public static void setUpBeforeClass() {
    final Instant now = Instant.ofEpochMilli(ClockUtil.getJodaInstant()
      .getMillis());
    clock = Clock.fixed(now, ZoneOffset.UTC);

    FakePubSub.clearPublishedEvents();
  }

  @BeforeEach
  public void setUp() {
    // reset the clock before each test (just in case)
    ClockUtil.setClock(clock);
  }

  @BeforeEach
  public void setUpNoticePolicy() {
    UUID recallToLoaneeTemplateId = UUID.randomUUID();
    JsonObject recallToLoaneeConfiguration = new NoticeConfigurationBuilder()
      .withTemplateId(recallToLoaneeTemplateId)
      .withEventType(NoticeEventType.ITEM_RECALLED.getRepresentation())
      .create();

    noticePolicy = new NoticePolicyBuilder()
      .withName("Policy with recall notice")
      .withLoanNotices(singletonList(recallToLoaneeConfiguration));

    useFallbackPolicies(
      loanPoliciesFixture.canCirculateRolling().getId(),
      requestPoliciesFixture.allowAllRequestPolicy().getId(),
      noticePoliciesFixture.create(noticePolicy).getId(),
      overdueFinePoliciesFixture.facultyStandard().getId(),
      lostItemFeePoliciesFixture.facultyStandard().getId());
  }

  @AfterEach
  public void afterEach() {
    // The clock must be reset after each test.
    ClockUtil.setDefaultClock();
  }

  @Test
  void cannotMoveRecallRequestsWithRequestPolicyNotAllowingHolds() {
    final String anyNoticePolicy = noticePoliciesFixture.activeNotice().getId().toString();
    final String anyLoanPolicy = loanPoliciesFixture.canCirculateRolling().getId().toString();
    final String bookMaterialType = materialTypesFixture.book().getId().toString();
    final String anyRequestPolicy = requestPoliciesFixture.allowAllRequestPolicy().getId().toString();
    final String anyOverdueFinePolicy = overdueFinePoliciesFixture.facultyStandard().getId().toString();
    final String anyLostItemFeePolicy = lostItemFeePoliciesFixture.facultyStandard().getId().toString();

    ArrayList<RequestType> allowedRequestTypes = new ArrayList<>();
    allowedRequestTypes.add(RequestType.RECALL);
    allowedRequestTypes.add(RequestType.PAGE);
    final String noHoldRequestPolicy = requestPoliciesFixture.customRequestPolicy(allowedRequestTypes,
      "All But Hold", "All but Hold request policy").getId().toString();

    //This rule is set up to show that the fallback policy won't be used but the material type rule m is used instead.
    //The material type rule m allows any patron to place any request but HOLDs on any BOOK, loan or notice types
    final String rules = String.join("\n",
      "priority: t, s, c, b, a, m, g",
      "fallback-policy : l " + anyLoanPolicy + " r " + anyRequestPolicy + " n " + anyNoticePolicy + " o " + anyOverdueFinePolicy + " i " + anyLostItemFeePolicy + "\n",
      "m " + bookMaterialType + ": l " + anyLoanPolicy + " r " + noHoldRequestPolicy +" n " + anyNoticePolicy + " o " + anyOverdueFinePolicy + " i " + anyLostItemFeePolicy
    );

    setRules(rules);

    IndividualResource smallAngryPlanet = itemsFixture.basedUponSmallAngryPlanet();
    IndividualResource interestingTimes = itemsFixture.basedUponInterestingTimes();

    IndividualResource james = usersFixture.james();
    IndividualResource jessica = usersFixture.jessica();
    IndividualResource charlotte = usersFixture.charlotte();

    checkOutFixture.checkOutByBarcode(smallAngryPlanet, jessica);

    checkOutFixture.checkOutByBarcode(interestingTimes, charlotte);

    IndividualResource requestByCharlotte = requestsFixture.placeHoldShelfRequest(
      smallAngryPlanet, charlotte, ClockUtil.getDateTime().minusHours(2), RequestType.RECALL.getValue());

    IndividualResource requestByJames = requestsFixture.placeHoldShelfRequest(
      interestingTimes, james, ClockUtil.getDateTime().minusHours(1), RequestType.RECALL.getValue());

    // move james' recall request as a hold shelf request from smallAngryPlanet to interestingTimes
    Response response = requestsFixture.attemptMove(new MoveRequestBuilder(
      requestByJames.getId(),
      smallAngryPlanet.getId(),
      RequestType.HOLD.getValue()
    ));

    assertThat("Move request should have correct response status code", response.getStatusCode(), is(422));
    assertThat("Move request should have correct response message",
      response.getJson().getJsonArray("errors").getJsonObject(0).getString("message"),
      is("Hold requests are not allowed for this patron and item combination"));

    requestByCharlotte = requestsClient.get(requestByCharlotte);
    assertThat(requestByCharlotte.getJson().getString(REQUEST_TYPE), is(RequestType.RECALL.getValue()));
    assertThat(requestByCharlotte.getJson().getInteger("position"), is(1));
    assertThat(requestByCharlotte.getJson().getString("itemId"), is(smallAngryPlanet.getId().toString()));

    requestByJames = requestsClient.get(requestByJames);
    assertThat(requestByJames.getJson().getString(REQUEST_TYPE), is(RequestType.RECALL.getValue()));
    assertThat(requestByJames.getJson().getInteger("position"), is(1));
    assertThat(requestByJames.getJson().getString("itemId"), is(interestingTimes.getId().toString()));

    // check item queues are correct size
    MultipleRecords<JsonObject> smallAngryPlanetQueue = requestsFixture.getQueueFor(smallAngryPlanet);
    assertThat(smallAngryPlanetQueue.getTotalRecords(), is(1));

    MultipleRecords<JsonObject> interestingTimesQueue = requestsFixture.getQueueFor(interestingTimes);
    assertThat(interestingTimesQueue.getTotalRecords(), is(1));
  }

  @Test
  void moveRecallRequestWithoutExistingRecallsAndWithNoPolicyValuesChangesDueDateToSystemDate() {
    final IndividualResource smallAngryPlanet = itemsFixture.basedUponSmallAngryPlanet();
    final IndividualResource interestingTimes = itemsFixture.basedUponInterestingTimes();
    final IndividualResource steve = usersFixture.steve();
    final IndividualResource charlotte = usersFixture.charlotte();
    final IndividualResource jessica = usersFixture.jessica();

    // steve checks out smallAngryPlanet
    final IndividualResource loan = checkOutFixture.checkOutByBarcode(
      smallAngryPlanet, steve, ClockUtil.getDateTime());

    final String originalDueDate = loan.getJson().getString("dueDate");

    // charlotte checks out interestingTimes
    checkOutFixture.checkOutByBarcode(interestingTimes, charlotte);

    // jessica places recall request on interestingTimes
    IndividualResource requestByJessica = requestsFixture.placeHoldShelfRequest(
      interestingTimes, jessica, ClockUtil.getDateTime(), RequestType.RECALL.getValue());

    // notice for the recall is expected
    verifyNumberOfSentNotices(1);
    verifyNumberOfPublishedEvents(NOTICE, 1);
    verifyNumberOfPublishedEvents(NOTICE_ERROR, 0);

    // move jessica's recall request from interestingTimes to smallAngryPlanet
    IndividualResource moveRequest = requestsFixture.move(new MoveRequestBuilder(
      requestByJessica.getId(),
      smallAngryPlanet.getId(),
      RequestType.RECALL.getValue()));

    assertThat("Move request should have correct item id",
      moveRequest.getJson().getString("itemId"), is(smallAngryPlanet.getId().toString()));

    assertThat("Move request should have correct type",
      moveRequest.getJson().getString("requestType"), is(RequestType.RECALL.getValue()));

    final JsonObject storedLoan = loansStorageClient.getById(loan.getId()).getJson();

    assertThat("due date is the original date",
      storedLoan.getString("dueDate"), not(originalDueDate));

    final String expectedDueDate = formatDateTime(ClockUtil.getDateTime());
    assertThat("due date is not the current date",
      storedLoan.getString("dueDate"), is(expectedDueDate));

    verifyNumberOfSentNotices(2);
    verifyNumberOfPublishedEvents(NOTICE, 2);
    verifyNumberOfPublishedEvents(NOTICE_ERROR, 0);
  }

  @Test
  void moveRecallRequestWithExistingRecallsAndWithNoPolicyValuesChangesDueDateToSystemDate() {
    final IndividualResource smallAngryPlanet = itemsFixture.basedUponSmallAngryPlanet();
    final IndividualResource interestingTimes = itemsFixture.basedUponInterestingTimes();
    final IndividualResource steve = usersFixture.steve();
    final IndividualResource charlotte = usersFixture.charlotte();
    final IndividualResource jessica = usersFixture.jessica();

    // steve checks out smallAngryPlanet
    final IndividualResource loan = checkOutFixture.checkOutByBarcode(
      smallAngryPlanet, steve, ClockUtil.getDateTime());

    final String originalDueDate = loan.getJson().getString("dueDate");

    // charlotte places recall request on smallAngryPlanet
    requestsFixture.placeHoldShelfRequest(
      smallAngryPlanet, charlotte, ClockUtil.getDateTime().minusHours(1), RequestType.RECALL.getValue());

    JsonObject storedLoan = loansStorageClient.getById(loan.getId()).getJson();

    assertThat("due date is the original date",
      storedLoan.getString("dueDate"), not(originalDueDate));

    final String expectedDueDate = formatDateTime(ClockUtil.getDateTime());
    assertThat("due date is not the current date",
      storedLoan.getString("dueDate"), is(expectedDueDate));

    // charlotte checks out interestingTimes
    checkOutFixture.checkOutByBarcode(interestingTimes, charlotte);

    // jessica places recall request on interestingTimes
    IndividualResource requestByJessica = requestsFixture.placeHoldShelfRequest(
      interestingTimes, jessica, ClockUtil.getDateTime(), RequestType.RECALL.getValue());

    // There should be 2 notices for each recall
    waitAtMost(1, SECONDS)
      .until(() -> patronNoticesForRecipientWasSent(steve));

    waitAtMost(1, SECONDS)
      .until(() -> patronNoticesForRecipientWasSent(charlotte));

    verifyNumberOfSentNotices(2);
    verifyNumberOfPublishedEvents(NOTICE, 2);
    verifyNumberOfPublishedEvents(NOTICE_ERROR, 0);

    // move jessica's recall request from interestingTimes to smallAngryPlanet
    IndividualResource moveRequest = requestsFixture.move(new MoveRequestBuilder(
      requestByJessica.getId(),
      smallAngryPlanet.getId(),
      RequestType.RECALL.getValue()));

    assertThat("Move request should have correct item id",
      moveRequest.getJson().getString("itemId"), is(smallAngryPlanet.getId().toString()));

    assertThat("Move request should have correct type",
      moveRequest.getJson().getString("requestType"), is(RequestType.RECALL.getValue()));

    storedLoan = loansStorageClient.getById(loan.getId()).getJson();

    assertThat("due date has changed",
      storedLoan.getString("dueDate"), is(expectedDueDate));

    assertThat("move recall request unexpectedly sent another patron notice",
      FakeModNotify.getSentPatronNotices(), hasSize(2));

    verifyNumberOfSentNotices(2);
    verifyNumberOfPublishedEvents(NOTICE, 2);
    verifyNumberOfPublishedEvents(NOTICE_ERROR, 0);
  }

  @Test
  void moveRecallRequestWithoutExistingRecallsAndWithMGDAndRDValuesChangesDueDateToRD() {
    final IndividualResource smallAngryPlanet = itemsFixture.basedUponSmallAngryPlanet();
    final IndividualResource interestingTimes = itemsFixture.basedUponInterestingTimes();
    final IndividualResource steve = usersFixture.steve();
    final IndividualResource charlotte = usersFixture.charlotte();
    final IndividualResource jessica = usersFixture.jessica();

    final LoanPolicyBuilder canCirculateRollingPolicy = new LoanPolicyBuilder()
      .withName("Can Circulate Rolling With Recalls")
      .withDescription("Can circulate item With Recalls")
      .rolling(Period.weeks(3))
      .unlimitedRenewals()
      .renewFromSystemDate()
      .withRecallsMinimumGuaranteedLoanPeriod(Period.weeks(2))
      .withRecallsRecallReturnInterval(Period.months(2));

    final IndividualResource loanPolicy = loanPoliciesFixture.create(canCirculateRollingPolicy);

    useFallbackPolicies(loanPolicy.getId(),
      requestPoliciesFixture.allowAllRequestPolicy().getId(),
      noticePoliciesFixture.create(noticePolicy).getId(),
      overdueFinePoliciesFixture.facultyStandard().getId(),
      lostItemFeePoliciesFixture.facultyStandard().getId());

    final IndividualResource loan = checkOutFixture.checkOutByBarcode(
      smallAngryPlanet, steve, ClockUtil.getDateTime());

    final String originalDueDate = loan.getJson().getString("dueDate");

    // charlotte checks out interestingTimes
    checkOutFixture.checkOutByBarcode(interestingTimes, charlotte);

    // jessica places recall request on interestingTimes
    IndividualResource requestByJessica = requestsFixture.placeHoldShelfRequest(
      interestingTimes, jessica, ClockUtil.getDateTime(), RequestType.RECALL.getValue());

    // One notice for the recall is expected
    verifyNumberOfSentNotices(1);
    verifyNumberOfPublishedEvents(NOTICE, 1);
    verifyNumberOfPublishedEvents(NOTICE_ERROR, 0);

    // move jessica's recall request from interestingTimes to smallAngryPlanet
    IndividualResource moveRequest = requestsFixture.move(new MoveRequestBuilder(
      requestByJessica.getId(),
      smallAngryPlanet.getId(),
      RequestType.RECALL.getValue()));

    assertThat("Move request should have correct item id",
      moveRequest.getJson().getString("itemId"), is(smallAngryPlanet.getId().toString()));

    assertThat("Move request should have correct type",
      moveRequest.getJson().getString("requestType"), is(RequestType.RECALL.getValue()));

    final JsonObject storedLoan = loansStorageClient.getById(loan.getId()).getJson();

    assertThat("due date is the original date",
      storedLoan.getString("dueDate"), not(originalDueDate));

    final String expectedDueDate = formatDateTime(ClockUtil.getDateTime().plusMonths(2));
    assertThat("due date is not the recall due date (2 months)",
      storedLoan.getString("dueDate"), is(expectedDueDate));

    assertThat("move recall request notice has not been sent",
      FakeModNotify.getSentPatronNotices(), hasSize(2));

    verifyNumberOfSentNotices(2);
    verifyNumberOfPublishedEvents(NOTICE, 2);
    verifyNumberOfPublishedEvents(NOTICE_ERROR, 0);
  }

  @Test
  void moveRecallRequestWithExistingRecallsAndWithMGDAndRDValuesChangesDueDateToRD() {
    final IndividualResource smallAngryPlanet = itemsFixture.basedUponSmallAngryPlanet();
    final IndividualResource interestingTimes = itemsFixture.basedUponInterestingTimes();
    final IndividualResource steve = usersFixture.steve();
    final IndividualResource charlotte = usersFixture.charlotte();
    final IndividualResource jessica = usersFixture.jessica();

    final LoanPolicyBuilder canCirculateRollingPolicy = new LoanPolicyBuilder()
      .withName("Can Circulate Rolling With Recalls")
      .withDescription("Can circulate item With Recalls")
      .rolling(Period.weeks(3))
      .unlimitedRenewals()
      .renewFromSystemDate()
      .withRecallsMinimumGuaranteedLoanPeriod(Period.weeks(2))
      .withRecallsRecallReturnInterval(Period.months(2));

    final IndividualResource loanPolicy = loanPoliciesFixture.create(canCirculateRollingPolicy);

    useFallbackPolicies(loanPolicy.getId(),
      requestPoliciesFixture.allowAllRequestPolicy().getId(),
      noticePoliciesFixture.create(noticePolicy).getId(),
      overdueFinePoliciesFixture.facultyStandard().getId(),
      lostItemFeePoliciesFixture.facultyStandard().getId());

    final IndividualResource loan = checkOutFixture.checkOutByBarcode(
      smallAngryPlanet, steve, ClockUtil.getDateTime());

    final String originalDueDate = loan.getJson().getString("dueDate");

    // charlotte places recall request on smallAngryPlanet
    requestsFixture.placeHoldShelfRequest(
      smallAngryPlanet, charlotte, ClockUtil.getDateTime().minusHours(1), RequestType.RECALL.getValue());

    JsonObject storedLoan = loansStorageClient.getById(loan.getId()).getJson();

    assertThat("due date is the original date",
      storedLoan.getString("dueDate"), not(originalDueDate));

    final String expectedDueDate = formatDateTime(ClockUtil.getDateTime().plusMonths(2));
    assertThat("due date is not the recall due date (2 months)",
      storedLoan.getString("dueDate"), is(expectedDueDate));

    // charlotte checks out interestingTimes
    checkOutFixture.checkOutByBarcode(interestingTimes, charlotte);

    // jessica places recall request on interestingTimes
    IndividualResource requestByJessica = requestsFixture.placeHoldShelfRequest(
      interestingTimes, jessica, ClockUtil.getDateTime(), RequestType.RECALL.getValue());

    // There should be 2 notices for each recall
    waitAtMost(1, SECONDS)
      .until(() -> patronNoticesForRecipientWasSent(steve));

    waitAtMost(1, SECONDS)
      .until(() -> patronNoticesForRecipientWasSent(charlotte));

    verifyNumberOfSentNotices(2);
    verifyNumberOfPublishedEvents(NOTICE, 2);
    verifyNumberOfPublishedEvents(NOTICE_ERROR, 0);

    // move jessica's recall request from interestingTimes to smallAngryPlanet
    IndividualResource moveRequest = requestsFixture.move(new MoveRequestBuilder(
      requestByJessica.getId(),
      smallAngryPlanet.getId(),
      RequestType.RECALL.getValue()));

    assertThat("Move request should have correct item id",
      moveRequest.getJson().getString("itemId"), is(smallAngryPlanet.getId().toString()));

    assertThat("Move request should have correct type",
      moveRequest.getJson().getString("requestType"), is(RequestType.RECALL.getValue()));

    storedLoan = loansStorageClient.getById(loan.getId()).getJson();

    assertThat("due date has changed",
      storedLoan.getString("dueDate"), is(expectedDueDate));

    assertThat("move recall request unexpectedly sent another patron notice",
      FakeModNotify.getSentPatronNotices(), hasSize(2));

    verifyNumberOfSentNotices(2);
    verifyNumberOfPublishedEvents(NOTICE, 2);
    verifyNumberOfPublishedEvents(NOTICE_ERROR, 0);
  }

  private boolean patronNoticesForRecipientWasSent(IndividualResource steve) {
    return FakeModNotify.getSentPatronNotices()
      .stream()
      .anyMatch(notice -> StringUtils.equals(
        notice.getString("recipientId"),
        steve.getId().toString())
      );
  }

  private void setRules(String rules) {
    try {
      circulationRulesFixture.updateCirculationRules(rules);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
