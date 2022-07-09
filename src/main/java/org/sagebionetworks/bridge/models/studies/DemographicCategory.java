package org.sagebionetworks.bridge.models.studies;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "DemographicsCategories")
public class DemographicCategory {
    @Id
    @GeneratedValue
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "demographicsCategoriesId")
    })
    private Long id;
    @JsonIgnore
    private String appId;
    @JsonIgnore
    private String studyId;
    private String categoryName;
    private DemographicCategoryType categoryType;
}
