package com.littlepay.assessment.config;


import com.littlepay.assessment.entity.Bus;
import com.littlepay.assessment.repository.BusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final BusRepository busRepository;

    public DataSeeder(BusRepository busRepository) {
        this.busRepository = busRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedBus("Company1", "Bus36", stops("Stop1", "0.00", "Stop2", "1.80", "Stop3", "5.85"));
        seedBus("Company1", "Bus37", stops("Stop1", "0.00", "Stop2", "1.80", "Stop3", "5.85"));
        seedBus("Company1", "Bus50", stops("Stop1", "0.00", "Stop3", "3.35", "Stop4", "4.55"));
        seedBus("Company1", "Bus51", stops("Stop1", "0.00", "Stop3", "3.35", "Stop4", "4.55"));
        log.info("Seeded buses and their stops");
    }

    private void seedBus(String companyId, String busId, Map<String, BigDecimal> stops) {
        Bus bus = busRepository.findByCompanyIdAndBusId(companyId, busId)
                .orElseGet(() -> new Bus(companyId, busId));
        stops.forEach(bus::putStop);
        busRepository.save(bus);  // saving a bus saves its stops too (cascade)
    }

    private static Map<String, BigDecimal> stops(String... stopIdsAndValues) {
        Map<String, BigDecimal> stops = new LinkedHashMap<>();
        for (int i = 0; i < stopIdsAndValues.length; i += 2) {
            stops.put(stopIdsAndValues[i], new BigDecimal(stopIdsAndValues[i + 1]));
        }
        return stops;
    }
}
