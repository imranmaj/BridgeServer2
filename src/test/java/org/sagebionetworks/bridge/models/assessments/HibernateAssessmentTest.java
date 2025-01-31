package org.sagebionetworks.bridge.models.assessments;

import static org.sagebionetworks.bridge.TestConstants.CREATED_ON;
import static org.sagebionetworks.bridge.TestConstants.CUSTOMIZATION_FIELDS;
import static org.sagebionetworks.bridge.TestConstants.GUID;
import static org.sagebionetworks.bridge.TestConstants.IDENTIFIER;
import static org.sagebionetworks.bridge.TestConstants.LABELS;
import static org.sagebionetworks.bridge.TestConstants.MODIFIED_ON;
import static org.sagebionetworks.bridge.TestConstants.TEST_OWNER_ID;
import static org.sagebionetworks.bridge.TestConstants.TAGS;
import static org.sagebionetworks.bridge.models.OperatingSystem.ANDROID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class HibernateAssessmentTest {

    @Test
    public void revisionDefaultsToOne() {
        HibernateAssessment assessment = new HibernateAssessment();
        assertEquals(assessment.getRevision(), 1);
    }
    
    @Test
    public void testFields() {
        HibernateAssessment assessment = createAssessment();
        assertAssessment(assessment);
    }
   
    @Test
    public void createFactoryMethod() {
        Assessment dto = AssessmentTest.createAssessment();
        HibernateAssessment assessment = HibernateAssessment.create("appId", dto);
        assertAssessment(assessment);
    }
    
    public static HibernateAssessment createAssessment() {
        HibernateAssessment assessment = new HibernateAssessment();
        assessment.setGuid(GUID);
        assessment.setAppId("appId");
        assessment.setIdentifier(IDENTIFIER);
        assessment.setTitle("title");
        assessment.setSummary("summary");
        assessment.setValidationStatus("validationStatus");
        assessment.setNormingStatus("normingStatus");
        assessment.setMinutesToComplete(10);
        assessment.setOsName(ANDROID);
        assessment.setOriginGuid("originGuid");
        assessment.setOwnerId(TEST_OWNER_ID);
        assessment.setTags(TAGS);
        assessment.setCustomizationFields(CUSTOMIZATION_FIELDS);
        assessment.setCreatedOn(CREATED_ON);
        assessment.setModifiedOn(MODIFIED_ON);
        assessment.setDeleted(true);
        assessment.setRevision(5);
        assessment.setVersion(8L);
        ImageResource imageResource = new ImageResource();
        imageResource.setName("default");
        imageResource.setModule("sage_survey");
        imageResource.setLabels(LABELS);
        assessment.setImageResource(imageResource);
        return assessment;
    }
    
    private void assertAssessment(HibernateAssessment assessment) {
        assertEquals(assessment.getGuid(), GUID);
        assertEquals(assessment.getAppId(), "appId");
        assertEquals(assessment.getIdentifier(), IDENTIFIER);
        assertEquals(assessment.getTitle(), "title");
        assertEquals(assessment.getSummary(), "summary");
        assertEquals(assessment.getValidationStatus(), "validationStatus");
        assertEquals(assessment.getNormingStatus(), "normingStatus");
        assertEquals(assessment.getMinutesToComplete(), new Integer(10));
        assertEquals(assessment.getOsName(), ANDROID);
        assertEquals(assessment.getOriginGuid(), "originGuid");
        assertEquals(assessment.getOwnerId(), TEST_OWNER_ID);
        assertEquals(assessment.getTags(), TAGS);
        assertEquals(assessment.getCustomizationFields(), CUSTOMIZATION_FIELDS);
        assertEquals(assessment.getCreatedOn(), CREATED_ON);
        assertEquals(assessment.getModifiedOn(), MODIFIED_ON);
        assertTrue(assessment.isDeleted());
        assertEquals(assessment.getRevision(), 5);
        assertEquals(assessment.getVersion(), 8);
        assertEquals(assessment.getImageResource().getName(), "default");
        assertEquals(assessment.getImageResource().getModule(), "sage_survey");
        assertEquals(assessment.getImageResource().getLabels().get(0).getLang(), LABELS.get(0).getLang());
        assertEquals(assessment.getImageResource().getLabels().get(0).getValue(), LABELS.get(0).getValue());
        assertEquals(assessment.getImageResource().getLabels().get(1).getLang(), LABELS.get(1).getLang());
        assertEquals(assessment.getImageResource().getLabels().get(1).getValue(), LABELS.get(1).getValue());
    }
}
