package org.example.rainzubinringcounter;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;

import org.example.rainzubinringcounter.exception.ExceptionMessage;
import org.example.rainzubinringcounter.exception.GlobalExceptionHandler;
import org.example.rainzubinringcounter.exception.TheHeaderTableException;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.Collator;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class RingReader {
    private static final Integer MIN_WORDS_TO_RING = 2;
    private List<Ring> ringList;
    private final Collator ukrLang = Collator.getInstance(Locale.of("uk", "UA"));
    public final Map<String, Integer> sortedHashMap = new TreeMap<>(ukrLang);
    public final List<String> errorsList = new ArrayList<>();
    private final Map<String, List<String>> nameToTimes = new HashMap<>();
    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    public ReaderResult reader(String docxPath, boolean calculateTheSumOfRings){
        ringList = new ArrayList<>();
        errorsList.clear();
        if (!calculateTheSumOfRings){
            clearSortedHashMapAndNameToTimes();
        }

        try (FileInputStream fis = new FileInputStream(docxPath);
            XWPFDocument originalDocument = new XWPFDocument(fis)) {
            if (originalDocument.getTables().isEmpty()) {
                dealWithRingsInParagraph(originalDocument);
            }
            else {
                dealWithRingsInTable(originalDocument);
            }
                for (Ring ring : ringList) {
                    if (!calculateTheSumOfRings) {
                        nameToTimesScore(ring);
                    }
                    if (sortedHashMap.containsKey(ring.getName())) {
                        sortedHashMap.put(ring.getName(), sortedHashMap.get(ring.getName()) + 1);
                    }
                    else {
                        sortedHashMap.put(ring.getName(), 1);
                    }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return new ReaderResult(errorsList, nameToTimes, sortedHashMap);
    }

    private void nameToTimesScore(Ring ring) {
            nameToTimes.computeIfAbsent(ring.getName(), k -> new ArrayList<>()).add(ring.getTime());
    }

    public void clearSortedHashMapAndNameToTimes() {
        sortedHashMap.clear();
        nameToTimes.clear();
    }

    private void dealWithRingsInParagraph(XWPFDocument document) {
        boolean foundFirstRing  = false;
        Pattern pattern = Pattern.compile(".*\\p{L}.*");
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            String sentence = paragraph.getText();
            String[] words = sentence.split("\\s+");

            if (foundFirstRing) {
                validateRing(sentence, words);
            }
            else {
                if (pattern.matcher(sentence).matches() && isValidTimeCode(words[0])) {
                    foundFirstRing  = true;
                    validateRing(sentence, words);
                }
            }
        }
    }

    private void dealWithRingsInTable(XWPFDocument document){
        boolean skipTheHeader = false;
        for (XWPFTable table : document.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                List<XWPFTableCell> cells = row.getTableCells();
                if (cells.size() != 3) {
                    writeError("The row must have 3 cells" + row);
                    continue;
                }
                String time = cells.get(0). getText().trim();
                String name = cells.get(1). getText().trim();
                String phrase = cells.get(2). getText().trim();
                if (!skipTheHeader) {
                    if (time.equals("ТАЙМ-КОД") && name.equals("ПЕРСОНАЖ") && phrase.equals("ТЕКСТ")){
                        skipTheHeader = true;
                        continue;
                    }
                    globalExceptionHandler.handleException(new TheHeaderTableException(ExceptionMessage.INCORRECT_TABLE_HANDLER.getMessage()));
                    return;
                }

                if (time.isEmpty() || name.isEmpty() || phrase.isEmpty()) {
                    errorsList.add("The cell is empty in the sentence: " + time + " " + name + "\t" + phrase);
                    continue;
                }

                name = name.split("\\(")[0];
                String sentence = time + " " + name + "\t" + phrase;
                String[] words = sentence.split("\\s+");
                validateRing(sentence, words);
            }
        }
    }

    private void validateRing(String sentence, String[] words){
        if (words.length < MIN_WORDS_TO_RING){
            return;
        }
        boolean isValidTimeCode = isValidTimeCode(words[0]);
        boolean isValidName = isValidName(words[1]);

        if (!isValidTimeCode && isValidName) {
           // if the sentence do not have a time code but have a Name
           if (words[1].length() > 1){
               writeError("The sentence does not have a time code: " + "in the sentence: " + sentence);
           }
           return;
        }
        else if (isValidTimeCode && !isValidName){
            // if the sentence has a time code but do not have a time code
            writeError("The sentence does not have a proper Name: " + words[1] + "in the sentence: " + sentence);
            return;
        }
        else if (!isValidTimeCode) {
            return;
        }

        int errorsListCount = errorsList.size();
        words[1] = createName(sentence);
        if (errorsList.size() == errorsListCount){
            Ring ring = new Ring(words[0], words[1], words[2]);
            ringList.add(ring);
            // log.info("The ring was added : {} in the sentence: {}", words[1], sentence);
        }
    }

    private String createName(String word1) {
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

    private boolean isValidTimeCode(String str) {
        if (!str.matches("\\d{2}[:;,.]\\d{2}")) {
            return false;
        }
        String standardizedStr = str.replace(':', '.')
                                    .replace(',', '.')
                                    .replace(';', '.');
        try {
            Double.parseDouble(standardizedStr);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }
    private boolean isValidName(String name){
        Pattern pattern = Pattern.compile("^[\\p{Lu}\\p{N}\\-_/.;:,]+$");
        Matcher matcher = pattern.matcher(name);
        return matcher.matches();
    }
}