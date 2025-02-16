package org.example.rainzubinringcounter;

import jakarta.annotation.Resource;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.example.rainzubinringcounter.exception.ExceptionMessage;
import org.example.rainzubinringcounter.exception.GlobalExceptionHandler;
import org.example.rainzubinringcounter.exception.IncorrectFileFormatException;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

@Controller
public class RingCounterController {
    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @FXML
    public CheckBox sumFile;
    @FXML
    private TextArea textArea;
    @FXML
    public Circle circleDrag;
    @FXML
    public Button fileChooserButton;
    private final XWPFDocument xwpfDocument = new XWPFDocument();
    StringBuilder sb = new StringBuilder();
    private final FileChooser fileChooser = new FileChooser();

    @Resource
    RingReader ringReader = new RingReader();






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
            HashMap<String, Integer> hashMap = new HashMap<>();




            if (db.hasFiles()) {
                success = true;
                for (File file : db.getFiles()) {
                   ringReader.reader(file.getAbsolutePath(), sumFile.isSelected());
                   hashMap = ringReader.hashMap.getHashMap();

                    for (String key : hashMap.keySet()) {
                        sb.append(key).append(": ").append(hashMap.get(key)).append("\n");
                    }

                    textArea.appendText(sb.toString());


                    try {
                        createDocument(sb);
                    } catch (IncorrectFileFormatException e) {
                        globalExceptionHandler.handleException(e);
                    }


                }
            }
            event.setDropCompleted(success);

            event.consume();

        });
}

    private void createDocument(StringBuilder sb) throws IncorrectFileFormatException {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("test.docx");
            XWPFRun run = xwpfDocument.createParagraph().createRun();
            run.setText(sb.toString());
            xwpfDocument.write(fileOutputStream);

        } catch (IOException e) {
            throw new IncorrectFileFormatException(ExceptionMessage.INCORRECT_FILE_FORMAT.toString());
        }
    }
}
