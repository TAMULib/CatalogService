package edu.tamu.catalog.domain.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class HoldRequest {

    private String requestId;

    private String itemId;

    private String requestType;

    private String itemTitle;

    private String statusText;

    private String pickupLocation;

    private Integer queuePosition;

    private Date expirationDate;

}
