package com.offbytwo.jenkins.client.validator;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;

public class HttpResponseValidator {

    public void validateResponse(HttpResponse response) throws HttpResponseException {
        int status = response.getStatusLine().getStatusCode();
        if ((status < 200 || status >= 400) && status != 404) {
            throw new HttpResponseException(status, response.getStatusLine().getReasonPhrase());
        }
    }

  public boolean isNotFound(HttpResponse response) throws HttpResponseException {
    return response.getStatusLine().getStatusCode() == 404;
  }

}
