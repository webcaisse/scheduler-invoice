package fr.maileva.siclv2.invoice.cabestan.scheduler;

import javax.ejb.EJB;
import javax.ejb.MessageDriven;

import org.apache.log4j.Logger;
import org.jboss.annotation.ejb.Depends;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import fr.maileva.siclv2.common.utils.SystemProperty;
import fr.maileva.siclv2.invoice.cabestan.IEmailTrackingService;

/**
 * Created by htaghbalout on 24/05/2017.
 */
@MessageDriven
@Depends({"jboss.ha:service=HASingletonDeployer,type=Barrier"})
public class FtpTrackingBatchScheduler implements StatefulJob {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger.getLogger(FtpTrackingBatchScheduler.class);

	@EJB
	private IEmailTrackingService cabestanTrackingService;

	/**
	 * Auto start config.
	 */
	private static final Boolean automaticStart = new SystemProperty("ftpTrackingBatchScheduler.automaticStart", "true").getBoolean();

	/**
	 * Implementation method
	 */
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {

		try {
			if (automaticStart) {
				LOGGER.info("FtpTrackingBatchScheduler is launched ......");
				// get the files from the ftp serveur and send JMS message to service-tracking for handling
				cabestanTrackingService.getFtpTrackingFiles();
			}
		} catch (Exception e) {
			LOGGER.error(e);
		}
	
	}
}
