package org.myorg.utils;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

/*
 * Copied and changed from vertx examples.
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class Runner {

    private static final String JAVA_DIR =  "src/main/java/";

    public static void runClusteredExample(Class<? extends AbstractVerticle> clazz) {
        runExample(JAVA_DIR, clazz, new VertxOptions().setClustered(true), null);
    }

    public static void runExample(Class<? extends AbstractVerticle> clazz) {
        runExample(JAVA_DIR, clazz, new VertxOptions().setClustered(false), null);
    }

    public static void runExample(Class<? extends AbstractVerticle> clazz, DeploymentOptions options) {
        runExample(JAVA_DIR, clazz, new VertxOptions().setClustered(false), options);
    }


    public static void runExample(String exampleDir, Class<? extends AbstractVerticle> clazz, VertxOptions options,
            DeploymentOptions deploymentOptions) {
        runExample(exampleDir + clazz.getPackage().getName().replace(".", "/"), clazz.getName(), options, deploymentOptions);
    }


    public static void runScriptExample(String prefix, String scriptName, VertxOptions options) {
        final File file = new File(scriptName);
        final String dirPart = file.getParent();
        final String scriptDir = prefix + dirPart;
        runExample(scriptDir, scriptDir + "/" + file.getName(), options, null);
    }

    public static void runExample(String exampleDir, String verticleID, VertxOptions options, DeploymentOptions deploymentOptions) {
        if (options == null) {
            // Default parameter
            options = new VertxOptions();
        }
        // Smart cwd detection

        // Based on the current directory (.) and the desired directory (exampleDir),
        // we try to compute the vertx.cwd directory:
        try {
            // We need to use the canonical file. Without the file name is .
            final File current = new File(".").getCanonicalFile();
            if (exampleDir.startsWith(current.getName()) && !exampleDir.equals(current.getName())) {
                exampleDir = exampleDir.substring(current.getName().length() + 1);
            }
        } catch (final IOException e) {
            // Ignore it.
        }

        System.setProperty("vertx.cwd", exampleDir);
        final Consumer<Vertx> runner = vertx -> {
            try {
                if (deploymentOptions != null) {
                    vertx.deployVerticle(verticleID, deploymentOptions);
                } else {
                    vertx.deployVerticle(verticleID);
                }
            } catch (final Throwable t) {
                t.printStackTrace();
            }
        };
        if (options.isClustered()) {
            Vertx.clusteredVertx(options, res -> {
                if (res.succeeded()) {
                    final Vertx vertx = res.result();
                    runner.accept(vertx);
                } else {
                    res.cause().printStackTrace();
                }
            });
        } else {
            final Vertx vertx = Vertx.vertx(options);
            runner.accept(vertx);
        }
    }
}
