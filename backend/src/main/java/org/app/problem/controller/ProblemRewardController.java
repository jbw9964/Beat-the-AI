package org.app.problem.controller;

import io.swagger.v3.oas.annotations.media.*;
import jakarta.validation.*;
import java.io.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.app.config.domain.image.*;
import org.app.problem.domain.exception.*;
import org.app.problem.dto.*;
import org.app.problem.dto.request.*;
import org.app.problem.dto.response.*;
import org.app.problem.service.*;
import org.app.util.api.*;
import org.springdoc.core.annotations.*;
import org.springframework.http.*;
import org.springframework.security.core.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/problem/{problem-id:\\d+}/reward")
public class ProblemRewardController {

    private final ProblemRewardService rewardService;
    private final ImageInfoBroker imageInfoBroker;

    private final RewardImageHandler rewardImageHandler;
    private final ImageBlurer imageBlurer;

    // 문제 설정된 보상 목록 보기
    @GetMapping
    public ApiResponse<GetProblemRewardsResponse> getProblemRewards(
            @PathVariable("problem-id") Long problemId,
            @AuthenticationPrincipal Long authenticatedUserId,
            @Valid @ParameterObject @ModelAttribute
            SimplePageRequest pageRequest
    ) {
        int pageNo = pageRequest.pageNum();
        int pageSize = pageRequest.pageSize();

        GetProblemRewardsResponse response = rewardService.getRewards(
                problemId, authenticatedUserId, pageNo, pageSize
        );

        return ApiResponse.success(response);
    }

    // 문제 보상 미리보기 이미지 보기
    @GetMapping("/{reward-id:\\d+}/overview")
    public ResponseEntity<byte[]> getOverviewRewardImage(
            @PathVariable("problem-id") Long problemId,
            @PathVariable("reward-id") Long rewardId,
            @AuthenticationPrincipal Long authenticatedUserId
    ) {

        Long overviewRewardImageId = rewardService.getOverviewRewardImageId(
                problemId, rewardId, authenticatedUserId
        );

        log.info("Obtained overview reward image id. Requesting image.");

        byte[] image = rewardImageHandler.getOverviewRewardImage(
                overviewRewardImageId
        );

        log.info("Obtained overview image.");

        MediaType mediaType = imageInfoBroker.examineImageMediaType(
                image
        );

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(image);
    }

    // 실제 문제 보상 이미지 보기
    @GetMapping("/{reward-id:\\d+}/actual")
    public ResponseEntity<byte[]> getActualRewardImage(
            @PathVariable("problem-id") Long problemId,
            @PathVariable("reward-id") Long rewardId,
            @AuthenticationPrincipal Long authenticatedUserId
    ) {

        Long actualRewardImageId = rewardService.getActualRewardImageId(
                problemId, rewardId, authenticatedUserId
        );

        log.info("Obtained actual reward image id. Requesting image.");

        byte[] image = rewardImageHandler.getActualRewardImage(
                actualRewardImageId
        );

        log.info("Obtained actual image.");

        MediaType mediaType = imageInfoBroker.examineImageMediaType(
                image
        );

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(image);
    }

    // 문제 보상 추가하기
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(encoding = @Encoding(
                    name = "request", contentType = MediaType.APPLICATION_JSON_VALUE
            ))
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Long> createReward(
            @PathVariable("problem-id") Long problemId,
            @AuthenticationPrincipal Long authenticatedUserId,
            @Valid @RequestPart(name = "request") CreateRewardRequest request,
            @RequestPart MultipartFile actualRewardImage,
            @RequestPart(required = false) MultipartFile overviewRewardImage
    ) {

        // 문제 보상 추가하기 전 문제 존재하는지, 사용자 탈퇴 안하고 문제 작성자 맞는지 검사
        rewardService.validateBeforeCreateReward(problemId, authenticatedUserId);

        log.info("Identified problem. Receving image data");

        byte[] actualRewardImageByte, overviewRewardImageByte;

        try {
            // 이미지 정보를 가져온다. overview 없으면 만든다.
            actualRewardImageByte = actualRewardImage.getBytes();

            if (
                    overviewRewardImage == null ||
                    overviewRewardImage.isEmpty()
            ) {
                log.info("No overview image given or it's empty.");

                if (!imageInfoBroker.acceptableImage(actualRewardImageByte)) {
                    log.info("Cannot generate overview reward image, "
                             + "due to non-acceptable image data.");
                    throw new UnacceptableImageGivenException();
                }

                log.info("Generating overview reward image");
                overviewRewardImageByte = imageBlurer.blurImage(actualRewardImageByte);
                log.info("Blur image has been generated.");
            } else {
                overviewRewardImageByte = overviewRewardImage.getBytes();
            }

        } catch (IOException e) {
            String errMsg = "Failed to get image from multipart file";
            log.warn(errMsg, e);
            throw new FailedToGetImageFromRequestException(errMsg, e);
        }

        if (    // 이미지가 허용되지 않는 무언가다.
                !imageInfoBroker.acceptableImage(actualRewardImageByte) ||
                !imageInfoBroker.acceptableImage(overviewRewardImageByte)
        ) {
            throw new UnacceptableImageGivenException();
        }

        log.info("Received image data. Creating reward image entities.");

        // 보상 entity 를 생성한다.
        RewardImageInfo savedRewardImageInfo = rewardImageHandler.saveRewardImages(
                actualRewardImageByte, overviewRewardImageByte
        );

        log.info("Created reward image entities. Linking to problem reward entity.");

        Long actualRewardId = savedRewardImageInfo.actualRewardImageId();
        Long overviewRewardId = savedRewardImageInfo.overviewRewardImageId();
        String description = request.description();

        Long response;

        try {
            response = rewardService.createReward(
                    problemId, actualRewardId, overviewRewardId, description
            );

            log.info("Reward has been linked successfully.");

        } catch (Exception e) {
            // ProblemReward 랑 연관짖다 뭔가 문제가 발생했다.

            String errMsg = "Failed to link reward entities to ProblemReward entity.";
            log.warn(errMsg, e);

            log.warn("Attempting to remove saved entities...");

            // TODO : 지금 일단 rewardImageHandler#deleteRewardImages api 로 만듬
            //  observability 붙여서 실제 성능 차이 보고 이벤트 기반으로 만들수도 있음.
            try {
                RewardImageInfo result = rewardImageHandler.deleteRewardImages(
                        savedRewardImageInfo
                );
                log.warn("Saved entities has been removed: {}", result);
            } catch (Exception ee) {
                log.warn(
                        "Failed to remove entities due to ex: {} - "
                        + "must be deleted in batch process (actualId={},overviewId={})",
                        ee.getClass().getSimpleName(),
                        savedRewardImageInfo.actualRewardImageId(),
                        savedRewardImageInfo.overviewRewardImageId()
                );
            }
            throw new FailedToLinkRewardException(errMsg, e);
        }

        return ApiResponse.created(response);
    }

    // 문제 보상 설명 수정하기
    @PutMapping("/{reward-id:\\d+}/description")
    public ApiResponse<Long> updateRewardDescription(
            @PathVariable("problem-id") Long problemId,
            @PathVariable("reward-id") Long rewardId,
            @AuthenticationPrincipal Long authenticatedUserId,
            @Valid @RequestBody UpdateRewardDescriptionRequest req
    ) {
        String description = req.description();

        Long response = rewardService.updateRewardDescription(
                problemId, rewardId, authenticatedUserId, description
        );

        return ApiResponse.success(response);
    }

    // 문제 보상 삭제하기
    @DeleteMapping("/{reward-id:\\d+}")
    public ApiResponse<Long> deleteReward(
            @PathVariable("problem-id") Long problemId,
            @PathVariable("reward-id") Long rewardId,
            @AuthenticationPrincipal Long authenticatedUserId
    ) {

        // 문제 보상과 연관된 image id 를 취한다.
        // 사용자 다른 사람이거나 관련 정보 없으면 아래 메서드에서 걸러진다.
        RewardImageIds entityInfo
                = rewardService.getRewardImageIdsInfoBeforeRemoval(
                problemId, rewardId, authenticatedUserId
        );

        Long actualRewardId = entityInfo.actualRewardImageId();
        Long overviewRewardId = entityInfo.overviewRewardImageId();

        log.info("Identified reward images");

        // ProblemReward 엔티티를 삭제한다.
        Long response = rewardService.deleteReward(
                problemId, rewardId
        );

        log.info("Detached reward image");

        RewardImageInfo removalInfo = new RewardImageInfo(
                actualRewardId, overviewRewardId
        );

        try {
            // 실제 image 들을 삭제한다.
            log.info("Removing reward image...");
            // TODO : 여기도 실제 observability 붙여서 성능차이 보고 이벤트 기반으로 만들 수 있음.
            RewardImageInfo result = rewardImageHandler.deleteRewardImages(removalInfo);
            log.info("Reward images has been removed: {}", result);
        } catch (Exception e) {
            log.warn(
                    "Failed to delete reward image due to ex: {}",
                    e.getClass().getSimpleName(), e
            );
            log.warn(
                    "Must be deleted in batch process: (actualId={},overviewId={})",
                    actualRewardId, overviewRewardId
            );
        }

        return ApiResponse.success(response);
    }
}
