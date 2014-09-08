package com.offbytwo.jenkins.model;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: eric
 * Date: 04/09/14
 * Time: 08:52
 */
public class MasterConfig extends BaseModel {
  List<String> disabledAdministrativeMonitors;
  String version;
  Integer numExecutors;
  String mode;
  Boolean useSecurity;
  List<String> authorizationStrategy; // TODO
  // SecurityRealm securityRealm; // TODO
  String projectNamingStrategy;
  String workspaceDir;
  String buildsDir;
  // MarkupFormatter markupFormatter //TODO
  List<JDK> jdks;
  List<ComputerConfig> slaves;
  Integer quietPeriod;
  Integer scmCheckoutRetryCount;
  // Views views; // TODO
  String primaryView;
  Integer slaveAgentPort;
  String label;
  NodeProperties nodeProperties;
  NodeProperties globalNodeProperties;

  public static class JDK {
    String name;
    String home;
    List<String> properties;
  }
}
