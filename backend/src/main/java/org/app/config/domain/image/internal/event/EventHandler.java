package org.app.config.domain.image.internal.event;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.app.config.domain.image.internal.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.*;

@Slf4j
@Component
@RequiredArgsConstructor
class EventHandler {

    private final MockableFiles files;

    @EventListener(RemoveOrphanServerFileEvent.class)
    public void handleRemoveOrphanServerFileEvent(RemoveOrphanServerFileEvent event) {
        List<String> removals = event.removals();

        log.info(
                "Remove orphan server file event recieved, num of removals: {}",
                removals.size()
        );

        boolean successAll = true;
        boolean fileNotExists = false;
        List<String> failures = new ArrayList<>(removals.size());
        List<String> notFoundPaths = new ArrayList<>(removals.size());

        for (String removal : removals) {
            log.info("Attempt to remove file: {}", removal);

            Path path;

            try {
                path = Paths.get(removal);
            } catch (InvalidPathException e) {
                String errMsg = String.format("Invalid path: %s", removal);
                log.warn(errMsg, e);

                successAll = false;
                failures.add(removal);
                continue;
            }

            if (!files.exists(path)) {
                log.warn(
                        "Expected to file exists on path({}), but found nothing. "
                        + "Skip file removal",
                        removal
                );
                notFoundPaths.add(removal);
                fileNotExists = true;
                continue;
            }

            try {
                files.delete(path);
            } catch (IOException e) {
                String errMsg = String.format(
                        "Failed to delete file at path: %s",
                        removal
                );
                log.warn(errMsg, e);

                successAll = false;
                failures.add(removal);
            }
        }

        if (successAll) {

            if (!fileNotExists) {
                log.info("Every file has been removed successfully.");
            } else {
                log.warn(
                        "Successfully removed files, but found some "
                        + "non-exsiting paths: {}",
                        notFoundPaths
                );
            }

        } else {
            int numOfFails = failures.size();

            String errMsg = String.format(
                    "Failed to remove files, num of failures: %d, paths: %s",
                    numOfFails, failures
            );
            log.warn(errMsg);

            throw new RuntimeException(errMsg);
        }
    }
}
