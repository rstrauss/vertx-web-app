package org.myorg.frontend;

import org.myorg.AppConfig;
import org.myorg.AppProperty;
import org.myorg.utils.Runner;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * This vertical handles static resources, such as the index.html and all the angular code
 * that accompanies it. It will run on 8000 and deliver everything the user needs.
 *
 */
public class StaticServer extends AbstractVerticle {
    private final Logger logger = LoggerFactory.getLogger(StaticServer.class);

    // Convenience method so it can run in the IDE
    public static void main(String[] args) {
        Runner.runExample(StaticServer.class);
    }

    @Override
    public void start(Future<Void> fut) {
        AppConfig.sayLogLevel(this.getClass());
        final Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());  // sets request body on the RoutingContext & handles file uploads

        createStaticHandler(router);
    }

    private void createStaticHandler(Router router) {
        final StaticHandler staticHandler = StaticHandler.create();
        staticHandler.setIndexPage("index.html");
        //staticHandler.setWebRoot("/Users/ras2/git-bin/pc-vert/target");
        router.route().handler(staticHandler);  // for the static content

        final int port = AppProperty.WebPort.getInt();
        vertx.createHttpServer().requestHandler(router::accept).listen(port);
        logger.info("Created the static handler at port: "+port);
    }

}
