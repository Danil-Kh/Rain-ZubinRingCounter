package org.example.rainzubinringcounter;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;


public class RingCounterController {
    @FXML
    private TextArea textArea;
    private RingReader ringReader = new RingReader();
    @FXML
    public Circle circleDrag;
    private final FileChooser fileChooser = new FileChooser();
    @FXML
    public Button fileChooserButton;
    private final XWPFDocument xwpfDocument = new XWPFDocument();

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
                    StringBuilder sb = new StringBuilder();
                    System.out.println("Dropped file: " + file.getAbsolutePath());
                    HashMap<String, Integer> hashMap = ringReader.reader(file.getAbsolutePath());
                    for (String key : hashMap.keySet()) {
                        sb.append(key).append(": ").append(hashMap.get(key)).append("\n");
                    }
                    textArea.appendText(sb.toString());
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream("test.docx");
                        XWPFRun run = xwpfDocument.createParagraph().createRun();
                        run.setText(sb.toString());
                        xwpfDocument.write(fileOutputStream);

                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }


                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
}
}
