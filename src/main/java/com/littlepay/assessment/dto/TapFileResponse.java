package com.littlepay.assessment.dto;

import com.littlepay.assessment.entity.TapFileStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "An uploaded taps.csv and its progress")
public record TapFileResponse(

        @Schema(description = "Unique identifier of the upload", example = "1")
        Long tapFileId,

        @Schema(example = "taps.csv")
        String fileName,

        @Schema(description = "UPLOADED, PROCESSING, PROCESSED or FAILED")
        TapFileStatus status,

        @Schema(description = "Taps stored from the file", example = "6")
        int storedTaps,

        @Schema(description = "Rows that could not be parsed", example = "0")
        int badRows,

        @Schema(description = "Error message. It will be empty for successful run", nullable = true)
        String errorMessage,

        @Schema(description = "File upload time")
        Instant uploadedAt,

        @Schema(description = "File processed time", nullable = true)
        Instant processedAt
) {
}
