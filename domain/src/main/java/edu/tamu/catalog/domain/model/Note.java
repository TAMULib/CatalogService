package edu.tamu.catalog.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Note {

    private String note;

    private Boolean isStaffOnly;

    private String noteTypeId;
    
}
