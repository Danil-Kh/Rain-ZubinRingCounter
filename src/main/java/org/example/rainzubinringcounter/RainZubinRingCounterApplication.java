package org.example.rainzubinringcounter;

import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class RainZubinRingCounterApplication {

    public static void main(String[] args) {
        Application.launch(RingCounter.class, args);

    }

}
