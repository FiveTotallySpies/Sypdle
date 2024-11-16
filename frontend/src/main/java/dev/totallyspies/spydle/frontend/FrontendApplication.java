package dev.totallyspies.spydle.frontend;

import dev.totallyspies.spydle.frontend.interface_adaptors.GameView;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.awt.*;

@SpringBootApplication
public class FrontendApplication {

    public static void main(String[] args) {
        var ctx = new SpringApplicationBuilder(FrontendApplication.class).headless(false).run(args);

        EventQueue.invokeLater(() -> {
            var gameView = ctx.getBean(GameView.class);
            gameView.setVisible(true);
                }
        );
    }

}