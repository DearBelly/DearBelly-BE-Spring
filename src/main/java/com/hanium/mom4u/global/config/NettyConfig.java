package com.hanium.mom4u.global.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.http.server.HttpServer;

import java.util.concurrent.TimeUnit;

@Configuration
public class NettyConfig {

    @Bean
    public HttpServer httpServer() {
        return HttpServer.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 180_000) // TODO: 3분에서 조정 필요
                .doOnConnection(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(180, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(180, TimeUnit.SECONDS))
                );
    }
}
