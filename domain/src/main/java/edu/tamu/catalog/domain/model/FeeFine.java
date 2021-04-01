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
public class FeeFine {

    private String fineId;

    private String itemId;

    private String instanceId;

    private Double amount;

    private String fineType;

    private Date fineDate;

    private String itemTitle;

}
