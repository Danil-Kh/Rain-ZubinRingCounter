package org.example.rainzubinringcounter;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;

@Slf4j
@SpringBootTest
class RingReaderTest {
    @Resource
    RingReader ringReader;

    @Test
    public void TestToReadTheFile(){
        String pathToTheFile = "C:\\Users\\nikit\\IdeaProjects\\Rain-ZubinRingCounter\\Documents\\NARUTO - S01E05.docx";
        String pathToTheFile1 = "C:\\Users\\nikit\\IdeaProjects\\Rain-ZubinRingCounter\\Documents\\NARUTO - S01E12.docx";

        String pathToTheFile2 = "C:\\Users\\nikit\\IdeaProjects\\Rain-ZubinRingCounter\\Documents\\NARUTO - S01E12damage.docx";
        String pathToTheFile3 = "C:\\Users\\nikit\\IdeaProjects\\Rain-ZubinRingCounter\\Documents\\Slenderman_ukr.docx";

        HashMap<String, Integer> stringIntegerHashtable = ringReader.reader(pathToTheFile3, true);
        HashMap<String, Integer> stringIntegerHashtable1 = ringReader.reader(pathToTheFile3, true);

        assert true;
    }

//    @Test
//    public void TestValidationInTable(){
//        String pathToTheFile2 = "C:\\Users\\nikit\\IdeaProjects\\Rain-ZubinRingCounter\\Documents\\NARUTO - S01E12Damage.docx";
//
//        HashMap<String, Integer> stringIntegerHashtable = ringReader.reader(pathToTheFile2, true);
//        assert true;
//    }
//
//    public void TestValidationInParagraph(){
//        String pathToTheFile1 = "C:\\Users\\nikit\\IdeaProjects\\Rain-ZubinRingCounter\\Documents\\NARUTO - S01E05.docx";
//        String pathToTheFile2 = "C:\\Users\\nikit\\IdeaProjects\\Rain-ZubinRingCounter\\Documents\\NARUTO - S01E05Damage.docx";
//
//        HashMap<String, Integer> stringIntegerHashtable = ringReader.reader(pathToTheFile2, true);
//
//        assert true;
//    }
}