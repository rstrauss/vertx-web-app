package org.myorg.backend.products;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.myorg.AppProperty;
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
import io.vertx.ext.sql.UpdateResult;
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
        prodRouter.get("/get/:productID").handler(this::handleGetProduct);
        prodRouter.put("/+/:productID/:productAbbrev/:productName").handler(this::handleAddProduct);
        prodRouter.put("/add").handler(this::handleAddProduct2);
        prodRouter.get("/").handler(this::handleGetProductList);
        prodRouter.get("/pa").handler(this::handleAddProduct2);
        setUpInitialData();
    }

    class AProduct {
        public final int id;
        public final String abbrev;
        public final String name;
        AProduct(int id, String abbrev, String name) {
            this.id = id;
            this.abbrev = abbrev;
            this.name = name;
        }
        JsonObject getJson() {
            return JsonObject.mapFrom(this);
        }
    }

    /**
     * Reads in the initial data set up in
     */
    private void setUpInitialData() {
        if (!AppProperty.UseMySql.getBoolean()) {
            products.put("1", new AProduct(1, "id", "InMem Doilies").getJson());
            products.put("2", new AProduct(2, "ip", "InMem Panties").getJson());
            return;
        }
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
                            products.put(row.getInteger("id").toString(), row);
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
        final String id     = routingContext.request().getParam("productID");
        final String abbrev = routingContext.request().getParam("productAbbrev");
        final String name   = routingContext.request().getParam("productName");
        logger.debug("handleAddProduct2: id="+id+", abbrev="+abbrev+", name="+name);
        final HttpServerResponse response = routingContext.response();
        if (id == null) {
            HtmlERR.BadRequest.sendPlus(response, "productID was missing");
        } else {
            final JsonObject product = routingContext.getBodyAsJson();
            if (product == null) {
                HtmlERR.BadRequest.sendPlus(response, "productID was unknown");
            } else {
                final AProduct aProduct = new AProduct(Integer.valueOf(id), abbrev, name);
                products.put(id, aProduct.getJson());
                response.end();
            }
        }
    }


    /**
     * Just an example of a bare-bones put
     */
    private void handleAddProduct2(RoutingContext routingContext) {
        final String abbrev = routingContext.request().getParam("a");
        final String name   = routingContext.request().getParam("n");
        logger.debug("handleAddProduct2: abbrev="+abbrev+", name="+name);
        final HttpServerResponse response = routingContext.response();
        if ((abbrev == null) || abbrev.isEmpty()) {
            HtmlERR.BadRequest.sendPlus(response, "productAbbrev was unknown");
        } else if ((name == null) || name.isEmpty()) {
            HtmlERR.BadRequest.sendPlus(response, "productName was unknown");
        } else {
            if (AppProperty.UseMySql.getBoolean())
                insertIntoDB(response, abbrev, name);
            else
                insertIntoHash(response, abbrev, name);
        }
    }

    private void insertIntoHash(HttpServerResponse response, String abbrev, String name) {
        int id = 0;
        for (final Entry<String, JsonObject> es: products.entrySet()) {
            final int n = es.getValue().getInteger("id");
            id = (id <= n) ? n : id;
        }
        id++;
        products.put(""+id, new AProduct(id, abbrev, name).getJson());
        logger.debug("Put in hash: "+id+", "+abbrev+", "+name);
        response.end("got id="+id+", "+abbrev+", "+name);
    }


    /**
     * Note - this shows how to fetch an auto-increment value after an insert
     */
    private void insertIntoDB(final HttpServerResponse response, String abbrev, String name) {
        MySqlDataSource.getInstance().getConnection(connResult -> {
            if ((connResult == null) || !connResult.succeeded()) { // couldn't get a connection
                return;  // init already reported this...
            }
            logger.debug("ProductHandler.setUpInitialData...");
            final SQLConnection conn = connResult.result();
            try {
                final JsonArray params = new JsonArray().add(abbrev).add(name);
                final String sql = "INSERT INTO product (abbrev, name) VALUES (?,?)";
                conn.updateWithParams(sql, params, event -> {
                    try {
                        final UpdateResult results = event.result();
                        final JsonArray keys = results.getKeys();
                        final int u = results.getUpdated();
                        int newid = -1;
                        String msg = "";
                        if (keys.size() < 1) {
                            msg = "Did the insert, but no key was returned, num rows updated = "+u;
                            logger.error(msg);
                            response.end(msg);
                        } else if (keys.size() > 1) {
                            msg = "Did the insert, but "+keys.size()+" keys returned! num rows updated = "+u;
                            newid = keys.getInteger(0);
                            logger.error(msg);
                        } else {
                            msg = "Did the insert, 1 key returned, num rows updated = "+u;
                            newid = keys.getInteger(0);
                            logger.info(msg);
                        }
                        products.put(""+newid, new AProduct(newid, abbrev, name).getJson());
                        final String info = "Put in db & hash: "+newid+", "+abbrev+", "+name;
                        logger.debug(info);
                        response.end(info);
                    } catch (final Throwable t) {
                        logger.error("insertIntoDB: Exception handling INSERT results", t);
                        HtmlERR.BadRequest.sendPlus(response, "got an error: "+t.getClass().getSimpleName());
                    } finally {
                        conn.close();
                    }
                });
            } catch (final Throwable t) {
                logger.error("insertIntoDB: Could not get a connection...", t);
                if (conn != null)
                    conn.close();
            }
        });
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
                sb.append("<ul>\n");
                products.forEach((k, v) -> {
                    final int id = v.getInteger("id");
                    final String abbrev = v.getString("abbrev");
                    final String name = v.getString("name");
                    sb.append("<li>").append(k)
                    .append(". [ ").append(abbrev).append(" ] &nbsp; ")
                    .append("<a href=\"/products/get/").append(id).append("\">").append(name).append("</a>")
                    .append("</li>\n");
                });
                sb.append("</ul>\n");
                sb.append("<p>To insert, use: /products/add?abbrev=ABBREV&name=NAME");
            }
            sb.append("</body>\n");
            sb.append("</html>\n");
            routingContext.response().putHeader("content-type", "text/html").end(sb.toString());
        }
    }

}
