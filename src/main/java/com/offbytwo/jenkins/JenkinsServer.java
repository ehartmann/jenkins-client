/*
 * Copyright (c) 2013 Rising Oak LLC.
 *
 * Distributed under the MIT license: http://opensource.org/licenses/MIT
 */

package com.offbytwo.jenkins;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.offbytwo.jenkins.client.JenkinsHttpClient;
import com.offbytwo.jenkins.model.*;
import com.offbytwo.jenkins.tools.Utils;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpResponseException;
import org.apache.http.message.BasicNameValuePair;
import org.jdom2.Content;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The main starting point for interacting with a Jenkins server.
 */
public class JenkinsServer {
    private final JenkinsHttpClient client;
    private final static XPathFactory XPATH_FACTORY = XPathFactory.instance();
    private final static XPathExpression<Content> JOB_NAMES = XPATH_FACTORY.compile("/hudson.model.ListView/jobNames/string//text()", Filters.content());
    /**
     * Create a new Jenkins server reference given only the server address
     *
     * @param serverUri address of jenkins server (ex. http://localhost:8080/jenkins)
     */
    public JenkinsServer(URI serverUri) {
        this(new JenkinsHttpClient(serverUri));
    }

    /**
     * Create a new Jenkins server reference given the address and credentials
     *
     * @param serverUri address of jenkins server (ex. http://localhost:8080/jenkins)
     * @param username username to use when connecting
     * @param passwordOrToken password (not recommended) or token (recommended)
     */
    public JenkinsServer(URI serverUri, String username, String passwordOrToken) {
        this(new JenkinsHttpClient(serverUri, username, passwordOrToken));
    }

    /**
     * Create a new Jenkins server directly from an HTTP client (ADVANCED)
     *
     * @param client Specialized client to use.
     */
    public JenkinsServer(JenkinsHttpClient client) {
        this.client = client;
    }

    /**
     * Get the current status of the Jenkins end-point by pinging it.
     *
     * @return true if Jenkins is up and running, false otherwise
     */
    public boolean isRunning() {
        try {
            client.get("/");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Get a list of all the defined jobs on the server (at the summary level)
     *
     * @return list of defined jobs (summary level, for details @see Job#details
     * @throws IOException
     */
    public Map<String, Job> getJobs() throws IOException {
        List<Job> jobs = client.get("/", MainView.class).getJobs();
        return Maps.uniqueIndex(jobs, new Function<Job, String>() {
            @Override
            public String apply(Job job) {
                job.setClient(client);
                return job.getName().toLowerCase();
            }
        });
    }

    /**
     * Get a single Job from the server.
     *
     * @return A single Job, null if not present
     * @throws IOException
     */
    public JobWithDetails getJob(String jobName) throws  IOException {
        try {
            JobWithDetails job = client.get("/job/"+encode(jobName),JobWithDetails.class);
            if (job != null) {
              job.setClient(client);
            }

            return job;
        } catch (HttpResponseException e) {
            throw e;
        }

    }

    public MavenJobWithDetails getMavenJob(String jobName) throws IOException {
        try {
            MavenJobWithDetails job = client.get("/job/"+encode(jobName), MavenJobWithDetails.class);
            job.setClient(client);

            return job;
        } catch (HttpResponseException e) {
            if(e.getStatusCode() == 404) {
                return null;
            }
            throw e;
        }
    }

    /**
     * Create a job on the server using the provided xml
     *
     * @throws IOException
     */
    public void createJob(String jobName, String jobXml) throws IOException {
        client.post_xml("/createItem?name=" + encode(jobName), jobXml);
    }

    /**
     * Get the xml description of an existing job
     *
     * @return the new job object
     * @throws IOException
     */
    public String getJobXml(String jobName) throws IOException {
        return client.get("/job/" + encode(jobName) + "/config.xml");
    }

    /**
     * Get the xml description of an existing promotion
     *
     * @return the new job object
     * @throws IOException
     */
    public String getPromotionXml(String jobName, String promotionName) throws IOException {
        return client.get("/job/" + encode(jobName) + "/promotion/process/" + encode(promotionName) + "/config.xml");
    }

    /**
     * Create a job on the server using the provided xml
     *
     * @throws IOException
     */
    public void updatePromotionXML(String jobName, String promotionName, String jobXml) throws IOException {
        client.post_xml("/job/" + encode(jobName) + "/promotion/process/" + encode(promotionName) + "/config.xml", jobXml);
    }

    /**
     * Create a job on the server using the provided xml
     *
     * @throws IOException
     */
    public String createPromotionXML(String jobName, String promotionName, String jobXml) throws IOException {
        return client.post_xml("/job/" + encode(jobName) + "/promotion/createProcess?name=" + encode(promotionName), jobXml);
    }

    /**
     * Get the description of an existing Label
     *
     * @return label object
     * @throws IOException
     */
    public LabelWithDetails getLabel(String labelName) throws IOException {
        return client.get("/label/" + encode(labelName), LabelWithDetails.class);
    }


    /**
     * Get a list of all the computers on the server (at the summary level)
     *
     * @return list of defined computers (summary level, for details @see Computer#details
     * @throws IOException
     */
    public Map<String, Computer> getComputers() throws IOException {
        List<Computer> computers = client.get("computer/", Computer.class).getComputers();
        return Maps.uniqueIndex(computers, new Function<Computer, String>() {
            @Override
            public String apply(Computer computer) {
                computer.setClient(client);
                return computer.getDisplayName().toLowerCase();
            }
        });
    }

  /**
   * Get a computer on the server (at the summary level)
   *
   * @return list of defined computers (summary level, for details @see Computer#details
   * @throws IOException
   */
  public Computer getComputer(String name) throws IOException {
    try {
      return client.get("computer/" + name, Computer.class);
    } catch (HttpResponseException ex) {
      return null;
    }
  }

  public void createNode(Node node) throws IOException {
    List<NameValuePair> parameters = new ArrayList<NameValuePair>(3);
    parameters.add(new BasicNameValuePair("name", node.getName()));
    parameters.add(new BasicNameValuePair("type", node.getType()));
    parameters.add(new BasicNameValuePair("json", Utils.getJsonMapper().writeValueAsString(node)));
    client.post_json("/computer/doCreateItem", parameters);
  }

  public void deleteNode(String nodeName) throws IOException {
    client.post_json(String.format("/computer/%s/doDelete", encode(nodeName)), new ArrayList<NameValuePair>());
  }

  public void addJobToView(String jobName, String viewName) throws IOException {
    List<NameValuePair> parameters = new ArrayList<NameValuePair>(3);
    parameters.add(new BasicNameValuePair("name", jobName));
    client.post_json("/view/" + encode(viewName) + "/addJobToView", parameters);
  }

  public void removeJobFromView(String jobName, String viewName) throws IOException {
    List<NameValuePair> parameters = new ArrayList<NameValuePair>(3);
    parameters.add(new BasicNameValuePair("name", jobName));
    client.post_json("/view/" + encode(viewName) + "/removeJobFromView", parameters);
  }

  public View getView(String viewName) throws IOException {
    try {
      return client.get("view/" + encode(viewName), View.class);
    } catch (HttpResponseException ex) {
      return null;
    }
  }

  public List<View> getViews() throws IOException {
    return client.get("/", MainView.class).getViews();
  }

  public List<String> getJobsOnView(String viewName) throws IOException, JDOMException {
    String result = client.get("view/" + encode(viewName) + "/config.xml");
    return JOB_NAMES.evaluate(new SAXBuilder().build(new StringReader(result))).stream().map(c -> c.getValue()).collect(Collectors.toList());
  }

    /*
     * Delete a job from jenkins
     *
     * @throws IOException
     */
    public void deleteJob(String jobName) throws IOException {
        client.post("/job/" + encode(jobName) + "/doDelete");
    }

  public String executeScript(String script) throws IOException {
      return client.executeScript(script);
    }

    /**
     * Update the xml description of an existing job
     *
     * @throws IOException
     */
    public void updateJob(String jobName, String jobXml) throws IOException {
        client.post_xml("/job/" + encode(jobName) + "/config.xml", jobXml);
    }

    private String encode(String pathPart) {
        // jenkins doesn't like the + for space, use %20 instead
        return URLEncoder.encode(pathPart).replaceAll("\\+","%20");
    }
}
