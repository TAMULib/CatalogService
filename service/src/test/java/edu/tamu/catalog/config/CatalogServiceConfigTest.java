package edu.tamu.catalog.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.tamu.catalog.properties.FolioProperties;
import edu.tamu.catalog.properties.VoyagerProperties;
import edu.tamu.catalog.service.CatalogService;
import edu.tamu.catalog.service.FolioCatalogService;
import edu.tamu.catalog.service.VoyagerCatalogService;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CatalogServiceConfigTest {

    @Autowired
    private List<CatalogService> catalogServices;

    @Autowired
    @Qualifier("evans")
    private VoyagerCatalogService evansCatalogService;

    @Autowired
    @Qualifier("msl")
    private VoyagerCatalogService mslCatalogService;

    @Autowired
    @Qualifier("folio")
    private FolioCatalogService folioCatalogService;

    @Test
    public void testExpectedNumberOfCatalogServices() {
        assertEquals(3, catalogServices.size());
        catalogServices.forEach(Assert::assertNotNull);
    }

    @Test
    public void testAutowiredEvansVoyagerCatalogService() throws NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        assertNotNull(evansCatalogService);
        assertEquals("evans", evansCatalogService.getName());
        Field field = VoyagerCatalogService.class.getDeclaredField("properties");
        field.setAccessible(true);
        VoyagerProperties properties = (VoyagerProperties) field.get(evansCatalogService);
        assertEquals("evans", properties.getName());
        assertEquals("voyager", properties.getType());
        assertEquals("http://localhost:7014/vxws", properties.getBaseUrl());
        assertEquals("libcat", properties.getSidPrefix());
    }

    @Test
    public void testAutowiredMslVoyagerCatalogService() throws NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        assertNotNull(mslCatalogService);
        assertEquals("msl", mslCatalogService.getName());
        Field field = VoyagerCatalogService.class.getDeclaredField("properties");
        field.setAccessible(true);
        VoyagerProperties properties = (VoyagerProperties) field.get(mslCatalogService);
        assertEquals("msl", properties.getName());
        assertEquals("voyager", properties.getType());
        assertEquals("http://localhost:7414/vxws", properties.getBaseUrl());
        assertEquals("chiron", properties.getSidPrefix());
    }

    @Test
    public void testAutowiredFolioVoyagerCatalogService() throws NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        assertNotNull(folioCatalogService);
        assertEquals("folio", folioCatalogService.getName());
        Field field = FolioCatalogService.class.getDeclaredField("properties");
        field.setAccessible(true);
        FolioProperties properties = (FolioProperties) field.get(folioCatalogService);
        assertEquals("folio", properties.getName());
        assertEquals("folio", properties.getType());
        assertEquals("http://localhost:9130", properties.getBaseOkapiUrl());
        assertEquals("http://localhost:8080", properties.getBaseEdgeUrl());
        assertEquals("diku", properties.getTenant());
        assertNotNull(properties.getCredentials());
        assertEquals("diku_admin", properties.getCredentials().getUsername());
        assertEquals("admin", properties.getCredentials().getPassword());
        assertEquals("mock_api_key", properties.getEdgeApiKey());
        assertEquals("localhost", properties.getRepositoryBaseUrl());
    }

}
