package edu.tamu.app.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

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
    private static final String NODE_CATALOGS = "catalogs";
    private static final String NODE_TYPE = "type";
    private static final String NODE_HOST = "host";
    private static final String NODE_PORT = "port";
    private static final String NODE_APP = "app";
    private static final String NODE_PROTOCOL = "protocol";
    private static final String NODE_SID_PREFIX = "sidPrefix";
    private static final String NODE_REPOSITORY_BASE_URL = "repositoryBaseUrl";
    private static final String NODE_APIKEY = "apikey";
    private static final String NODE_AUTHENTICATION = "authentication";

    private static final String TYPE_VOYAGER = "voyager";
    private static final String TYPE_FOLIO = "folio";

    private Map<String, CatalogService> catalogServices = new HashMap<String, CatalogService>();

    @Autowired
    private ObjectMapper objectMapper;

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

            JsonNode newCatalog = catalogsJson.get(NODE_CATALOGS).get(name);
            if (newCatalog != null) {
                String type = newCatalog.get(NODE_TYPE).asText();
                String host = newCatalog.get(NODE_HOST).asText();
                String port = newCatalog.get(NODE_PORT).asText();
                String app = newCatalog.get(NODE_APP).asText();
                String protocol = newCatalog.get(NODE_PROTOCOL).asText();
                String sidPrefix = newCatalog.get(NODE_SID_PREFIX).asText();

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
        CatalogService catalogService = new FolioCatalogService();

        Map<String, String> authentication = new HashMap<String, String>();

        if (newCatalog.has(NODE_AUTHENTICATION)) {
            if (newCatalog.get(NODE_AUTHENTICATION).has(NODE_APIKEY)) {
                authentication.put(NODE_APIKEY, newCatalog.get(NODE_AUTHENTICATION).get(NODE_APIKEY).asText());
            } else {
                authentication.put(NODE_APIKEY, "");
            }

            if (newCatalog.get(NODE_AUTHENTICATION).has(NODE_REPOSITORY_BASE_URL)) {
                authentication.put(NODE_REPOSITORY_BASE_URL, newCatalog.get(NODE_AUTHENTICATION).get(NODE_REPOSITORY_BASE_URL).asText());
            } else {
                authentication.put(NODE_REPOSITORY_BASE_URL, "");
            }
        }

        catalogService = new FolioCatalogService();
        catalogService.setAuthentication(authentication);

        return catalogService;
    }

}
