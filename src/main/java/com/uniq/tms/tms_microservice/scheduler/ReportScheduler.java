package com.uniq.tms.tms_microservice.scheduler;

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
public class ReportScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReportScheduler.class);

    private final Path tempDirectory;

    public ReportScheduler(@Value("${csv.download.dir}") Path tempDirectory){
        this.tempDirectory = tempDirectory;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void reportScheduler() {
        log.info("Report Cleanup Scheduler started");
        if (!Files.exists(tempDirectory)) return;
        try (Stream<Path> files = Files.list(tempDirectory)) {
            files.forEach(file -> {
                try {
                    Files.deleteIfExists(file);
                } catch (IOException e) {
                    log.error("Failed to delete report file: {}", file.getFileName(), e);
                }
            });
            log.info("Report files deleted from temp directory successfully");
        } catch (Exception e) {
            log.error("Failed to clean report directory", e);
        }
    }
}
