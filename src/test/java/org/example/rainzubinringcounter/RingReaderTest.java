package org.example.rainzubinringcounter;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class RingReaderTest {
    RingReader ringReader = new RingReader();

    @Test
    public void less_than_eight_error_parsing_episod_28(){
        String pathToTheFile = "Documents/NARUTO - S01E28.docx";

        ReaderResult stringIntegerHashtable = ringReader.reader(pathToTheFile, false);
        HashMap<String, Integer> hashMap = stringIntegerHashtable.getHashMap();
        assertEquals(1, hashMap.get("НІНДЖЯ ТРАВИ"));
        assertTrue(stringIntegerHashtable.getErrorsList().size() < 8);
    }

    @Test
    public void one_error_parsing_episod_27(){
        String pathToTheFile = "Documents/NARUTO - S01E27.docx";
        ReaderResult stringIntegerHashtable = ringReader.reader(pathToTheFile, false);
        assertEquals(1, stringIntegerHashtable.getErrorsList().size());
    }

    @Test
    public void one_error_parsing_episod_12(){
        String pathToTheFile = "Documents/NARUTO - S01E12.docx";
        ReaderResult stringIntegerHashtable = ringReader.reader(pathToTheFile, false);
        assertEquals(1, stringIntegerHashtable.getErrorsList().size());
    }
    @Test
    public void one_error_parsing_episod_33ed(){
        String pathToTheFile = "Documents/NARUTO - S01E33_ed.docx";
        ReaderResult stringIntegerHashtable = ringReader.reader(pathToTheFile, false);

        assertEquals(15, stringIntegerHashtable.getHashMap().size());
        assertEquals(9, stringIntegerHashtable.getHashMap().get("МАЛИЙ САСУКЕ"));
    }
}