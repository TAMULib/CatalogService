package edu.tamu.catalog.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;

import edu.tamu.catalog.properties.CatalogServiceProperties;
import edu.tamu.catalog.properties.FolioProperties;
import edu.tamu.catalog.properties.VoyagerProperties;
import edu.tamu.catalog.service.CatalogService;
import edu.tamu.catalog.service.FolioCatalogService;
import edu.tamu.catalog.service.VoyagerCatalogService;

@Configuration
public class CatalogServiceConfig {

    private static final String TYPE_FIELD = "type";

    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public List<CatalogService> createCatalogServices() throws IOException {
        List<CatalogService> catalogServices = new ArrayList<>();
        for (Resource resource : loadResources("classpath:/catalogs/*.json")) {
            JsonNode propertiesNode = objectMapper.readTree(resource.getInputStream());
            JsonNode typeNode = propertiesNode.get(TYPE_FIELD);
            if (typeNode.isValueNode()) {
                CatalogServiceFactory configServiceFactory = CatalogServiceFactory.valueOf(typeNode.asText().toUpperCase());
                CatalogServiceProperties properties = (CatalogServiceProperties) objectMapper.treeToValue(propertiesNode, configServiceFactory.getPropertiesType());
                CatalogService catalogService = configServiceFactory.build(properties);
                beanFactory.initializeBean(catalogService, catalogService.getName());
                beanFactory.autowireBean(catalogService);
                beanFactory.registerSingleton(catalogService.getName(), catalogService);
                catalogServices.add(catalogService);
            }
        }

        return catalogServices;
    }

    private List<Resource> loadResources(String pattern) throws IOException {
        return Arrays.asList(ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(pattern));
    }

    private interface CatalogServiceType {
        public CatalogService build(CatalogServiceProperties properties);

        public Class<?> getPropertiesType();
    }

    private enum CatalogServiceFactory implements CatalogServiceType {

        VOYAGER {
            @Override
            public CatalogService build(CatalogServiceProperties properties) {
                return new VoyagerCatalogService(properties);
            }

            @Override
            public Class<?> getPropertiesType() {
                return VoyagerProperties.class;
            }
        },

        FOLIO {
            @Override
            public CatalogService build(CatalogServiceProperties properties) {
                return new FolioCatalogService(properties);
            }

            @Override
            public Class<?> getPropertiesType() {
                return FolioProperties.class;
            }
        };

    }

}
