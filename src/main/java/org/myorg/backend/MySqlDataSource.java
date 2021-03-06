package org.myorg.backend;

import org.myorg.AppProperty;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLConnection;

/**
 * See http://vertx.io/docs/vertx-mysql-postgresql-client/java/
 * <p>
 * This is a singleton.  If init(vertx)
 * Use MySqlDataSource.getInstance().getConnection(handler);
 */
public class MySqlDataSource {
    private static final Logger logger = LoggerFactory.getLogger(MySqlDataSource.class);

    static Object lock = new Object();
    static AsyncSQLClient client;
    static MySqlDataSource dataSource;
    static String connectionString;

    public void getConnection(Handler<AsyncResult<SQLConnection>> handler) {
        if (client == null)
            handler.handle(null); // can happen after the data source stops
        else
            client.getConnection(handler);
    }


    public static MySqlDataSource getInstance(Vertx vertx) {
        if (dataSource == null) {
            synchronized (lock) {
                if (dataSource == null)
                    dataSource = new MySqlDataSource(vertx);
            }
        }
        return dataSource;
    }


    public static MySqlDataSource getInstance() {
        return dataSource;
    }

    private MySqlDataSource(Vertx vertx) {
        final String username = AppProperty.MysqlDBUsername.getString();
        final String password = AppProperty.MysqlDBPassword.getString();
        final String userPass = username + (password.isEmpty() ? "" : (" --password=" + password));
        final String dbname = AppProperty.MysqlDBName.getString();
        final String host = AppProperty.MysqlDBHost.getString();
        final int port = AppProperty.MysqlDBPort.getInt();

        if (password.isEmpty())
            logger.info("Probably the mysql library cannot handle not having a password...");

        connectionString = "mysql -u "+userPass+" --bind-address "+host+":"+port+" -A -D "+dbname;
        final JsonObject config = new JsonObject()
                .put("host",     host)
                .put("port", port)
                .put("database", dbname)
                .put("username", username)
                .put("password", password)
                .put("maxPoolSize", AppProperty.MysqlDBPoolSize.getInt())
                .put("queryTimeout", AppProperty.MysqlDBTimeout.getInt());

        client = MySQLClient.createShared(vertx, config, "VertPool");
    }



    public static void init(Vertx vertx) {
        if (dataSource != null)
            return;
        dataSource = getInstance(vertx);
        dataSource.getConnection(result -> {
            final Logger logger = LoggerFactory.getLogger(MySqlDataSource.class);
            if ((result == null) || !result.succeeded()) {
                logger.error("Cant open a MySQL database connection to: "+connectionString);
                if (result != null)
                    logger.error("-- Database connection string was: ", result.cause());
                stop();
                vertx.close();
            } else {
                logger.info("Succeeded in opening a MySQL database connection");
                result.result().close();
            }
        });
    }


    public static synchronized void stop() {
        if (client != null) {
            logger.info("Shutting down the MySQL Data source.");
            client.close();
            client = null;
        }
    }
}
