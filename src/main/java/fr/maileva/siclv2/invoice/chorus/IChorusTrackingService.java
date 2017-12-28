package fr.maileva.siclv2.invoice.chorus;

import javax.ejb.Local;

@Local
public interface IChorusTrackingService {

	/**
	 * recuperer les factures signées envoyées à SERES
	 */
	void getSignedInvoice() ;

}
