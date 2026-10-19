package com.littlepay.assessment.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;


@Entity
@Table(name = "trip", indexes = {
        @Index(name = "ix_trip_card", columnList = "company_id, pan, started"),
        @Index(name = "ix_trip_started", columnList = "started")})
public class Trip {

    private static final BigDecimal NO_CHARGE = new BigDecimal("0.00");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "started", nullable = false)
    private Instant started;

    @Column(name = "finished")
    private Instant finished;

    @Column(name = "duration_secs")
    private Long durationSecs;

    @Column(name = "from_stop_id", nullable = false, length = 50)
    private String fromStopId;

    @Column(name = "to_stop_id", length = 50)
    private String toStopId;

    @Column(name = "charge_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal chargeAmount;

    @Column(name = "company_id", nullable = false, length = 50)
    private String companyId;

    @Column(name = "bus_id", nullable = false, length = 50)
    private String busId;

    @Column(name = "pan", nullable = false, length = 19)
    private String pan;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TripStatus status;

    /** Unique, so a tap can never be charged twice. */
    @Column(name = "on_tap_id", nullable = false, unique = true)
    private Long onTapId;

    @Column(name = "off_tap_id", unique = true)
    private Long offTapId;

    protected Trip() {
    }

    private Trip(Tap on, Tap off, BigDecimal chargeAmount, TripStatus status) {
        this.started = on.getTapTime();
        this.fromStopId = on.getStopId();
        this.companyId = on.getCompanyId();
        this.busId = on.getBusId();
        this.pan = on.getPan();
        this.onTapId = on.getId();
        this.chargeAmount = chargeAmount;
        this.status = status;
        if (off != null) {
            this.finished = off.getTapTime();
            this.durationSecs = Duration.between(on.getTapTime(), off.getTapTime()).toSeconds();
            this.toStopId = off.getStopId();
            this.offTapId = off.getId();
        }
    }

    public static Trip completed(Tap on, Tap off, BigDecimal fare) {
        return new Trip(on, off, fare, TripStatus.COMPLETED);
    }

    public static Trip cancelled(Tap on, Tap off) {
        return new Trip(on, off, NO_CHARGE, TripStatus.CANCELLED);
    }

    public static Trip incomplete(Tap on, BigDecimal maxFare) {
        return new Trip(on, null, maxFare, TripStatus.INCOMPLETE);
    }

    public Long getId() {
        return id;
    }

    public Instant getStarted() {
        return started;
    }

    public Instant getFinished() {
        return finished;
    }

    public Long getDurationSecs() {
        return durationSecs;
    }

    public String getFromStopId() {
        return fromStopId;
    }

    public String getToStopId() {
        return toStopId;
    }

    public BigDecimal getChargeAmount() {
        return chargeAmount;
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

    public TripStatus getStatus() {
        return status;
    }

    public Long getOnTapId() {
        return onTapId;
    }

    public Long getOffTapId() {
        return offTapId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Trip trip)) {
            return false;
        }
        return Objects.equals(onTapId, trip.onTapId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(onTapId);
    }

    @Override
    public String toString() {
        return "Trip{onTapId=" + onTapId + ", offTapId=" + offTapId + ", " + fromStopId + " -> " + toStopId
                + ", chargeAmount=" + chargeAmount + ", status=" + status + "}";
    }
}
