package demo;

public class Config {

  private int serverPort;
  private String webHost;
  private String templatePath;
  private String slackCallbackUrl;
  private String client_id;
  private String client_secret;
  private String db_address;
  private String db_username;
  private String db_password;

  public int getServerPort() {
    return serverPort;
  }

  public String getWebHost() {
    return webHost;
  }

  public String getTemplatePath() {
    return templatePath;
  }

  public String getSlackCallbackUrl() {
    return slackCallbackUrl;
  }

  public String getClient_id() {
    return client_id;
  }

  public String getClient_secret() {
    return client_secret;
  }

  public String getDb_address() {
    return db_address;
  }

  public String getDb_username() {
    return db_username;
  }

  public String getDb_password() {
    return db_password;
  }
}
