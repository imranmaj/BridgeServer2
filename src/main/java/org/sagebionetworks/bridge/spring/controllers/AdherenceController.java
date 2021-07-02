package org.sagebionetworks.bridge.spring.controllers;

import static org.sagebionetworks.bridge.Roles.RESEARCHER;
import static org.sagebionetworks.bridge.Roles.STUDY_COORDINATOR;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

import org.sagebionetworks.bridge.exceptions.EntityNotFoundException;
import org.sagebionetworks.bridge.models.PagedResourceList;
import org.sagebionetworks.bridge.models.StatusMessage;
import org.sagebionetworks.bridge.models.accounts.Account;
import org.sagebionetworks.bridge.models.accounts.UserSession;
import org.sagebionetworks.bridge.models.schedules2.adherence.AdherenceRecord;
import org.sagebionetworks.bridge.models.schedules2.adherence.AdherenceRecordList;
import org.sagebionetworks.bridge.models.schedules2.adherence.AdherenceRecordsSearch;
import org.sagebionetworks.bridge.services.AdherenceService;

@CrossOrigin
@RestController
public class AdherenceController extends BaseController {
    
    static final StatusMessage SAVED_MSG = new StatusMessage("Adherence records saved.");
    static final StatusMessage DELETED_MSG = new StatusMessage("Adherence records deleted");
    
    private AdherenceService service;

    @Autowired
    final void setAdherenceService(AdherenceService service) {
        this.service = service;
    }
    
    @PostMapping("/v5/studies/{studyId}/participants/self/adherence")
    public StatusMessage updateAdherenceRecordsForSelf(@PathVariable String studyId) {
        UserSession session = getAuthenticatedAndConsentedSession();
        
        AdherenceRecordList recordsList = parseJson(AdherenceRecordList.class);
        for (AdherenceRecord oneRecord : recordsList.getRecords()) {
            oneRecord.setAppId(session.getAppId());
            oneRecord.setUserId(session.getId());
            oneRecord.setStudyId(studyId);
        }
        service.updateAdherenceRecords(session.getAppId(), recordsList);
        return SAVED_MSG;
    }
    
    @PostMapping("/v5/studies/{studyId}/participants/{userIdToken}/adherence")
    public StatusMessage updateAdherenceRecords(@PathVariable String studyId, @PathVariable String userIdToken) {
        UserSession session = getAuthenticatedSession(RESEARCHER, STUDY_COORDINATOR);
        
        String userId = accountService.getAccountId(session.getAppId(), userIdToken)
                .orElseThrow(() -> new EntityNotFoundException(Account.class));
        
        AdherenceRecordList recordsList = parseJson(AdherenceRecordList.class);
        for (AdherenceRecord oneRecord : recordsList.getRecords()) {
            oneRecord.setAppId(session.getAppId());
            oneRecord.setUserId(userId);
            oneRecord.setStudyId(studyId);
        }
        service.updateAdherenceRecords(session.getAppId(), recordsList);
        return SAVED_MSG;
    }
    
    @PostMapping("/v5/studies/{studyId}/participants/self/adherence/search")
    public PagedResourceList<AdherenceRecord> searchForAdherenceRecordsForSelf(@PathVariable String studyId) {
        UserSession session = getAuthenticatedAndConsentedSession();
        
        AdherenceRecordsSearch payload = parseJson(AdherenceRecordsSearch.class);
        
        AdherenceRecordsSearch search = payload.toBuilder()
                .withUserId(session.getId())
                .withStudyId(studyId).build();
        
        return service.getAdherenceRecords(session.getAppId(), search);
    }
    
    @PostMapping("/v5/studies/{studyId}/participants/{userIdToken}/adherence/search")
    public PagedResourceList<AdherenceRecord> searchForAdherenceRecords(@PathVariable String studyId,
            @PathVariable String userIdToken) {
        UserSession session = getAuthenticatedSession(RESEARCHER, STUDY_COORDINATOR);
        
        AdherenceRecordsSearch payload = parseJson(AdherenceRecordsSearch.class);
        String userId = accountService.getAccountId(session.getAppId(), userIdToken)
                .orElseThrow(() -> new EntityNotFoundException(Account.class));
        
        AdherenceRecordsSearch search = payload.toBuilder()
                .withUserId(userId)
                .withStudyId(studyId).build();
        
        return service.getAdherenceRecords(session.getAppId(), search);
    }

    @DeleteMapping("/v5/studies/{studyId}/participants/self/adherence")
    public StatusMessage deleteAdherenceRecordsForSelf(@PathVariable String studyId) {
        UserSession session = getAuthenticatedAndConsentedSession();

        AdherenceRecordList recordsList = parseJson(AdherenceRecordList.class);
        for (AdherenceRecord oneRecord : recordsList.getRecords()) {
            oneRecord.setAppId(session.getAppId());
            oneRecord.setUserId(session.getId());
            oneRecord.setStudyId(studyId);
        }
//        TODO: Build deletion method in service
//        service.deleteAdherenceRecords(session.getAppId(), recordsList);
        return DELETED_MSG;
    }

    @DeleteMapping("/v5/studies/{studyId}/participants/{userIdToken}/adherence")
    public StatusMessage deleteAdherenceRecords(@PathVariable String studyId, @PathVariable String userIdToken) {
        UserSession session = getAuthenticatedSession(RESEARCHER, STUDY_COORDINATOR);

        String userId = accountService.getAccountId(session.getAppId(), userIdToken)
                .orElseThrow(() -> new EntityNotFoundException(Account.class));

        AdherenceRecordList recordsList = parseJson(AdherenceRecordList.class);
        for (AdherenceRecord oneRecord : recordsList.getRecords()) {
            oneRecord.setAppId(session.getAppId());
            oneRecord.setUserId(userId);
            oneRecord.setStudyId(studyId);
        }

//        service.deleteAdherenceRecords(session.getAppId(), recordsList);
        return DELETED_MSG;
    }
}
