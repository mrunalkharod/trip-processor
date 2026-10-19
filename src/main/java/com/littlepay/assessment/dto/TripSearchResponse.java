package com.littlepay.assessment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "A card's trips for one day")
public record TripSearchResponse(

        @Schema(example = "Company1")
        String companyId,

        @Schema(description = "Masked card number", example = "550000******5559")
        String pan,

        @Schema(description = "Date the trips started.", example = "2023-01-22")
        LocalDate date,

        List<TripResponse> trips
) {
}
