package api.requests.scenarios;

import static api.support.builders.ItemBuilder.AVAILABLE;
import static api.support.builders.ItemBuilder.PAGED;
import static api.support.matchers.ItemStatusCodeMatcher.hasItemStatus;
import static api.support.matchers.TextDateTimeMatcher.isEquivalentTo;
import static api.support.matchers.UUIDMatcher.is;
import static api.support.matchers.ValidationErrorMatchers.hasErrorWith;
import static api.support.matchers.ValidationErrorMatchers.hasMessage;
import static api.support.matchers.ValidationErrorMatchers.hasUUIDParameter;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.time.LocalDate;

import org.folio.circulation.support.http.client.Response;
import org.folio.circulation.support.utils.ClockUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

import api.support.APITests;
import api.support.builders.RequestBuilder;
import api.support.http.IndividualResource;
import io.vertx.core.json.JsonObject;

class ClosedRequestTests extends APITests {
  @Test
  void canCancelARequest() {

    IndividualResource smallAngryPlanet = itemsFixture.basedUponSmallAngryPlanet();

    checkOutFixture.checkOutByBarcode(smallAngryPlanet, usersFixture.jessica());

    IndividualResource requester = usersFixture.steve();

    DateTime requestDate = new DateTime(2017, 7, 22, 10, 22, 54, DateTimeZone.UTC);

    final IndividualResource request =
      requestsFixture.placeHoldShelfRequest(smallAngryPlanet, requester, requestDate);

    DateTime cancelDate = new DateTime(2018, 1, 14, 8, 30, 45, DateTimeZone.UTC);

    final IndividualResource courseReservesCancellationReason
      = cancellationReasonsFixture.courseReserves();

    requestsClient.replace(request.getId(),
      RequestBuilder.from(request)
        .cancelled()
        .withCancellationReasonId(courseReservesCancellationReason.getId())
        .withCancelledByUserId(requester.getId())
        .withCancelledDate(cancelDate));

    IndividualResource getRequest = requestsClient.get(request.getId());

    JsonObject getRepresentation = getRequest.getJson();

    assertThat(getRepresentation.getString("id"), is(request.getId()));
    assertThat(getRepresentation.getString("status"), is("Closed - Cancelled"));
    assertThat(getRepresentation.getString("cancelledByUserId"), is(requester.getId().toString()));
    assertThat(getRepresentation.getString("cancelledDate"), isEquivalentTo(cancelDate));
  }

  @Test
  void cannotEditCancelledRequest() {

    IndividualResource smallAngryPlanet = itemsFixture.basedUponSmallAngryPlanet();

    checkOutFixture.checkOutByBarcode(smallAngryPlanet, usersFixture.jessica());

    DateTime requestDate = new DateTime(2017, 7, 22, 10, 22, 54, DateTimeZone.UTC);

    final IndividualResource request =
      requestsFixture.placeHoldShelfRequest(smallAngryPlanet,
        usersFixture.steve(), requestDate);

    requestsFixture.cancelRequest(request);

    Response response = requestsClient.attemptReplace(request.getId(),
      RequestBuilder.from(request)
        .open()
        .withRequestExpiration(LocalDate.of(2018, 3, 14)));

    assertThat(response.getStatusCode(), is(422));

    assertThat(response.getJson(), hasErrorWith(allOf(
      hasMessage("Cannot edit a closed request"),
      hasUUIDParameter("id", request.getId()))));
  }

  @Test
  void cannotEditFulfilledRequest() {

    IndividualResource smallAngryPlanet = itemsFixture.basedUponSmallAngryPlanet();
    final IndividualResource jessica = usersFixture.jessica();
    final IndividualResource steve = usersFixture.steve();

    checkOutFixture.checkOutByBarcode(smallAngryPlanet, jessica);

    DateTime requestDate = new DateTime(2018, 6, 22, 10, 22, 54, DateTimeZone.UTC);

    final IndividualResource request =
      requestsFixture.placeHoldShelfRequest(smallAngryPlanet,
        steve, requestDate);

    checkInFixture.checkInByBarcode(smallAngryPlanet);

    checkOutFixture.checkOutByBarcode(smallAngryPlanet, steve,
      new DateTime(2018, 7, 5, 14, 48, 23, DateTimeZone.UTC));

    Response response = requestsClient.attemptReplace(request.getId(),
      RequestBuilder.from(request)
        .open()
        .withRequestExpiration(LocalDate.of(2018, 3, 14)));

    assertThat(response.getStatusCode(), is(422));

    assertThat(response.getJson(), hasErrorWith(allOf(
      hasMessage("Cannot edit a closed request"),
      hasUUIDParameter("id", request.getId()))));
  }

  @Test
  void canCancelARequestLeavingEmptyQueueAndItemStatusChange() {

    IndividualResource smallAngryPlanet = itemsFixture.basedUponSmallAngryPlanet();

    IndividualResource jessica = usersFixture.jessica();

    // make requests for smallAngryPlanet
    IndividualResource requestByJessica = requestsFixture.place(new RequestBuilder()
      .page()
      .fulfilToHoldShelf()
      .withItemId(smallAngryPlanet.getId())
      .withRequestDate(ClockUtil.getDateTime().minusHours(4))
      .withRequesterId(jessica.getId())
      .withPickupServicePointId(servicePointsFixture.cd1().getId()));

    smallAngryPlanet = itemsClient.get(smallAngryPlanet);
    assertThat(smallAngryPlanet, hasItemStatus(PAGED));

    final IndividualResource courseReservesCancellationReason
      = cancellationReasonsFixture.courseReserves();

    requestsClient.replace(requestByJessica.getId(),
        RequestBuilder.from(requestByJessica)
          .cancelled()
          .withCancellationReasonId(courseReservesCancellationReason.getId())
          .withCancelledByUserId(jessica.getId())
          .withCancelledDate(ClockUtil.getDateTime().minusHours(3)));

    smallAngryPlanet = itemsClient.get(smallAngryPlanet);
    assertThat(smallAngryPlanet, hasItemStatus(AVAILABLE));
  }
}
