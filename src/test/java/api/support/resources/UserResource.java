package api.support.resources;

import org.folio.circulation.support.http.client.IndividualResource;

public class UserResource extends IndividualResource {
  public UserResource(IndividualResource resource) {
    super(resource.getResponse());
  }
}
