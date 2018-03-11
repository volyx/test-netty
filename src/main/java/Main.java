
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.NettyContext;
import reactor.ipc.netty.http.server.HttpServer;
import reactor.ipc.netty.http.server.HttpServerRequest;
import reactor.ipc.netty.http.server.HttpServerResponse;
import reactor.ipc.netty.http.server.HttpServerRoutes;

import javax.annotation.Nonnull;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class Main {

    public static void main(String[] args) {
        Flux<String> test = Flux.range(0, 100)
                .map(n -> String.format("%010d", n));

        NettyContext server =
                HttpServer.create(8080)
                        .newRouter(new Consumer<HttpServerRoutes>() {
                            @Override
                            public void accept(HttpServerRoutes routes) {
                                routes.file("/test", resource("index.html"))
                                        .directory("/folder", resource("folder"))
                                        .get("/get/{param}", (req, res) -> {
                                            Test test1 = new Test();
                                            String param = req.param("param");
                                            test1.data = Integer.parseInt(param);
                                            return res.sendString(Flux.just(JsonParser.toJson(test1)));
                                        })
                                        .index((req, res) -> res.sendString(Mono.just("OK!")));
                            }
                        })
//                        .newHandler((req, res) -> res.sendString(test.map(s -> s + "\n")))
                        .block(Duration.ofSeconds(30));

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
           throw new RuntimeException(e);
        }
    }

    private static Path resource(@Nonnull String path) {
        Objects.requireNonNull(path);
        URL url = Main.class.getClassLoader().getResource(path);
        Objects.requireNonNull(url);
        return Paths.get(url.getPath());
    }

    public static class Test {
        public int data;
    }
}
