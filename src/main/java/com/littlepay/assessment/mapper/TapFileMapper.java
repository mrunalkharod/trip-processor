package com.littlepay.assessment.mapper;


import com.littlepay.assessment.dto.TapFileResponse;
import com.littlepay.assessment.entity.TapFile;
import org.springframework.stereotype.Component;

@Component
public class TapFileMapper {

    public TapFileResponse toResponse(TapFile tapFile) {
        return new TapFileResponse(
                tapFile.getId(),
                tapFile.getFileName(),
                tapFile.getStatus(),
                tapFile.getStoredTaps(),
                tapFile.getBadRows(),
                tapFile.getErrorMessage(),
                tapFile.getUploadedAt(),
                tapFile.getProcessedAt());
    }
}
