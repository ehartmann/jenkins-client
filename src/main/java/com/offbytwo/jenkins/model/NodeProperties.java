package com.offbytwo.jenkins.model;

/**
 * Created by IntelliJ IDEA.
 * User: eric
 * Date: 04/09/14
 * Time: 08:55
 */
public class NodeProperties {

  EnvironmentVariables environmentVariables;
  ToolLocation toolLocation;

  public EnvironmentVariables getEnvironmentVariables() {
    return environmentVariables;
  }

  public ToolLocation getToolLocation() {
    return toolLocation;
  }

  public static class EnvironmentVariables {

  }

  public static class ToolLocation {

  }
}
