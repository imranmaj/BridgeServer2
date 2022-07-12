package org.sagebionetworks.bridge.dao;

import java.util.List;

import org.sagebionetworks.bridge.models.studies.DemographicCategory;
import org.sagebionetworks.bridge.models.studies.DemographicValue;

public interface DemographicDao {
    void saveDemographicCategoryString(DemographicCategory demographicCategory);
    void saveDemographicCategoryInt(DemographicCategory demographicCategory, DemographicCategory.DemographicCategoryInt demographicCategoryInt);
    void saveDemographicCategoryFloat(DemographicCategory demographicCategory, DemographicCategory.DemographicCategoryFloat demographicCategoryFloat);
    void saveDemographicCategoryEnum(DemographicCategory demographicCategory, List<DemographicCategory.DemographicCategoryEnum> demographicCategoryEnum);

    void deleteDemographicCategoryStudy(String studyId, String categoryName);
    void deleteDemographicCategoryApp(String appId, String categoryName);

     getDemographicCategoryStudy(String studyId, String categoryName);
     getDemographicCategoryApp(String appId, String categoryName);
    
    void saveDemographicValueString(DemographicCategory demographicCategory, DemographicValue.DemographicValueString demographicValueString);
    void saveDemographicValueInt(DemographicCategory demographicCategory, DemographicValue.DemographicValueInt demographicValueInt);
    void saveDemographicValueFloat(DemographicCategory demographicCategory, DemographicValue.DemographicValueFloat demographicValueFloat);
    void saveDemographicValueEnum(DemographicCategory demographicCategory, DemographicValue.DemographicValueEnum demographicValueEnum);

    void deleteDemographicValueStudy(String studyId, String categoryName, String userId);
    void deleteDemographicValueApp(String appId, String categoryName, String userId);

     getDemographicValueStudy(String studyId, String categoryName, String userId);
     getDemographicValueApp(String appId, String categoryName, String userId);
}
