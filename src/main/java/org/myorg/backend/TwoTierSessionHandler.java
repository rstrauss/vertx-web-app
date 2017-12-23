package org.myorg.backend;

import org.myorg.AppProperty;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.ClusteredSessionStore;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;

/**
 * Creates a session handler that supports two kinds of sessions:
 * long, 40-day sessions if a cookie says the user is on a private computer,
 * and short, 30-minute sessions otherwise (these times are configurable);
 *
 * <p>Suggested cookie name:  isOnSharedDevice
 * <br>Suggested private computer value: private (vs shared)
 */
public class TwoTierSessionHandler implements SessionHandler {
    private final Logger logger = LoggerFactory.getLogger(TwoTierSessionHandler.class);

    final String publicComputerCookieName;
    final String privateComputerAnswer;
    final SessionHandler shortSessionHandler;
    final SessionHandler longSessionHandler;

    public TwoTierSessionHandler(Vertx vertx, String publicComputerCookieName, String privateComputerAnswer) {
        this.publicComputerCookieName = publicComputerCookieName;
        throwIfNullOrEmpty(publicComputerCookieName, "public-computer cookie name", "isOnSharedDevice?");

        this.privateComputerAnswer = privateComputerAnswer;
        throwIfNullOrEmpty(publicComputerCookieName, "public-computer non-shared value", "private?");

        SessionStore store = null;
        if (AppProperty.IsClustered.getBoolean() && !AppProperty.UsingStickySessions.getBoolean()) {
            logger.info("Using a clustered session store");
            store = ClusteredSessionStore.create(vertx);
        } else {
            logger.info("Using a regular (non-clustered) session store");
            LocalSessionStore.create(vertx);
        }

        shortSessionHandler = SessionHandler.create(store);
        longSessionHandler = SessionHandler.create(store);
        setMinLength(20);
        setSessionTimeout(30, 24*40);
    }

    private void throwIfNullOrEmpty(String s, String msg, String suggest) {
        if ((s == null) || s.isEmpty())
            throw new RuntimeException("Don't pass MySessionHandler a null/empty "+msg+" - "+suggest+"?");
    }

    long minutes(int num) {
        return num * 60000L;
    }

    long hours(int num) {
        return minutes(num * 60);
    }

    @Override
    public void handle(RoutingContext context) {
        context.response().ended();

        // Look for existing session cookie
        final Cookie cookie = context.getCookie(publicComputerCookieName);
        boolean useLongSession = false;
        if (cookie != null) {
            final String publicComputerValue = cookie.getValue();
            useLongSession = privateComputerAnswer.equals(publicComputerValue);
        }
        if (useLongSession) {
            longSessionHandler.handle(context);
        } else {
            shortSessionHandler.handle(context);
        }
    }

    @Override
    public SessionHandler setSessionTimeout(long timeout) {
        throw new RuntimeException("Don't call MySessionHandler.setSessionTimeout(long)- pass 2 ints");
    }

    public SessionHandler setSessionTimeout(int shortTimeoutMin, int longTimeoutHours) {
        shortSessionHandler.setSessionTimeout(minutes(shortTimeoutMin));
        longSessionHandler.setSessionTimeout(hours(longTimeoutHours));
        return this;
    }

    @Override
    public SessionHandler setNagHttps(boolean nag) {
        shortSessionHandler.setNagHttps(nag);
        longSessionHandler.setNagHttps(nag);
        return this;
    }

    @Override
    public SessionHandler setCookieSecureFlag(boolean secure) {
        shortSessionHandler.setCookieSecureFlag(secure);
        longSessionHandler.setCookieSecureFlag(secure);
        return this;
    }

    @Override
    public SessionHandler setCookieHttpOnlyFlag(boolean httpOnly) {
        shortSessionHandler.setCookieHttpOnlyFlag(httpOnly);
        longSessionHandler.setCookieHttpOnlyFlag(httpOnly);
        return this;
    }

    @Override
    public SessionHandler setSessionCookieName(String sessionCookieName) {
        shortSessionHandler.setSessionCookieName(sessionCookieName);
        longSessionHandler.setSessionCookieName(sessionCookieName);
        return this;
    }

    /**
     * Nothing less than 16 is accepted
     */
    @Override
    public SessionHandler setMinLength(int minLength) {
        minLength = (minLength < 16) ? 16 : (minLength > 64 ? 64 : minLength);
        shortSessionHandler.setMinLength(minLength);
        longSessionHandler.setMinLength(minLength);
        return this;
    }

}
