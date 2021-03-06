package edu.tamu.catalog.domain.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class HoldRequest {

    private String requestId;

    private String itemId;

    private String instanceId;

    private String requestType;

    private String itemTitle;

    private String statusText;

    private String pickupServicePoint;

    private Integer queuePosition;

    private Date requestDate;

    private Date expirationDate;

}
