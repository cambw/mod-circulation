package org.folio.circulation.domain;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.folio.circulation.domain.representations.CallNumberComponentsRepresentation.createCallNumberComponents;
import static org.folio.circulation.domain.representations.ContributorsToNamesMapper.mapContributorsToNamesOnly;
import static org.folio.circulation.domain.representations.ItemProperties.CALL_NUMBER_COMPONENTS;
import static org.folio.circulation.support.json.JsonPropertyFetcher.copyProperty;
import static org.folio.circulation.support.json.JsonPropertyWriter.write;
import static org.folio.circulation.support.utils.DateFormatUtil.formatDateTime;

import java.lang.invoke.MethodHandles;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class RequestRepresentation {
  private static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

  private static final String PICKUP_SERVICE_POINT = "pickupServicePoint";

  public JsonObject extendedRepresentation(Request request) {
    final JsonObject requestRepresentation = request.asJson();

    addAdditionalItemProperties(requestRepresentation, request.getItem());
    addAdditionalLoanProperties(requestRepresentation, request.getLoan());
    addAdditionalRequesterProperties(requestRepresentation, request.getRequester());
    addAdditionalProxyProperties(requestRepresentation, request.getProxy());
    addAdditionalServicePointProperties(requestRepresentation, request.getPickupServicePoint());
    addDeliveryAddress(requestRepresentation, request, request.getRequester());

    return requestRepresentation;
  }

  private static void addAdditionalRequesterProperties(JsonObject request, User requester) {
    if (requester == null) {
      String msg = "Unable to add requester properties to the request: {}, requester is null.";
      log.info(msg, request.getString("id"));
      return;
    }

    request.put("requester", userSummary(requester));
  }

  private static void addAdditionalProxyProperties(JsonObject request, User proxy) {
    if (proxy == null) {
      String msg = "Unable to add proxy properties to request {}, proxy object is null";
      log.info(msg, request.getString("id"));
      return;
    }

    request.put("proxy", userSummary(proxy));
  }

  private static void addAdditionalItemProperties(JsonObject request, Item item) {
    if (item == null || item.isNotFound()) {
      logUnableAddItemToTheRequest(request, item);
      return;
    }

    JsonObject itemSummary = request.containsKey("item")
      ? request.getJsonObject("item")
      : new JsonObject();

    write(itemSummary, "holdingsRecordId", item.getHoldingsRecordId());
    write(itemSummary, "instanceId", item.getInstanceId());


    final Location location = item.getLocation();

    if (location != null) {
      write(itemSummary, "location", locationSummary(location));
    }

    JsonArray contributorNames = mapContributorsToNamesOnly(item.getContributors());

    if (contributorNames != null) {
      itemSummary.put("contributorNames", contributorNames);
    }

    String enumeration = item.getEnumeration();
    if (enumeration != null) {
      itemSummary.put("enumeration", enumeration);
    }
    write(itemSummary, "volume", item.getVolume());
    write(itemSummary, "chronology", item.getChronology());

    ItemStatus status = item.getStatus();
    if (status != null) {
      String statusValue = status.getValue();
      itemSummary.put("status", statusValue);
    }

    write(itemSummary, "callNumber", item.getCallNumber());
    write(itemSummary, CALL_NUMBER_COMPONENTS, createCallNumberComponents(item.getCallNumberComponents()));
    write(itemSummary, "copyNumber", item.getCopyNumber());
  }

  private static JsonObject locationSummary(Location location) {
    JsonObject locationSummary = new JsonObject();

    write(locationSummary, "name", location.getName());
    write(locationSummary, "libraryName", location.getLibraryName());
    write(locationSummary, "code", location.getCode());
    return locationSummary;
  }
  private static void logUnableAddItemToTheRequest(JsonObject request, Item item) {
    String reason = isNull(item) ? "null" : "not found";
    String msg = "Unable to add item properties to the request: {}, item is {}";
    log.info(msg, request.getString("id"), reason);
  }

  private static void addDeliveryAddress(
    JsonObject requestRepresentation,
    Request request,
    User requester) {

    if (isNull(requestRepresentation)
      || isNull(request)
      || isNull(requester)) {
      log.info("Unable to add delivery address. Request is null or request is null or request representation is null.");
      return;
    }

    ofNullable(request.getDeliveryAddressTypeId())
      .map(requester::getAddressByType)
      .ifPresent(address -> {
        JsonObject jsonObject = new JsonObject();

        copyProperty(address, jsonObject, "addressTypeId");
        copyProperty(address, jsonObject, "addressLine1");
        copyProperty(address, jsonObject, "addressLine2");
        copyProperty(address, jsonObject, "city");
        copyProperty(address, jsonObject, "region");
        copyProperty(address, jsonObject, "postalCode");
        copyProperty(address, jsonObject, "countryId");

        requestRepresentation.put("deliveryAddress", jsonObject);
      });
  }

  private static void addAdditionalLoanProperties(JsonObject request, Loan loan) {
    if (loan == null || loan.isClosed()) {
      String reason = isNull(loan) ? "null" : "closed";
      log.info("Unable to add loan properties to request {}, loan is {}", request.getString("id"), reason);
      return;
    }

    JsonObject loanSummary = request.containsKey("loan")
      ? request.getJsonObject("loan")
      : new JsonObject();

    if (loan.getDueDate() != null) {
      loanSummary.put("dueDate", formatDateTime(loan.getDueDate()));
      log.info("Adding loan properties to request {}", request.getString("id"));
    }

    request.put("loan", loanSummary);
  }

  private static void addAdditionalServicePointProperties(JsonObject request,
    ServicePoint servicePoint) {
    if (servicePoint == null) {
      String msg = "Unable to add servicepoint properties to request {}, servicepoint is null";
      log.info(msg, request.getString("id"));
      return;
    }
    JsonObject spSummary = request.containsKey(PICKUP_SERVICE_POINT)
      ? request.getJsonObject(PICKUP_SERVICE_POINT)
      : new JsonObject();

    spSummary.put("name", servicePoint.getName());
    spSummary.put("code", servicePoint.getCode());
    spSummary.put("discoveryDisplayName", servicePoint.getDiscoveryDisplayName());
    spSummary.put("description", servicePoint.getDescription());
    spSummary.put("shelvingLagTime", servicePoint.getShelvingLagTime());
    spSummary.put("pickupLocation", servicePoint.isPickupLocation());

    request.put(PICKUP_SERVICE_POINT, spSummary);
  }

  private static JsonObject userSummary(User user) {
    JsonObject userSummary = new JsonObject();

    write(userSummary, "lastName", user.getLastName());
    write(userSummary, "firstName", user.getFirstName());
    write(userSummary, "middleName", user.getMiddleName());
    write(userSummary, "barcode", user.getBarcode());

    final PatronGroup patronGroup = user.getPatronGroup();

    if (patronGroup != null) {
      JsonObject patronGroupSummary = new JsonObject();
      write(patronGroupSummary, "id", patronGroup.getId());
      write(patronGroupSummary, "group", patronGroup.getGroup());
      write(patronGroupSummary, "desc", patronGroup.getDesc());
      userSummary.put("patronGroup", patronGroupSummary);
    }

    String patronGroupId = user.getPatronGroupId();

    if (patronGroupId != null) {
      userSummary.put("patronGroupId", patronGroupId);
    }

    return userSummary;
  }
}

