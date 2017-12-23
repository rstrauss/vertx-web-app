package org.myorg.backend;

import java.util.HashMap;

import org.myorg.AppConfig;
import org.myorg.AppProperty;
import org.myorg.backend.products.ProductHandlers;
import org.myorg.utils.Runner;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class ApiServer extends AbstractVerticle {
    private final Logger logger = LoggerFactory.getLogger(ApiServer.class);
    // Vertx vertx;  // in the superclass

    HashMap<String,JsonObject>products = new HashMap<>();

    // Convenience method so it can run in the IDE
    public static void main(String[] args) {
        Runner.runExample(ApiServer.class);
    }

    @Override
    public void start(Future<Void> fut) {
        try {
            AppConfig.sayLogLevel(this.getClass());
            MySqlDataSource.init(vertx);

            final Router router = Router.router(vertx);

            if (AppProperty.UseSessions.getBoolean())
                new SessionManager(vertx).addToRouter(router);

            // BodyHandler gathers the entire request body and sets it on the RoutingContext.
            // also handles HTTP file uploads and can be used to limit body sizes.
            // We have no plans for large requests - biggest is a report, a max of about 20k.
            router.route().handler(BodyHandler.create().setBodyLimit(25000));

            createApiHandler(fut, router);
        } catch (final Throwable t) {
            logger.error(t);
            try {
                stop();
            } catch (final Exception e) {
                // do nothing
            }
        }
    }

    private void createApiHandler(Future<Void> fut, Router router) {
        router.get("/").handler(this::handleRoot);

        final ProductHandlers productHandlers = new ProductHandlers(vertx);
        productHandlers.addProductRoutes(fut, router);

        final int port = AppProperty.ApiPort.getInt();
        vertx.createHttpServer().requestHandler(router::accept).listen(port);
        logger.info("Created the static handler at port: "+port);
    }


    private void handleRoot(RoutingContext routingContext) {
        System.out.println("ApiServer.handleRoot() was called");
        routingContext.response().end("<h1>vertx-web-app ApiServer is up</h1>"
                + "<p><a href=\"products\">Product List</a></p>");
    }


}
