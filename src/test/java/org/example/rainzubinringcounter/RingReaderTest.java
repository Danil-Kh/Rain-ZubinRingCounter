package org.example.rainzubinringcounter;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class RingReaderTest {
    @Resource
    RingReader ringReader;

    @Test
    public void TestToReadTheFile(){
        String pathToTheFile = "C:\\Users\\nikit\\IdeaProjects\\Rain-ZubinRingCounter\\Documents\\NARUTO - S01E12.docx";
        ringReader.reader(pathToTheFile);
        assert pathToTheFile.endsWith(".docx");
    }




}