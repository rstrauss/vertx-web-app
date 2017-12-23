package org.myorg.backend;

import java.util.List;

import org.myorg.AppProperty;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;

/**
 * Adds a 2-tier session handler to the router, after making sure there's already a
 * cookie handler on the router.
 */
public class SessionManager {
    private final Logger logger = LoggerFactory.getLogger(SessionManager.class);

    private final String IsPublicDeviceCookieName = "isOnSharedDevice";
    private final String IsPrivateCookieValue = "private";
    private final String SessionIdCookieName = "id";

    final Vertx vertx;
    final SessionHandler sessionHandler;

    public SessionManager(Vertx vertx) {
        this.vertx = vertx;

        // Use our own session handler that customizes a session for public and private client devices
        sessionHandler = new TwoTierSessionHandler(vertx, IsPublicDeviceCookieName, IsPrivateCookieValue)
                .setSessionTimeout(35, 40*24);  // 35 minutes, 40 days
        sessionHandler
        .setSessionCookieName(SessionIdCookieName)
        .setMinLength(20)
        .setNagHttps(true)
        .setCookieSecureFlag(AppProperty.IsHttpSecure.getBoolean())
        .setCookieHttpOnlyFlag(true); // means JavaScript shouldn't access this
    }


    public Router addToRouter(Router router) {
        ensureCookieHandler(router);

        // Make sure all requests are routed through the session handler
        logger.info("Adding to the router a TwoTierSessionHandler");
        router.route().handler(sessionHandler);

        return router;
    }


    void ensureCookieHandler(Router router) {
        if (!hasCookieHandler(router)) {
            logger.info("Adding to the router a CookieHandler");
            router.route().handler(CookieHandler.create());
        }
    }


    boolean hasCookieHandler(Router router) {
        final List<Route> list = router.getRoutes();
        for (final Route route: list) {
            if (route instanceof CookieHandler)
                return true;
        }
        return false;
    }
}
