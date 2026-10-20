package com.littlepay.assessment.controller;


import com.littlepay.assessment.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Trips", description = "Download trips.csv (USER or ADMIN). PANs are masked.")
@SecurityRequirement(name = "basicAuth")
public class TripResource {

    private static final MediaType TEXT_CSV = MediaType.parseMediaType("text/csv");

    private final TripService tripService;

    public TripResource(TripService tripService) {
        this.tripService = tripService;
    }

    @Operation(summary = "Download trips.csv for a day.",
            description = "Download trips.csv for a day.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "trips.csv"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid date")
    })
    @GetMapping("/trips.csv")
    public ResponseEntity<String> exportTrips(
            @Parameter(description = "UTC date", example = "2023-01-22")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok()
                .contentType(TEXT_CSV)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"trips-" + date + ".csv\"")
                .body(tripService.exportTripsCsv(date));
    }
}