package org.folio.circulation;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.folio.circulation.support.VertxCollectionResource;

public class CirculationVerticle extends AbstractVerticle {

  HttpServer server;

  public void start(Future<Void> startFuture) {
    System.out.println("Starting inventory module");

    Router router = Router.router(vertx);

    this.server = vertx.createHttpServer();

    JsonObject config = vertx.getOrCreateContext().config();

    new VertxCollectionResource("/circulation/loans").register(router);

    server.requestHandler(router::accept)
      .listen(config.getInteger("port"), result -> {
        if (result.succeeded()) {
          System.out.println(
            String.format("Listening on %s", server.actualPort()));
          startFuture.complete();
        } else {
          startFuture.fail(result.cause());
        }
      });
  }

  public void stop(Future<Void> stopFuture) {
    System.out.println("Stopping inventory module");

    if(server != null) {
      server.close(result -> {
        if (result.succeeded()) {
          System.out.println(
            String.format("Stopped listening on %s", server.actualPort()));
          stopFuture.complete();
        } else {
          stopFuture.fail(result.cause());
        }
      });
    }
  }
}
