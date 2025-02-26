package org.example.rainzubinringcounter.configuration;


import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class StageInitializer implements ApplicationListener<RingCounter.StageReadyEvent>{
    @Value("classpath:/ringcounter.fxml")
    private Resource ringCounter;

    @Override
    public void onApplicationEvent(RingCounter.StageReadyEvent event) {
        try {
          FXMLLoader fxmlLoader =  new FXMLLoader(ringCounter.getURL());
            Parent parent = fxmlLoader.load();
            Stage stage = event.getStage();
            stage.setScene(new Scene(parent, 1300, 600));
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
