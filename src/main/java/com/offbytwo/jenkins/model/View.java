package com.offbytwo.jenkins.model;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: eric
 * Date: 09/09/14
 * Time: 14:12
 */
public class View extends BaseModel {
  List<Job> jobs;
  String description;
  String name;
  List<String> properties;
  String url;

  public List<Job> getJobs() {
    return jobs;
  }

  public void setJobs(List<Job> jobs) {
    this.jobs = jobs;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getProperties() {
    return properties;
  }

  public void setProperties(List<String> properties) {
    this.properties = properties;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
