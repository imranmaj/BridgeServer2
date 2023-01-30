package org.sagebionetworks.bridge.hibernate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.bridge.TestConstants.TEST_APP_ID;
import static org.sagebionetworks.bridge.TestConstants.TEST_STUDY_ID;
import static org.sagebionetworks.bridge.TestConstants.TEST_USER_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Optional;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.bridge.json.BridgeObjectMapper;
import org.sagebionetworks.bridge.models.PagedResourceList;
import org.sagebionetworks.bridge.models.studies.Alert;
import org.sagebionetworks.bridge.models.studies.Alert.AlertCategory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class HibernateAlertDaoTest {
    private static final String ALERT_ID = "test-alert-id";

    @Mock
    HibernateHelper hibernateHelper;

    @InjectMocks
    HibernateAlertDao hibernateAlertDao;

    Alert alert;

    @BeforeMethod
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);

        alert = new Alert(ALERT_ID, null, TEST_STUDY_ID, TEST_APP_ID, TEST_USER_ID, AlertCategory.NEW_ENROLLMENT,
                BridgeObjectMapper.get().nullNode());
    }

    @Test
    public void createAlert() {
        hibernateAlertDao.createAlert(alert);

        verify(hibernateHelper).create(alert);
    }

    @Test
    public void getAlert() {
        when(hibernateHelper.queryGetOne(any(), any(), any())).thenReturn(Optional.of(alert));
        
        Optional<Alert> returnedAlert = hibernateAlertDao.getAlert(TEST_STUDY_ID, TEST_APP_ID, TEST_USER_ID, AlertCategory.NEW_ENROLLMENT);

        assertTrue(returnedAlert.isPresent());
        assertSame(returnedAlert.get(), alert);
        verify(hibernateHelper).queryGetOne("FROM Alert a WHERE " +
            "a.studyId = :studyId AND " +
            "a.appId = :appId AND " +
            "a.userId = :userId AND " + 
            "a.category = :category",
            ImmutableMap.of("studyId", TEST_STUDY_ID, 
                "appId", TEST_APP_ID, 
                "userId", TEST_USER_ID, 
                "category", AlertCategory.NEW_ENROLLMENT), Alert.class);
    }

    @Test
    public void getAlertDoesNotExist() {
        when(hibernateHelper.queryGetOne(any(), any(), any())).thenReturn(Optional.empty());

        Optional<Alert> returnedAlert = hibernateAlertDao.getAlert(TEST_STUDY_ID, TEST_APP_ID, TEST_USER_ID, AlertCategory.NEW_ENROLLMENT);

        assertFalse(returnedAlert.isPresent());
        verify(hibernateHelper).queryGetOne("FROM Alert a WHERE " +
            "a.studyId = :studyId AND " +
            "a.appId = :appId AND " +
            "a.userId = :userId AND " + 
            "a.category = :category",
            ImmutableMap.of("studyId", TEST_STUDY_ID, 
                "appId", TEST_APP_ID, 
                "userId", TEST_USER_ID, 
                "category", AlertCategory.NEW_ENROLLMENT), Alert.class);
    }

    @Test
    public void getAlertById() {
        when(hibernateHelper.getById(Alert.class, ALERT_ID)).thenReturn(alert);

        Optional<Alert> returnedAlert = hibernateAlertDao.getAlertById(ALERT_ID);

        assertTrue(returnedAlert.isPresent());
        assertSame(returnedAlert.get(), alert);
        verify(hibernateHelper).getById(Alert.class, ALERT_ID);
    }

    @Test
    public void getAlertByIdDoesNotExist() {
        when(hibernateHelper.getById(Alert.class, ALERT_ID)).thenReturn(null);

        Optional<Alert> returnedAlert = hibernateAlertDao.getAlertById(ALERT_ID);

        assertFalse(returnedAlert.isPresent());
        verify(hibernateHelper).getById(Alert.class, ALERT_ID);
    }

    @Test
    public void getAlerts() {
        when(hibernateHelper.queryCount(any(), any())).thenReturn(1);
        when(hibernateHelper.queryGet(any(), any(), any(), any(), any())).thenReturn(ImmutableList.of(alert));

        PagedResourceList<Alert> returnedAlerts = hibernateAlertDao.getAlerts(TEST_APP_ID, TEST_STUDY_ID, 2, 100);

        assertEquals(returnedAlerts.getTotal().intValue(), 1);
        assertEquals(returnedAlerts.getRequestParams().get("offsetBy"), 2);
        assertEquals(returnedAlerts.getRequestParams().get("pageSize"), 100);
        assertEquals(returnedAlerts.getItems().size(), 1);
        assertSame(returnedAlerts.getItems().get(0), alert);
        String QUERY = "FROM Alert a WHERE " +
            "a.appId = :appId AND " +
            "a.studyId = :studyId " +
            "ORDER BY createdOn DESC";
        verify(hibernateHelper).queryCount("SELECT COUNT(*) " + QUERY,
            ImmutableMap.of("studyId", TEST_STUDY_ID, 
            "appId", TEST_APP_ID));
        verify(hibernateHelper).queryGet(QUERY,
            ImmutableMap.of("studyId", TEST_STUDY_ID, 
            "appId", TEST_APP_ID), 2, 100, Alert.class);
    }

    @Test
    public void deleteAlerts() {
        hibernateAlertDao.deleteAlerts(ImmutableList.of(ALERT_ID));

        verify(hibernateHelper).query("DELETE FROM Alert a WHERE " +
                "a.id in (:alertIds)",
                ImmutableMap.of("alertIds", ImmutableList.of(ALERT_ID)));
    }

    @Test
    public void deleteAlertsForStudy() {
        hibernateAlertDao.deleteAlertsForStudy(TEST_APP_ID, TEST_STUDY_ID);

        verify(hibernateHelper).query("DELETE FROM Alert a WHERE " +
                "a.appId = :appId AND " +
                "a.studyId = :studyId",
                ImmutableMap.of("studyId", TEST_STUDY_ID,
                        "appId", TEST_APP_ID));
    }

    @Test
    public void deleteAlertsForUserInApp() {
        hibernateAlertDao.deleteAlertsForUserInApp(TEST_APP_ID, TEST_USER_ID);

        verify(hibernateHelper).query("DELETE FROM Alert a WHERE " +
                "a.appId = :appId AND " +
                "a.userId = :userId",
                ImmutableMap.of("appId", TEST_APP_ID,
                        "userId", TEST_USER_ID));
    }

    @Test
    public void deleteAlertsForUserInStudy() {
        hibernateAlertDao.deleteAlertsForUserInStudy(TEST_APP_ID, TEST_STUDY_ID, TEST_USER_ID);

        verify(hibernateHelper).query("DELETE FROM Alert a WHERE " +
                "a.appId = :appId AND " +
                "a.studyId = :studyId AND " +
                "a.userId = :userId",
                ImmutableMap.of("appId", TEST_APP_ID,
                        "studyId", TEST_STUDY_ID,
                        "userId", TEST_USER_ID));
    }
}
