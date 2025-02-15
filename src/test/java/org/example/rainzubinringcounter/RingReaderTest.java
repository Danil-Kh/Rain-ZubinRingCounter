package org.example.rainzubinringcounter;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;

@SpringBootTest
class RingReaderTest {
    @Resource
    RingReader ringReader;

    @Test
    public void TestToReadTheFile(){
        String pathToTheFile = "C:\\Users\\nikit\\IdeaProjects\\Rain-ZubinRingCounter\\Documents\\NARUTO - S01E05.docx";
        String pathToTheFile1 = "C:\\Users\\nikit\\IdeaProjects\\Rain-ZubinRingCounter\\Documents\\NARUTO - S01E12.docx";

        HashMap<String, Integer> stringIntegerHashtable = ringReader.reader(pathToTheFile, true);
        HashMap<String, Integer> stringIntegerHashtable1 = ringReader.reader(pathToTheFile1, true);

        assert pathToTheFile.endsWith(".docx");
    }
}