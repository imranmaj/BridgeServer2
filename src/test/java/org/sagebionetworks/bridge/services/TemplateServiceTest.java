package org.sagebionetworks.bridge.services;

import static org.sagebionetworks.bridge.BridgeConstants.API_DEFAULT_PAGE_SIZE;
import static org.sagebionetworks.bridge.TestConstants.LANGUAGES;
import static org.sagebionetworks.bridge.TestConstants.TEST_APP_ID;
import static org.sagebionetworks.bridge.TestConstants.TIMESTAMP;
import static org.sagebionetworks.bridge.TestConstants.UA;
import static org.sagebionetworks.bridge.TestConstants.USER_DATA_GROUPS;
import static org.sagebionetworks.bridge.TestConstants.TEST_USER_ID;
import static org.sagebionetworks.bridge.TestConstants.USER_STUDY_IDS;
import static org.sagebionetworks.bridge.models.apps.MimeType.HTML;
import static org.sagebionetworks.bridge.models.templates.TemplateType.EMAIL_ACCOUNT_EXISTS;
import static org.sagebionetworks.bridge.models.templates.TemplateType.EMAIL_APP_INSTALL_LINK;
import static org.sagebionetworks.bridge.models.templates.TemplateType.EMAIL_RESET_PASSWORD;
import static org.sagebionetworks.bridge.models.templates.TemplateType.EMAIL_SIGNED_CONSENT;
import static org.sagebionetworks.bridge.models.templates.TemplateType.EMAIL_SIGN_IN;
import static org.sagebionetworks.bridge.models.templates.TemplateType.EMAIL_VERIFY_EMAIL;
import static org.sagebionetworks.bridge.models.templates.TemplateType.SMS_ACCOUNT_EXISTS;
import static org.sagebionetworks.bridge.models.templates.TemplateType.SMS_APP_INSTALL_LINK;
import static org.sagebionetworks.bridge.models.templates.TemplateType.SMS_PHONE_SIGN_IN;
import static org.sagebionetworks.bridge.models.templates.TemplateType.SMS_RESET_PASSWORD;
import static org.sagebionetworks.bridge.models.templates.TemplateType.SMS_SIGNED_CONSENT;
import static org.sagebionetworks.bridge.models.templates.TemplateType.SMS_VERIFY_PHONE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.joda.time.DateTime;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.sagebionetworks.bridge.RequestContext;
import org.sagebionetworks.bridge.TestUtils;
import org.sagebionetworks.bridge.dao.CriteriaDao;
import org.sagebionetworks.bridge.dao.TemplateDao;
import org.sagebionetworks.bridge.dao.TemplateRevisionDao;
import org.sagebionetworks.bridge.exceptions.BadRequestException;
import org.sagebionetworks.bridge.exceptions.ConstraintViolationException;
import org.sagebionetworks.bridge.exceptions.EntityNotFoundException;
import org.sagebionetworks.bridge.exceptions.InvalidEntityException;
import org.sagebionetworks.bridge.models.ClientInfo;
import org.sagebionetworks.bridge.models.Criteria;
import org.sagebionetworks.bridge.models.CriteriaContext;
import org.sagebionetworks.bridge.models.GuidVersionHolder;
import org.sagebionetworks.bridge.models.PagedResourceList;
import org.sagebionetworks.bridge.models.apps.App;
import org.sagebionetworks.bridge.models.templates.Template;
import org.sagebionetworks.bridge.models.templates.TemplateRevision;
import org.sagebionetworks.bridge.models.templates.TemplateType;

public class TemplateServiceTest extends Mockito {
    
    private static final String GUID1 = "guidOne";
    private static final String GUID2 = "guidTwo";
    
    @Mock
    TemplateDao mockTemplateDao;
    
    @Mock
    TemplateRevisionDao mockTemplateRevisionDao;
    
    @Mock
    CriteriaDao mockCriteriaDao;
    
    @Mock
    AppService mockAppService;
    
    @Mock
    StudyService mockStudyService;
    
    @InjectMocks
    @Spy
    TemplateService service;
    
    @Captor
    ArgumentCaptor<Criteria> criteriaCaptor;
    
    @Captor
    ArgumentCaptor<Template> templateCaptor;
    
    @Captor
    ArgumentCaptor<TemplateRevision> revisionCaptor;
    
    @Captor
    ArgumentCaptor<CriteriaContext> contextCaptor;
    
    App app;
    
    @BeforeMethod
    public void beforeMethod() throws Exception {
        MockitoAnnotations.initMocks(this);
        service.setDefaultEmailVerificationTemplate(res(EMAIL_VERIFY_EMAIL));
        service.setDefaultEmailVerificationTemplateSubject(res(EMAIL_VERIFY_EMAIL));
        service.setDefaultPasswordTemplate(res(EMAIL_RESET_PASSWORD));
        service.setDefaultPasswordTemplateSubject(res(EMAIL_RESET_PASSWORD));
        service.setDefaultEmailSignInTemplate(res(EMAIL_SIGN_IN));
        service.setDefaultEmailSignInTemplateSubject(res(EMAIL_SIGN_IN));
        service.setDefaultAccountExistsTemplate(res(EMAIL_ACCOUNT_EXISTS));
        service.setDefaultAccountExistsTemplateSubject(res(EMAIL_ACCOUNT_EXISTS));
        service.setSignedConsentTemplate(res(EMAIL_SIGNED_CONSENT));
        service.setSignedConsentTemplateSubject(res(EMAIL_SIGNED_CONSENT));
        service.setAppInstallLinkTemplate(res(EMAIL_APP_INSTALL_LINK));
        service.setAppInstallLinkTemplateSubject(res(EMAIL_APP_INSTALL_LINK));
        service.setResetPasswordSmsTemplate(SMS_RESET_PASSWORD.name());
        service.setPhoneSignInSmsTemplate(SMS_PHONE_SIGN_IN.name());
        service.setAppInstallLinkSmsTemplate(SMS_APP_INSTALL_LINK.name());
        service.setVerifyPhoneSmsTemplate(SMS_VERIFY_PHONE.name());
        service.setAccountExistsSmsTemplate(SMS_ACCOUNT_EXISTS.name());
        service.setSignedConsentSmsTemplate(SMS_SIGNED_CONSENT.name());
        service.makeDefaultTemplateMap();
        when(service.generateGuid()).thenReturn(GUID1);
        when(service.getTimestamp()).thenReturn(TIMESTAMP);
        when(service.getUserId()).thenReturn(TEST_USER_ID);
        
        app = App.create();
        app.setIdentifier(TEST_APP_ID);
        app.setDataGroups(USER_DATA_GROUPS);
        app.setDefaultTemplates(new HashMap<>());
        when(mockAppService.getApp(TEST_APP_ID)).thenReturn(app);
        when(mockStudyService.getStudyIds(TEST_APP_ID)).thenReturn(USER_STUDY_IDS);
    }
    
    @AfterMethod
    public void afterMethod() {
        RequestContext.set(null);
    }
    
    private Resource res(TemplateType type) {
        return new ByteArrayResource(type.name().getBytes());
    }
    
    private Criteria makeCriteria(String guid, String lang) {
        Criteria criteria = Criteria.create();
        criteria.setKey("template:"+guid);
        criteria.setLanguage(lang);
        criteria.setAllOfGroups(ImmutableSet.of());
        criteria.setNoneOfGroups(ImmutableSet.of());
        criteria.setAllOfStudyIds(ImmutableSet.of());
        criteria.setNoneOfStudyIds(ImmutableSet.of());
        when(mockCriteriaDao.getCriteria("template:"+guid)).thenReturn(criteria);
        return criteria;
    }
    
    private Template makeTemplate(String guid, String lang) {
        Template template = Template.create();
        template.setGuid(guid);
        template.setCriteria(makeCriteria(guid, lang));
        when(mockTemplateDao.getTemplate(TEST_APP_ID, guid)).thenReturn(Optional.of(template));
        return template;
    }
    
    private CriteriaContext makeContext(String lang) {
        return new CriteriaContext.Builder().withAppId(TEST_APP_ID).withLanguages(ImmutableList.of(lang))
                .build();
    }
    
    private void mockGetTemplates(List<? extends Template> list) {
        PagedResourceList<? extends Template> page = new PagedResourceList<>(list, list.size());
        doReturn(page).when(mockTemplateDao).getTemplates(TEST_APP_ID, EMAIL_RESET_PASSWORD, null, null, false);
    }
    
    private void mockTemplateDefault(String guid) {
        if (guid == null) {
            app.setDefaultTemplates(ImmutableMap.of());
        } else {
            app.setDefaultTemplates(ImmutableMap.of(EMAIL_RESET_PASSWORD.name().toLowerCase(), guid));    
        }
        when(mockAppService.getApp(TEST_APP_ID)).thenReturn(app);
    }
    
    // This is the happy case... one template matches the provided criteria and is returned
    @Test
    public void getTemplateForUserMatchesOne() {
        Template t1 = makeTemplate(GUID1, "en");
        Template t2 = makeTemplate(GUID2, "fr");
        mockGetTemplates(ImmutableList.of(t1, t2));
        
        Template template = service.getTemplateForUser(app, makeContext("fr"), EMAIL_RESET_PASSWORD).get();
        assertEquals(template, t2);
    }
    
    // More than one template matches, so the app default is used instead
    @Test
    public void getTemplateForUserMatchesManyUsesDefault() {
        Template t1 = makeTemplate(GUID1, "fr");
        Template t2 = makeTemplate(GUID2, "fr");
        mockGetTemplates(ImmutableList.of(t1, t2));
        
        mockTemplateDefault(GUID2);
        
        Template template = service.getTemplateForUser(app, makeContext("fr"), EMAIL_RESET_PASSWORD).get();
        assertEquals(template, t2);
    }
    
    // More than one template matches, but the default is broken so the first matcher is returned
    @Test
    public void getTemplateForUserMatchesManyDefaultBroken() {
        Template t1 = makeTemplate(GUID1, "fr");
        Template t2 = makeTemplate(GUID2, "fr");
        mockGetTemplates(ImmutableList.of(t1, t2));
        
        mockTemplateDefault("guid-matches-nothing");
        
        Template template = service.getTemplateForUser(app, makeContext("fr"), EMAIL_RESET_PASSWORD).get();
        assertEquals(template, t1);
    }
    
    // Same as prior test but the first matcher is returned because there is no default for some reason
    @Test
    public void getTemplateForUserMatchesManyReturnsFirstOne() { 
        Template t1 = makeTemplate(GUID1, "fr");
        Template t2 = makeTemplate(GUID2, "fr");
        mockGetTemplates(ImmutableList.of(t1, t2));
        
        // no default in the app map at all
        mockTemplateDefault(null);
        
        Template template = service.getTemplateForUser(app, makeContext("fr"), EMAIL_RESET_PASSWORD).get();
        assertEquals(template, t1);
    }
    
    // No default, nothing matches, fall back to returning the first of ANY templates that were returned
    @Test
    public void getTemplateForUserMatchesNoneReturnsFirstOne() {
        Template t1 = makeTemplate(GUID1, "en");
        Template t2 = makeTemplate(GUID2, "en");
        mockGetTemplates(ImmutableList.of(t1, t2));
        
        // no default in the app map at all
        mockTemplateDefault(null);
        
        Template template = service.getTemplateForUser(app, makeContext("fr"), EMAIL_RESET_PASSWORD).get();
        assertEquals(template, t1);
    }
    
    // No templates returned, none matching, default exists but irrelevant, only then do we return null
    @Test
    public void getTemplateForUserMatchesNoneNoTemplateToReturn() {
        mockGetTemplates(ImmutableList.of());
        
        mockTemplateDefault(GUID1); // doesn't matter
        
        assertFalse(service.getTemplateForUser(app, makeContext("fr"), EMAIL_RESET_PASSWORD).isPresent());
    }
    
    // No templates returned, none matching, no default, return null
    @Test
    public void getTemplateForUserMatchesNoneNoDefaultNoTemplateToReturn() {
        mockGetTemplates(ImmutableList.of());

        assertFalse(service.getTemplateForUser(app, makeContext("fr"), EMAIL_RESET_PASSWORD).isPresent());
    }

    @Test
    public void getTemplatesForType() {
        Template t1 = Template.create();
        t1.setGuid("guidOne");
        
        Template t2 = Template.create();
        t2.setGuid("guidTwo");
        
        List<Template> list = ImmutableList.of(t1, t2);
        PagedResourceList<? extends Template> resourceList = new PagedResourceList<>(list, 150);
        doReturn(resourceList).when(mockTemplateDao).getTemplates(TEST_APP_ID, EMAIL_RESET_PASSWORD, 5, 50, true);
        
        Criteria criteria = Criteria.create();
        when(mockCriteriaDao.getCriteria(any())).thenReturn(criteria);
        
        PagedResourceList<? extends Template> results = service.getTemplatesForType(TEST_APP_ID, EMAIL_RESET_PASSWORD, 5, 50, true);
        assertSame(results, resourceList);
        
        for (Template template : results.getItems()) {
            assertNotNull(template.getCriteria());
        }
        verify(mockTemplateDao).getTemplates(TEST_APP_ID, EMAIL_RESET_PASSWORD, 5, 50, true);
        verify(mockCriteriaDao).getCriteria("template:guidOne");
        verify(mockCriteriaDao).getCriteria("template:guidTwo");
    }
    
    @Test
    public void getTemplatesForTypeDefaultsCriteriaObject() {
        Template t1 = Template.create();
        t1.setGuid("guidOne");
        
        Template t2 = Template.create();
        t2.setGuid("guidTwo");
        
        List<Template> list = ImmutableList.of(t1, t2);
        PagedResourceList<? extends Template> resourceList = new PagedResourceList<>(list, 150);
        doReturn(resourceList).when(mockTemplateDao).getTemplates(TEST_APP_ID, EMAIL_RESET_PASSWORD, 5, 50, true);
        
        PagedResourceList<? extends Template> results = service.getTemplatesForType(TEST_APP_ID, EMAIL_RESET_PASSWORD, 5, 50, true);
        
        for (Template template : results.getItems()) {
            assertNotNull(template.getCriteria());
        }
    }
    
    @Test
    public void getTemplatesForTypeDefaultsOffset() {
        PagedResourceList<? extends Template> resourceList = new PagedResourceList<>(ImmutableList.of(), 150);
        doReturn(resourceList).when(mockTemplateDao).getTemplates(TEST_APP_ID, EMAIL_RESET_PASSWORD, 0, 50, true);
        
        service.getTemplatesForType(TEST_APP_ID, EMAIL_RESET_PASSWORD, null, 50, true);
        
        verify(mockTemplateDao).getTemplates(TEST_APP_ID, EMAIL_RESET_PASSWORD, 0, 50, true);
    }

    @Test
    public void getTemplatesForTypeDefaultsPageSize() {
        PagedResourceList<? extends Template> resourceList = new PagedResourceList<>(ImmutableList.of(), 150);
        doReturn(resourceList).when(mockTemplateDao).getTemplates(TEST_APP_ID, EMAIL_RESET_PASSWORD, 5, API_DEFAULT_PAGE_SIZE, true);
        
        service.getTemplatesForType(TEST_APP_ID, EMAIL_RESET_PASSWORD, 5, null, true);
        
        verify(mockTemplateDao).getTemplates(TEST_APP_ID, EMAIL_RESET_PASSWORD, 5, API_DEFAULT_PAGE_SIZE, true);
    }

    @Test(expectedExceptions = BadRequestException.class)
    public void getTemplatesOffsetLessThanZero() {
        service.getTemplatesForType(TEST_APP_ID, EMAIL_RESET_PASSWORD, -5, null, true);
    }

    @Test(expectedExceptions = BadRequestException.class)
    public void getTemplatesPageSizeBelowMin() {
        service.getTemplatesForType(TEST_APP_ID, EMAIL_RESET_PASSWORD, null, 0, true);
    }

    @Test(expectedExceptions = BadRequestException.class)
    public void getTemplatesPageSizeAboveMax() {
        service.getTemplatesForType(TEST_APP_ID, EMAIL_RESET_PASSWORD, null, 1000, true);
    }

    @Test
    public void getTemplate() {
        Criteria criteria = Criteria.create();
        criteria.setKey("template:"+GUID1); // this is persisted as part of criteria, not set on load
        when(mockCriteriaDao.getCriteria("template:"+GUID1)).thenReturn(criteria);

        Template template = Template.create();
        template.setGuid(GUID1);
        template.setAppId(TEST_APP_ID);
        when(mockTemplateDao.getTemplate(TEST_APP_ID, GUID1)).thenReturn(Optional.of(template));
        
        Template result = service.getTemplate(TEST_APP_ID, GUID1);
        assertSame(result, template);
        assertSame(result.getCriteria(), template.getCriteria());
        assertEquals(result.getCriteria().getKey(), "template:"+GUID1);
    }
    
    @Test(expectedExceptions = BadRequestException.class)
    public void getTemplateNoGuid() {
        service.getTemplate(TEST_APP_ID, null);
    }

    @Test(expectedExceptions = EntityNotFoundException.class)
    public void getTemplateNotFound() {
        when(mockTemplateDao.getTemplate(TEST_APP_ID, GUID1)).thenReturn(Optional.empty());
        
        service.getTemplate(TEST_APP_ID, GUID1);
    }
    
    @Test
    public void createTemplate() {
        doAnswer(answer -> {
            return answer.getArgument(0);
        }).when(mockCriteriaDao).createOrUpdateCriteria(any());
        doAnswer(answer -> {
            Template captured = answer.getArgument(0);
            captured.setVersion(10);
            return null;
        }).when(mockTemplateDao).createTemplate(any());
        
        Criteria criteria = Criteria.create();
        criteria.setAllOfGroups(ImmutableSet.of("group1", "group2"));
        
        Template template = Template.create();
        template.setName("Test name");
        template.setTemplateType(EMAIL_RESET_PASSWORD);
        template.setDeleted(true);
        template.setVersion(3);
        template.setCriteria(criteria);
        
        GuidVersionHolder holder = service.createTemplate(app, template);
        assertEquals(holder.getGuid(), GUID1);
        assertEquals(holder.getVersion(), new Long(10));
        
        assertEquals(template.getAppId(), TEST_APP_ID);
        assertFalse(template.isDeleted());
        assertEquals(template.getVersion(), 10);
        assertEquals(template.getGuid(), GUID1);
        assertEquals(template.getCreatedOn(), TIMESTAMP);
        assertEquals(template.getModifiedOn(), TIMESTAMP);
        assertEquals(template.getCriteria().getKey(), "template:"+GUID1);
        assertEquals(template.getPublishedCreatedOn(), TIMESTAMP);
        
        verify(mockTemplateRevisionDao).createTemplateRevision(revisionCaptor.capture());
        verify(mockCriteriaDao).createOrUpdateCriteria(criteria);
        verify(mockTemplateDao).createTemplate(template);
        
        TemplateRevision revision = revisionCaptor.getValue();
        assertEquals(revision.getCreatedBy(), TEST_USER_ID);
        assertEquals(revision.getCreatedOn(), TIMESTAMP);
        assertEquals(revision.getTemplateGuid(), GUID1);
        assertEquals(revision.getStoragePath(), GUID1 + "." + TIMESTAMP.getMillis());
        
        assertEquals(revision.getSubject(), EMAIL_RESET_PASSWORD.name());
        assertEquals(revision.getDocumentContent(), EMAIL_RESET_PASSWORD.name());
        assertEquals(revision.getMimeType(), HTML);
    }
        
    @Test
    public void createTemplateDefaultsCriteria() {
        Template template = Template.create();
        template.setName("Test");
        template.setTemplateType(EMAIL_RESET_PASSWORD);
        
        service.createTemplate(app, template);
        
        verify(mockCriteriaDao).createOrUpdateCriteria(any(Criteria.class));
    }
    
    @Test(expectedExceptions = InvalidEntityException.class)
    public void createTemplateInvalid() {
        service.createTemplate(app, Template.create());
    }
    
    @Test
    public void updateTemplate() {
        doAnswer(answer -> {
            Template captured = answer.getArgument(0);
            captured.setVersion(10);
            return null;
        }).when(mockTemplateDao).updateTemplate(any());
        
        Template existing = Template.create();
        existing.setTemplateType(EMAIL_RESET_PASSWORD);
        existing.setCreatedOn(TIMESTAMP);
        existing.setAppId(TEST_APP_ID);
        when(mockTemplateDao.getTemplate(TEST_APP_ID, GUID1)).thenReturn(Optional.of(existing));
        
        Criteria criteria = TestUtils.createCriteria(1, 4, null, null);
        
        Template template = Template.create();
        template.setAppId("some-other-app-id");
        template.setGuid(GUID1);
        template.setName("Test");
        // Change these... they will be changed back
        template.setCreatedOn(DateTime.now().plusHours(1));
        template.setModifiedOn(DateTime.now().plusHours(1));
        template.setTemplateType(EMAIL_SIGN_IN);
        template.setCriteria(criteria);
        
        GuidVersionHolder result = service.updateTemplate(TEST_APP_ID, template);
        assertEquals(result.getGuid(), GUID1);
        assertEquals(result.getVersion(), new Long(10));
        
        assertEquals(template.getAppId(), TEST_APP_ID);
        assertEquals(template.getGuid(), GUID1);
        assertEquals(template.getVersion(), 10);
        // cannot be changed by an update.
        assertEquals(template.getTemplateType(), EMAIL_RESET_PASSWORD);
        assertEquals(template.getCreatedOn(), TIMESTAMP);
        assertEquals(template.getModifiedOn(), TIMESTAMP);
        assertEquals(template.getCriteria().getKey(), "template:"+GUID1);
        
        verify(mockCriteriaDao).createOrUpdateCriteria(criteria);
        verify(mockTemplateDao).updateTemplate(template);
    }
    
    @Test
    public void updateTemplateSucceedsWithDefaultIfNotDeleting() {
        doAnswer(answer -> {
            Template captured = answer.getArgument(0);
            captured.setVersion(10);
            return null;
        }).when(mockTemplateDao).updateTemplate(any());
        
        Template existing = Template.create();
        existing.setTemplateType(EMAIL_RESET_PASSWORD);
        existing.setGuid(GUID1);
        existing.setCreatedOn(TIMESTAMP);
        existing.setAppId(TEST_APP_ID);
        when(mockTemplateDao.getTemplate(TEST_APP_ID, GUID1)).thenReturn(Optional.of(existing));
        
        Template template = Template.create();
        template.setCriteria(makeCriteria(GUID1, null));
        template.setGuid(GUID1);
        template.setName("Test Change");
        
        mockTemplateDefault(GUID1);
        
        service.updateTemplate(TEST_APP_ID, template);
        
        verify(mockCriteriaDao).createOrUpdateCriteria(template.getCriteria());
        verify(mockTemplateDao).updateTemplate(template);        
    }
    
    @Test(expectedExceptions = EntityNotFoundException.class)
    public void updateTemplateFailsIfDeleted() { 
        Template existing = Template.create();
        existing.setAppId(TEST_APP_ID);
        existing.setDeleted(true);
        when(mockTemplateDao.getTemplate(TEST_APP_ID, GUID1)).thenReturn(Optional.of(existing));
        
        Template template = Template.create();
        template.setAppId(TEST_APP_ID);
        template.setGuid(GUID1);
        template.setDeleted(true);
        
        service.updateTemplate(TEST_APP_ID, template);
    }
    
    @Test(expectedExceptions = InvalidEntityException.class)
    public void updateTemplateInvalid() {
        Template template = Template.create();
        template.setGuid(GUID1);
        when(mockTemplateDao.getTemplate(TEST_APP_ID, GUID1)).thenReturn(Optional.of(template));
        
        service.updateTemplate(TEST_APP_ID, template);
    }
    
    @Test
    public void deleteTemplate() {
        Template existing = Template.create();
        existing.setAppId(TEST_APP_ID);
        existing.setGuid(GUID1);
        existing.setTemplateType(EMAIL_ACCOUNT_EXISTS);
        when(mockTemplateDao.getTemplate(TEST_APP_ID, GUID1)).thenReturn(Optional.of(existing));

        service.deleteTemplate(TEST_APP_ID, GUID1);
        
        verify(mockTemplateDao).updateTemplate(templateCaptor.capture());
        
        Template persisted = templateCaptor.getValue();
        assertTrue(persisted.isDeleted());
        assertEquals(persisted.getModifiedOn(), TIMESTAMP);
    }
    
    @Test(expectedExceptions = EntityNotFoundException.class)
    public void deleteTemplateLogicallyDeleted() { 
        Template existing = Template.create();
        existing.setAppId(TEST_APP_ID);
        existing.setGuid(GUID1);
        existing.setDeleted(true);
        when(mockTemplateDao.getTemplate(TEST_APP_ID, GUID1)).thenReturn(Optional.of(existing));

        service.deleteTemplate(TEST_APP_ID, GUID1);
    }
    
    @Test(expectedExceptions = EntityNotFoundException.class)
    public void deleteTemplateMissing() { 
        when(mockTemplateDao.getTemplate(TEST_APP_ID, GUID1)).thenReturn(Optional.empty());

        service.deleteTemplate(TEST_APP_ID, GUID1);
    }
    
    @Test
    public void deleteTemplatePermanently() {
        Template existing = Template.create();
        existing.setAppId(TEST_APP_ID);
        existing.setGuid(GUID1);
        existing.setTemplateType(EMAIL_ACCOUNT_EXISTS);
        when(mockTemplateDao.getTemplate(TEST_APP_ID, GUID1)).thenReturn(Optional.of(existing));

        service.deleteTemplatePermanently(TEST_APP_ID, GUID1);

        verify(mockCriteriaDao).deleteCriteria("template:"+GUID1);
        verify(mockTemplateDao).deleteTemplatePermanently(TEST_APP_ID, GUID1);
    }
    
    @Test(expectedExceptions = EntityNotFoundException.class)
    public void deleteTemplatePermanentlyMissing() {
        when(mockTemplateDao.getTemplate(TEST_APP_ID, GUID1)).thenReturn(Optional.empty());

        service.deleteTemplatePermanently(TEST_APP_ID, GUID1);
    }
    
    @Test(expectedExceptions = ConstraintViolationException.class)
    public void cannotUpdateToDeleteDefaultTemplate() {
        app.getDefaultTemplates().put(EMAIL_RESET_PASSWORD.name().toLowerCase(), GUID1);
        
        Template existing = Template.create();
        existing.setTemplateType(EMAIL_RESET_PASSWORD);
        existing.setCreatedOn(TIMESTAMP);
        existing.setAppId(TEST_APP_ID);
        existing.setDeleted(false);
        when(mockTemplateDao.getTemplate(TEST_APP_ID, GUID1)).thenReturn(Optional.of(existing));
        
        Template template = Template.create();
        template.setGuid(GUID1);
        template.setTemplateType(EMAIL_RESET_PASSWORD);
        template.setName("Test");
        template.setDeleted(true);
        
        service.updateTemplate(TEST_APP_ID, template);
    }
    
    @Test(expectedExceptions = ConstraintViolationException.class)
    public void cannotLogicallyDeleteDefaultTemplate() {
        app.getDefaultTemplates().put(EMAIL_ACCOUNT_EXISTS.name().toLowerCase(), GUID1);
        Template existing = Template.create();
        existing.setAppId(TEST_APP_ID);
        existing.setGuid(GUID1);
        existing.setTemplateType(EMAIL_ACCOUNT_EXISTS);
        when(mockTemplateDao.getTemplate(TEST_APP_ID, GUID1)).thenReturn(Optional.of(existing));

        service.deleteTemplate(TEST_APP_ID, GUID1);
    }
    
    @Test
    public void getRevisionForUser() throws Exception {
        ClientInfo clientInfo = ClientInfo.fromUserAgentCache(UA);
        RequestContext.set(new RequestContext.Builder()
                .withCallerClientInfo(clientInfo).withCallerLanguages(LANGUAGES).build());
        
        DateTime createdOn = DateTime.now();
        Template t1 = makeTemplate(GUID1, "de");
        t1.setPublishedCreatedOn(createdOn);
        
        // This one should match based on languages ("en")
        Template t2 = makeTemplate(GUID2, "en");
        t2.setPublishedCreatedOn(createdOn.plusHours(1));
        mockGetTemplates(ImmutableList.of(t1, t2));
        
        TemplateRevision r2 = TemplateRevision.create();
        when(mockTemplateRevisionDao.getTemplateRevision(GUID2, createdOn.plusHours(1))).thenReturn(Optional.of(r2));
        
        app.setIdentifier(TEST_APP_ID);
        
        TemplateRevision retrieved = service.getRevisionForUser(app, EMAIL_RESET_PASSWORD);
        assertSame(retrieved, r2);
        
        verify(service).getTemplateForUser(eq(app), contextCaptor.capture(), eq(EMAIL_RESET_PASSWORD));
        
        CriteriaContext context = contextCaptor.getValue();
        assertEquals(context.getLanguages(), LANGUAGES);
        assertEquals(context.getClientInfo(), clientInfo);
    }
    
    @Test(expectedExceptions = EntityNotFoundException.class, 
            expectedExceptionsMessageRegExp = "TemplateRevision not found.")
    public void getRevisionForUserWhenTemplateExistsButRevisionMissing() throws Exception {
        ClientInfo clientInfo = ClientInfo.fromUserAgentCache(UA);
        RequestContext.set(new RequestContext.Builder()
                .withCallerClientInfo(clientInfo).withCallerLanguages(LANGUAGES).build());
        
        DateTime createdOn = DateTime.now();
        Template t1 = makeTemplate(GUID1, "en");
        t1.setPublishedCreatedOn(createdOn);
        mockGetTemplates(ImmutableList.of(t1));
        
        when(mockTemplateRevisionDao.getTemplateRevision(GUID1, createdOn)).thenReturn(Optional.empty());
        
        app.setIdentifier(TEST_APP_ID);
        
        service.getRevisionForUser(app, EMAIL_RESET_PASSWORD);
    }
    
    @Test
    public void deleteTemplatesForApp() {
        service.deleteAllTemplates(TEST_APP_ID);
        
        verify(mockTemplateDao).deleteTemplatesForApp(TEST_APP_ID);
    }
}
