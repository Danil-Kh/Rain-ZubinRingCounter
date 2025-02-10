package org.example.rainzubinringcounter;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;


@Slf4j
@Service
public class RingReader {

    private final HashMap<String, Integer> hashtable = new HashMap<>();
    private final List<Ring> tableList = new ArrayList<>();

    public void reader (String docxPath){
        try (FileInputStream fis = new FileInputStream(docxPath);
             XWPFDocument document = new XWPFDocument(fis)) {

            List<XWPFParagraph> paragraphs = document.getParagraphs();
            List<XWPFTable> tables = document.getTables();
             //tables.toString().trim().split("\\s+");



            System.out.println("================start table===============");
            for (XWPFTable table : document.getTables()) {
                // Iterate over rows
                for (XWPFTableRow row : table.getRows()) {
                    List<XWPFTableCell> cells = row.getTableCells();

                    if (cells.size() >= 3) {  // Ensure there are at least 3 columns
                        String time = cells.get(0). getText().trim();
                        String name = cells.get(1). getText().trim();
                        String phrase = cells.get(2). getText().trim();

                        Ring ring = new Ring(time, name, phrase);
                        //System.out.println(ring.getTime() + " " + ring.getName() + " " + ring.getPhrase());
                        tableList.add(ring);
                    }
                }
            }

            for (Ring ring : tableList) {
                if (hashtable.containsKey(ring.getName())) {
                    hashtable.put(ring.getName(), hashtable.get(ring.getName()) + 1);
                } else {
                    hashtable.put(ring.getName(), 1);
                }
            }
            System.out.println("=== Ring Name Frequency ===");
            for (Map.Entry<String, Integer> entry : hashtable.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }

            /*System.out.println("================start paragraph===============");
            for (XWPFParagraph paragraph : paragraphs) {
                System.out.println(paragraph.getText());
            }*/

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
