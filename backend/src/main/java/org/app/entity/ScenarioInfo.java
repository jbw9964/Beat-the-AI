package org.app.entity;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class ScenarioInfo implements Comparable<ScenarioInfo> {

    @Min(value = 0, message = "시나리오 순서는 0 보다 크거나 같은 정수여야 합니다.")
    private int scenarioOrder;

    @NotBlank(message = "시나리오 정보는 반드시 제공되어야 합니다.")
    private String scenarioContent;

    private String answerContent;

    public ScenarioInfo(int scenarioOrder, @NonNull String scenarioContent) {
        if (scenarioOrder < 0) {
            throw new IllegalArgumentException("scenarioOrder should be greater or equal to 0");
        }

        if (scenarioContent.isEmpty()) {
            throw new IllegalArgumentException("scenarioContent cannot be empty");
        }

        this.scenarioOrder = scenarioOrder;
        this.scenarioContent = scenarioContent;
    }

    public ScenarioInfo(
            int scenarioOrder, String scenarioContent, String answerContent
    ) {
        this(scenarioOrder, scenarioContent);
        this.answerContent = answerContent;
    }

    @Override
    public int compareTo(ScenarioInfo o) {
        return Integer.compare(this.scenarioOrder, o.scenarioOrder);
    }
}
