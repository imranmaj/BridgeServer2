package org.sagebionetworks.bridge.services;

import com.amazonaws.services.s3.AmazonS3;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.sagebionetworks.bridge.TestConstants;
import org.sagebionetworks.bridge.config.BridgeConfig;
import org.sagebionetworks.bridge.dao.ParticipantFileDao;
import org.sagebionetworks.bridge.dynamodb.DynamoParticipantFile;
import org.sagebionetworks.bridge.exceptions.EntityAlreadyExistsException;
import org.sagebionetworks.bridge.exceptions.EntityNotFoundException;
import org.sagebionetworks.bridge.models.files.ParticipantFile;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URL;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.bridge.config.Environment.PROD;
import static org.testng.Assert.assertEquals;

public class ParticipantFileServiceTest {

    private static final String UPLOAD_BUCKET = "participant-file.sagebridge.org";
    // private static final String UPLOAD_BUCKET_STAGING = "participant-file-staging.sagebridge.org";
    private static final String DOWNLOAD_URL_1 = "https://participant-file.sagebridge.org/test_user/file_id";
    // private static final String DOWNLOAD_URL_2 = "https://participant-file.sagebridge.org/oneGuid.1422311912486";

    @Mock
    ParticipantFileDao mockFileDao;

    @Mock
    BridgeConfig mockConfig;

    @Mock
    AmazonS3 mockS3Client;

    @InjectMocks
    @Spy
    ParticipantFileService service;

    @BeforeMethod
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);

        when(mockConfig.getHostnameWithPostfix("participant-file")).thenReturn(UPLOAD_BUCKET);
        when(mockConfig.getEnvironment()).thenReturn(PROD);
        service.setConfig(mockConfig);

    }

    @Test
    public void getParticipantFiles() {
        service.getParticipantFiles("test_user", "dummy-key", 20);

        verify(mockFileDao).getParticipantFiles("test_user", "dummy-key", 20);
    }

    @Test
    public void getParticipantFilesNoOffsetKey() {
        service.getParticipantFiles("test_user", null, 20);

        verify(mockFileDao).getParticipantFiles("test_user", null, 20);
    }

    @Test
    public void getParticipantFile() {
        ParticipantFile file = new DynamoParticipantFile("test_user", "file_id");
        file.setAppId("api");
        file.setMimeType("dummy-type");
        file.setCreatedOn(TestConstants.TIMESTAMP);
        file.setDownloadUrl(DOWNLOAD_URL_1);

        when(mockFileDao.getParticipantFile("test_user", "file_id")).thenReturn(Optional.of(file));

        ParticipantFile result = service.getParticipantFile("test_user", "file_id");
        assertEquals(result.getUserId(), "test_user");
        assertEquals(result.getFileId(), "file_id");
        assertEquals(result.getCreatedOn(), TestConstants.TIMESTAMP);
        assertEquals(result.getMimeType(), "dummy-type");
        assertEquals(result.getDownloadUrl(), DOWNLOAD_URL_1);
        assertEquals(result.getAppId(), "api");
    }

    @Test(expectedExceptions = EntityNotFoundException.class)
    public void getParticipantFileNoSuchFile() {
        when(mockFileDao.getParticipantFile(any(), any())).thenReturn(Optional.empty());

        service.getParticipantFile("test_user", "file_id");
    }

    @Test
    public void createParticipantFile() throws Exception {
        URL url = new URL("https://" + UPLOAD_BUCKET);
        when(mockS3Client.generatePresignedUrl(any())).thenReturn(url);

        when(mockFileDao.getParticipantFile(any(), any())).thenReturn(Optional.empty());

        ParticipantFile file = new DynamoParticipantFile("test_user", "file_id");
        file.setMimeType("dummy-type");
        file.setAppId("api");
        file.setCreatedOn(TestConstants.TIMESTAMP);
        ParticipantFile result = service.createParticipantFile(file);
        assertEquals(result.getUserId(), "test_user");
        assertEquals(result.getFileId(), "file_id");
        assertEquals(result.getMimeType(), "dummy-type");
        assertEquals(result.getCreatedOn(), TestConstants.TIMESTAMP);
        assertEquals(result.getUploadUrl(), "https://" + UPLOAD_BUCKET);
        assertEquals(result.getAppId(), "api");
    }

    @Test(expectedExceptions = EntityAlreadyExistsException.class)
    public void createParticipantFileAlreadyExists() {
        ParticipantFile file = new DynamoParticipantFile("test_user", "file_id");
        file.setAppId("api");
        file.setMimeType("dummy-type");
        file.setCreatedOn(TestConstants.TIMESTAMP);
        file.setDownloadUrl(DOWNLOAD_URL_1);

        ParticipantFile newFile = new DynamoParticipantFile("test_user", "file_id");
        newFile.setAppId("not_api");
        newFile.setMimeType("new-dummy-type");
        newFile.setCreatedOn(TestConstants.TIMESTAMP);

        when(mockFileDao.getParticipantFile(eq("test_user"), eq("file_id"))).thenReturn(Optional.of(file));
        service.createParticipantFile(newFile);
    }

    @Test
    public void deleteParticipantFile() {
        ParticipantFile file = new DynamoParticipantFile("test_user", "file_id");
        file.setAppId("api");
        file.setMimeType("dummy-type");
        file.setCreatedOn(TestConstants.TIMESTAMP);
        file.setDownloadUrl(DOWNLOAD_URL_1);

        when(mockFileDao.getParticipantFile(eq("test_user"), eq("file_id"))).thenReturn(Optional.of(file));
        service.deleteParticipantFile("test_user", "file_id");

        verify(mockFileDao).deleteParticipantFile(eq("test_user"), eq("file_id"));
    }

    @Test(expectedExceptions = EntityNotFoundException.class)
    public void deleteParticipantFileButNoSuchFile() {
        when(mockFileDao.getParticipantFile(eq("test_user"), eq("file_id"))).thenReturn(Optional.empty());
        service.deleteParticipantFile("test_user", "file_id");
    }
}
