package org.sagebionetworks.bridge.models.schedules2.adherence;

import static org.sagebionetworks.bridge.TestConstants.CREATED_ON;
import static org.sagebionetworks.bridge.TestConstants.GUID;
import static org.sagebionetworks.bridge.TestConstants.MODIFIED_ON;
import static org.sagebionetworks.bridge.TestConstants.TEST_STUDY_ID;
import static org.sagebionetworks.bridge.TestConstants.TEST_USER_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.fasterxml.jackson.databind.JsonNode;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import org.sagebionetworks.bridge.TestUtils;
import org.sagebionetworks.bridge.json.BridgeObjectMapper;

public class AdherenceRecordTest extends Mockito {

    AdherenceRecord record;
    
    @Test
    public void canSerialize() throws Exception { 
        AdherenceRecord record = new AdherenceRecord();
        record.setUserId(TEST_USER_ID);
        record.setStudyId(TEST_STUDY_ID);
        record.setStartedOn(CREATED_ON);
        record.setFinishedOn(MODIFIED_ON);
        record.setEventTimestamp(CREATED_ON.plusHours(1));
        record.setClientData(TestUtils.getClientData());
        record.setInstanceGuid(GUID);
        
        JsonNode node = BridgeObjectMapper.get().valueToTree(record);
        assertEquals(node.get("userId").textValue(), TEST_USER_ID);
        assertEquals(node.get("studyId").textValue(), TEST_STUDY_ID);
        assertEquals(node.get("startedOn").textValue(), CREATED_ON.toString());
        assertEquals(node.get("finishedOn").textValue(), MODIFIED_ON.toString());
        assertEquals(node.get("eventTimestamp").textValue(), CREATED_ON.plusHours(1).toString());
        assertEquals(node.get("clientData").get("intValue").intValue(), 4);
        assertEquals(node.get("instanceGuid").textValue(), GUID);
        assertEquals(node.get("type").textValue(), "AdherenceRecord");
        
        AdherenceRecord deser = BridgeObjectMapper.get()
                .readValue(node.toString(), AdherenceRecord.class);
        assertEquals(deser.getUserId(), TEST_USER_ID);
        assertEquals(deser.getStudyId(), TEST_STUDY_ID);
        assertEquals(deser.getStartedOn(), CREATED_ON);
        assertEquals(deser.getFinishedOn(), MODIFIED_ON);
        assertEquals(deser.getEventTimestamp(), CREATED_ON.plusHours(1));
        assertNotNull(deser.getClientData());
        assertEquals(deser.getInstanceGuid(), GUID);
    }
}
