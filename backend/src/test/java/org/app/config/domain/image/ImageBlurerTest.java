package org.app.config.domain.image;

import java.io.*;
import java.nio.file.*;
import lombok.extern.slf4j.*;
import org.app.config.domain.image.internal.*;
import org.junit.jupiter.api.*;
import org.springframework.util.*;

@Slf4j
class ImageBlurerTest {

    private static final String BASE_CLASS_PATH = String.format(
            "%sasset", ResourceUtils.CLASSPATH_URL_PREFIX
    );
    private static final String TEST_IMG_PATH = String.format(
            "%s/temp-image.png",
            BASE_CLASS_PATH
    );
    private static final Path OVERVIEW_IMG_DST_PATH = Paths.get(
            "build/blurred-image.png"
    );

    static byte[] testImage;

    ImageBlurer imageBlurer = new ImageBlurerImpl();

    @BeforeAll
    static void setup() {
        log.info("Reading test image from: {}", TEST_IMG_PATH);

        try {
            testImage = Files.readAllBytes(
                    ResourceUtils.getFile(TEST_IMG_PATH).toPath()
            );
        } catch (IOException e) {
            log.warn("Failed to read test image", e);
            throw new AssertionError(e);
        }

        log.info("Test image has been imported.");
    }

    @Test
    @DisplayName("블러처리된 이미지를 생성할 수 있다.")
    void blurImage() {

        byte[] blurredImage = imageBlurer.blurImage(testImage);

        log.info("Blur image test success. Exporting overview image.");

        try {
            Path parent = OVERVIEW_IMG_DST_PATH.getParent();

            if (!Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            Files.write(OVERVIEW_IMG_DST_PATH, blurredImage);

            log.info(
                    "Overview image has been created on: {}",
                    OVERVIEW_IMG_DST_PATH
            );

        } catch (Exception e) {
            log.warn("Failed to export overview image on test", e);
        }
    }
}