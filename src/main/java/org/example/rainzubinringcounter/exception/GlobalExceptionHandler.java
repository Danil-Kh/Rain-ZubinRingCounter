package org.example.rainzubinringcounter.exception;

import jakarta.annotation.PostConstruct;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

@Slf4j
@RequiredArgsConstructor
@Component
public class GlobalExceptionHandler {
    private static GlobalExceptionHandler instance;

    @PostConstruct
    public void init() {
        instance = this;
        Thread.setDefaultUncaughtExceptionHandler(this::handleException);
    }

    public void handleException(Thread t, Throwable e) {
        if (e instanceof BaseException) {
            showErrorDialog(e.getMessage());
        }
    }

    private void showErrorDialog(String message) {
        if (Platform.isFxApplicationThread()) {
            createAndShowDialog(message);
        } else {
            Platform.runLater(() -> createAndShowDialog(message));
        }
    }

    private void createAndShowDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    public static GlobalExceptionHandler getInstance() {
        return instance;
    }
}