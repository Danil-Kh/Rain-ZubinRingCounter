package org.example.rainzubinringcounter;

import javafx.application.Application;
import org.example.rainzubinringcounter.configuration.RingCounter;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.*;

@SpringBootApplication
public class RainZubinRingCounterApplication {

    private static final int REQUIRED_JAVA_VERSION = 17;

    public static void main(String[] args) {
        if (!validateJavaVersion()) {
            String javaVersion = System.getProperty("java.version");
            String errorMessage = String.format(
                    "Ошибка: Требуется Java %d или выше.\nТекущая версия: %s",
                    REQUIRED_JAVA_VERSION,
                    javaVersion
            );

            JOptionPane.showMessageDialog(null,
                    errorMessage,
                    "Несовместимая версия Java",
                    JOptionPane.ERROR_MESSAGE);

            System.err.println(errorMessage);
            System.exit(1);
        }

        Application.launch(RingCounter.class, args);
    }

    private static boolean validateJavaVersion() {
        String version = System.getProperty("java.version");
        try {
            String[] versionParts = version.split("\\.");
            int majorVersion;

            if (versionParts[0].equals("1")) {
                majorVersion = Integer.parseInt(versionParts[1]);
            } else {
                majorVersion = Integer.parseInt(versionParts[0]);
            }

            return majorVersion >= REQUIRED_JAVA_VERSION;
        } catch (Exception e) {
            System.err.println("Unable to determine Java version: " + version);
            return false;
        }
    }

    public static String getJavaVersion() {
        return System.getProperty("java.version");
    }
}
