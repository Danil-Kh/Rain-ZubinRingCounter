package org.example.rainzubinringcounter.configuration;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import org.example.rainzubinringcounter.RainZubinRingCounterApplication;
import org.example.rainzubinringcounter.exception.GlobalExceptionHandler;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RingCounter extends Application {
    private ConfigurableApplicationContext applicationContext;
    private GlobalExceptionHandler globalExceptionHandler;


    @Override
    public void init() {
        applicationContext = new SpringApplicationBuilder(RainZubinRingCounterApplication.class).run();
        globalExceptionHandler = applicationContext.getBean(GlobalExceptionHandler.class); // Инжект вручную

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            globalExceptionHandler.handleException(throwable);
        });
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
