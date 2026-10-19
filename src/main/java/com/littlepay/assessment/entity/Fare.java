package com.littlepay.assessment.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "fare", uniqueConstraints = @UniqueConstraint(columnNames = {"bus_ref", "stop_id"}))
public class Fare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bus_ref", nullable = false, foreignKey = @ForeignKey(name = "fk_fare_bus"))
    private Bus bus;

    @Column(name = "stop_id", nullable = false, length = 50)
    private String stopId;

    @Column(name = "distance_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal distanceValue;

    protected Fare() {
    }

    /** Use {@link Bus#putStop} to add a stop to a bus. */
    Fare(Bus bus, String stopId, BigDecimal distanceValue) {
        this.bus = bus;
        this.stopId = stopId;
        this.distanceValue = distanceValue;
    }

    public Long getId() {
        return id;
    }

    public Bus getBus() {
        return bus;
    }

    public String getStopId() {
        return stopId;
    }

    public BigDecimal getDistanceValue() {
        return distanceValue;
    }

    void setDistanceValue(BigDecimal distanceValue) {
        this.distanceValue = distanceValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Fare that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Fare{" + stopId + " = " + distanceValue + "}";
    }
}
