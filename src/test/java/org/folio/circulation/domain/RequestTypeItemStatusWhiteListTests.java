package org.folio.circulation.domain;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.folio.circulation.domain.ItemStatus.AGED_TO_LOST;
import static org.folio.circulation.domain.ItemStatus.AVAILABLE;
import static org.folio.circulation.domain.ItemStatus.AWAITING_DELIVERY;
import static org.folio.circulation.domain.ItemStatus.CHECKED_OUT;
import static org.folio.circulation.domain.ItemStatus.CLAIMED_RETURNED;
import static org.folio.circulation.domain.ItemStatus.DECLARED_LOST;
import static org.folio.circulation.domain.ItemStatus.INTELLECTUAL_ITEM;
import static org.folio.circulation.domain.ItemStatus.IN_PROCESS;
import static org.folio.circulation.domain.ItemStatus.IN_PROCESS_NON_REQUESTABLE;
import static org.folio.circulation.domain.ItemStatus.LONG_MISSING;
import static org.folio.circulation.domain.ItemStatus.LOST_AND_PAID;
import static org.folio.circulation.domain.ItemStatus.ON_ORDER;
import static org.folio.circulation.domain.ItemStatus.PAGED;
import static org.folio.circulation.domain.ItemStatus.RESTRICTED;
import static org.folio.circulation.domain.ItemStatus.UNAVAILABLE;
import static org.folio.circulation.domain.ItemStatus.UNKNOWN;
import static org.folio.circulation.domain.ItemStatus.WITHDRAWN;
import static org.folio.circulation.domain.RequestType.HOLD;
import static org.folio.circulation.domain.RequestType.PAGE;
import static org.folio.circulation.domain.RequestType.RECALL;
import static org.folio.circulation.domain.RequestType.from;
import static org.folio.circulation.domain.RequestTypeItemStatusWhiteList.canCreateRequestForItem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class  RequestTypeItemStatusWhiteListTests {

  @Test
  public void canCreateHoldRequestWhenItemStatusCheckedOut() {
    assertTrue(canCreateRequestForItem(CHECKED_OUT, HOLD));
  }

  @Test
  public void canCreateRecallRequestWhenItemStatusCheckedOut() {
    assertTrue(canCreateRequestForItem(CHECKED_OUT, RECALL));
  }

  @Test
  public void cannotCreatePagedRequestWhenItemStatusCheckedOut() {
    assertFalse(canCreateRequestForItem(CHECKED_OUT, PAGE));
  }

  @Test
  public void cannotCreateNoneRequestWhenItemStatusIsAnything() {
    assertFalse(canCreateRequestForItem(CHECKED_OUT, RequestType.NONE));
  }

  @Test
  public void canCreateHoldRequestWhenItemStatusOnOrder() {
    assertTrue(canCreateRequestForItem(ON_ORDER, HOLD));
  }

  @Test
  public void canCreateRecallRequestWhenItemStatusOnOrder() {
    assertTrue(canCreateRequestForItem(ON_ORDER, RECALL));
  }

  @Test
  public void cannotCreatePagedRequestWhenItemStatusOnOrder() {
    assertFalse(canCreateRequestForItem(ON_ORDER, PAGE));
  }

  @Test
  public void canCreateHoldRequestWhenItemStatusInProcess() {
    assertTrue(canCreateRequestForItem(IN_PROCESS, HOLD));
  }

  @Test
  public void canCreateRecallRequestWhenItemStatusInProcess() {
    assertTrue(canCreateRequestForItem(IN_PROCESS, RECALL));
  }

  @Test
  public void cannotCreatePagedRequestWhenItemStatusInProcess() {
    assertFalse(canCreateRequestForItem(IN_PROCESS, PAGE));
  }

  @Test
  public void canCreateRecallRequestWhenItemStatusPaged() {
    assertTrue(canCreateRequestForItem(PAGED, RECALL));
  }

  @Test
  public void cannotCreatePagedRequestWhenItemStatusIsNone() {
    assertFalse(canCreateRequestForItem(ItemStatus.NONE, PAGE));
  }

  @Test
  public void canCreatePagedRequestWhenItemStatusIsAvailable() {
    assertTrue(canCreateRequestForItem(AVAILABLE, PAGE));
  }

  @Test
  public void canCreateHoldRequestWhenItemStatusAwaitingDelivery() {
    assertTrue(canCreateRequestForItem(AWAITING_DELIVERY, HOLD));
  }

  @Test
  public void canCreateRecallRequestWhenItemStatusAwaitingDelivery() {
    assertTrue(canCreateRequestForItem(AWAITING_DELIVERY, RECALL));
  }

  @Test
  public void cannotCreatePagedRequestWhenItemStatusAwaitingDelivery() {
    assertFalse(canCreateRequestForItem(AWAITING_DELIVERY, PAGE));
  }

  @Test
  public void cannotCreateNoneRequestWhenItemStatusAwaitingDelivery() {
    assertFalse(canCreateRequestForItem(AWAITING_DELIVERY, RequestType.NONE));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "Hold",
    "Recall",
    "Page"
  })
  public void canCreateRequestWhenItemIsRestricted(String requestType) {
    assertTrue(canCreateRequestForItem(RESTRICTED, from(requestType)));
  }

  @ParameterizedTest
  @EmptySource
  @ValueSource(strings = {
    "Hold",
    "Recall",
    "Page"
  })
  public void cannotCreateRequestWhenItemStatusDeclaredLostItem(String requestType) {
    assertFalse(canCreateRequestForItem(DECLARED_LOST, from(requestType)));
  }

  @ParameterizedTest
  @EmptySource
  @ValueSource(strings = {
    "Hold",
    "Recall",
    "Page"
  })
  public void cannotCreateRequestWhenItemStatusClaimedReturned(String requestType) {
    assertFalse(canCreateRequestForItem(CLAIMED_RETURNED, from(requestType)));
  }

  @ParameterizedTest
  @EmptySource
  @ValueSource(strings = {
    "Hold",
    "Recall",
    "Page"
  })
  public void cannotCreateRequestWhenItemStatusWithdrawn(String requestType) {
    assertFalse(canCreateRequestForItem(WITHDRAWN, from(requestType)));
  }

  @ParameterizedTest
  @EmptySource
  @ValueSource(strings = {
    "Hold",
    "Recall",
    "Page"
  })
  public void cannotCreateRequestWhenItemStatusLostAndPaid(String requestType) {
    assertFalse(canCreateRequestForItem(LOST_AND_PAID, from(requestType)));
  }

  @ParameterizedTest
  @EmptySource
  @ValueSource(strings = {
    "Hold",
    "Recall",
    "Page"
  })
  public void cannotCreateRequestWhenItemIsAgedToLost(String requestType) {
    assertFalse(canCreateRequestForItem(AGED_TO_LOST, from(requestType)));
  }

  @ParameterizedTest
  @EmptySource
  @ValueSource(strings = {
    "Hold",
    "Recall",
    "Page"
  })
  public void cannotCreateRequestWhenItemHasIntellectualItemStatus(String requestType) {
    assertFalse(canCreateRequestForItem(INTELLECTUAL_ITEM, from(requestType)));
  }

  @ParameterizedTest
  @EmptySource
  @ValueSource(strings = {
    "Hold",
    "Recall",
    "Page"
  })
  public void cannotCreateRequestWhenItemIsInProcessNonRequestable(String requestType) {
    assertFalse(canCreateRequestForItem(IN_PROCESS_NON_REQUESTABLE, from(requestType)));
  }

  @ParameterizedTest
  @EmptySource
  @ValueSource(strings = {
    "Hold",
    "Recall",
    "Page"
  })
  public void cannotCreateRequestWhenItemIsLongMissing(String requestType) {
    assertFalse(canCreateRequestForItem(LONG_MISSING, from(requestType)));
  }

  @ParameterizedTest
  @EmptySource
  @ValueSource(strings = {
    "Hold",
    "Recall",
    "Page"
  })
  public void cannotCreateRequestWhenItemIsUnavailable(String requestType) {
    assertFalse(canCreateRequestForItem(UNAVAILABLE, from(requestType)));
  }

  @ParameterizedTest
  @EmptySource
  @ValueSource(strings = {
    "Hold",
    "Recall",
    "Page"
  })
  public void cannotCreateRequestWhenItemIsUnknown(String requestType) {
    assertFalse(canCreateRequestForItem(UNKNOWN, from(requestType)));
  }
}
