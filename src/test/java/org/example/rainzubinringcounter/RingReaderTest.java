package org.example.rainzubinringcounter;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;

@Slf4j
@SpringBootTest
class RingReaderTest {
    @Resource
    RingReader ringReader;

    @Test
    public void Test28(){
        String pathToTheFile = "C:\\Users\\nikit\\IdeaProjects\\Rain-ZubinRingCounter1\\Documents\\NARUTO - S01E28.docx";

        ReaderResult stringIntegerHashtable = ringReader.reader(pathToTheFile, false);
        HashMap<String, Integer> hashMap = stringIntegerHashtable.getHashMap();
        if (hashMap.get("НІНДЖЯ ТРАВИ") == 1 && stringIntegerHashtable.getErrorsList().size() < 8) {
           assert true;
        }
        else {
            assert false;
        }
    }
    @Test
    public void Test27(){
        String pathToTheFile = "C:\\Users\\nikit\\IdeaProjects\\Rain-ZubinRingCounter1\\Documents\\NARUTO - S01E27.docx";
        ReaderResult stringIntegerHashtable = ringReader.reader(pathToTheFile, false);
        if (stringIntegerHashtable.getErrorsList().size() == 1) {
            assert true;
        }
        else {
            assert false;
        }
    }
    @Test
    public void Test12(){
        String pathToTheFile = "C:\\Users\\nikit\\IdeaProjects\\Rain-ZubinRingCounter1\\Documents\\NARUTO - S01E12.docx";
        ReaderResult stringIntegerHashtable = ringReader.reader(pathToTheFile, false);
        if (stringIntegerHashtable.getErrorsList().size() == 1) {
            assert true;
        }
        else {
            assert false;
        }
    }
}