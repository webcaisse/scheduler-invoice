package fr.maileva.siclv2.invoice.cabestan.impl;

import static fr.maileva.siclv2.invoice.cabestan.CabestanConfiguration.CABESTAN_FTP_OUT_DIR;
import static fr.maileva.siclv2.invoice.cabestan.CabestanConfiguration.CABESTAN_IN_DIR;
import static fr.maileva.siclv2.invoice.cabestan.CabestanConfiguration.CABESTAN_TRACKING_RANGE;
import static fr.maileva.siclv2.invoice.cabestan.CabestanConfiguration.ENV_NAME;
import static fr.maileva.siclv2.invoice.cabestan.CabestanConfiguration.FTP_HOST;
import static fr.maileva.siclv2.invoice.cabestan.CabestanConfiguration.FTP_PASSWORD;
import static fr.maileva.siclv2.invoice.cabestan.CabestanConfiguration.FTP_PORT;
import static fr.maileva.siclv2.invoice.cabestan.CabestanConfiguration.FTP_PROXY_ADRESS;
import static fr.maileva.siclv2.invoice.cabestan.CabestanConfiguration.FTP_PROXY_PORT;
import static fr.maileva.siclv2.invoice.cabestan.CabestanConfiguration.FTP_USERNAME;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;

import com.docapostdps.utils.common.Collections;
import com.docapostdps.utils.common.remotefs.ConnectionInfo;
import com.docapostdps.utils.common.remotefs.FilenameFilter;
import com.docapostdps.utils.common.remotefs.HostInfo;
import com.docapostdps.utils.common.remotefs.ProxyInfo;
import com.docapostdps.utils.common.remotefs.ProxyType;
import com.docapostdps.utils.common.remotefs.RemoteEntry;
import com.docapostdps.utils.common.remotefs.RemoteFsClient;
import com.docapostdps.utils.common.remotefs.RemoteFsProtocol;
import com.docapostdps.utils.common.remotefs.impl.RemoteFsClientFactory;
import com.google.common.base.Joiner;

import fr.maileva.siclv2.business.dao.IBatchCategoryPaperDao;
import fr.maileva.siclv2.business.dao.IBatchDao;
import fr.maileva.siclv2.business.dao.IRequestPaperDao;
import fr.maileva.siclv2.business.entities.RequestPaper;
import fr.maileva.siclv2.business.entities.batch.Batch;
import fr.maileva.siclv2.business.entities.batch.Batch.BatchState;
import fr.maileva.siclv2.business.entities.batch.BatchCategory;
import fr.maileva.siclv2.business.entities.batch.BatchCategoryEnum;
import fr.maileva.siclv2.business.enums.Partner;
import fr.maileva.siclv2.business.managers.api.IProcessEngineHelper;
import fr.maileva.siclv2.common.utils.ProcessEngineUtils.ProcessEngineCommand;
import fr.maileva.siclv2.common.utils.ServiceLocator;
import fr.maileva.siclv2.common.utils.exception.TechnicalErrorCode;
import fr.maileva.siclv2.common.utils.exception.TechnicalException;
import fr.maileva.siclv2.invoice.cabestan.IEmailTrackingService;

/**
 * Created by htaghbalout on 29/05/2017. Service that treat cabestan tracking
 * status
 */
@Stateless
public class EmailTrackingServiceImpl implements IEmailTrackingService {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger.getLogger(EmailTrackingServiceImpl.class);

	private RemoteFsClient client;
	
	@EJB
	private IBatchCategoryPaperDao batchCategoryPaperDao;
	
	@EJB
	private IBatchDao batchDao;
	
	@EJB
	private IRequestPaperDao requestPaperDao;

	@EJB
	private IProcessEngineHelper processEngineHelper;


	@PostConstruct
	public void init() {
		// Connection parameters
		ConnectionInfo connectionInfo = new ConnectionInfo();
		connectionInfo.setProtocol(RemoteFsProtocol.FTP);
		connectionInfo.setUsername(FTP_USERNAME);
		connectionInfo.setPassword(FTP_PASSWORD);

		HostInfo server = new HostInfo();
		server.setHostname(FTP_HOST);
		server.setPort(new Integer(FTP_PORT));
		connectionInfo.setServer(server);

		if (FTP_PROXY_ADRESS != null) {
			LOGGER.debug("FTP PROXY ADRESS USED :" + FTP_PROXY_ADRESS);
			ProxyInfo proxyInfo = new ProxyInfo();

			HostInfo proxyHost = new HostInfo();

			proxyHost.setHostname(FTP_PROXY_ADRESS);
			proxyHost.setPort(new Integer(FTP_PROXY_PORT));
			proxyInfo.setHost(proxyHost);
			proxyInfo.setType(ProxyType.FTP);
			connectionInfo.setProxyInfo(proxyInfo);
		}

		client = RemoteFsClientFactory.createClient(connectionInfo);
		LOGGER.debug("Connexion established to Serrver FTP Cabestan");
	}

	@Override
	public void getFtpTrackingFiles() {
		LOGGER.info("get Ftp file from cabestan ftp");
		try {
			client.connect();
			LOGGER.info("UserFileTransferAccountSendingService : start send ");

			List<RemoteEntry> ftpFilesLst = getFiltredFilesList();
			RemoteEntry[] ftpFiles = ftpFilesLst
					.toArray(new RemoteEntry[ftpFilesLst.size()]);
			LOGGER.debug("we have found " + ftpFiles.length
					+ "  ftp files found to treat");
			if (ftpFiles.length > 0) {
				int range = Integer.parseInt(CABESTAN_TRACKING_RANGE);
				double size = ftpFiles.length * 1.0 / range;
				LOGGER.debug("ftpFiles size " + size);

				if (size <= 1) {
					treatFiles(ftpFiles);
				} else {
					// ranges
					for (int i = 0; i < ((int) size) * range; i = i + range) {
						RemoteEntry[] rangeFtpFiles = Arrays.copyOfRange(
								ftpFiles, i, i + range);
						LOGGER.debug("rangeFtpFiles " + rangeFtpFiles);
						treatFiles(rangeFtpFiles);
					}
					// The rest
					RemoteEntry[] rangeFtpFiles = Arrays.copyOfRange(ftpFiles,
							((int) size) * range, ftpFiles.length);
					treatFiles(rangeFtpFiles);
				}
			}
		} catch (TechnicalException e) {
			LOGGER.error(TechnicalErrorCode.GENERAL, e);
		} catch (IOException e) {
			LOGGER.error(TechnicalErrorCode.FILE_TRANSFER_ERROR, e);
		} finally {
			client.disconnect();
		}
	}

	
	@SuppressWarnings("rawtypes")
	@Override
	public void checkReports() {
		try {
			
			BatchCategory<RequestPaper> batchCategoryInvoice = batchCategoryPaperDao.getBatchCategory(BatchCategoryEnum.ELECTRONIC_INVOICE, Partner.MAILEVA);
			
			List<Batch> batchsPaper  = batchDao.loadBatchesByCategoryAndState(batchCategoryInvoice, BatchState.SENT);	
			
			if (!Collections.isEmpty(batchsPaper)){
				
				for (Batch batchEntity : batchsPaper) {
				
					for (RequestPaper requestPaper : requestPaperDao.loadRequestsForBatch(batchEntity.getId())) {
				    	Integer result = requestPaperDao.updateRequestStatusToProducedWhenAllEnvelopesStatusProduced(requestPaper.getId());
				    	
			    		LOGGER.debug("Updating request "+requestPaper.getId()+" - set status to PRODUCED (if all envelopes PRODUCED) => number of updated rows = "+result);				    	
				    	
			    		if (result > 0) {
				    		// the batch is produced
							batchEntity.setCurrentState(BatchState.PRODUCED);
					    	batchDao.save(batchEntity);
					    	
				    		LOGGER.info("Reloading process engine for request "+requestPaper.getId());
				    		processEngineHelper.executeRequestProcess(requestPaper, ProcessEngineCommand.SIGNAL_EXISTING_PROCESS);
				    	}
				    }
				}
			}
		} catch (TechnicalException e) {
			LOGGER.error(TechnicalErrorCode.GENERAL, e);
		}

	}
	private void treatFiles(RemoteEntry[] ftpFiles) throws TechnicalException {
		try {
			List<String> listNames = new ArrayList<String>();
			for (RemoteEntry ftpFile : ftpFiles) {
				LOGGER.debug("ftpFile.getName() " + ftpFile.getBasename());
				String locName = CABESTAN_IN_DIR + ftpFile.getBasename();
				LOGGER.debug("cabestan file name " + locName);
				File file = new File(locName);
				client.download(CABESTAN_FTP_OUT_DIR + ftpFile.getBasename(),
						file);
				listNames.add(ftpFile.getBasename());
				client.delete(ftpFile.getPath());
			}

			LOGGER.debug("list file to track are " + listNames.size());
			
			sendJMSMessage(listNames);
			
		} catch (Exception e) {
			throw new TechnicalException(e,
					TechnicalErrorCode.FILE_TRANSFER_ERROR);
		}
	}

	public void sendJMSMessage(Collection<?> listNames)
			throws TechnicalException {
		if (!listNames.isEmpty()) {
		
			String names = Joiner.on(";").join(listNames);
			LOGGER.debug("Send JMS message for files : " + names);
			InitialContext initialContext = null;
			ConnectionFactory connectionFactory;
			Connection connection = null;
			Queue destination = null;
			Session session = null;
			MessageProducer producer = null;
			try {
				initialContext = new InitialContext(properties);
				connectionFactory = (ConnectionFactory) ServiceLocator.getService(
						initialContext, "ConnectionFactory");
				connection = connectionFactory.createConnection();
				session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
				destination = (Queue) ServiceLocator.getService(initialContext,
						"queue/cabestanReturnedFilesQueue");
				producer = session.createProducer(destination);
				TextMessage message = session.createTextMessage();
				message.setStringProperty("names", names);
				producer.send(message);
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new TechnicalException(e,
						TechnicalErrorCode.JMS_MESSAGE_SENDING);
			} finally {
				// close queue sender
				if (producer != null) {
					try {
						producer.close();
					} catch (JMSException e) {
						LOGGER.error(e.getMessage(), e);
					}
				}
				if (session != null) {
					try {
						session.close();
					} catch (JMSException e) {
						LOGGER.error(e.getMessage(), e);
					}
				}
				if (connection != null) {
					try {
						connection.close();
					} catch (JMSException e) {
						LOGGER.error(e.getMessage(), e);
					}
				}
			}
		}		
	}

	
	private List<RemoteEntry> getFiltredFilesList() throws IOException {
		if (ENV_NAME != null) {
			return client.listFilesWithFilter(CABESTAN_FTP_OUT_DIR,
					new FilenameFilter() {
						@Override
						public boolean accept(String filename) {
							return filename.contains(ENV_NAME);
						}
					});
		} else {
			return java.util.Collections.emptyList();
		}
	}


	private static Properties properties = new Properties();

	static {
		properties.put(Context.INITIAL_CONTEXT_FACTORY,
				"org.jnp.interfaces.NamingContextFactory");
		properties.put(Context.URL_PKG_PREFIXES,
				"jboss.naming:org.jnp.interfaces");
		String partitionName = System.getProperty("jboss.partition.name",
				"DefaultPartition");
		properties.put("jnp.partitionName", partitionName);
	}

}