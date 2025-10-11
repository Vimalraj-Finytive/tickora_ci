package com.uniq.tms.tms_microservice.shared.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.HolidayDto;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class HolidayJsonLoaderUtil {

    private Map<String, Map<String, List<HolidayDto>>> holidaysData;

    @PostConstruct
    public void loadHolidays() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            mapper.findAndRegisterModules();

            InputStream is = getClass().getResourceAsStream("/data/holidays.json");
            holidaysData = mapper.readValue(is, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to load holidays.json", e);
        }
    }

    public List<HolidayDto> getHolidaysForYears(String countryCode, List<Integer> years) {
        List<HolidayDto> result = new ArrayList<>();
        if (holidaysData.containsKey(countryCode)) {
            Map<String, List<HolidayDto>> yearlyHolidays = holidaysData.get(countryCode);
            for (Integer year : years) {
                if (yearlyHolidays.containsKey(String.valueOf(year))) {
                    result.addAll(yearlyHolidays.get(String.valueOf(year)));
                }
            }
        }
        return result;
    }
}
