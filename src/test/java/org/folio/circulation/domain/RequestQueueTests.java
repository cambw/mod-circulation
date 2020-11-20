package org.folio.circulation.domain;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

import java.util.UUID;

import org.junit.Test;

import api.support.builders.RequestBuilder;

public class RequestQueueTests {
  @Test
  public void canRemoveOnlyRequestInQueue() {
    final UUID itemId = UUID.randomUUID();

    Request onlyRequest = requestAtPosition(itemId, 1);

    final var requestQueue = new RequestQueue(asList(onlyRequest));

    final var updatedQueue = requestQueue.remove(onlyRequest);

    assertThat("Should have no requests", updatedQueue.size(), is(0));

    assertThat("Should not contain removed request",
      updatedQueue.contains(onlyRequest), is(false));

    assertThat("Removed request should not have a position",
      onlyRequest.getPosition(), is(nullValue()));

    assertThat("No requests have changed position",
      updatedQueue.getRequestsWithChangedPosition(), is(empty()));
  }

  @Test
  public void canRemoveLastRequestInQueue() {
    final UUID itemId = UUID.randomUUID();

    Request firstRequest = requestAtPosition(itemId, 1);
    Request secondRequest = requestAtPosition(itemId, 2);
    Request thirdRequest = requestAtPosition(itemId, 3);

    final var requestQueue = new RequestQueue(
      asList(firstRequest, secondRequest, thirdRequest));

    final var updatedQueue = requestQueue.remove(thirdRequest);

    assertThat("Should have two requests", updatedQueue.size(), is(2));

    assertThat("Should not contain removed request",
      updatedQueue.contains(thirdRequest), is(false));

    assertThat("Removed request should not have a position",
      thirdRequest.getPosition(), is(nullValue()));

    assertThat("Should contain first request",
      updatedQueue.contains(firstRequest), is(true));

    assertThat("Should contain second request",
      updatedQueue.contains(secondRequest), is(true));

    assertThat("First request should still in correct position",
      firstRequest.getPosition(), is(1));

    assertThat("Second request should still in correct position",
      secondRequest.getPosition(), is(2));

    assertThat("No requests have changed position",
      updatedQueue.getRequestsWithChangedPosition(), is(empty()));
  }

  @Test
  public void canRemoveFirstRequestInQueue() {
    final UUID itemId = UUID.randomUUID();

    Request firstRequest = requestAtPosition(itemId, 1);
    Request secondRequest = requestAtPosition(itemId, 2);
    Request thirdRequest = requestAtPosition(itemId, 3);

    final var requestQueue = new RequestQueue(
      asList(firstRequest, secondRequest, thirdRequest));

    final var updatedQueue = requestQueue.remove(firstRequest);

    assertThat("Should have two requests", updatedQueue.size(), is(2));

    assertThat("Should not contain removed request",
      updatedQueue.contains(firstRequest), is(false));

    assertThat("Removed request should not have a position",
      firstRequest.getPosition(), is(nullValue()));

    assertThat("Should contain second request",
      updatedQueue.contains(secondRequest), is(true));

    assertThat("Should contain third request",
      updatedQueue.contains(thirdRequest), is(true));


    assertThat("Second request should have moved up the queue",
      secondRequest.getPosition(), is(1));

    assertThat("Third request should have moved up the queue",
      thirdRequest.getPosition(), is(2));

    assertThat("Second and third requests have changed position",
      updatedQueue.getRequestsWithChangedPosition(), contains(thirdRequest, secondRequest));
  }

  @Test
  public void canRemoveMiddleRequestInQueue() {
    final UUID itemId = UUID.randomUUID();

    Request firstRequest = requestAtPosition(itemId, 1);
    Request secondRequest = requestAtPosition(itemId, 2);
    Request thirdRequest = requestAtPosition(itemId, 3);
    Request fourthRequest = requestAtPosition(itemId, 4);

    final var requestQueue = new RequestQueue(
      asList(firstRequest, secondRequest, thirdRequest, fourthRequest));

    final var updatedQueue = requestQueue.remove(secondRequest);

    assertThat("Should have three requests", updatedQueue.size(), is(3));

    assertThat("Should not contain removed request",
      updatedQueue.contains(secondRequest), is(false));

    assertThat("Removed request should not have a position",
      secondRequest.getPosition(), is(nullValue()));

    assertThat("Should contain first request",
      updatedQueue.contains(firstRequest), is(true));

    assertThat("Should contain third request",
      updatedQueue.contains(thirdRequest), is(true));

    assertThat("Should contain fourth request",
      updatedQueue.contains(fourthRequest), is(true));

    assertThat("First request should be at the same position",
      firstRequest.getPosition(), is(1));

    assertThat("Third request should have moved up the queue",
      thirdRequest.getPosition(), is(2));

    assertThat("Fourth request should have moved up the queue",
      fourthRequest.getPosition(), is(3));

    assertThat("Second and third requests have changed position",
      updatedQueue.getRequestsWithChangedPosition(), contains(fourthRequest, thirdRequest));
  }

  private Request requestAtPosition(UUID itemId, Integer position) {
    return Request.from(new RequestBuilder()
      .withId(UUID.randomUUID())
      .open()
      .hold()
      .withItemId(itemId)
      .withPosition(position)
      .create());
  }
}
