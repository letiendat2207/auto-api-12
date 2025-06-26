package utils;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import lombok.Getter;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@Getter
public class MockUtils {
    private static WireMockServer refDataServer;
    private static WireMockServer buildCardServer;

    private static WireMockServer startServer(WireMockServer server, int port, String path) {
        if (server == null) {
            server = new WireMockServer(options().port(port)
                    .notifier(new ConsoleNotifier(true))
                    .usingFilesUnderClasspath(path));
        }
        if (!server.isRunning()) {
            server.start();
        }
        return server;
    }

    public static void startRefDataServer() {
        refDataServer = startServer(refDataServer, 7777, "mock/ref-data");
    }

    public static void startBuildCardServer() {
        buildCardServer = startServer(buildCardServer, 7778, "mock/build-card");
    }

    public static void startAllMockServer() {
        startRefDataServer();
        startBuildCardServer();
    }
}
