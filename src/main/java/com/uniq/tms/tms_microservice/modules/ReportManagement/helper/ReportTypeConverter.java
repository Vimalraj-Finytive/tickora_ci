package com.uniq.tms.tms_microservice.modules.ReportManagement.helper;

import com.uniq.tms.tms_microservice.modules.ReportManagement.enums.ReportType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ReportTypeConverter
        implements Converter<String, ReportType> {

    @Override
    public ReportType convert(String source) {
        return ReportType.from(source);
    }
}
