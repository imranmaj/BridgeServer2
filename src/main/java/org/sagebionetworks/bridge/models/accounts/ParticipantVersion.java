package org.sagebionetworks.bridge.models.accounts;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.sagebionetworks.bridge.dynamodb.DynamoParticipantVersion;
import org.sagebionetworks.bridge.json.BridgeTypeName;
import org.sagebionetworks.bridge.models.BridgeEntity;
import org.sagebionetworks.bridge.models.studies.Demographic;

/**
 * Represents a de-identified snapshot of a study participant at a moment in time. This is used by Exporter 3.0 to
 * generate the de-identified participant roster.
 */
@BridgeTypeName("ParticipantVersion")
@JsonDeserialize(as = DynamoParticipantVersion.class)
public interface ParticipantVersion extends BridgeEntity {
    static ParticipantVersion create() {
        return new DynamoParticipantVersion();
    }

    /** App that the participant lives in. This is one of the primary identifiers. */
    String getAppId();
    void setAppId(String appId);

    /** Participant's health code. This is the one of the primary identifiers. */
    String getHealthCode();
    void setHealthCode(String healthCode);

    /**
     * Participant version. This is incremented every time the participant is updated and we create a new row. Note
     * that this is different from the DDB version, which tracks the version of the row itself.
     */
    int getParticipantVersion();
    void setParticipantVersion(int participantVersion);

    /** Epoch milliseconds when any version of this participant was first created. */
    long getCreatedOn();
    void setCreatedOn(long createdOn);

    /** Epoch milliseconds when this participant was last updated (ie, when this participant version was created). */
    long getModifiedOn();
    void setModifiedOn(long modifiedOn);

    /** Data groups assigned to this participant. */
    Set<String> getDataGroups();
    void setDataGroups(Set<String> dataGroups);

    /**
     * Languages captured from a request by this user's Accept-Language header. This should be an ordered list of
     * unique ISO 639-1 language codes.
     */
    List<String> getLanguages();
    void setLanguages(List<String> languages);

    /** The sharing scope set for data being generated by this study participant. */
    SharingScope getSharingScope();
    void setSharingScope(SharingScope sharingScope);

    /**
     * The studies assigned to the user, and the optional external ID being used for each assignment, if any.
     * The keys of this map are study IDs, and the values are either the associated external ID, or an empty
     * string if there is no associated external ID.
     *
     * When exporting to Synapse, if the project is study specific, the Exporter will only export that specific study's
     * external ID.
     */
    Map<String, String> getStudyMemberships();
    void setStudyMemberships(Map<String, String> studyMemberships);

    /**
     * Participant's time zone, as an IANA time zone name (eg "America/Los_Angeles"). This corresponds to
     * StudyParticipant.clientTimeZone.
     */
    String getTimeZone();
    void setTimeZone(String timeZone);

    /**
     * Participant's app-level demographic information. Maps appId to map of
     * categoryName to one of the participant's Demographics for that app. Each
     * Demographic contains the participant's demographic information for a specific
     * category.
     */
    Map<String, Demographic> getAppDemographics();
    void setAppDemographics(Map<String, Demographic> appDemographics);

    /**
     * Participant's study-level demographic information. Maps studyId to map of
     * categoryName to one of the participant's Demographics for that study. Each
     * Demographic contains the participant's demographic information for a specific
     * category.
     */
    Map<String, Map<String, Demographic>> getStudyDemographics();
    void setStudyDemographics(Map<String, Map<String, Demographic>> studyDemographics);
}
