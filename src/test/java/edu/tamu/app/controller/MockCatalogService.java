package edu.tamu.app.controller;

import java.util.List;

import edu.tamu.app.model.CatalogHolding;
import edu.tamu.app.service.CatalogService;

public class MockCatalogService implements CatalogService {
    // TODO: point to the localhost MockVoyagerServiceController endpoints

    @Override
    public List<CatalogHolding> getHoldingsByBibId(String bibId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getHost() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPort() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getApp() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getProtocol() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setName(String name) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setType(String type) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setHost(String host) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setPort(String port) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setApp(String app) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setProtocol(String protocol) {
        // TODO Auto-generated method stub

    }

}
