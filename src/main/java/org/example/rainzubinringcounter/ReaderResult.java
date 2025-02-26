package org.example.rainzubinringcounter;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Getter
public class ReaderResult {
    private HashMap<String, Integer> hashMap;
    private List<String> errorsList;

    public ReaderResult(HashMap<String, Integer> hashMap, List<String> errorsList) {
        this.hashMap = hashMap;
        this.errorsList = errorsList;
    }
}
