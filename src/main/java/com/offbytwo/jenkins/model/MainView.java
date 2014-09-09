/*
 * Copyright (c) 2013 Rising Oak LLC.
 *
 * Distributed under the MIT license: http://opensource.org/licenses/MIT
 */

package com.offbytwo.jenkins.model;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainView extends BaseModel {
    private List<Job> jobs;
    private List<View> views;
  private String description;
  private String mode;
  private String nodeDescription;
  private String nodeName;
  private int numExecutors;
  private View primaryView;
  private boolean quietingDown;
  private int slaveAgentPort;
  private boolean useCrumbs;
  private boolean useSecurity;

  /*
   * TODO : Missing properties
   * overallLoad
   * unlabeledLoad
   * assignedLabels
   */


    /* default constructor needed for Jackson */
    public MainView() {
        this(Lists.<Job>newArrayList());
    }

    public MainView(List<Job> jobs) {
        this.jobs = jobs;
      this.views = new ArrayList<View>();
    }

    public MainView(Job... jobs) {
        this(Arrays.asList(jobs));
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

  public List<View> getViews() {
    return views;
  }

  public void setViews(List<View> views) {
    this.views = views;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getNodeDescription() {
    return nodeDescription;
  }

  public void setNodeDescription(String nodeDescription) {
    this.nodeDescription = nodeDescription;
  }

  public String getNodeName() {
    return nodeName;
  }

  public void setNodeName(String nodeName) {
    this.nodeName = nodeName;
  }

  public int getNumExecutors() {
    return numExecutors;
  }

  public void setNumExecutors(int numExecutors) {
    this.numExecutors = numExecutors;
  }

  public View getPrimaryView() {
    return primaryView;
  }

  public void setPrimaryView(View primaryView) {
    this.primaryView = primaryView;
  }

  public boolean isQuietingDown() {
    return quietingDown;
  }

  public void setQuietingDown(boolean quietingDown) {
    this.quietingDown = quietingDown;
  }

  public int getSlaveAgentPort() {
    return slaveAgentPort;
  }

  public void setSlaveAgentPort(int slaveAgentPort) {
    this.slaveAgentPort = slaveAgentPort;
  }

  public boolean isUseCrumbs() {
    return useCrumbs;
  }

  public void setUseCrumbs(boolean useCrumbs) {
    this.useCrumbs = useCrumbs;
  }

  public boolean isUseSecurity() {
    return useSecurity;
  }

  public void setUseSecurity(boolean useSecurity) {
    this.useSecurity = useSecurity;
  }
}
