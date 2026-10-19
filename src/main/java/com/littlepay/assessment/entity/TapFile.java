package com.littlepay.assessment.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "tap_file")
public class TapFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "stored_path", nullable = false, length = 1000)
    private String storedPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TapFileStatus status = TapFileStatus.UPLOADED;

    /** Taps inserted from this file. Taps already stored (e.g. from an earlier upload) are not counted. */
    @Column(name = "stored_taps", nullable = false)
    private int storedTaps;

    /** Rows that could not be parsed. Each one is on the dead-letter stream. */
    @Column(name = "bad_rows", nullable = false)
    private int badRows;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    protected TapFile() {
    }

    public TapFile(String fileName, String storedPath, Instant uploadedAt) {
        this.fileName = fileName;
        this.storedPath = storedPath;
        this.uploadedAt = uploadedAt;
    }

    public void markProcessing() {
        this.status = TapFileStatus.PROCESSING;
        this.storedTaps = 0;
        this.badRows = 0;
        this.errorMessage = null;
    }

    public void markProcessed(int storedTaps, int badRows, Instant processedAt) {
        this.status = TapFileStatus.PROCESSED;
        this.storedTaps = storedTaps;
        this.badRows = badRows;
        this.processedAt = processedAt;
    }

    public void markFailed(String errorMessage, Instant processedAt) {
        this.status = TapFileStatus.FAILED;
        this.errorMessage = errorMessage == null || errorMessage.length() <= 1000
                ? errorMessage : errorMessage.substring(0, 1000);
        this.processedAt = processedAt;
    }

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getStoredPath() {
        return storedPath;
    }

    public TapFileStatus getStatus() {
        return status;
    }

    public int getStoredTaps() {
        return storedTaps;
    }

    public int getBadRows() {
        return badRows;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TapFile tapFile)) {
            return false;
        }
        return id != null && Objects.equals(id, tapFile.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "TapFile{id=" + id + ", fileName='" + fileName + "', status=" + status
                + ", storedTaps=" + storedTaps + ", badRows=" + badRows + "}";
    }
}
