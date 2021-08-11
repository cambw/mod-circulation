package org.folio.circulation.domain.validation;

import static api.support.matchers.ValidationErrorMatchers.hasMessage;
import static api.support.matchers.ValidationErrorMatchers.hasParameter;
import static api.support.matchers.ValidationErrorMatchers.isErrorWith;
import static org.folio.circulation.support.ValidationErrorFailure.singleValidationError;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import org.folio.circulation.domain.Item;
import org.folio.circulation.support.ValidationErrorFailure;
import org.folio.circulation.support.results.Result;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.json.JsonObject;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class CheckInValidatorsTests {
  @Test
  @Parameters({
    "Available",
    "Long missing",
    "In process (non-requestable)",
    "Restricted",
    "Unavailable",
    "Unknown"
  })
  public void canCheckInItemInAllowedStatus(String itemStatus) {
    final var validator = new CheckInValidators(this::validationError);

    final var item = itemIn(itemStatus);

    final var validationResult = validator
      .refuseWhenItemIsNotAllowedForCheckIn(item);

    assertTrue(validationResult.succeeded());
    assertThat(validationResult.value(), is(item));
  }

  @Test
  @Parameters({
    "Intellectual item"
  })
  public void cannotCheckInItemInDisallowedStatus(String itemStatus) {
    CheckInValidators validator = new CheckInValidators(this::validationError);

    Result<Item> validationResult = validator
      .refuseWhenItemIsNotAllowedForCheckIn(itemIn(itemStatus));

    assertTrue(validationResult.failed());
    assertThat(validationResult.cause(), isErrorWith(allOf(
      hasMessage("error"),
      hasParameter("barcode", "some-barcode"))));
  }

  private Item itemIn(String itemStatus) {
    JsonObject itemRepresentation = new JsonObject()
      .put("status", new JsonObject().put("name", itemStatus));

    return Item.from(itemRepresentation);
  }

  private ValidationErrorFailure validationError(Item item) {
    return singleValidationError("error", "barcode", "some-barcode");
  }
}
