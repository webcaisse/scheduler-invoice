package fr.maileva.siclv2.invoice.cabestan;

import fr.maileva.siclv2.common.utils.SystemProperty;

public interface CabestanConfiguration {

  /**
   * FTP Connection info.
   */
  String FTP_HOST = System.getProperty("cabestan.server.ftp.host");
  String FTP_PORT = System.getProperty("cabestan.server.ftp.port");
  String FTP_USERNAME = System.getProperty("cabestan.server.ftp.user");
  String FTP_PASSWORD = System.getProperty("cabestan.server.ftp.password");
  String CSV_INPUT_DIRECTORY = System.getProperty("cabestan.server.ftp.csv.directory");
  String PDF_INPUT_DIRECTORY = System.getProperty("cabestan.server.ftp.pdf.directory");
  String CABESTAN_FTP_OUT_DIR = System.getProperty("cabestan.server.ftp.out.directory");

  /**
   * FTP Proxy config.
   */
  String FTP_PROXY_ADRESS =System.getProperty("ftpProxy.address");
  String FTP_PROXY_PORT =System.getProperty("ftpProxy.port");

  /**
   * Environment config (to retrieve only environment specific reports).
   */
  String ENV_NAME = new SystemProperty("cabestan.env.name").getMandatory();

  String CABESTAN_IN_DIR = System.getProperty("cabestan.return.in.dir");
  String CABESTAN_TRACKING_RANGE = System.getProperty("cabestan.tracking.range");

  /**
   * Webservice config.
   */
  String CABESTAN_SERVICE = new SystemProperty("cabestan.base.uri").getMandatory();
  String CABESTAN_LOGIN = new SystemProperty("cabestan.login").getMandatory();
  String CABESTAN_PWD = new SystemProperty("cabestan.pwd").getMandatory();
  String USERS_PATH = new SystemProperty("cabestan.users.path").getMandatory();

  String EMAIL_TEMPLATE_CODE = System.getProperty("cabestan.email.template.code");
}
