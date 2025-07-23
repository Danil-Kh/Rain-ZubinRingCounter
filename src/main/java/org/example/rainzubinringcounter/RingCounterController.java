package org.example.rainzubinringcounter;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.*;
import org.example.rainzubinringcounter.exception.ExceptionMessage;
import org.example.rainzubinringcounter.exception.GlobalExceptionHandler;
import org.example.rainzubinringcounter.exception.IncorrectFileFormatException;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STVerticalAlignRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.springframework.stereotype.Controller;

import java.io.File;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            ReaderResult readerResult = ringReader.reader(file.getAbsolutePath(), sumFile.isSelected());
            printRingToTextArea(sb, file, readerResult);

            displayErrors(file, readerResult);
            String absolutePath = file.getAbsolutePath();
            try {
                createDocument(sb, file.getName(), absolutePath);
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
                    if (isValidFile(file)) {
                        continue;
                    }
                    ReaderResult readerResult = ringReader.reader(file.getAbsolutePath(), sumFile.isSelected());
                    printRingToTextArea(sb, file, readerResult);
                    displayErrors(file, db, readerResult);
                }
                try {
                    createDocument(sb, db.getFiles().getFirst().getName(), db.getFiles().getFirst().getAbsolutePath());
                } catch (IncorrectFileFormatException e) {
                    globalExceptionHandler.handleException(e);
                }
                sb.setLength(0);
            } else {
                HashMap<String, Integer> hashMap = new HashMap<>();
                sb.append("---").append(db.getFiles().getFirst().getName()).append("---").append("\n");
                for (File file : db.getFiles()) {
                    if (isValidFile(file)) continue;

                    ReaderResult readerResult = ringReader.reader(file.getAbsolutePath(), sumFile.isSelected());
                    hashMap = readerResult.getHashMap();

                    displayErrors(file, db, readerResult);
                }

                for (String key : hashMap.keySet()) {
                    sb.append(key).append(": ").append(hashMap.get(key)).append("\n");
                }
                textArea.appendText(sb.toString());
                try {
                    createDocument(sb, db.getFiles().getFirst().getName(), db.getFiles().getFirst().getAbsolutePath());
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

    private void printRingToTextArea(StringBuilder sb, File file, ReaderResult readerResult) {
        HashMap<String, Integer> hashMap;
        hashMap = readerResult.getHashMap();
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

    private void displayErrors(File file, Dragboard db, ReaderResult readerResult) {
        StringBuilder sb = new StringBuilder();
        if (db.getFiles().getFirst().equals(file)) {
            textAreaError.clear();
        }
        rederResult(file, sb, readerResult);
    }

    private void displayErrors(File file, ReaderResult readerResult) {
        textAreaError.clear();
        StringBuilder sb = new StringBuilder();
        rederResult(file, sb, readerResult);
    }

    private void rederResult(File file, StringBuilder sb, ReaderResult readerResult) {
        for (String error : readerResult.getErrorsList()) {
            sb.append("\n").append(file.getName()).append(": ").append(error).append("\n");
        }
        textAreaError.appendText(sb.toString());
        sb.setLength(0);
    }

    private void createDocument(StringBuilder sb, String fileName, String absolutePath) throws IncorrectFileFormatException {
        try {
            String userHome = System.getProperty("user.home");
            String dirPath = Paths.get(userHome, "Documents", "GeneratedDocs").toString();
            File dir = new File(dirPath);

            boolean dirCreated = dir.mkdirs();
            if (!dir.exists() && !dirCreated) {
                throw new IOException("Failed to create directory: " + dirPath);
            }

            File originalFile = new File(absolutePath);
            String newFilePath = Paths.get(dirPath, "ring_" + fileName).toString();

            FileInputStream fileInputStream = new FileInputStream(originalFile);
            XWPFDocument originalDoc = new XWPFDocument(fileInputStream);
            XWPFDocument xwpfDocument = new XWPFDocument();

            XWPFParagraph samplePar = originalDoc.getParagraphs().isEmpty() ? null : originalDoc.getParagraphs().get(0);
            CTPPr samplePPr = (samplePar != null && samplePar.getCTP().getPPr() != null)
                    ? (CTPPr) samplePar.getCTP().getPPr().copy() : null;

            String fontFamily = "Calibri";
            int fontSize = 11;
            STVerticalAlignRun.Enum verticalAlignment = null;
            if (!originalDoc.getParagraphs().isEmpty()) {
                XWPFParagraph firstParagraph = originalDoc.getParagraphs().get(0);
                if (!firstParagraph.getRuns().isEmpty()) {
                    XWPFRun firstRun = firstParagraph.getRuns().get(0);
                    if (firstRun.getFontFamily() != null) fontFamily = firstRun.getFontFamily();
                    if (firstRun.getFontSize() > 0) fontSize = firstRun.getFontSize();
                    if (firstRun.getVerticalAlignment() != null) verticalAlignment = firstRun.getVerticalAlignment();
                }
            }

            String[] lines = sb.toString().split("\n");
            for (String line : lines) {
                XWPFParagraph paragraph = xwpfDocument.createParagraph();
                if (samplePPr != null) {
                    paragraph.getCTP().setPPr((CTPPr) samplePPr.copy());
                }

                XWPFRun run = paragraph.createRun();
                run.setText(line);
                run.setFontFamily(fontFamily);
                run.setFontSize(fontSize);
                if (verticalAlignment != null) run.setVerticalAlignment(verticalAlignment.toString());
            }

            for (IBodyElement elem : originalDoc.getBodyElements()) {
                if (elem instanceof XWPFParagraph) {
                    XWPFParagraph origPar =  (XWPFParagraph) elem;
                    XWPFParagraph newPar = xwpfDocument.createParagraph();

                    if (origPar.getCTP().getPPr() != null)
                        newPar.getCTP().setPPr((CTPPr) origPar.getCTP().getPPr().copy());


                    int indentationLeft = origPar.getIndentationLeft() != -1 ? origPar.getIndentationLeft() : (int) Math.round(2.17 * 1440);
                    int indentationRight = origPar.getIndentationRight() != -1 ? origPar.getIndentationRight() : 0;
                    int indentationHanging = origPar.getIndentationHanging() != -1 ? origPar.getIndentationHanging() : (int) Math.round(2.17 * 1440);
                    int spacingBefore = origPar.getSpacingBefore() != -1 ? origPar.getSpacingBefore() : 0;
                    int spacingAfter = origPar.getSpacingAfter() != -1 ? origPar.getSpacingAfter() : 0;
                    double spacingBetween = origPar.getSpacingBetween() != -1 ? origPar.getSpacingBetween() : 1.15;

                    ParagraphAlignment alignment = origPar.getAlignment() != null ? origPar.getAlignment() : ParagraphAlignment.LEFT;

                    newPar.setAlignment(alignment);
                    newPar.setIndentationLeft(indentationLeft);
                    newPar.setIndentationRight(indentationRight);
                    newPar.setIndentationHanging(indentationHanging);
                    newPar.setSpacingBefore(spacingBefore);
                    newPar.setSpacingAfter(spacingAfter);
                    newPar.setSpacingBetween(spacingBetween);

                    for (XWPFRun origRun : origPar.getRuns()) {
                        XWPFRun newRun = newPar.createRun();
                        if (origRun.getCTR().getRPr() != null)
                            newRun.getCTR().setRPr((CTRPr) origRun.getCTR().getRPr().copy());
                        newRun.setText(origRun.text());
                    }

                } else if (elem instanceof XWPFTable) {
                    XWPFTable origTable = (XWPFTable) elem;
                    XWPFTable newTable = xwpfDocument.createTable();
                    try {
                        newTable.getCTTbl().set(origTable.getCTTbl().copy());
                    } catch (Exception ex) {
                        for (XWPFTableRow row : origTable.getRows()) {
                            XWPFTableRow newRow = newTable.createRow();
                            for (int i = 0; i < row.getTableCells().size(); i++) {
                                XWPFTableCell origCell = row.getCell(i);
                                XWPFTableCell newCell = newRow.addNewTableCell();
                                newCell.setText(origCell.getText());
                            }
                        }
                    }
                }
            }
            FileOutputStream fileOutputStream = new FileOutputStream(newFilePath);
            xwpfDocument.write(fileOutputStream);

            fileOutputStream.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IncorrectFileFormatException(ExceptionMessage.INCORRECT_FILE_FORMAT.toString());
        }
    }
}
