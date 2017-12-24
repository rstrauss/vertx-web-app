package org.myorg.backend.products;

import java.util.HashMap;
import java.util.List;

import org.myorg.backend.MySqlDataSource;
import org.myorg.frontend.StaticServer;
import org.myorg.utils.HtmlERR;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class ProductHandlers {
    private final Logger logger = LoggerFactory.getLogger(StaticServer.class);

    final HashMap<String, JsonObject>products = new HashMap<>();
    final Vertx vertx;
    final MySqlDataSource dataSource;


    public ProductHandlers(Vertx vertx) {
        this.vertx = vertx;
        dataSource = MySqlDataSource.getInstance(vertx);
    }


    public void addProductRoutes(Future<Void> fut, Router router) {
        final Router prodRouter = Router.router(vertx);
        router.mountSubRouter("/products", prodRouter);
        prodRouter.get("/:productID").handler(this::handleGetProduct);
        prodRouter.put("/:productID").handler(this::handleAddProduct);
        prodRouter.get("/").handler(this::handleGetProductList);
        setUpInitialData();
    }


    /**
     * Reads in the initial data set up in
     */
    private void setUpInitialData() {
        MySqlDataSource.getInstance().getConnection(connResult -> {
            if ((connResult == null) || !connResult.succeeded()) { // couldn't get a connection
                return;  // init already reported this...
            }
            logger.debug("ProductHandler.setUpInitialData...");
            final SQLConnection conn = connResult.result();
            try {
                conn.query("SELECT * FROM product", event -> {
                    try {
                        final ResultSet results = event.result();
                        final List<JsonObject> rows = results.getRows();
                        for (final JsonObject row: rows)
                            products.put(row.getString("id"), row);
                        logger.info("Fetched "+rows.size()+" ("+products.size()+") products from the product table");
                    } catch (final Throwable t) {
                        logger.error("Exception handling SELECT results", t);
                        conn.close();
                    }
                });
            } catch (final Throwable t) {
                logger.error("Could not fetch products from the table: Is the table missing?", t);
                conn.close();
            }
        });
    }


    /**
     * Just an example of a bare-bones get
     */
    private void handleGetProduct(final RoutingContext routingContext) {
        final String productID = routingContext.request().getParam("productID");
        final HttpServerResponse response = routingContext.response();
        if (productID == null) {
            HtmlERR.BadRequest.sendPlus(response, "productID was missing");
        } else {
            final JsonObject product = products.get(productID);
            if (product == null) {
                HtmlERR.BadRequest.sendPlus(response, "productID was unknown");
            } else {
                response.putHeader("content-type", "application/json").end(product.encodePrettily());
            }
        }
    }


    /**
     * Just an example of a bare-bones put
     */
    private void handleAddProduct(RoutingContext routingContext) {
        final String productID = routingContext.request().getParam("productID");
        final HttpServerResponse response = routingContext.response();
        if (productID == null) {
            HtmlERR.BadRequest.sendPlus(response, "productID was missing");
        } else {
            final JsonObject product = routingContext.getBodyAsJson();
            if (product == null) {
                HtmlERR.BadRequest.sendPlus(response, "productID was unknown");
            } else {
                products.put(productID, product);
                response.end();
            }
        }
    }


    static final boolean giveCallerJSON = false;  // set to false to test w/o front-end

    /**
     * Just an example of getting a list of products
     */
    private void handleGetProductList(RoutingContext routingContext) {
        if (giveCallerJSON) {
            final JsonArray arr = new JsonArray();
            products.forEach((k, v) -> arr.add(v));
            routingContext.response().putHeader("content-type", "application/json").end(arr.encodePrettily());

        } else { // HTML
            final StringBuilder sb = new StringBuilder(500);
            sb.append("<html>\n");
            sb.append("<body>\n");
            sb.append("<h1>Products</h1>\n");
            if (products.isEmpty()) {
                sb.append("<p>No products &nbsp; :?( </p>\n");
            } else {
                sb.append("<ol>\n");
                products.forEach((k, v) -> {
                    final String id = v.getString("id");
                    final String name = v.getString("name");
                    sb.append("<li>")
                    .append("[ ").append(id).append(" ] &nbsp; ")
                    .append("<a href=\"/products/").append(id).append("\">").append(name).append("</a>")
                    .append("</li>\n");
                });
                sb.append("</ol>\n");
            }
            sb.append("</body>\n");
            sb.append("</html>\n");
            routingContext.response().putHeader("content-type", "text/html").end(sb.toString());
        }
    }

}
