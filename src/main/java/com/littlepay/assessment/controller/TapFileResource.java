package com.littlepay.assessment.controller;


import com.littlepay.assessment.dto.TapFileResponse;
import com.littlepay.assessment.exception.InvalidTapFileException;
import com.littlepay.assessment.service.TapFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/v1/tap-files")
@Tag(name = "Tap files", description = "Upload taps.csv files and check their status.")
@SecurityRequirement(name = "basicAuth")
public class TapFileResource {

    private final TapFileService tapFileService;

    public TapFileResource(TapFileService tapFileService) {
        this.tapFileService = tapFileService;
    }

    @Operation(summary = "Upload a taps.csv",
            description = "Saves the file as UPLOADED. Run the import-taps command to insert the taps to DB. ")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "File saved, waiting for import-taps"),
            @ApiResponse(responseCode = "400", description = "The file is empty")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TapFileResponse> upload(
            @Parameter(description = "taps.csv file") @RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new InvalidTapFileException("The file is empty");
        }
        try (InputStream content = file.getInputStream()) {
            return ResponseEntity.accepted().body(tapFileService.upload(file.getOriginalFilename(), content));
        }
    }

    @Operation(summary = "Get an uploaded file's status",
            description = "UPLOADED, PROCESSING, PROCESSED or FAILED, with the number of taps stored and bad rows.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File found"),
            @ApiResponse(responseCode = "404", description = "No file with this ID")
    })
    @GetMapping("/{tapFileId}")
    public TapFileResponse getTapFile(@PathVariable Long tapFileId) {
        return tapFileService.getTapFile(tapFileId);
    }
}
