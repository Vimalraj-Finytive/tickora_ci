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
public class PayRollScheduler {

    private static final Logger log = LoggerFactory.getLogger(PayRollScheduler.class);

    private final Path downloadDir;

    public PayRollScheduler(@Value("${csv.payroll.download.dir}") Path downloadDir){
        this.downloadDir = downloadDir;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void deleteOldCsvFiles() {
        log.info("PayRoll Cleanup Scheduler started");
        if (!Files.exists(downloadDir)) return;

        try (Stream<Path> files = Files.list(downloadDir)) {
            files.forEach(file -> {
                try {
                    Files.deleteIfExists(file);
                } catch (IOException e) {
                    log.error("Failed to delete PayRoll file: {}", file.getFileName(), e);
                }
            });
            log.info("PayRoll files deleted from temp directory successfully");
        } catch (Exception e) {
            log.error("Failed to clean PayRoll directory", e);
        }
    }

}
