package com.example;

import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.ExpandWar;
import org.apache.catalina.startup.Tomcat;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ApplicationBase {

    // Changing these also requires changing config in the test package solutions_ws.properties
    private static final int APPLICATION_SERVER_PORT = 27901;
    public static final String APPLICATION_SERVER_URL = "http://localhost:" + APPLICATION_SERVER_PORT + "/";

    private static Tomcat server;

    public void startServer() throws Exception {
        server = startServer(APPLICATION_SERVER_PORT);
    }

    public void stopServer() throws Exception {
        server.stop();
        server.destroy();
    }

    public Tomcat startServer(int port) throws Exception {
        Tomcat server = createServer(getWarFileUrl(), port);
        server.start();
        return server;
    }

    private Tomcat createServer(String pathToWar, int port)
            throws IOException, ServletException, LifecycleException {
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(new File(System.getProperty("user.dir")).getCanonicalPath());
        String contextPath = "";
        File war = new File(pathToWar);

        if (!war.exists()) {
            String errorMsg = "The specified path \"" + pathToWar + "\" does not exist.";
            System.err.println(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        File appBase = new File(System.getProperty(Globals.CATALINA_BASE_PROP), tomcat.getHost().getAppBase());
        if (appBase.exists()) {
            appBase.delete();
        }
        appBase.mkdir();
        URL fileUrl = new URL("jar:" + war.toURI().toURL() + "!/");
        String expandedDirName = "expanded";
        String expandedDir = ExpandWar.expand(tomcat.getHost(), fileUrl, "/" + expandedDirName);
        System.out.println("Expanding " + war.getName() + " into " + expandedDir);

        System.out.println("Adding Context " + contextPath + " for " + expandedDir);
        Context ctx = tomcat.addWebapp(contextPath, expandedDir);
        ((StandardContext) ctx).setUnpackWAR(false);
        tomcat.setPort(port);
        tomcat.start();
        return tomcat;
    }

    private static String getWarFileUrl() throws MalformedURLException {
        URL resource = ApplicationBase.class.getClassLoader().getResource(".");
        if (resource == null) {
            throw new RuntimeException("Failed to get path to project");
        } else {
            URL warUrl = new URL(resource, "../../target/oauth_ws.war");
            System.out.println("War file URL: " + warUrl.toString());
            return warUrl.getFile();
        }
    }
}
