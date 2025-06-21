import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class MockApp {
    public static void main(String[] args){
        WireMockServer wireMockServer = new WireMockServer(options().port(7777)
                .notifier(new ConsoleNotifier(true))
                .usingFilesUnderClasspath("mock/ref-data")); //No-args constructor will start on port 8080, no HTTPS
        wireMockServer.start();

        WireMockServer wireMockServer2 = new WireMockServer(options().port(7778)
                .notifier(new ConsoleNotifier(true))
                .usingFilesUnderClasspath("mock/build-card")); //No-args constructor will start on port 8080, no HTTPS
        wireMockServer2.start();
    }
}
