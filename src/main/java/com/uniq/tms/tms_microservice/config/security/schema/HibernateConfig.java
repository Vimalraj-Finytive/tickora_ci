package com.uniq.tms.tms_microservice.config.security.schema;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Configuration;
import java.util.Map;

@Configuration
public class HibernateConfig implements HibernatePropertiesCustomizer {

    private final SchemaMultiTenantConnectionProvider connectionProvider;
    private final SchemaCurrentTenantIdentifierResolver tenantResolver;

    public HibernateConfig(
            SchemaMultiTenantConnectionProvider connectionProvider,
            SchemaCurrentTenantIdentifierResolver tenantResolver) {
        this.connectionProvider = connectionProvider;
        this.tenantResolver = tenantResolver;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put("hibernate.multiTenancy", "SCHEMA");
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, connectionProvider);
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantResolver);
    }
}
