package org.example.rainzubinringcounter;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;

import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;


@Slf4j
@Service
public class RingReader {

    private static final Integer MIN_WORDS_TO_RING = 3;
    private List<Ring> ringList;
    public final HashMap<String, Integer> hashMap = new HashMap<>();

    public HashMap<String, Integer> reader (String docxPath, boolean calculateTheSumOfRings){
        ringList = new ArrayList<>();
        if (!calculateTheSumOfRings){
            hashMap.clear();
        }

        try (FileInputStream fis = new FileInputStream(docxPath);
            XWPFDocument document = new XWPFDocument(fis)) {
            if (document.getTables().isEmpty()) {
                log.info("==============Deal with paragraphs==============");
                dealWithRingsInParagraph(document);
            } else {
                log.info("==============Deal with tables==============");
                dealWithRingsInTable(document);
            }

            for (Ring ring : ringList) {
                if (hashMap.containsKey(ring.getName())) {
                    hashMap.put(ring.getName(), hashMap.get(ring.getName()) + 1);
                } else {
                    hashMap.put(ring.getName(), 1);
                }
            }
            System.out.println("=== Ring Name Frequency ===");
            for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HashMap<>(hashMap);
    }

    private void dealWithRingsInParagraph(XWPFDocument document) {
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            String word = paragraph.getText();
            String[] words = word.split("\\s+");
            if (isValidDouble(words[0])){
                if (words.length >= MIN_WORDS_TO_RING) {
                    Ring ring = new Ring(words[0], words[1], words[2]);
                    ringList.add(ring);
                }
            }
            else  {
                System.out.println("Invalid time code: " + words[0] + " " + "in the sentence: " + words[0] + " " + words[1] + " " + words[2]);
                //throw new RuntimeException("Invalid time code: " + words[0] + " " + "in the sentence: " + words[0] + " " + words[1] + " " + words[2]);
            }
        }
    }

    private void dealWithRingsInTable(XWPFDocument document){
        String t = "", n = "", p = "";
        for (XWPFTable table : document.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                List<XWPFTableCell> cells = row.getTableCells();

                if (cells.size() >= MIN_WORDS_TO_RING) {
                    String time = cells.get(0). getText().trim();
                    String name = cells.get(1). getText().trim();
                    String phrase = cells.get(2). getText().trim();
                    t= time; n = name; p = phrase;
                    if (isValidDouble(time)){
                        Ring ring = new Ring(time, name, phrase);
                        ringList.add(ring);
                    }
                } else {
                    System.out.println("Invalid time code: " + t + " " + "in the sentence: " + t + " " + n + " " + p);
                    //throw new RuntimeException("Invalid time code: " + t + " " + "in the sentence: " + t + " " + n + " " + p);
                }
            }
        }
    }

    public static boolean isValidDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
