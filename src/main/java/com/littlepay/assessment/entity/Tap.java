package com.littlepay.assessment.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "tap", indexes = @Index(name = "ix_tap_unprocessed",
        columnList = "status, company_id, pan, tap_time, tap_id"))
public class Tap {

    @Id
    @Column(name = "tap_id")
    private Long id;

    @Column(name = "tap_time", nullable = false)
    private Instant tapTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "tap_type", nullable = false, length = 3)
    private TapType tapType;

    @Column(name = "stop_id", nullable = false, length = 50)
    private String stopId;

    @Column(name = "company_id", nullable = false, length = 50)
    private String companyId;

    @Column(name = "bus_id", nullable = false, length = 50)
    private String busId;

    @Column(name = "pan", nullable = false, length = 19)
    private String pan;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TapStatus status = TapStatus.UNPROCESSED;

    protected Tap() {
    }

    public Tap(Long id, Instant tapTime, TapType tapType, String stopId, String companyId, String busId,
               String pan) {
        this.id = id;
        this.tapTime = tapTime;
        this.tapType = tapType;
        this.stopId = stopId;
        this.companyId = companyId;
        this.busId = busId;
        this.pan = pan;
    }

    public boolean isOn() {
        return tapType == TapType.ON;
    }

    public boolean isOff() {
        return tapType == TapType.OFF;
    }

    public boolean isSameBusAs(Tap other) {
        return busId.equals(other.busId);
    }

    public boolean isSameStopAs(Tap other) {
        return stopId.equals(other.stopId);
    }

    public Long getId() {
        return id;
    }

    public Instant getTapTime() {
        return tapTime;
    }

    public TapType getTapType() {
        return tapType;
    }

    public String getStopId() {
        return stopId;
    }

    public String getCompanyId() {
        return companyId;
    }

    public String getBusId() {
        return busId;
    }

    public String getPan() {
        return pan;
    }

    public TapStatus getStatus() {
        return status;
    }

    public void setStatus(TapStatus status) {
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Tap tap)) {
            return false;
        }
        return Objects.equals(id, tap.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Tap{id=" + id + ", tapTime=" + tapTime + ", tapType=" + tapType + ", stopId='" + stopId
                + "', companyId='" + companyId + "', busId='" + busId + "', status=" + status + "}";
    }
}
