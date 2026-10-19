package com.littlepay.assessment.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "bus", uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "bus_id"}))
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false, length = 50)
    private String companyId;

    @Column(name = "bus_id", nullable = false, length = 50)
    private String busId;

    @OneToMany(mappedBy = "bus", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Fare> fares = new ArrayList<>();

    protected Bus() {
    }

    public Bus(String companyId, String busId) {
        this.companyId = companyId;
        this.busId = busId;
    }

    public Fare putStop(String stopId, BigDecimal distanceValue) {
        for (Fare fare : fares) {
            if (fare.getStopId().equals(stopId)) {
                fare.setDistanceValue(distanceValue);
                return fare;
            }
        }
        Fare fare = new Fare(this, stopId, distanceValue);
        fares.add(fare);
        return fare;
    }

    public Long getId() {
        return id;
    }

    public String getCompanyId() {
        return companyId;
    }

    public String getBusId() {
        return busId;
    }

    public List<Fare> getFares() {
        return Collections.unmodifiableList(fares);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Bus bus)) {
            return false;
        }
        return Objects.equals(getCompanyId(), bus.getCompanyId()) && Objects.equals(getBusId(), bus.getBusId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCompanyId(), getBusId());
    }

    @Override
    public String toString() {
        return "Bus{companyId='" + companyId + "', busId='" + busId + "'}";
    }
}
