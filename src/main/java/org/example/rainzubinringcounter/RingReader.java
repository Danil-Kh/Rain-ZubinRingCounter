package org.example.rainzubinringcounter;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;

import org.example.rainzubinringcounter.exception.ExceptionMessage;
import org.example.rainzubinringcounter.exception.GlobalExceptionHandler;
import org.example.rainzubinringcounter.exception.TheHeaderTableException;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@Service
public class RingReader {

    private static final Integer MIN_WORDS_TO_RING = 2;
    private List<Ring> ringList;
    public final HashMap<String, Integer> hashMap = new HashMap<>();
    public final List<String> errorsList = new ArrayList<>();
    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    public ReaderResult reader(String docxPath, boolean calculateTheSumOfRings){
        ringList = new ArrayList<>();
        errorsList.clear();
        if (!calculateTheSumOfRings){
            hashMap.clear();
        }

        try (FileInputStream fis = new FileInputStream(docxPath);
            XWPFDocument document = new XWPFDocument(fis)) {
            if (document.getTables().isEmpty()) {
//                log.info("==============Deal with paragraphs==============");
                dealWithRingsInParagraph(document);
            } else {
//                log.info("==============Deal with tables==============");
                dealWithRingsInTable(document);
            }

            for (Ring ring : ringList) {
                if (hashMap.containsKey(ring.getName())) {
                    hashMap.put(ring.getName(), hashMap.get(ring.getName()) + 1);
                } else {
                    hashMap.put(ring.getName(), 1);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ReaderResult(hashMap, errorsList);
    }

    private void dealWithRingsInParagraph(XWPFDocument document) {
        boolean skipFirstNames = false;
        Pattern pattern = Pattern.compile(".*\\p{L}.*");
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            String sentence = paragraph.getText();
            String[] words = sentence.split("\\s+");
            if (skipFirstNames) {
                validateRing(sentence, words, true);
            }
            else {
                if (pattern.matcher(sentence).matches()) {
                    if (!isValidDouble(words[0]))
                    {
                        continue;
                    }
                    else {
                        validateRing(sentence, words, true);
                        skipFirstNames = true;
                    }
                }
            }
        }
    }



    private void dealWithRingsInTable(XWPFDocument document){
        boolean checkTheHeader = false;
        for (XWPFTable table : document.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                List<XWPFTableCell> cells = row.getTableCells();
                if (cells.size() == 3) {
                    String time = cells.get(0). getText().trim();
                    String name = cells.get(1). getText().trim();
                    String phrase = cells.get(2). getText().trim();
                    if (!checkTheHeader) {
                        if (time.equals("ТАЙМ-КОД") && name.equals("ПЕРСОНАЖ") && phrase.equals("ТЕКСТ")){
                            checkTheHeader = true;
                            continue;
                        }
                    }
                    if (!checkTheHeader) {
                        globalExceptionHandler.handleException(new TheHeaderTableException(ExceptionMessage.INCORRECT_TABLE_HANDLER.getMessage()));
                        return;
                    }

                    String sentence = time + " " + name + " " + phrase;
                    String[] words = sentence.split("\\s+");
                    if (time.isEmpty() || name.isEmpty() || phrase.isEmpty()) {
                        errorsList.add("The cell is empty in the sentence: " + sentence);
                        continue;
                    }
                    validateRing(sentence, words, false);
                }
                else {
                    writeError("The row must have 3 cells" + row);
                }
            }
        }
    }

    private void validateRing(String sentence, String[] words, boolean isForParagraph){
        if (words.length > 0){
            if (words.length >= MIN_WORDS_TO_RING){
                boolean isValidDouble = isValidDouble(words[0]);
                //Pattern pattern = Pattern.compile("^[\\p{Lu}\\-_/.;:,]+$");
                Pattern pattern = Pattern.compile("^[\\p{Lu}\\p{N}\\-_/.;:,]+$");
                Matcher matcher = pattern.matcher(words[1]);
                boolean hasTheName = matcher.matches();

                if (isValidDouble && hasTheName){
                    int errorsListCount = errorsList.size();
                    if (isForParagraph){
                        words[1] = createTheName(sentence);
                    }
                    if (errorsList.size() - 1 != errorsListCount){
                        Ring ring = new Ring(words[0], words[1], words[2]);
                        ringList.add(ring);
                       // log.info("The ring was added : {} in the sentence: {}", words[1], sentence);
                    }
                } else if (!isValidDouble && hasTheName) {
                    // if the sentence do not have a time code but have a Name
                    if (words[1].length() > 1){
                        writeError("The sentence does not have a time code: " + "in the sentence: " + sentence);
                    }
                }
                else if (isValidDouble && !hasTheName){
                    // if the sentence has a time code but do not have a time code
                     writeError("The sentence does not have a proper Name: " + words[1] + "in the sentence: " + sentence);
                }
            }
        }
    }

    private String createTheName(String word1) {
        String result;
        String word = word1.replaceFirst("^\\s*\\S+", "").trim();
        if (word.contains("\t")){
            word = word.split("\t")[0];
            result = word.trim();
        }
        else {
            String error = "The sentence does not have a TAB: " +  word1;
            result = error;
            errorsList.add(error);
        }
        return result;
    }

    private void writeError(String error){
        errorsList.add(error);
    }

    private static boolean isValidDouble(String str) {
        if (!str.matches("\\d{2}[:;,\\.]\\d{2}")) {
            return false;
        }
        String standardizedStr = str.replace(':', '.').replace(',', '.').replace(';', '.');
        try {
            Double.parseDouble(standardizedStr);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
