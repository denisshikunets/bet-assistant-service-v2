package com.breedish.bet;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE;

/**
 * @author zenind
 */
@Component
public class FtpUploader {

    private static final Logger LOG = LoggerFactory.getLogger(FtpUploader.class);

    @Autowired
    private BetProperties betProperties;

    public void uploadData(String data, String filename) {
        LOG.info(format("Start upload of %s", filename));
        StopWatch watch = new StopWatch();
        watch.start();

        FTPClient client = new FTPClient();
        InputStream is = new ByteArrayInputStream(data.getBytes(UTF_8));
        try {
            client.connect(betProperties.getFtpServer());
            client.enterLocalPassiveMode();
            client.setFileType(BINARY_FILE_TYPE);
            client.login(betProperties.getFtpUser(), betProperties.getFtpPassword());
            client.storeFile(new File(betProperties.getFtpDataDirectory(), filename).toString(), is);
            client.logout();
        } catch (IOException e) {
            LOG.error(String.format("Error during upload %s. Reason: %s", filename, e));
        } finally {
            try {
                is.close();
                client.disconnect();
            } catch (Exception e) {
                LOG.error(String.format("Error during upload %s. Reason: %s", filename, e));
            }
        }
        watch.stop();
        LOG.info(format("Upload of %s was done in %s", filename, watch.getLastTaskInfo().getTimeMillis()));
    }
}
