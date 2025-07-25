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

import javax.xml.crypto.Data;
import java.io.File;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class RingCounterController {
    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
    private boolean isUpdatingSelection = false;
    private final FileChooser fileChooser = new FileChooser();
    private final RingReader ringReader = new RingReader();
    private String fontFamily = "Calibri";
    private int fontSize = 11;

    @FXML
    public CheckBox sumFile;
    @FXML
    public TextArea textAreaError;
    @FXML
    public CheckBox splitFile;
    @FXML
    private TextArea textArea;
    @FXML
    public Circle circleDrag;
    @FXML
    public Button fileChooserButton;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss");

    public RingCounterController() throws IOException {
    }

    @FXML
    private void initialize() {
        sumFile.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            if (isUpdatingSelection) return;

            if (isNowSelected) {
                isUpdatingSelection = true;
                splitFile.setSelected(false);
                ringReader.hashMap.clear();
                isUpdatingSelection = false;
            }
        });

        splitFile.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            if (isUpdatingSelection) return;

            if (isNowSelected) {
                isUpdatingSelection = true;
                sumFile.setSelected(false);
                isUpdatingSelection = false;
            }
        });

        fileChooserButton.setOnAction(event -> {
            StringBuilder sb = new StringBuilder();
            XWPFDocument newWordDoc = new XWPFDocument();
            String newFilePath;
            try {
                newFilePath = Paths.get(createDirectory(), "ring_All " + LocalDateTime.now().format(formatter) + ".docx").toString();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            textArea.clear();
            File file = fileChooser.showOpenDialog(fileChooserButton.getScene().getWindow());
            if (isValidFile(file)) {
                globalExceptionHandler.handleException(
                        new IncorrectFileFormatException(ExceptionMessage.INCORRECT_FILE_FORMAT.getMessage()));
                return;
            }
            PrintAllInformationAboutTheFileInTextField(file, sb);
            safeCreateDocument(sb, file, true, newWordDoc, newFilePath);
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
            Dragboard eventDragboard = event.getDragboard();
            textArea.clear();
            if (eventDragboard.hasFiles()) {
                List<File> files = eventDragboard.getFiles();

                if (sumFile.isSelected()) {
                    try {
                        handleSumFiles(files, eventDragboard);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else if (splitFile.isSelected()) {
                    try {
                        handleSplitFiles(files, eventDragboard);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        handleDefault(files, eventDragboard);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
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

    private void handleDefault(List<File> files, Dragboard eventDragboard) throws IOException {
        XWPFDocument newWordDoc = new XWPFDocument();
        String newFilePath = getFileNameWithTime("ring_All");
        for (File file : files) {
            StringBuilder stringBuilder = new StringBuilder();
            PrintAllInformationAboutTheFileInTextField(file, stringBuilder, eventDragboard);

            if (!stringBuilder.isEmpty()) {
                safeCreateDocument(stringBuilder, file, false, newWordDoc, newFilePath);
            }
        }
    }

    private void handleSplitFiles(List<File> files, Dragboard eventDragboard) throws IOException {
        XWPFDocument newWordDoc = new XWPFDocument();
        for (File file : files) {
            StringBuilder stringBuilder = new StringBuilder();
            PrintAllInformationAboutTheFileInTextField(file, stringBuilder, eventDragboard);

            if (!stringBuilder.isEmpty()) {
                safeCreateDocument(stringBuilder, file, true, newWordDoc, "");
            }
        }
    }

    private void handleSumFiles(List<File> files, Dragboard eventDragboard) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder allFilesName = new StringBuilder("The sum consist of the next files: \n");
        ReaderResult readerResultForSum = null;
        String newFilePath = getFileNameWithTime("sum_All");
        XWPFDocument newWordDoc = new XWPFDocument();
        for (File file : files) {
            if (isValidFile(file)) continue;
            allFilesName.append(file.getName()).append("\n");
            readerResultForSum = ringReader.reader(file.getAbsolutePath(), false);
            displayErrors(file, eventDragboard, readerResultForSum);
        }

        if (readerResultForSum != null) {
            printRingToTextArea(stringBuilder, files.getFirst(), readerResultForSum);
            int newlineIndex = stringBuilder.indexOf("\n");
            if (newlineIndex != -1) {
                stringBuilder.delete(0, newlineIndex + 1);
            }

            stringBuilder.insert(0, allFilesName);
            stringBuilder.append("\n");
            safeCreateDocument(stringBuilder, files.getFirst(),false, newWordDoc, newFilePath);
        }
    }

    private String getFileNameWithTime(String fileName) throws IOException {
        return Paths.get(createDirectory(), fileName + " " + LocalDateTime.now().format(formatter) + ".docx").toString();
    }

    private void safeCreateDocument(StringBuilder content, File file, boolean splitOriginalFilesTexts
            , XWPFDocument newWordDoc, String newFilePath) {
        try {
            createDocument(content, file, splitOriginalFilesTexts, newWordDoc, newFilePath);
        } catch (IncorrectFileFormatException e) {
            globalExceptionHandler.handleException(e);
        }
    }

    private void PrintAllInformationAboutTheFileInTextField(File file
            , StringBuilder stringBuilder, Dragboard eventDragboard) {
        if (isValidFile(file)) {
            return;
        }
        ReaderResult readerResult = ringReader.reader(file.getAbsolutePath(), sumFile.isSelected());
        printRingToTextArea(stringBuilder, file, readerResult);
        displayErrors(file, eventDragboard, readerResult);
    }

    private void PrintAllInformationAboutTheFileInTextField(File file
            , StringBuilder stringBuilder) {
        if (isValidFile(file)) {
            return;
        }
        ReaderResult readerResult = ringReader.reader(file.getAbsolutePath(), sumFile.isSelected());
        printRingToTextArea(stringBuilder, file, readerResult);
        displayErrors(file, readerResult);
    }


    private void printRingToTextArea(StringBuilder sb, File file, ReaderResult readerResult) {
        HashMap<String, Integer> resultToPrintRing;
        Map<String, List<String>> resultToPrintNameToTime;
        resultToPrintRing = readerResult.getHashMap();
        resultToPrintNameToTime = readerResult.getNameToTimes();

        sb.append("---").append(file.getName()).append("---").append("\n");
        for (String key : resultToPrintRing.keySet()) {
            sb.append(key).append(": ").append(resultToPrintRing.get(key)).append("\n");
            sb.append(key).append(": ").append(resultToPrintNameToTime.get(key)).append("\n");
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

    private String createDirectory() throws IOException {
        String userHome = System.getProperty("user.home");
        String dirPath = Paths.get(userHome, "Documents", "GeneratedDocs").toString();
        File dir = new File(dirPath);

        boolean dirCreated = dir.mkdirs();
        if (!dir.exists() && !dirCreated) {
            throw new IOException("Failed to create directory: " + dirPath);
        }
        return dirPath;
    }

    private void createDocument(StringBuilder sb, File originalFile, boolean splitOriginalFilesTexts
            , XWPFDocument newWordDoc, String newFilePath) throws IncorrectFileFormatException {
        try {
            String dirPath = createDirectory();

            FileInputStream fileInputStream = new FileInputStream(originalFile);
            XWPFDocument originalWordDoc = new XWPFDocument(fileInputStream);
            if (splitOriginalFilesTexts) {
                newFilePath = Paths.get(dirPath, "ring_" + originalFile.getName()).toString();
                newWordDoc = new XWPFDocument();
            }

            XWPFParagraph samplePar =
                    originalWordDoc.getParagraphs().isEmpty() ? null : originalWordDoc.getParagraphs().get(0);

            CTPPr samplePPr = (samplePar != null && samplePar.getCTP().getPPr() != null)
                    ? (CTPPr) samplePar.getCTP().getPPr().copy() : null;

            STVerticalAlignRun.Enum verticalAlignment = null;
            if (!originalWordDoc.getParagraphs().isEmpty()) {
                XWPFParagraph firstParagraph = originalWordDoc.getParagraphs().get(0);
                if (!firstParagraph.getRuns().isEmpty()) {
                    XWPFRun firstRun = firstParagraph.getRuns().get(0);
                    if (firstRun.getFontFamily() != null) fontFamily = firstRun.getFontFamily();
                    if (firstRun.getFontSize() > 0) fontSize = firstRun.getFontSize();
                    if (firstRun.getVerticalAlignment() != null) verticalAlignment = firstRun.getVerticalAlignment();
                }
            }

            String textForTheNameOfTheFile = "\nOriginal text from the file: " + originalFile.getName();
            sb.append(textForTheNameOfTheFile);
            String[] lines = sb.toString().split("\n");

            for (String line : lines) {
                XWPFParagraph paragraph = newWordDoc.createParagraph();
                if (samplePPr != null) {
                    paragraph.getCTP().setPPr((CTPPr) samplePPr.copy());
                }
                XWPFRun run = paragraph.createRun();
                run.setText(line);
                run.setFontFamily(fontFamily);
                run.setFontSize(fontSize);
                if (Objects.equals(lines[lines.length - 1], line))
                {
                    run.setBold(true);
                }
                if (verticalAlignment != null) run.setVerticalAlignment(verticalAlignment.toString());
            }

            sb.delete(sb.length() - 1, sb.length());
            for (IBodyElement elem : originalWordDoc.getBodyElements()) {
                if (elem instanceof XWPFParagraph origPar) {
                    XWPFParagraph newPar = newWordDoc.createParagraph();

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

                } else if (elem instanceof XWPFTable origTable) {
                    XWPFTable newTable = newWordDoc.createTable();
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
            newWordDoc.write(fileOutputStream);

            fileOutputStream.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IncorrectFileFormatException(ExceptionMessage.INCORRECT_FILE_FORMAT.toString());
        }
    }
}
