package fr.maileva.siclv2.invoice.cabestan;

import javax.ejb.Local;

/**
 * Created by htaghbalout on 29/05/2017.
 */
@Local
public interface IEmailTrackingService {

	/**
	 * Get Cabestan tracking files.
	 * 
	 */
	void getFtpTrackingFiles();

	/**
	 * verifie l'etat des lots envoyés à cabestan
	 * 
	 */
	void checkReports();

}
