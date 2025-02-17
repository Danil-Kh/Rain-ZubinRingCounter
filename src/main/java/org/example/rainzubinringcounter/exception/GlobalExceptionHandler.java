package org.example.rainzubinringcounter.exception;


import javafx.application.Platform;
import javafx.scene.control.Alert;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;



@Slf4j
@Component
public class GlobalExceptionHandler {

    public void handleException(Throwable e) {
        log.error("Произошла ошибка: ", e);

        // Показываем всплывающее окно с информацией об ошибке
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("An error occurred in the application");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        });
    }

}