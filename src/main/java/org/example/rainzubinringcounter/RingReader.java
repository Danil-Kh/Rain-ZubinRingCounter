package org.example.rainzubinringcounter;

import lombok.Getter;
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
    private final List<Ring> ringList = new ArrayList<>();
    private HashMap<String, Integer> hashMap;

    public HashMap<String, Integer> reader (String docxPath){
        hashMap = new HashMap<>();
        try (FileInputStream fis = new FileInputStream(docxPath);
            XWPFDocument document = new XWPFDocument(fis)) {
            if (document.getTables().isEmpty()) {
                log.info("==============Deal with paragraphs==============");
                DealWithRingsInParagraph(document);
            } else {
                log.info("==============Deal with tables==============");
                DealWithRingsInTable(document);
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
        return hashMap;

    }

    private void DealWithRingsInParagraph(XWPFDocument document) {
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            String word = paragraph.getText();
            String[] words = word.split("\\s+");
            if (words.length >= MIN_WORDS_TO_RING) {
                Ring ring = new Ring(words[0], words[1], words[2]);
                ringList.add(ring);
            }
            else {
                log.info("========" + word + "========");
            }

        }
    }

    private void DealWithRingsInTable(XWPFDocument document){
        for (XWPFTable table : document.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                List<XWPFTableCell> cells = row.getTableCells();

                if (cells.size() >= MIN_WORDS_TO_RING) {
                    String time = cells.get(0). getText().trim();
                    String name = cells.get(1). getText().trim();
                    String phrase = cells.get(2). getText().trim();
                    Ring ring = new Ring(time, name, phrase);
                    ringList.add(ring);
                }
            }
        }

    }
}
