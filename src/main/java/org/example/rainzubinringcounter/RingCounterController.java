package org.example.rainzubinringcounter;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.example.rainzubinringcounter.exception.ExceptionMessage;
import org.example.rainzubinringcounter.exception.GlobalExceptionHandler;
import org.example.rainzubinringcounter.exception.IncorrectFileFormatException;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;

@Controller
public class RingCounterController {
    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @FXML
    public CheckBox sumFile;
    @FXML
    public TextArea textAreaError;
    @FXML
    private TextArea textArea;
    @FXML
    public Circle circleDrag;
    @FXML
    public Button fileChooserButton;

    private final FileChooser fileChooser = new FileChooser();

    RingReader ringReader = new RingReader();


    @FXML
    private void initialize() {
        fileChooserButton.setOnAction(event -> {
            StringBuilder sb = new StringBuilder();
            textArea.clear();
            File file = fileChooser.showOpenDialog(fileChooserButton.getScene().getWindow());

            if (isValidFile(file)) {
               globalExceptionHandler.handleException(
                       new IncorrectFileFormatException(ExceptionMessage.INCORRECT_FILE_FORMAT.getMessage()));
               return;
            }

            printRingToTextArea(sb, file);

            displayErrors(file);

            try {
                    createDocument(sb, file.getName());
                } catch (IncorrectFileFormatException e) {
                    globalExceptionHandler.handleException(e);
                }
                sb.setLength(0);

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
            textArea.clear();
            StringBuilder sb = new StringBuilder();

            if (db.hasFiles() && !sumFile.isSelected()) {
                for (File file : db.getFiles()) {
                   if (isValidFile(file)) {continue;}
                    printRingToTextArea(sb, file);
                    displayErrors(file, db);
                }
                try {
                    createDocument(sb, db.getFiles().getFirst().getName());
                } catch (IncorrectFileFormatException e) {
                    globalExceptionHandler.handleException(e);
                }
                sb.setLength(0);
            }else {
                HashMap<String, Integer> hashMap = new HashMap<>();
                    sb.append("---").append(db.getFiles().getFirst().getName()).append("---").append("\n");
                for (File file : db.getFiles()) {
                    if (isValidFile(file)) continue;

                    ReaderResult readerResult = ringReader.reader(file.getAbsolutePath(), sumFile.isSelected());
                    hashMap = readerResult.getHashMap();
                    displayErrors(file, db);
                }

                for (String key : hashMap.keySet()) {
                    sb.append(key).append(": ").append(hashMap.get(key)).append("\n");
                }
                textArea.appendText(sb.toString());
                try {
                    createDocument(sb, db.getFiles().getFirst().getName());
                } catch (IncorrectFileFormatException e) {
                    globalExceptionHandler.handleException(e);
                }
            }
            event.consume();
        });

        sumFile.setOnAction(event -> {

            if (sumFile.isSelected()) {
                ringReader.hashMap.clear();
            }

        });

}

    private void printRingToTextArea(StringBuilder sb, File file) {
        HashMap<String, Integer> hashMap;
        hashMap = ringReader.reader(file.getAbsolutePath(), sumFile.isSelected()).getHashMap();
        sb.append("---").append(file.getName()).append("---").append("\n");
        for (String key : hashMap.keySet()) {
            sb.append(key).append(": ").append(hashMap.get(key)).append("\n");
        }
        textArea.clear();
        textArea.appendText(sb.toString());
    }

    private boolean isValidFile(File file) {
        try {
            new XWPFDocument(new FileInputStream(file));
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    private void displayErrors(File file, Dragboard db) {
        StringBuilder sb = new StringBuilder();
        if (db.getFiles().getFirst().equals(file)) {
            textAreaError.clear();
        }
        rederResult(file, sb);
    }
    private void displayErrors(File file) {
        textAreaError.clear();
        StringBuilder sb = new StringBuilder();
        rederResult(file, sb);
    }

    private void rederResult(File file, StringBuilder sb) {
        ReaderResult readerResult = ringReader.reader(file.getAbsolutePath(), sumFile.isSelected());
        for (String error : readerResult.getErrorsList()) {
            sb.append("\n").append(file.getName()).append(": ").append(error).append("\n");
        }
        textAreaError.appendText(sb.toString());
        sb.setLength(0);
    }

    private void createDocument(StringBuilder sb, String fileName) throws IncorrectFileFormatException {
        try {
            String userHome = System.getProperty("user.home");
            String dirPath = Paths.get(userHome, "Documents", "GeneratedDocs").toString();
            File dir = new File(dirPath);

            boolean dirCreated = dir.mkdirs();
            if (!dir.exists() && !dirCreated) {
                throw new IOException("Failed to create directory: " + dirPath);
            }

            String filename = Paths.get(dirPath, "ring_" + fileName + ".docx").toString();
            FileOutputStream fileOutputStream = new FileOutputStream(filename);

            XWPFDocument xwpfDocument = new XWPFDocument();


            String[] lines = sb.toString().split("\n");
            for (String line : lines) {
                XWPFParagraph paragraph = xwpfDocument.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setText(line);
            }

            xwpfDocument.write(fileOutputStream);
            fileOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
            throw new IncorrectFileFormatException(ExceptionMessage.INCORRECT_FILE_FORMAT.toString());
        }
    }

}
