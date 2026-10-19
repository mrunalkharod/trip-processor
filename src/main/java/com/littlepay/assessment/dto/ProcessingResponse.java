package com.littlepay.assessment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of a tap processing run")
public record ProcessingResponse(

        @Schema(description = "UNPROCESSED taps read in this run", example = "6")
        int tapsRead,

        @Schema(description = "Taps marked PROCESSED", example = "6")
        int tapsProcessed,

        @Schema(description = "Unmatched taps held for the next run because their partner may still arrive", example = "0")
        int tapsHeld,

        @Schema(description = "Trips created", example = "3")
        int tripsCreated,

        @Schema(description = "Orphan tap OFFs logged (not charged, not stored as trips)", example = "1")
        int orphanTapsLogged
) {
}
