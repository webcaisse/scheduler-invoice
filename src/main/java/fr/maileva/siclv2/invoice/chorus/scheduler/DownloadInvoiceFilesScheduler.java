package fr.maileva.siclv2.invoice.chorus.scheduler;

import javax.ejb.EJB;
import javax.ejb.MessageDriven;

import org.apache.log4j.Logger;
import org.jboss.annotation.ejb.Depends;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import fr.maileva.siclv2.common.utils.SystemProperty;
import fr.maileva.siclv2.invoice.chorus.IChorusTrackingService;


@MessageDriven
@Depends({"jboss.ha:service=HASingletonDeployer,type=Barrier"})
public class DownloadInvoiceFilesScheduler implements StatefulJob {
	
	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger.getLogger(DownloadInvoiceFilesScheduler.class);
	

	/**
	 * Auto start config.
	 */
	private static final Boolean automaticStart = new SystemProperty("downloadInvoiceFilesScheduler.automaticStart", "true").getBoolean();

	@EJB
	private IChorusTrackingService chorusTrackingService;


	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		try {
			if (automaticStart) {
				LOGGER.info("DownloadInvoiceFilesScheduler is launched ......");
				chorusTrackingService.getSignedInvoice();
			}
		} catch (Exception e) {
			LOGGER.error(e);
		}
	}
}
