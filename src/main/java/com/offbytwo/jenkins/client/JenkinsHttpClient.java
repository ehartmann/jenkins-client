/*
 * Copyright (c) 2013 Rising Oak LLC.
 *
 * Distributed under the MIT license: http://opensource.org/licenses/MIT
 */

package com.offbytwo.jenkins.client;

import com.google.common.io.CharStreams;
import com.offbytwo.jenkins.client.validator.HttpResponseValidator;
import com.offbytwo.jenkins.model.BaseModel;
import com.offbytwo.jenkins.model.Crumb;
import com.offbytwo.jenkins.tools.Utils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.apache.commons.lang.StringUtils.isNotBlank;

public class JenkinsHttpClient {

    private URI uri;
    private DefaultHttpClient client;
    private BasicHttpContext localContext;
    private HttpResponseValidator httpResponseValidator;

    private String context;

    /**
     * Create an unauthenticated Jenkins HTTP client
     *
     * @param uri Location of the jenkins server (ex. http://localhost:8080)
     * @param defaultHttpClient Configured DefaultHttpClient to be used
     */
    public JenkinsHttpClient(URI uri, DefaultHttpClient defaultHttpClient) {
        this.context = uri.getPath();
        if (!context.endsWith("/")) {
            context += "/";
        }
        this.uri = uri;
        this.client = defaultHttpClient;
        this.httpResponseValidator = new HttpResponseValidator();
    }

    /**
     * Create an unauthenticated Jenkins HTTP client
     *
     * @param uri Location of the jenkins server (ex. http://localhost:8080)
     */
    public JenkinsHttpClient(URI uri) {
        this(uri, new DefaultHttpClient());
    }

    /**
     * Create an authenticated Jenkins HTTP client
     *
     * @param uri Location of the jenkins server (ex. http://localhost:8080)
     * @param username Username to use when connecting
     * @param password Password or auth token to use when connecting
     */
    public JenkinsHttpClient(URI uri, String username, String password) {
        this(uri);
        if (isNotBlank(username)) {
            CredentialsProvider provider = client.getCredentialsProvider();
            AuthScope scope = new AuthScope(uri.getHost(), uri.getPort(), "realm");
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
            provider.setCredentials(scope, credentials);

            localContext = new BasicHttpContext();
            localContext.setAttribute("preemptive-auth", new BasicScheme());
            client.addRequestInterceptor(new PreemptiveAuth(), 0);
        }
    }


  /**
   * Perform a GET request and parse the response to the given class
   *
   * @param path path to request, can be relative or absolute
   * @param cls class of the response
   * @param <T> type of the response
   * @return an instance of the supplied class
   * @throws IOException
   */
  public <T extends BaseModel> T get(String path, Class<T> cls) throws IOException {
    HttpGet getMethod = new HttpGet(jsonApi(path));
    HttpResponse response = client.execute(getMethod, localContext);
    try {
      httpResponseValidator.validateResponse(response);
      return httpResponseValidator.isNotFound(response) ? null : objectFromResponse(cls, response);
    } finally {
      EntityUtils.consume(response.getEntity());
      releaseConnection(getMethod);
    }
  }


  /**
   * Perform a GET request and parse the response to the given class
   *
   * @param path path to request, can be relative or absolute
   * @param cls class of the response
   * @param <T> type of the response
   * @return an instance of the supplied class
   * @throws IOException
   */
  public <T extends BaseModel> T getXml(String path, Class<T> cls) throws IOException {
    HttpGet getMethod = new HttpGet(correctPath(path));
    HttpResponse response = client.execute(getMethod, localContext);
    try {
      httpResponseValidator.validateResponse(response);
      return httpResponseValidator.isNotFound(response) ? null : objectFromResponse(cls, response);
    } finally {
      EntityUtils.consume(response.getEntity());
      releaseConnection(getMethod);
    }
  }

    /**
     * Perform a GET request and parse the response and return a simple string of the content
     *
     * @param path path to request, can be relative or absolute
     * @return the entity text
     * @throws IOException
     */
    public String get(String path) throws IOException {
        HttpGet getMethod = new HttpGet(jsonApi(path));
        HttpResponse response = client.execute(getMethod, localContext);
        try {
            httpResponseValidator.validateResponse(response);
            if (response.getStatusLine().getStatusCode() != 404) {
              Scanner s = new Scanner(response.getEntity().getContent());
              s.useDelimiter("\\z");
              StringBuffer sb = new StringBuffer();
              while (s.hasNext()) {
                sb.append(s.next());
              }
              return sb.toString();
            } else {
              return null;
            }
        } finally {
            releaseConnection(getMethod);
        }
    }

    /**
     * Perform a GET request and return the response as InputStream
     *
     * @param path path to request, can be relative or absolute
     * @return the response stream
     * @throws IOException
     */
    public InputStream getFile(URI path) throws IOException {
        HttpGet getMethod = new HttpGet(path);
        try {
            HttpResponse response = client.execute(getMethod, localContext);
            httpResponseValidator.validateResponse(response);
            return httpResponseValidator.isNotFound(response) ? null : response.getEntity().getContent();
        } finally {
            releaseConnection(getMethod);
        }
    }

    /**
     * Perform a POST request and parse the response to the given class
     *
     * @param path path to request, can be relative or absolute
     * @param data data to post
     * @param cls class of the response
     * @param <R> type of the response
     * @param <D> type of the data
     * @return an instance of the supplied class
     * @throws IOException
     */
    public <R extends BaseModel, D> R post(String path, D data, Class<R> cls) throws IOException {
        HttpPost request = new HttpPost(jsonApi(path));
        Crumb crumb = get("/crumbIssuer", Crumb.class);
        if (crumb != null) {
            request.addHeader(new BasicHeader(crumb.getCrumbRequestField(), crumb.getCrumb()));
        }

        if (data != null) {
            StringEntity stringEntity = new StringEntity(Utils.getJsonMapper().writeValueAsString(data), "application/json");
            request.setEntity(stringEntity);
        }
        HttpResponse response = client.execute(request, localContext);

        try {
            httpResponseValidator.validateResponse(response);

            if (cls != null) {
                return httpResponseValidator.isNotFound(response) ? null : objectFromResponse(cls, response);
            } else {
                return null;
            }
        } finally {
            EntityUtils.consume(response.getEntity());
            releaseConnection(request);
        }
    }

  /**
   * Perform a POST request of XML (instead of using json jsonMapper) and return a string rendering of the response
   * entity.
   *
   * @param script to post data data to post
   * @return A string containing the xml response (if present)
   * @throws IOException
   */
  public String executeScript(String script) throws IOException {
    HttpPost request = new HttpPost(correctPath("/scriptText"));
    Crumb crumb = get("/crumbIssuer", Crumb.class);
    if (crumb != null) {
      request.addHeader(new BasicHeader(crumb.getCrumbRequestField(), crumb.getCrumb()));
    }

    if (script != null) {
      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
      nameValuePairs.add(new BasicNameValuePair("script", script));
      request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
    }

    HttpResponse response = client.execute(request, localContext);
    httpResponseValidator.validateResponse(response);

    try {
      if (!httpResponseValidator.isNotFound(response)) {
        String result = CharStreams.toString(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
        String[] resultByLines = StringUtils.split(result, "\n");
        if (resultByLines[resultByLines.length - 1].startsWith("Result: [")) {
          result = StringUtils.removeEnd(result, resultByLines[resultByLines.length - 1] + "\n");
        }
        return result;
      } else {
        return null;
      }
    } finally {
      EntityUtils.consume(response.getEntity());
      releaseConnection(request);
    }
  }

    /**
     * Perform a POST request of XML (instead of using json jsonMapper) and return a string rendering of the response
     * entity.
     *
     * @param path path to request, can be relative or absolute
     * @param xml_data data data to post
     * @return A string containing the xml response (if present)
     * @throws IOException
     */
    public String post_xml(String path, String xml_data) throws IOException {
        HttpPost request = new HttpPost(correctPath(path));
        Crumb crumb = get("/crumbIssuer", Crumb.class);
        if (crumb != null) {
            request.addHeader(new BasicHeader(crumb.getCrumbRequestField(), crumb.getCrumb()));
        }

        if (xml_data != null) {
            request.setEntity(new StringEntity(xml_data, ContentType.APPLICATION_XML));
        }
        HttpResponse response = client.execute(request, localContext);
        httpResponseValidator.validateResponse(response);

        try {
            if (!httpResponseValidator.isNotFound(response)) {
              InputStream content = response.getEntity().getContent();
              Scanner s = new Scanner(content);
              StringBuffer sb = new StringBuffer();
              while (s.hasNext()) {
                sb.append(s.next());
              }
              return sb.toString();
            } else {
              return null;
            }
        } finally {
            EntityUtils.consume(response.getEntity());
            releaseConnection(request);
        }
    }

  /**
   * Perform a POST request of JSON (instead of using json mapper) and return a string rendering of the response entity.
   *
   * @param path path to request, can be relative or absolute
   * @param parameters data data to post
   * @return A string containing the xml response (if present)
   * @throws IOException
   */
  public String post_json(String path, List<NameValuePair> parameters) throws IOException {
    HttpPost request = new HttpPost(postApi(path));
    request.setEntity(new UrlEncodedFormEntity(parameters));
    HttpResponse response = null;
    try {
      response = client.execute(request, localContext);
      int status = response.getStatusLine().getStatusCode();
      if (status < 200 || status >= 400) {
        throw new HttpResponseException(status, response.getStatusLine().getReasonPhrase());
      }

      InputStream content = response.getEntity().getContent();
      Scanner s = new Scanner(content);
      StringBuilder sb = new StringBuilder();
      while (s.hasNext()) {
        sb.append(s.next());
      }
      return sb.toString();
    } finally {
      if (response != null) {
        EntityUtils.consume(response.getEntity());
      }
      releaseConnection(request);
    }
  }

    /**
     * Perform POST request that takes no parameters and returns no response
     *
     * @param path path to request
     * @throws IOException
     */
    public void post(String path) throws IOException {
        post(path, null, null);
    }

    private String urlJoin(String path1, String path2) {
        if (!path1.endsWith("/")) {
            path1 += "/";
        }
        if (path2.startsWith("/")) {
            path2 = path2.substring(1);
        }
        return path1 + path2;
    }

    private URI jsonApi(String path) {
        if (!path.toLowerCase().matches("https?://.*")) {
            path = urlJoin(this.context, path);
        }
        if (!path.contains("?")) {
          path = urlJoin(path, "api/json");
        } else {
          String[] components = path.split("\\?", 2);
          path = urlJoin(components[0], "api/json") + "?" + components[1];
        }
        return uri.resolve("/").resolve(path);
    }

    private URI postApi(String path) {
      return uri.resolve("/").resolve(correctPath(path));
    }

    private URI correctPath(String path) {
      if (!path.toLowerCase().matches("https?://.*")) {
        path = urlJoin(this.context, path);
      }
      return uri.resolve("/").resolve(path);
    }

    private <T extends BaseModel> T objectFromResponse(Class<T> cls, HttpResponse response) throws IOException {
        T result;
        InputStream content = response.getEntity().getContent();
        if (response.getFirstHeader("Content-Type").getValue().contains("application/xml")) {
          result = Utils.getXmlMapper().readValue(content, cls);
        } else {
          result = Utils.getJsonMapper().readValue(content, cls);
        }
        result.setClient(this);
        return result;
    }

  private void releaseConnection(HttpRequestBase httpRequestBase) {
        httpRequestBase.releaseConnection();
    }

}
