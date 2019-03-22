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

            JsonNode newCatalog = catalogsJson.get("catalogs").get(name);
            if (newCatalog != null) {
                String host = newCatalog.get("host").asText();
                String port = newCatalog.get("port").asText();
                String app = newCatalog.get("app").asText();
                String protocol = newCatalog.get("protocol").asText();

                switch (newCatalog.get("type").asText()) {
                case "voyager":
                    catalogService = new VoyagerCatalogService();
                    catalogService.setHost(host);
                    catalogService.setPort(port);
                    catalogService.setApp(app);
                    catalogService.setProtocol(protocol);
                    catalogService.setType("voyager");
                    break;
                }
            }
        }
        return catalogService;
    }
}
