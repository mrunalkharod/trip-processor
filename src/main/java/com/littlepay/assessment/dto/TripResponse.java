package com.littlepay.assessment.dto;

import com.littlepay.assessment.entity.TripStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "A trip")
public record TripResponse(

        @Schema(description = "Tap ON time ")
        Instant started,

        @Schema(description = "Tap OFF time. Null for INCOMPLETE trips", nullable = true)
        Instant finished,

        @Schema(example = "300", nullable = true)
        Long durationSecs,

        @Schema(example = "Stop1")
        String fromStopId,

        @Schema(example = "Stop2", nullable = true)
        String toStopId,

        @Schema(description = "Amount charged, as a string to avoid rounding", example = "3.25")
        String chargeAmount,

        @Schema(example = "AUD")
        String currency,

        @Schema(example = "Bus37")
        String busId,

        TripStatus status
) {
}
