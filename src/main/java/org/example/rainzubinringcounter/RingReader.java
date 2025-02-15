package org.example.rainzubinringcounter;

import jakarta.annotation.Resource;
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

    @Resource
    GlobalHashMap hashMap;

    private static final Integer MIN_WORDS_TO_RING = 3;
    private List<Ring> ringList;

    public HashMap<String, Integer> reader (String docxPath, boolean calculateTheSumOfRings){
        ringList = new ArrayList<>();
        if (!calculateTheSumOfRings){
            hashMap.getHashMap().clear();
        }

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
                if (hashMap.getHashMap().containsKey(ring.getName())) {
                    hashMap.getHashMap().put(ring.getName(), hashMap.getHashMap().get(ring.getName()) + 1);
                } else {
                    hashMap.getHashMap().put(ring.getName(), 1);
                }
            }
            System.out.println("=== Ring Name Frequency ===");
            for (Map.Entry<String, Integer> entry : hashMap.getHashMap().entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return hashMap.getHashMap();

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
