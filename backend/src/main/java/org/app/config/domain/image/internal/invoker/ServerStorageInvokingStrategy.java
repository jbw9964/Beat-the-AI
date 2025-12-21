package org.app.config.domain.image.internal.invoker;

import jakarta.annotation.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import lombok.extern.slf4j.*;
import org.app.config.domain.image.*;
import org.app.config.domain.image.internal.*;
import org.app.config.domain.image.internal.event.*;
import org.app.config.domain.image.internal.repository.*;
import org.app.entity.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.*;
import org.springframework.stereotype.*;

@Slf4j
@Component
public non-sealed class ServerStorageInvokingStrategy
        extends AbstractRewardImageInvokingStrategy<ServerStorageActualRewardImage,
        ServerStorageOverviewRewardImage> {

    private final String storageBasePathStr;
    private Path storageBasePath;
    private final UuidProvider uuidProvider;
    private final EntityEditor entityEditor;
    private final ApplicationEventPublisher eventPublisher;

    private final MockableFiles files;

    public ServerStorageInvokingStrategy(
            @Value("${reward-image.server-storing-abs-location}")
            String storageBasePathStr,
            UuidProvider uuidProvider,
            EntityEditor entityEditor,
            ApplicationEventPublisher eventPublisher,
            MockableFiles files
    ) {
        super(
                ServerStorageActualRewardImage.class,
                ServerStorageOverviewRewardImage.class
        );
        this.storageBasePathStr = storageBasePathStr;
        this.uuidProvider = uuidProvider;
        this.entityEditor = entityEditor;
        this.eventPublisher = eventPublisher;
        this.files = files;
    }

    @PostConstruct
    void initStorageBasePath() {

        if (this.storageBasePathStr == null || this.storageBasePathStr.isBlank()) {
            throw new IllegalArgumentException("storageBasePathStr cannot be null or blank");
        }

        try {
            Path basePath = Paths.get(storageBasePathStr);

            if (!files.exists(basePath)) {
                files.createDirectories(basePath);
            } else {
                log.debug("Storage base directory already exists. "
                          + "skip create directories");
            }

            this.storageBasePath = basePath;
        } catch (InvalidPathException e) {
            log.warn(
                    "Invalid path was given: {}", e.getMessage(),
                    e
            );
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.warn(
                    "Failed to create base storage directory: {}", e.getMessage(),
                    e
            );
            throw new RuntimeException(e);
        }

        log.debug(
                "Initialized server storage invocking strategy with base path: {}",
                storageBasePathStr
        );
    }

    @Override
    public byte[] getImageFrom(ActualRewardImage actualRewardImage)
            throws ImageNotFoundException, RewardStorageTypeMismatchException,
            FailedToFindImageOnServerException, FailedToGetFileOnServerException {

        super.throwExOnTypeMismatch(actualRewardImage);
        ServerStorageActualRewardImage casted
                = super.castEntity(actualRewardImage);

        if (casted.doesRemovalScheduled()) {
            throw new ImageNotFoundException();
        }

        return this.readImageFromPath(casted.getActualImagePath());
    }

    @Override
    public byte[] getImageFrom(OverviewRewardImage overviewRewardImage)
            throws ImageNotFoundException, RewardStorageTypeMismatchException,
            FailedToFindImageOnServerException, FailedToGetFileOnServerException {

        super.throwExOnTypeMismatch(overviewRewardImage);
        ServerStorageOverviewRewardImage casted
                = super.castEntity(overviewRewardImage);

        if (casted.doesRemovalScheduled()) {
            throw new ImageNotFoundException();
        }

        return this.readImageFromPath(casted.getOverviewImagePath());
    }

    @Override
    public RewardImageInfo saveRewardImages(
            byte[] actualRewardImage, byte[] overviewRewardImage
    )
            throws RewardStorageTypeMismatchException, FailedToSaveImageToServerException {

        String[] imagePaths = new String[2];
        byte[][] images = new byte[][]{
                actualRewardImage, overviewRewardImage
        };

        log.info("Attempting to save image on server storage.");

        RuntimeException failure = null;
        for (int i = 0; i < images.length; i++) {
            byte[] image = images[i];
            String randomName = uuidProvider.getRandomUuidAsString();
            Path savePath = storageBasePath.resolve(randomName);

            try {
                imagePaths[i] = this.saveImageAndGetAbsPath(image, savePath);
            } catch (FailedToSaveImageToServerException e) {
                failure = e;
                break;
            }
        }

        if (failure != null) {
            List<String> orphans = Arrays.stream(imagePaths)
                    .filter(Objects::nonNull)
                    .toList();
            String message = String.format(
                    "Failed to save image on server due to ex: %s",
                    failure.getClass().getSimpleName()
            );

            throw this.logExAndPublishRemovalEvent(
                    orphans, message, failure
            );
        }

        log.info("Image has been saved to server. "
                 + "Attempting to save path info as entity.");

        ServerStorageActualRewardImage newAEntity
                = new ServerStorageActualRewardImage(imagePaths[0]);
        ServerStorageOverviewRewardImage newOEntity
                = new ServerStorageOverviewRewardImage(imagePaths[1]);

        try {
            RewardImageInfo response = entityEditor.saveEntities(newAEntity, newOEntity);
            log.info("Reward entities has been saved.");
            return response;
        } catch (Exception e) {
            String errMsg = String.format(
                    "Failed to save entity due to ex: %s",
                    e.getClass().getSimpleName()
            );

            List<String> orphans = Arrays.stream(imagePaths)
                    .filter(Objects::nonNull)
                    .toList();

            throw this.logExAndPublishRemovalEvent(
                    orphans, errMsg, e
            );
        }
    }

    @Override
    public Long delete(ActualRewardImage removalEntity)
            throws RewardStorageTypeMismatchException {

        super.throwExOnTypeMismatch(removalEntity);
        ServerStorageActualRewardImage casted
                = super.castEntity(removalEntity);

        if (casted.doesRemovalScheduled()) {
            throw new ImageNotFoundException();
        }

        Long id = removalEntity.getId();

        return entityEditor.requestActualRewardImageRemoval(
                id, removalEntity.getStorageType()
        );
    }

    @Override
    public Long delete(OverviewRewardImage removalEntity)
            throws RewardStorageTypeMismatchException {

        super.throwExOnTypeMismatch(removalEntity);
        ServerStorageOverviewRewardImage casted
                = super.castEntity(removalEntity);

        if (casted.doesRemovalScheduled()) {
            throw new ImageNotFoundException();
        }

        Long id = removalEntity.getId();

        return entityEditor.requestOverviewRewardImageRemoval(
                id, removalEntity.getStorageType()
        );
    }

    @Override
    public RewardStorageType handleableType() {
        return RewardStorageType.SERVER;
    }

    private byte[] readImageFromPath(String absolutePath) {
        Path existingFilePath = getExistingFilePathFrom(absolutePath);

        try {
            return files.readAllBytes(existingFilePath);
        } catch (IOException e) {
            String errMsg = String.format("Failed to read image from file: %s", absolutePath);
            log.warn(errMsg, e);
            throw new FailedToGetFileOnServerException(errMsg, e);
        }
    }

    private FailedToSaveImageToServerException logExAndPublishRemovalEvent(
            List<String> removals, String message, Throwable cause
    ) {
        log.warn("Failed to save image on server storage.");

        if (removals.isEmpty()) {
            log.warn("Skip publishing event due to empty removals");
        } else {
            log.warn("Publishing removal events for paths: {}", removals);

            eventPublisher.publishEvent(new RemoveOrphanServerFileEvent(removals));

            log.warn("Event has been published");
        }

        return new FailedToSaveImageToServerException(message, cause);
    }

    private Path getExistingFilePathFrom(String absolutePath) {

        Path path;
        String errMsg;

        try {
            path = Paths.get(absolutePath);
        } catch (InvalidPathException e) {
            errMsg = String.format(
                    "Received invalid path(%s): %s",
                    absolutePath, e.getMessage()
            );
            log.warn(errMsg, e);
            throw new FailedToFindImageOnServerException(errMsg, e);
        }

        if (!files.exists(path)) {
            errMsg = String.format("No file found with path: %s", absolutePath);
            log.warn(errMsg);
            throw new FailedToFindImageOnServerException(errMsg, null);
        }

        return path;
    }

    private String saveImageAndGetAbsPath(byte[] image, Path dst)
            throws FailedToSaveImageToServerException {
        try {
            return files.write(dst, image).toAbsolutePath().toString();
        } catch (IOException e) {
            String errMsg = String.format("Failed to save image on file: %s", dst);
            log.warn(errMsg, e);
            throw new FailedToSaveImageToServerException(errMsg, e);
        }
    }
}
