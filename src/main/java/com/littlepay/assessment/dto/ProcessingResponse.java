package com.littlepay.assessment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of a tap processing run")
public record ProcessingResponse(

        @Schema(description = "UNPROCESSED taps read in this run", example = "6")
        int tapsProcessed,

        @Schema(description = "Trips created", example = "3")
        int tripsCreated,

        @Schema(description = "Orphan tap OFFs", example = "1")
        int orphanTapsLogged
) {
}