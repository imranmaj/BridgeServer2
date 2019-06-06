package org.sagebionetworks.bridge.models;

import org.testng.annotations.Test;

import org.sagebionetworks.bridge.json.BridgeObjectMapper;

import static org.testng.Assert.assertEquals;

import com.fasterxml.jackson.core.type.TypeReference;

import nl.jqno.equalsverifier.EqualsVerifier;

public class TupleTest {

    @Test
    public void equalsHashCode() {
        EqualsVerifier.forClass(Tuple.class).allFieldsShouldBeUsed().verify();
    }
    
    @Test
    public void canSerialize() throws Exception {
        Tuple<String> tuple = new Tuple<>("value1", "value2");
        
        String json = BridgeObjectMapper.get().writeValueAsString(tuple);
        
        Tuple<String> deser = BridgeObjectMapper.get().readValue(json, new TypeReference<Tuple<String>>() {});
        assertEquals(deser, tuple);
    }

}
