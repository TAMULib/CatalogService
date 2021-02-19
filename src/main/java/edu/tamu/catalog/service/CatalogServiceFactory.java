package edu.tamu.catalog.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Registers and manages the available CatalogServices and provides them as
 * needed to the rest of the application
 *
 * @author Jason Savell <jsavell@library.tamu.edu>
 * @author James Creel <jcreel@library.tamu.edu>
 *
 */

@Service
public class CatalogServiceFactory {
    public static final String FIELD_CATALOGS = "catalogs";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_HOST = "host";
    public static final String FIELD_PORT = "port";
    public static final String FIELD_APP = "app";
    public static final String FIELD_PROTOCOL = "protocol";
    public static final String FIELD_SID_PREFIX = "sidPrefix";
    public static final String FIELD_REPOSITORY_BASE_URL = "repositoryBaseUrl";
    public static final String FIELD_APIKEY = "apiKey";
    public static final String FIELD_AUTHENTICATION = "authentication";
    public static final String FIELD_TENANT = "tenant";

    public static final String TYPE_VOYAGER = "voyager";
    public static final String TYPE_FOLIO = "folio";

    private Map<String, CatalogService> catalogServices = new HashMap<String, CatalogService>();

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${catalogs.file.location:''}")
    private String catalogsFile;

    public CatalogService getOrCreateCatalogService(String name) {
        if (catalogServices.containsKey(name)) {
            return catalogServices.get(name);
        } else {
            // didn't find it? Then parse the JSON and construct it and save it
            CatalogService catalogService = buildFromName(name);
            catalogServices.put(name, catalogService);
            return catalogService;
        }
    }

    private CatalogService buildFromName(String name) {
        CatalogService catalogService = null;

        if (!catalogsFile.equals("")) {
            ClassPathResource catalogsRaw = new ClassPathResource(catalogsFile);
            JsonNode catalogsJson = null;
            try {
                catalogsJson = objectMapper.readTree(new FileInputStream(catalogsRaw.getFile()));
            } catch (JsonProcessingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            JsonNode newCatalog = catalogsJson.get(FIELD_CATALOGS).get(name);
            if (newCatalog != null) {
                String type = newCatalog.get(FIELD_TYPE).asText();
                String host = newCatalog.get(FIELD_HOST).asText();
                String port = newCatalog.get(FIELD_PORT).asText();
                String app = newCatalog.get(FIELD_APP).asText();
                String protocol = newCatalog.get(FIELD_PROTOCOL).asText();
                String sidPrefix = newCatalog.get(FIELD_SID_PREFIX).asText();

                switch (type) {
                case TYPE_VOYAGER:
                    catalogService = new VoyagerCatalogService();
                    catalogService.setType(type);
                    break;
                case TYPE_FOLIO:
                    catalogService = buildFolio(newCatalog);
                    catalogService.setType(type);
                    break;
                }

                catalogService.setHost(host);
                catalogService.setPort(port);
                catalogService.setApp(app);
                catalogService.setProtocol(protocol);
                catalogService.setSidPrefix(sidPrefix);
            }
        }

        return catalogService;
    }

    private CatalogService buildFolio(JsonNode newCatalog) {
        CatalogService catalogService = new FolioCatalogService(restTemplate);

        Map<String, String> authentication = new HashMap<String, String>();

        if (newCatalog.has(FIELD_AUTHENTICATION)) {
            JsonNode fieldAuthentication = newCatalog.get(FIELD_AUTHENTICATION);

            if (fieldAuthentication.has(FIELD_APIKEY)) {
                authentication.put(FIELD_APIKEY, fieldAuthentication.get(FIELD_APIKEY).asText());
            } else {
                authentication.put(FIELD_APIKEY, "");
            }

            if (fieldAuthentication.has(FIELD_REPOSITORY_BASE_URL)) {
                authentication.put(FIELD_REPOSITORY_BASE_URL, fieldAuthentication.get(FIELD_REPOSITORY_BASE_URL).asText());
            } else {
                authentication.put(FIELD_REPOSITORY_BASE_URL, "");
            }

            if (fieldAuthentication.has(FIELD_TENANT)) {
                authentication.put(FIELD_TENANT, fieldAuthentication.get(FIELD_TENANT).asText());
            } else {
                authentication.put(FIELD_TENANT, "");
            }
        }

        catalogService.setAuthentication(authentication);
        return catalogService;
    }

}
