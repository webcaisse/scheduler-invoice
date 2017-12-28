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

@MessageDriven
@Depends({"jboss.ha:service=HASingletonDeployer,type=Barrier"})
public class EnvelopeTrackingBatchScheduler implements StatefulJob{

	
	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger.getLogger(EnvelopeTrackingBatchScheduler.class);

	@EJB
	private IEmailTrackingService cabestanTrackingService;

	/**
	 * Auto start config.
	 */
	private static final Boolean automaticStart = new SystemProperty("envelopeTrackingBatchScheduler.automaticStart", "true").getBoolean();

	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		try {
			if (automaticStart) {
				LOGGER.info("EnvelopeTrackingBatchScheduler is launched ......");
				// check the status for all batch sent to cabestan
				cabestanTrackingService.checkReports();
			}
		} catch (Exception e) {
			LOGGER.error(e);
		}
	}
}
