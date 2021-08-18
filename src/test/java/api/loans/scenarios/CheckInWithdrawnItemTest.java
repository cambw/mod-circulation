package api.loans.scenarios;

import static api.support.matchers.ItemMatchers.isAvailable;
import static api.support.matchers.ItemMatchers.isAwaitingPickup;
import static api.support.matchers.ItemMatchers.isInTransit;
import static api.support.matchers.ItemMatchers.isWithdrawn;
import static api.support.matchers.RequestMatchers.isOpenAwaitingPickup;
import static api.support.matchers.RequestMatchers.isOpenInTransit;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

import api.support.APITests;
import api.support.builders.ItemBuilder;
import api.support.builders.RequestBuilder;
import api.support.http.IndividualResource;
import api.support.http.ItemResource;
import io.vertx.core.json.JsonObject;

public class CheckInWithdrawnItemTest extends APITests {

  @Test
  public void canCheckInAtHomeLocation() {
    final ItemResource item = itemsFixture
      .basedUponSmallAngryPlanet(ItemBuilder::withdrawn);

    assertThat(item.getJson(), isWithdrawn());

    checkInFixture.checkInByBarcode(item);

    assertThat(itemsClient.getById(item.getId()).getJson(), isAvailable());
  }

  @Test
  public void canCheckInAtNonHomeLocation() {
    final ItemResource item = itemsFixture
      .basedUponSmallAngryPlanet(ItemBuilder::withdrawn);

    assertThat(item.getJson(), isWithdrawn());

    checkInFixture.checkInByBarcode(item, servicePointsFixture.cd2().getId());

    assertThat(itemsClient.getById(item.getId()).getJson(), isInTransit());
  }

  @Test
  public void shouldStartRequestFulfillmentIfCheckedInAtPickupLocation() {
    final ItemResource item = itemsFixture.basedUponSmallAngryPlanet();

    final IndividualResource request = requestsFixture.place(new RequestBuilder()
      .forItem(item)
      .page()
      .by(usersFixture.jessica())
      .fulfilToHoldShelf()
      .withPickupServicePointId(servicePointsFixture.cd1().getId()));

    itemsClient.replace(item.getId(), item.getJson().copy()
      .put("status", new JsonObject().put("name", "Withdrawn")));

    checkInFixture.checkInByBarcode(item);

    assertThat(itemsClient.getById(item.getId()).getJson(), isAwaitingPickup());

    assertThat(requestsFixture.getById(request.getId()).getJson(), isOpenAwaitingPickup());
  }

  @Test
  public void shouldStartRequestFulfillmentIfCheckedInAtNotPickupLocation() {
    final ItemResource item = itemsFixture.basedUponSmallAngryPlanet();

    final IndividualResource request = requestsFixture.place(new RequestBuilder()
      .forItem(item)
      .page()
      .by(usersFixture.jessica())
      .fulfilToHoldShelf()
      .withPickupServicePointId(servicePointsFixture.cd1().getId()));

    itemsClient.replace(item.getId(), item.getJson().copy()
      .put("status", new JsonObject().put("name", "Withdrawn")));

    checkInFixture.checkInByBarcode(item, servicePointsFixture.cd2().getId());

    assertThat(itemsClient.getById(item.getId()).getJson(), isInTransit());

    assertThat(requestsFixture.getById(request.getId()).getJson(), isOpenInTransit());
  }
}
