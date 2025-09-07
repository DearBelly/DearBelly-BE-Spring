package com.hanium.mom4u.global.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NettyConfig {

    @Bean
    public WebServerFactoryCustomizer<NettyReactiveWebServerFactory> nettyCustomizer() {
        return factory -> factory.addServerCustomizers(http ->
                http
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 180_000) // TODO: 3분에서 조정 필요
                        .doOnConnection(conn -> conn
                                .addHandlerLast(new ReadTimeoutHandler(180))
                                .addHandlerLast(new WriteTimeoutHandler(180))
                        )
        );
    }

}
