package edu.tamu.catalog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An implementation of the edge-patron cancellation request body json.
 *
 * Notes:
 *   edge-patron uses "holdId" instead of "requestId" for the cancellation request body json.
 *   edge-patron uses a single 'l' for 'canceledByUserId' and 'canceledDate'.
 *   edge-patron uses a double 'l' for 'cancellationReasonId' and 'cancellationAdditionalInformation'.
 *
 * @see https://github.com/folio-org/edge-patron/blob/master/src/main/java/org/folio/edge/patron/model/HoldCancellation.java
 * @see https://github.com/folio-org/edge-patron/blob/master/ramls/examples/hold-cancellation.json
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FolioHoldCancellation {

    private String holdId;

    private String cancellationReasonId;

    private String canceledByUserId;

    private String cancellationAdditionalInformation;

    private String canceledDate;

}
