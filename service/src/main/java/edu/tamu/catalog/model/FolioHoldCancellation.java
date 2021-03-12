package edu.tamu.catalog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
