package org.example.rainzubinringcounter;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;


public class RingCounter extends Application {
    private ConfigurableApplicationContext applicationContext;
    private final RingCounterController controller = new RingCounterController();
    @Override
    public void init() {
        applicationContext = new SpringApplicationBuilder(RainZubinRingCounterApplication.class).run();
    }
    @Override
    public void start(Stage stage)  {
        applicationContext.publishEvent(new StageReadyEvent(stage));

    }
    @Override
    public void stop(){
        applicationContext.close();
        Platform.exit();
    }
    static class StageReadyEvent extends ApplicationEvent {
        public StageReadyEvent(Stage stage) {
            super(stage);
        }

        public Stage getStage() {
            return (Stage) getSource();
        }
    }
}
