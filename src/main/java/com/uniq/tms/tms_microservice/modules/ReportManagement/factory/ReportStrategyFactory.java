package com.uniq.tms.tms_microservice.modules.ReportManagement.factory;

import com.uniq.tms.tms_microservice.modules.ReportManagement.enums.ReportType;
import com.uniq.tms.tms_microservice.modules.ReportManagement.strategy.ReportStrategy;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ReportStrategyFactory {

    private final Map<ReportType, ReportStrategy> strategyMap;

    public ReportStrategyFactory(List<ReportStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        ReportStrategy::getType,
                        Function.identity()
                ));
    }

    public ReportStrategy get(ReportType type) {
        ReportStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new IllegalStateException(
                    "No ReportStrategy found for type: " + type
            );
        }
        return strategy;
    }
}


