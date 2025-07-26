package org.example.rainzubinringcounter;

import java.io.IOException;

@FunctionalInterface
public interface IOThrowingFunction {
    void apply() throws IOException;
}
