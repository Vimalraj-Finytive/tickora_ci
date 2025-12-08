package com.uniq.tms.tms_microservice.shared.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;

@Component
public class ExportStatusTracker {

    private static final Logger log = LogManager.getLogger(ExportStatusTracker.class);

    public void writeStatus(File file, String status) {
        try {
            File statusFile = new File(file.getAbsolutePath() + ".status");
            try (FileWriter writer = new FileWriter(statusFile)) {
                writer.write(status);
            }
        } catch (Exception e) {
            log.error("Failed to write status file: {}", e.getMessage());
        }
    }

    public String readStatus(File file) {
        try {
            File statusFile = new File(file.getAbsolutePath() + ".status");
            if (!statusFile.exists()) return null;
            return Files.readString(statusFile.toPath()).trim();
        } catch (Exception e) {
            log.error("Failed to read status file: {}", e.getMessage());
            return null;
        }
    }

}

