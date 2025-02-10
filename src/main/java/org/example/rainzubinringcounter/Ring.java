package org.example.rainzubinringcounter;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Ring {
    private String time;
    private String name;
    private String phrase;

    public Ring(String time, String name, String phrase) {
        this.time = time;
        this.name = name;
        this.phrase = phrase;
    }

}

