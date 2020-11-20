package org.folio.circulation.domain;

import static org.folio.circulation.domain.RequestQueue.emptyQueue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.UUID;

import org.folio.circulation.domain.representations.CheckInByBarcodeRequest;
import org.joda.time.DateTime;
import org.junit.Test;

import api.support.builders.CheckInByBarcodeRequestBuilder;
import api.support.builders.ItemBuilder;
import api.support.builders.LocationBuilder;
import io.vertx.core.json.JsonObject;

public class LoanCheckInServiceTest {
  private final LoanCheckInService loanCheckInService = new LoanCheckInService();

  @Test
  public void isInHouseUseWhenServicePointIsPrimaryForHomeLocation() {
    final UUID checkInServicePoint = UUID.randomUUID();
    JsonObject itemRepresentation = new ItemBuilder()
      .available()
      .create();

    JsonObject locationRepresentation = new LocationBuilder()
      .withPrimaryServicePoint(checkInServicePoint)
      .create();

    CheckInByBarcodeRequest checkInRequest = getCheckInRequest(checkInServicePoint);

    Item item = Item.from(itemRepresentation)
      .withLocation(Location.from(locationRepresentation));

    assertTrue(loanCheckInService.isInHouseUse(item, emptyQueue(), checkInRequest));
  }

  @Test
  public void isInHouseUseWhenNonPrimaryServicePointServesHomeLocation() {
    final UUID checkInServicePoint = UUID.randomUUID();
    JsonObject itemRepresentation = new ItemBuilder()
      .available()
      .create();

    JsonObject locationRepresentation = new LocationBuilder()
      .withPrimaryServicePoint(UUID.randomUUID())
      .servedBy(checkInServicePoint)
      .create();

    CheckInByBarcodeRequest checkInRequest = getCheckInRequest(checkInServicePoint);

    Item item = Item.from(itemRepresentation)
      .withLocation(Location.from(locationRepresentation));

    assertTrue(loanCheckInService.isInHouseUse(item, emptyQueue(), checkInRequest));
  }

  @Test
  public void isNotInHouseUseWhenItemIsUnavailable() {
    final UUID checkInServicePoint = UUID.randomUUID();
    JsonObject itemRepresentation = new ItemBuilder()
      .checkOut()
      .create();

    JsonObject locationRepresentation = new LocationBuilder()
      .withPrimaryServicePoint(checkInServicePoint)
      .create();

    CheckInByBarcodeRequest checkInRequest = getCheckInRequest(checkInServicePoint);

    Item item = Item.from(itemRepresentation)
      .withLocation(Location.from(locationRepresentation));

    assertFalse(loanCheckInService.isInHouseUse(item, emptyQueue(), checkInRequest));
  }

  @Test
  public void isNotInHouseUseWhenItemIsRequested() {
    final UUID checkInServicePoint = UUID.randomUUID();
    JsonObject itemRepresentation = new ItemBuilder()
      .available()
      .create();

    JsonObject locationRepresentation = new LocationBuilder()
      .withPrimaryServicePoint(checkInServicePoint)
      .create();

    CheckInByBarcodeRequest checkInRequest = getCheckInRequest(checkInServicePoint);

    Item item = Item.from(itemRepresentation)
      .withLocation(Location.from(locationRepresentation));

    RequestQueue requestQueue = new RequestQueue(Collections
      .singleton(Request.from(new JsonObject())));

    assertFalse(loanCheckInService.isInHouseUse(item, requestQueue, checkInRequest));
  }

  @Test
  public void isNotInHouseUseWhenServicePointDoesNotServeHomeLocation() {
    JsonObject itemRepresentation = new ItemBuilder()
      .available()
      .create();

    JsonObject locationRepresentation = new LocationBuilder()
      .withPrimaryServicePoint(UUID.randomUUID())
      .servedBy(UUID.randomUUID())
      .create();

    CheckInByBarcodeRequest checkInRequest = getCheckInRequest(UUID.randomUUID());

    Item item = Item.from(itemRepresentation)
      .withLocation(Location.from(locationRepresentation));

    assertFalse(loanCheckInService.isInHouseUse(item, emptyQueue(), checkInRequest));
  }

  private CheckInByBarcodeRequest getCheckInRequest(UUID checkInServicePoint) {
    JsonObject representation = new CheckInByBarcodeRequestBuilder()
      .withItemBarcode("barcode")
      .on(DateTime.now())
      .at(checkInServicePoint)
      .create();

    return CheckInByBarcodeRequest.from(representation).value();
  }
}
