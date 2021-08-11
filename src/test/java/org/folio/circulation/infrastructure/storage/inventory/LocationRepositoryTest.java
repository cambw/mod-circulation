package org.folio.circulation.infrastructure.storage.inventory;

import static org.folio.circulation.infrastructure.storage.inventory.LocationRepository.using;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.folio.circulation.domain.Location;
import org.folio.circulation.support.Clients;
import org.folio.circulation.support.ServerErrorFailure;
import org.folio.circulation.support.results.Result;
import org.junit.Test;

public class LocationRepositoryTest {

  @Test
  public void shouldReturnNullWhenLocationIdIsNull() {
    final LocationRepository repository = using(mock(Clients.class));

    Result<Location> result = repository.fetchLocationById(null)
      .getNow(Result.failed(new ServerErrorFailure("Error")));

    assertTrue(result.succeeded());
    assertNull(result.value());
  }
}
