package com.uniq.tms.tms_microservice.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CreateBulkUserScheduler {

    private static final  Logger log = LoggerFactory.getLogger(CreateBulkUserScheduler.class);

    private final Path uploadDir;

    public CreateBulkUserScheduler(@Value("${csv.upload.dir}") Path uploadDir){
        this.uploadDir = uploadDir;
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void deleteOldCsvFiles() {
        log.info("Bulk User Cleanup Scheduler started");
        if (!Files.exists(uploadDir)) return;

        try (Stream<Path> files = Files.list(uploadDir)) {
            files.forEach(file -> {
                try {
                    Files.deleteIfExists(file);
                } catch (IOException e) {
                    log.error("Failed to delete bulk user file: {}", file.getFileName(), e);
                }
            });
            log.info("Bulk user files deleted from temp directory successfully");
        } catch (Exception e) {
            log.error("Failed to clean bulk user directory", e);
        }
    }

}
