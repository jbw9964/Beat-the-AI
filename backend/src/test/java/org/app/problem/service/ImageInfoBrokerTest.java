package org.app.problem.service;

import static org.assertj.core.api.Assertions.*;

import java.io.*;
import java.nio.file.*;
import lombok.extern.slf4j.*;
import org.junit.jupiter.api.*;
import org.springframework.http.*;
import org.springframework.util.*;

@Slf4j
class ImageInfoBrokerTest {

    private static final String BASE_CLASS_PATH = String.format(
            "%sasset", ResourceUtils.CLASSPATH_URL_PREFIX
    );
    private static final String PNG = String.format(
            "%s/test-png-image.png",
            BASE_CLASS_PATH
    );
    private static final String JPG = String.format(
            "%s/test-jpg-image.jpg",
            BASE_CLASS_PATH
    );
    private static final String HEIC = String.format(
            "%s/test-heic-image.heic",
            BASE_CLASS_PATH
    );

    static byte[] testPngImage, testJpgImage, testHeicImage;

    ImageInfoBroker imageInfoBroker = new ImageInfoBroker();

    @BeforeAll
    static void setup() {
        log.info("Reading test images from: {}", BASE_CLASS_PATH);

        try {
            testPngImage = Files.readAllBytes(
                    ResourceUtils.getFile(PNG).toPath()
            );
            testJpgImage = Files.readAllBytes(
                    ResourceUtils.getFile(JPG).toPath()
            );
            testHeicImage = Files.readAllBytes(
                    ResourceUtils.getFile(HEIC).toPath()
            );
        } catch (IOException e) {
            log.warn("Failed to read test image", e);
            throw new AssertionError(e);
        }

        log.info("Test image has been imported.");
    }

    @Test
    @DisplayName("PNG, JPG 이미지를 식별할 수 있다.")
    void examineImageMediaType() {
        assertThat(imageInfoBroker.examineImageMediaType(testPngImage))
                .isEqualTo(MediaType.IMAGE_PNG);
        assertThat(imageInfoBroker.examineImageMediaType(testJpgImage))
                .isEqualTo(MediaType.IMAGE_JPEG);
    }

    @Test
    @DisplayName("PNG, JPG 이미지에 대해선 true 를 뱉는다.")
    void acceptableImage1() {
        assertThat(imageInfoBroker.acceptableImage(testPngImage)).isTrue();
        assertThat(imageInfoBroker.acceptableImage(testJpgImage)).isTrue();
    }

    @Test
    @DisplayName("PNG, JPG 가 아닌 이미지에 대해선 false 를 뱉는다.")
    void acceptableImage2() {
        assertThat(imageInfoBroker.acceptableImage(testHeicImage)).isFalse();
    }
}