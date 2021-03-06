package edu.tamu.catalog.service;

import java.util.List;

import edu.tamu.catalog.domain.model.FeeFine;
import edu.tamu.catalog.domain.model.HoldRequest;
import edu.tamu.catalog.domain.model.HoldingsRecord;
import edu.tamu.catalog.domain.model.LoanItem;

/**
 * An interface describing Catalog Service API connectors
 *
 * @author Jason Savell <jsavell@library.tamu.edu>
 * @author James Creel <jcreel@library.tamu.edu>
 *
 */
public interface CatalogService {

    String getName();

    List<HoldingsRecord> getHoldingsByBibId(String bibId);

    HoldingsRecord getHolding(String id, String holdingId);

    List<FeeFine> getFeesFines(String uin) throws Exception;

    List<LoanItem> getLoanItems(String uin) throws Exception;

    List<HoldRequest> getHoldRequests(String uin) throws Exception;

    void cancelHoldRequest(String uin, String requestId) throws Exception;

    LoanItem renewItem(String uin, String itemId) throws Exception;

    Boolean getBlockStatus(String uin) throws Exception;

}
