package fr.maileva.siclv2.cabestan.impl;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import fr.maileva.siclv2.invoice.cabestan.impl.EmailTrackingServiceImpl;


/**
 * Created by htaghbalout on 19/07/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class CabestanServiceTest {

  @InjectMocks
  private EmailTrackingServiceImpl cabestanService;

  @Mock
  private Queue queue;

  @Mock
  private ConnectionFactory connectionFactory;


  @Before
  public void setUp() throws Exception {
    System.setProperty("CABESTAN_FTP_OUT_DIR",  "/Users/synchro/out");
    System.setProperty("CABESTAN_IN_DIR",  "/Users/synchro/in");
    System.setProperty("CABESTAN_TRACKING_RANGE",  "5");
    System.setProperty("cabestan.server.ftp.host","ftp.cabestan.com");
    System.setProperty("cabestan.server.ftp.port","21");
    System.setProperty("cabestan.server.ftp.user", "mailevacible");
    System.setProperty("cabestan.server.ftp.password", "k7@fE=b0Dy");
    System.setProperty("cabestan.env.name", "local");
    cabestanService.init();

  }

  @Test
  public void test_save_ftp_file_ok() throws Exception {
    cabestanService.getFtpTrackingFiles();
  }

}
