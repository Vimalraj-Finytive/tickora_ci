package com.uniq.tms.tms_microservice.shared.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Component
public class TimeoffRequestScheduler {

    private static final Logger log = LoggerFactory.getLogger(TimeoffRequestScheduler.class);

    private final Path downloadDir;

    public TimeoffRequestScheduler(@Value("${csv.request.download.dir}") Path downloadDir){
        this.downloadDir = downloadDir;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void deleteOldCsvFiles() {
        log.info("Bulk Timeoff request Cleanup Scheduler started");
        if (!Files.exists(downloadDir)) return;

        try (Stream<Path> files = Files.list(downloadDir)) {
            files.forEach(file -> {
                try {
                    Files.deleteIfExists(file);
                } catch (IOException e) {
                    log.error("Failed to delete Bulk Timeoff requestr file: {}", file.getFileName(), e);
                }
            });
            log.info("Bulk Timeoff request files deleted from temp directory successfully");
        } catch (Exception e) {
            log.error("Failed to clean Bulk Timeoff request directory", e);
        }
    }

}
