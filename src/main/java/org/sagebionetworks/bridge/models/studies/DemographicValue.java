package org.sagebionetworks.bridge.models.studies;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

public class DemographicValue {
    @Embeddable
    public static class DemographicId implements Serializable {
        private String demographicCategoryId;
        private String userId;

        public DemographicId(String demographicCategoryId, String userId) {
            this.demographicCategoryId = demographicCategoryId;
            this.userId = userId;
        }

        public String getDemographicCategoryId() {
            return demographicCategoryId;
        }

        public void setDemographicCategoryId(String demographicCategoryId) {
            this.demographicCategoryId = demographicCategoryId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((demographicCategoryId == null) ? 0 : demographicCategoryId.hashCode());
            result = prime * result + ((userId == null) ? 0 : userId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            DemographicId other = (DemographicId) obj;
            if (demographicCategoryId == null) {
                if (other.demographicCategoryId != null)
                    return false;
            } else if (!demographicCategoryId.equals(other.demographicCategoryId))
                return false;
            if (userId == null) {
                if (other.userId != null)
                    return false;
            } else if (!userId.equals(other.userId))
                return false;
            return true;
        }
    }

    @Entity
    @Table(name = "DemographicValuesStrings")
    @IdClass(DemographicId.class)
    public static class DemographicValueString {
        @Id
        private String demographicCategoryId;
        @Id
        private String userId;
        private String value;

        @ManyToOne
        @JoinColumn(name = "demographicCategoryId")
        DemographicCategory demographicCategory;

        public DemographicValueString(String demographicCategoryId, String userId, String value) {
            this.demographicCategoryId = demographicCategoryId;
            this.userId = userId;
            this.value = value;
        }

        public String getDemographicCategoryId() {
            return demographicCategoryId;
        }

        public void setDemographicCategoryId(String demographicCategoryId) {
            this.demographicCategoryId = demographicCategoryId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @Entity
    @Table(name = "DemographicValuesInts")
    public static class DemographicValueInt {
        @Id
        private String demographicCategoryId;
        @Id
        private String userId;
        private long value;

        @ManyToOne
        @JoinColumn(name = "demographicCategoryId")
        DemographicCategory demographicCategory;

        public DemographicValueInt(String demographicCategoryId, String userId, long value) {
            this.demographicCategoryId = demographicCategoryId;
            this.userId = userId;
            this.value = value;
        }

        public String getDemographicCategoryId() {
            return demographicCategoryId;
        }

        public void setDemographicCategoryId(String demographicCategoryId) {
            this.demographicCategoryId = demographicCategoryId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }
    }

    @Entity
    @Table(name = "DemographicValuesFloats")
    public static class DemographicValueFloat {
        @Id
        private String demographicCategoryId;
        @Id
        private String userId;
        private double value;

        @ManyToOne
        @JoinColumn(name = "demographicCategoryId")
        DemographicCategory demographicCategory;

        public DemographicValueFloat(String demographicCategoryId, String userId, double value) {
            this.demographicCategoryId = demographicCategoryId;
            this.userId = userId;
            this.value = value;
        }

        public String getDemographicCategoryId() {
            return demographicCategoryId;
        }

        public void setDemographicCategoryId(String demographicCategoryId) {
            this.demographicCategoryId = demographicCategoryId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }
    }

    @Entity
    @Table(name = "DemographicValuesEnums")
    public static class DemographicValueEnum {
        @Id
        private String demographicCategoryId;
        @Id
        private String userId;
        private String value;

        @ManyToOne
        @JoinColumn(name = "demographicCategoryId")
        DemographicCategory demographicCategory;

        public DemographicValueEnum(String demographicCategoryId, String userId, String value) {
            this.demographicCategoryId = demographicCategoryId;
            this.userId = userId;
            this.value = value;
        }

        public String getDemographicCategoryId() {
            return demographicCategoryId;
        }

        public void setDemographicCategoryId(String demographicCategoryId) {
            this.demographicCategoryId = demographicCategoryId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
