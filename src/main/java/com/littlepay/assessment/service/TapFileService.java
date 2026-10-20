package com.littlepay.assessment.service;


import com.littlepay.assessment.dto.ImportResponse;
import com.littlepay.assessment.dto.TapFileResponse;

import java.io.IOException;
import java.io.InputStream;

public interface TapFileService {

    TapFileResponse upload(String fileName, InputStream content) throws IOException;

    TapFileResponse getTapFile(Long tapFileId);

    ImportResponse importTaps();
}
