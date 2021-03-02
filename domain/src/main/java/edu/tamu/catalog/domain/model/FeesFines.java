package edu.tamu.catalog.domain.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class FeesFines {

    private String id;

    private Double total;

    private Integer fineCount;

    private List<FeeFine> list;
}
