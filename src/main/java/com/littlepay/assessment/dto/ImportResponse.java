package com.littlepay.assessment.dto;


/**
 *
 * @param filesImported
 * @param filesFailed
 * @param tapsStored
 * @param badRows
 */
public record ImportResponse(int filesImported, int filesFailed, int tapsStored, int badRows) {
}
