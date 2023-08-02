package org.jboss.arquillian.drone.webdriver.binary.handler;

import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.ExternalBinarySource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openqa.selenium.MutableCapabilities;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.arquillian.drone.webdriver.binary.handler.LocalBinaryHandler.LOCAL_SOURCE_BINARY_VERSION_PROPERTY;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BinaryHandlerDesiredVersionTestCase {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private static final String FILE_VERSION_SHOULD_EXIST = "should_exist";
    private static final String FILE_VERSION_SHOULD_NOT_EXIST = "should_not_exist";
    private static final String FILE_NAME_TO_DOWNLOAD = "file_to_download";

    private ExternalBinarySource binarySource;

    @Before
    public void setMock() throws Exception {
        binarySource = mock(ExternalBinarySource.class);
        verify(binarySource, never()).getReleaseForVersion(FILE_VERSION_SHOULD_EXIST);
        when(binarySource.getReleaseForVersion(FILE_VERSION_SHOULD_NOT_EXIST)).thenAnswer(invocation -> {
            File toDownload = tmpFolder.newFile(FILE_NAME_TO_DOWNLOAD);
            fillFile(toDownload.getAbsolutePath(), FILE_VERSION_SHOULD_NOT_EXIST.length());
            return new ExternalBinary(FILE_VERSION_SHOULD_NOT_EXIST, toDownload.toURI().toString());
        });
        when(binarySource.getFileNameRegexToDownload(anyString()))
            .thenAnswer(invocation -> invocation.getArguments()[0]);
    }

    @Test
    public void test_when_directory_contains_downloaded_file_binary_source_should_not_be_invoked() throws Exception {
        // given
        HandlerWithTmpFolder handler = prepareHandler(FILE_VERSION_SHOULD_EXIST);
        File existingFile = prepareExistingFile();

        // when
        File file = handler.downloadAndPrepare();

        // then
        assertThat(file).hasName(FILE_VERSION_SHOULD_EXIST).hasSameContentAs(existingFile);
    }

    @Test
    public void test_when_directory_does_not_contain_downloaded_file_binary_source_should_be_invoked() throws Exception {
        // given
        HandlerWithTmpFolder handler = prepareHandler(FILE_VERSION_SHOULD_NOT_EXIST);
        prepareExistingFile();

        // when
        File file = handler.downloadAndPrepare();

        // then
        assertThat(file).hasName(FILE_NAME_TO_DOWNLOAD);
        assertThat(file.length()).isEqualTo(FILE_VERSION_SHOULD_NOT_EXIST.length());
    }

    private HandlerWithTmpFolder prepareHandler(String fileVersionToUse){
        MutableCapabilities capabilities = new MutableCapabilities();
        capabilities.setCapability(LOCAL_SOURCE_BINARY_VERSION_PROPERTY, fileVersionToUse);
        return new HandlerWithTmpFolder(capabilities);
    }

    private File prepareExistingFile() throws IOException {
        File shouldExistFile = tmpFolder.newFile(FILE_VERSION_SHOULD_EXIST);
        fillFile(shouldExistFile.getAbsolutePath(), FILE_VERSION_SHOULD_EXIST.length());
        return shouldExistFile;
    }

    private void fillFile(String absolutePath, int length) throws IOException {
        RandomAccessFile f = new RandomAccessFile(absolutePath, "rw");
        f.setLength(length);
    }

    class HandlerWithTmpFolder extends  LocalBinaryHandler {

        HandlerWithTmpFolder(MutableCapabilities capabilities) {
            super(capabilities);
        }

        protected File createAndGetCacheDirectory(String subdirectory) {
            return tmpFolder.getRoot();
        }

        @Override
        protected ExternalBinarySource getExternalBinarySource() {
            return binarySource;
        }
    }
}
