package fr.maileva.siclv2.invoice.chorus.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;

import webservice.ApiWebResource;
import webservice.RestClient;

import com.docapostdps.utils.common.auth.AdminTokenLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import fr.maileva.siclv2.business.dao.IBatchCategoryPaperDao;
import fr.maileva.siclv2.business.dao.IBatchDao;
import fr.maileva.siclv2.business.dao.IRequestPaperDao;
import fr.maileva.siclv2.business.entities.RequestPaper;
import fr.maileva.siclv2.business.entities.batch.Batch;
import fr.maileva.siclv2.business.entities.batch.Batch.BatchState;
import fr.maileva.siclv2.business.entities.batch.BatchCategory;
import fr.maileva.siclv2.business.entities.batch.BatchCategoryEnum;
import fr.maileva.siclv2.business.entities.batch.BatchFile;
import fr.maileva.siclv2.business.enums.Partner;
import fr.maileva.siclv2.business.managers.api.IProcessEngineHelper;
import fr.maileva.siclv2.common.utils.ProxyHelper;
import fr.maileva.siclv2.common.utils.SystemProperty;
import fr.maileva.siclv2.common.utils.exception.TechnicalErrorCode;
import fr.maileva.siclv2.common.utils.exception.TechnicalException;
import fr.maileva.siclv2.invoice.chorus.IChorusTrackingService;
import fr.maileva.siclv2.invoice.chorus.beans.DownloadInvoice;

@Stateless
public class ChorusTrackingServiceImpl implements IChorusTrackingService {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger.getLogger(ChorusTrackingServiceImpl.class);
	
	private final String AUTH_SERVER_CLIENT_ID = System.getProperty("oauth2.token.invoice.service.authentification.client_id");
	private final String AUTH_SERVER_CLIENT_SECRET = System.getProperty("oauth2.token.invoice.service.authentification.client_secret");
	private final String AUTH_SERVER_GRANT_TYPE = System.getProperty("oauth2.token.invoice.service.generator.grant_type");
	private final String AUTH_TOCKENADMIN_SERVICE_GENERATOR_PATH = System.getProperty("oauth2.token.invoice.service.generator.path");
	
	private final String CERTIFICAT_PASSWORD = new SystemProperty("invoices.api.certificate.password").getMandatory();
	private final SystemProperty CERTIFICAT_FILENAME = new SystemProperty("invoices.api.certificate.fileName");
	private final String SERVER_URI = new SystemProperty("invoices.api.server.uri").getMandatory();
	private final SystemProperty READ_TIMEOUT = new SystemProperty("invoices.api.readTimeout", "6000");
	

	@EJB
	private IBatchCategoryPaperDao batchCategoryPaperDao;

	@EJB
	private IBatchDao batchDao;
	
	@EJB
	private IRequestPaperDao requestPaperDao;
	
	@EJB
	private IProcessEngineHelper processEngineHelper;
	
	private RestClient restClient;
	
	
	@PostConstruct
	private void init(){
		ProxyHelper.setCustomProxy();	
		restClient = new ApiWebResource(SERVER_URI,CERTIFICAT_FILENAME.getFile(),CERTIFICAT_PASSWORD,READ_TIMEOUT.getInteger()).getWebServiceResource(Collections.singletonList(new JacksonJsonProvider()));
		restClient.type(MediaType.APPLICATION_JSON);
	}
	

	@Override
	public void getSignedInvoice() {

		try {

			BatchCategory<RequestPaper> batchCategoryInvoice = batchCategoryPaperDao.getBatchCategory(BatchCategoryEnum.CHORUS_INVOICE,	Partner.MAILEVA);
			List<Batch> batchsPaper = batchDao.loadBatchesByCategoryAndState(batchCategoryInvoice, BatchState.SENT);
			
			for (Batch batchEntity : batchsPaper) {
				
//				for (BatchFile batchFile : batchEntity.getBatchFiles()) {
//					
//				}
//				RequestPaper requestPaper  = Iterables.getFirst( requestPaperDao.loadRequestsForBatch(batchEntity.getId()), null);
//				Preconditions.checkNotNull(requestPaper);
//				
//						
//				if (requestPaperDao.countEnvelopeByNotInStateAndRequestId(requestPaper.getId(), EnvelopeState.PRODUCED)==0) {
//		    		// the batch is produced
//					LOGGER.info("Setting batch state to PRODUCED [batchId] :"+batchEntity.getId());
//					batchEntity.setCurrentState(BatchState.PRODUCED);
//			    	batchDao.save(batchEntity);
//			    	LOGGER.info("Setting request state to PRODUCED [requestId] :"+requestPaper.getId());
//			    	requestPaper.setCurrentState(RequestState.PRODUCED);
//			    	requestPaperDao.saveWithNewTransaction(requestPaper);
//			    	LOGGER.info("Reloading process engine for request "+requestPaper.getId());
//		    		processEngineHelper.executeRequestProcess(requestPaper, ProcessEngineCommand.SIGNAL_EXISTING_PROCESS);
//		    	}

			}
			
		} catch (TechnicalException e) {
			LOGGER.error(TechnicalErrorCode.GENERAL, e);
		}
	}
	
	
	private void getArchStatusWithPartnerTrackId(String partnerTrackId)
			throws TechnicalException {

//		try {
//			LOGGER.debug("URI Of RestClient :  " + restClient.getCurrentURI()+", partnerId "+partnerTrackId);
//			restClient.header("Authorization",
//					"Bearer " + AdminTokenLoader.getInstance()
//							.computeToken(AUTH_TOCKENADMIN_SERVICE_GENERATOR_PATH, AUTH_SERVER_CLIENT_ID, AUTH_SERVER_CLIENT_SECRET, AUTH_SERVER_GRANT_TYPE));
//
//			restClient.path(partnerTrackId).get();
//
//			Response response = restClient.getResponse();
//			LOGGER.debug("response status " + response.getStatus()+" for partnerTrackId "+partnerTrackId);
//
//			if (HttpStatus.SC_OK == response.getStatus()) {
//
//				DownloadInvoice downloadInvoice = new ObjectMapper()
//						.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES)
//						.readValue((InputStream) response.getEntity(),DownloadInvoice.class);
//
//				StatusEnum valueOf = StatusEnum.valueOf(archiveOut.getStatus());
//				if (StatusEnum.ERROR == valueOf) {
//					LOGGER.error("archiving process with error " + restClient.getCurrentURI());
//				}
//				return valueOf;
//
//			} else {
//				LOGGER.error("archive not found: "+response.getStatus()+ "  => " + restClient.getCurrentURI());
//				return StatusEnum.ERROR;
//			}
//
//		} catch (IOException e) {
//			throw new TechnicalException(e, TechnicalErrorCode.FILESYSTEM_ACCESS);
//		} catch (Exception e) {
//			throw new TechnicalException("general exception", e, TechnicalErrorCode.GENERAL);
//		}
	}

}