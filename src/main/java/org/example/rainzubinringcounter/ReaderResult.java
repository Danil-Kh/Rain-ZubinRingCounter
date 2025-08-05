package org.example.rainzubinringcounter;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Getter

public class ReaderResult {
    //private HashMap<String, Integer> hashMap;
    private List<String> errorsList;
    private Map<String, List<String>> nameToTimes;
    private Map<String, Integer> sortedMap;

    public ReaderResult(List<String> errorsList, Map<String, List<String>> nameToTimes, Map<String, Integer> sortedMap) {
        this.sortedMap = sortedMap;
        //this.hashMap = hashMap;
        this.errorsList = errorsList;
        this.nameToTimes = nameToTimes;
    }
}
