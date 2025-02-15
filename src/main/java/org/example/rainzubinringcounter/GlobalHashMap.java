package org.example.rainzubinringcounter;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Getter
@Service
public class GlobalHashMap {
    private final HashMap<String, Integer> hashMap = new HashMap<>();
}
