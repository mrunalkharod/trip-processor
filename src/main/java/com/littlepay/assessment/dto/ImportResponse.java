package com.littlepay.assessment.dto;


public record ImportResponse(int filesImported, int filesFailed, int tapsStored, int badRows) {
}
