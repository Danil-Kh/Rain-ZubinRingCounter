package org.example.rainzubinringcounter;

import javafx.application.Application;
import org.example.rainzubinringcounter.configuration.RingCounter;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RainZubinRingCounterApplication {

    public static void main(String[] args) {
        Application.launch(RingCounter.class, args);
    }
}
