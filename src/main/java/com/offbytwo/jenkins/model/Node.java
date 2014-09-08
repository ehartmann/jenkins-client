package com.offbytwo.jenkins.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: eric
 * Date: 18/06/14
 * Time: 08:55
 */
public class Node {
  private String name;
  private String type = "hudson.slaves.DumbSlave$DescriptorImpl";
  private String nodeDescription;
  private String remoteFS;
  private String labelString;
  private int numExecutors;
  private String mode = "NORMAL"; // NORMAL or EXCLUSIVE
  private RetentionStrategy retentionStrategy = new RetentionStrategy();
  private NodeProperties nodeProperties = new NodeProperties();
  private Launcher launcher = new Launcher();

  public Node() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }


  public String getNodeDescription() {
    return nodeDescription;
  }

  public void setNodeDescription(String nodeDescription) {
    this.nodeDescription = nodeDescription;
  }

  public String getRemoteFS() {
    return remoteFS;
  }

  public void setRemoteFS(String remoteFS) {
    this.remoteFS = remoteFS;
  }

  public String getLabelString() {
    return labelString;
  }

  public void setLabelString(String labelString) {
    this.labelString = labelString;
  }

  public int getNumExecutors() {
    return numExecutors;
  }

  public void setNumExecutors(int numExecutors) {
    this.numExecutors = numExecutors;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public RetentionStrategy getRetentionStrategy() {
    return retentionStrategy;
  }

  public void setRetentionStrategy(RetentionStrategy retentionStrategy) {
    this.retentionStrategy = retentionStrategy;
  }

  public NodeProperties getNodeProperties() {
    return nodeProperties;
  }

  public void setNodeProperties(NodeProperties nodeProperties) {
    this.nodeProperties = nodeProperties;
  }

  public Launcher getLauncher() {
    return launcher;
  }

  public void setLauncher(Launcher launcher) {
    this.launcher = launcher;
  }

  @JsonIgnore
  public void setGitLocation(String name, String location) {
    nodeProperties.getToolLocations().addVariable("hudson.plugins.git.GitTool$DescriptorImpl@" + name, location);
  }

  @JsonIgnore
  public void setJDK6Location(String name, String location) {
    nodeProperties.getToolLocations().addVariable("hudson.model.JDK$DescriptorImpl@" + name, location);
  }

  @JsonIgnore
  public void setMaven305Location(String name, String location) {
    nodeProperties.getToolLocations().addVariable("hudson.tasks.Maven$MavenInstallation$DescriptorImpl@" + name, location);
  }

  public static class RetentionStrategy {
    private String staplerClass = "hudson.slaves.RetentionStrategy$Always";

    @JsonProperty("stapler-class")
    public String getStaplerClass() {
      return staplerClass;
    }

    public void setStaplerClass(String staplerClass) {
      this.staplerClass = staplerClass;
    }
  }

  public static class NodeProperties {
    private String staplerClassBag = "true";
    private EnvironmentVariables environmentVariables = new EnvironmentVariables();
    private ToolLocations toolLocations = new ToolLocations();

    @JsonProperty("stapler-class-bag")
    public String getStaplerClassBag() {
      return staplerClassBag;
    }

    public void setStaplerClassBag(String staplerClassBag) {
      this.staplerClassBag = staplerClassBag;
    }

    @JsonIgnore
    public void addVariable(String key, String value) {
      environmentVariables.addVariable(key, value);
    }

    @JsonProperty("hudson-slaves-EnvironmentVariablesNodeProperty")
    public EnvironmentVariables getEnvironmentVariables() {
      return environmentVariables;
    }

    public void setEnvironmentVariables(EnvironmentVariables environmentVariables) {
      this.environmentVariables = environmentVariables;
    }

    @JsonProperty("hudson-tools-ToolLocationNodeProperty")
    public ToolLocations getToolLocations() {
      return toolLocations;
    }

    public void setToolLocations(ToolLocations toolLocations) {
      this.toolLocations = toolLocations;
    }
  }

  public static class EnvironmentVariables {
    private List<EnvKeyValue> env = new ArrayList<EnvKeyValue>();

    public List<EnvKeyValue> getEnv() {
      return env;
    }

    public void setEnv(List<EnvKeyValue> env) {
      this.env = env;
    }

    @JsonIgnore
    public void addVariable(String key, String value) {
      env.add(new EnvKeyValue(key, value));
    }
  }

  public static class ToolLocations {
    private List<ToolKeyValue> locations = new ArrayList<ToolKeyValue>();

    public List<ToolKeyValue> getLocations() {
      return locations;
    }

    public void setLocations(List<ToolKeyValue> locations) {
      this.locations = locations;
    }

    @JsonIgnore
    public void addVariable(String key, String value) {
      locations.add(new ToolKeyValue(key, value));
    }
  }

  public static class EnvKeyValue {
    private String key;
    private String value;

    public EnvKeyValue() {
    }

    public EnvKeyValue(String key, String value) {
      this.key = key;
      this.value = value;
    }

    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }
  }

  public static class ToolKeyValue {
    private String key;
    private String home;

    public ToolKeyValue() {
    }

    public ToolKeyValue(String key, String home) {
      this.key = key;
      this.home = home;
    }

    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    public String getHome() {
      return home;
    }

    public void setHome(String home) {
      this.home = home;
    }
  }
  public static class Launcher {
    private String staplerClass = "hudson.slaves.JNLPLauncher";

    @JsonProperty("stapler-class")
    public String getStaplerClass() {
      return staplerClass;
    }

    public void setStaplerClass(String staplerClass) {
      this.staplerClass = staplerClass;
    }
  }
}
