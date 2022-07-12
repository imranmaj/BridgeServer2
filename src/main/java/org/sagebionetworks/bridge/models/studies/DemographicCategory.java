package org.sagebionetworks.bridge.models.studies;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "DemographicsCategories")
public class DemographicCategory {
    @Id
    private String guid;
    @JsonIgnore
    private String appId;
    @JsonIgnore
    private String studyId;
    private String categoryName;
    @Enumerated(EnumType.STRING)
    private DemographicCategoryType categoryType;

    @OneToMany
    List<DemographicCategoryInt> demographicCategoriesInts;
    @OneToMany
    List<DemographicCategoryFloat> demographicCategoriesFloats;
    @OneToMany
    List<DemographicCategoryEnum> demographicCategoriesEnums;

    public DemographicCategory(String guid, String appId, String studyId, String categoryName,
            DemographicCategoryType categoryType) {
        this.guid = guid;
        this.appId = appId;
        this.studyId = studyId;
        this.categoryName = categoryName;
        this.categoryType = categoryType;
    }

    @Entity
    @Table(name = "DemographicCategoriesInts")
    public static class DemographicCategoryInt {
        private String demographicCategoryId;
        private String units;
        private long min;
        private long max;

        @ManyToOne
        @JoinColumn(name = "demographicCategoryId")
        DemographicCategory demographicCategory;

        public DemographicCategoryInt(String demographicCategoryId, String units, long min, long max) {
            this.demographicCategoryId = demographicCategoryId;
            this.units = units;
            this.min = min;
            this.max = max;
        }

        public String getDemographicCategoryId() {
            return demographicCategoryId;
        }

        public void setDemographicCategoryId(String demographicCategoryId) {
            this.demographicCategoryId = demographicCategoryId;
        }

        public String getUnits() {
            return units;
        }

        public void setUnits(String units) {
            this.units = units;
        }

        public long getMin() {
            return min;
        }

        public void setMin(long min) {
            this.min = min;
        }

        public long getMax() {
            return max;
        }

        public void setMax(long max) {
            this.max = max;
        }
    }

    @Entity
    @Table(name = "DemographicCategoriesFloats")
    public static class DemographicCategoryFloat {
        private String demographicCategoryId;
        private String units;
        private double min;
        private double max;

        @ManyToOne
        @JoinColumn(name = "demographicCategoryId")
        DemographicCategory demographicCategory;

        public DemographicCategoryFloat(String demographicCategoryId, String units, double min, double max) {
            this.demographicCategoryId = demographicCategoryId;
            this.units = units;
            this.min = min;
            this.max = max;
        }

        public String getDemographicCategoryId() {
            return demographicCategoryId;
        }

        public void setDemographicCategoryId(String demographicCategoryId) {
            this.demographicCategoryId = demographicCategoryId;
        }

        public String getUnits() {
            return units;
        }

        public void setUnits(String units) {
            this.units = units;
        }

        public double getMin() {
            return min;
        }

        public void setMin(double min) {
            this.min = min;
        }

        public double getMax() {
            return max;
        }

        public void setMax(double max) {
            this.max = max;
        }
    }

    @Entity
    @Table(name = "DemographicCategoriesEnums")
    public static class DemographicCategoryEnum {
        private String demographicCategoryId;
        private String possibleValue;

        @ManyToOne
        @JoinColumn(name = "demographicCategoryId")
        DemographicCategory demographicCategory;

        public DemographicCategoryEnum(String demographicCategoryId, String possibleValue) {
            this.demographicCategoryId = demographicCategoryId;
            this.possibleValue = possibleValue;
        }

        public String getDemographicCategoryId() {
            return demographicCategoryId;
        }

        public void setDemographicCategoryId(String demographicCategoryId) {
            this.demographicCategoryId = demographicCategoryId;
        }

        public String getPossibleValue() {
            return possibleValue;
        }

        public void setPossibleValue(String possibleValue) {
            this.possibleValue = possibleValue;
        }
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public DemographicCategoryType getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(DemographicCategoryType categoryType) {
        this.categoryType = categoryType;
    }
}
