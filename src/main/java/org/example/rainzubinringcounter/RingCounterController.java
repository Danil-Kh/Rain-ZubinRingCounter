package org.example.rainzubinringcounter;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.io.File;


public class RingCounterController {
    @FXML
    public Circle circleDrag;
    private final FileChooser fileChooser = new FileChooser();
    @FXML
    public Button fileChooserButton;

    @FXML
    private void initialize() {
        fileChooserButton.setOnAction(event -> {
            File file = fileChooser.showOpenDialog(fileChooserButton.getScene().getWindow());
            System.out.println("file = " + file);
        });
        circleDrag.setOnDragDetected(event -> {
            System.out.println("\"file detected \" = " + "file detected ");
            Dragboard db = circleDrag.startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();
            content.putString("file");
            db.setContent(content);
            event.consume();
    });
        circleDrag.setOnDragOver(event -> {
            if (event.getGestureSource() != circleDrag &&
                    event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });
        circleDrag.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles()) {
                success = true;
                for (File file : db.getFiles()) {
                    System.out.println("Dropped file: " + file.getAbsolutePath());
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });
}
}
