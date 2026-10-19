package com.littlepay.assessment.dto;

import com.littlepay.assessment.util.PanMasker;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

@Schema(description = "Find one card's trips for one day")
public record TripSearchRequest(

        @Schema(description = "Card number", example = "5500005555555559")
        @NotBlank(message = "pan must not be blank")
        @Pattern(regexp = "\\d{12,19}", message = "pan must be 12-19 digits")
        String pan,

        @Schema(description = "Date the trips started, in UTC like the tap times", example = "2023-01-22")
        @NotNull(message = "date must not be null")
        @PastOrPresent(message = "date must not be in the future")
        LocalDate date
) {

    @Override
    public String toString() {
        return "TripSearchRequest[pan=" + PanMasker.mask(pan) + ", date=" + date + "]";
    }
}
