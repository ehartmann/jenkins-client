package com.offbytwo.jenkins.model;

/**
 * Created by IntelliJ IDEA.
 * User: eric
 * Date: 04/09/14
 * Time: 08:52
 */
public class ComputerConfig extends BaseModel {
  String name;
  String description;
  String remoteFS;
  int numExecutors;
  String mode;
  String retentionStrategy; // TODO : attribute
  String launcher; // TODO : attribute
  String label; // TODO : split
  NodeProperties nodeProperties;
  String userId;

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getRemoteFS() {
    return remoteFS;
  }

  public int getNumExecutors() {
    return numExecutors;
  }

  public String getMode() {
    return mode;
  }

  public String getRetentionStrategy() {
    return retentionStrategy;
  }

  public String getLauncher() {
    return launcher;
  }

  public String getLabel() {
    return label;
  }

  public NodeProperties getNodeProperties() {
    return nodeProperties;
  }
}
