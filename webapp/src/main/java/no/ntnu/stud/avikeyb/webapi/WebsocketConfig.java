package no.ntnu.stud.avikeyb.webapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;

/**
 * Created by pitmairen on 06/03/2017.
 */
@Configuration
@EnableWebSocket
public class WebsocketConfig implements WebSocketConfigurer  {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(inputs(), "/input").setAllowedOrigins("*");
        registry.addHandler(outputs(), "/output").setAllowedOrigins("*");
        registry.addHandler(ui(), "/ui").setAllowedOrigins("*");

    }

    @Bean
    public WebSocketHandler inputs() {
        return new InputController();
    }

    @Bean
    public WebSocketHandler outputs() {
        return new OutputController();
    }

    @Bean
    public WebSocketHandler ui() {
        return new UIController();
    }
}
