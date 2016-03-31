package net.elbandi.pve2api;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import java.util.List;


// Based on http://lukencode.com/2010/04/27/calling-web-services-in-android-using-httpclient/
// but now fixed for allowing threading...
public class RestClient {

    private static final HttpClient CLIENT = new DefaultHttpClient(new PoolingClientConnectionManager());
    private static final Logger LOG = LoggerFactory.getLogger(RestClient.class);

    public static final String SYS_PROP_SOCKS_PROXY_HOST = "socksProxyHost";
    public static final String SYS_PROP_SOCKS_PROXY_PORT = "socksProxyPort";

    public enum RequestMethod {

        DELETE,
        GET,
        POST,
        PUT
    }

    public RestClient() {

        try {
            SSLSocketFactory sslsf = new SSLSocketFactory(new TrustSelfSignedStrategy(),
                    new AllowAllHostnameVerifier());
            Scheme https = new Scheme("https", 8006, sslsf);
            CLIENT.getConnectionManager().getSchemeRegistry().register(https);
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | UnrecoverableKeyException e) {
            throw new IllegalStateException("Could not instanciate RestClient.", e);
        }
    }

    public Response execute(RequestMethod method, String url, String jsonBody, List<NameValuePair> params,
        List<NameValuePair> headers) throws IOException {

        switch (method) {
            case GET: {
                HttpGet request = new HttpGet(url + createQueryString(params));
                request = (HttpGet) addHeaderParams(request, headers);

                return executeRequest(request);
            }

            case POST: {
                HttpPost request = new HttpPost(url);
                request = (HttpPost) addHeaderParams(request, headers);
                request = (HttpPost) addBodyParams(request, jsonBody, params);

                return executeRequest(request);
            }

            case PUT: {
                HttpPut request = new HttpPut(url);
                request = (HttpPut) addHeaderParams(request, headers);
                request = (HttpPut) addBodyParams(request, jsonBody, params);

                return executeRequest(request);
            }

            case DELETE: {
                HttpDelete request = new HttpDelete(url);
                request = (HttpDelete) addHeaderParams(request, headers);

                return executeRequest(request);
            }
        }

        return null;
    }


    private HttpUriRequest addHeaderParams(HttpUriRequest request, List<NameValuePair> headers) {

        if (headers == null) {
            return request;
        }

        for (NameValuePair h : headers) {
            request.addHeader(h.getName(), h.getValue());
        }

        return request;
    }


    private HttpUriRequest addBodyParams(HttpUriRequest request, String jsonBody, List<NameValuePair> params) {

        try {
            if (jsonBody != null) {
                request.addHeader("Content-Type", "application/json");

                if (request instanceof HttpPost) {
                    ((HttpPost) request).setEntity(new StringEntity(jsonBody, "UTF-8"));
                } else if (request instanceof HttpPut) {
                    ((HttpPut) request).setEntity(new StringEntity(jsonBody, "UTF-8"));
                }
            } else if (!params.isEmpty()) {
                if (request instanceof HttpPost) {
                    ((HttpPost) request).setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                } else if (request instanceof HttpPut) {
                    ((HttpPut) request).setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                }
            }
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException("UTF-8 not available for encoding!", ex);
        }

        return request;
    }


    private String createQueryString(List<NameValuePair> params) throws UnsupportedEncodingException {

        StringBuilder combinedParams = new StringBuilder();

        if (!params.isEmpty()) {
            combinedParams.append("?");

            for (NameValuePair p : params) {
                combinedParams.append(combinedParams.length() > 1 ? "&" : "")
                    .append(p.getName())
                    .append("=")
                    .append(URLEncoder.encode(p.getValue(), "UTF-8"));
            }
        }

        return combinedParams.toString();
    }


    private Response executeRequest(HttpUriRequest request) throws IOException {

        HttpParams requestParams = CLIENT.getParams();

        // Setting 30 second timeouts
        HttpConnectionParams.setConnectionTimeout(requestParams, 30 * 1000);
        HttpConnectionParams.setSoTimeout(requestParams, 30 * 1000);

        HttpResponse httpResponse;

        httpResponse = CLIENT.execute(request);

        int responseCode = httpResponse.getStatusLine().getStatusCode();
        String reasonPhrase = httpResponse.getStatusLine().getReasonPhrase();

        HttpEntity entity = httpResponse.getEntity();

        if (entity != null) {
            try(InputStream instream = entity.getContent()) {
                String response = convertStreamToString(instream);

                return new Response(response, responseCode, reasonPhrase);

                // Closing the input stream will trigger connection release
            }
        }

        return new Response(null, responseCode, reasonPhrase);
    }


    private static String convertStreamToString(InputStream is) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;

        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                LOG.warn("Failed to close input stream", ex);
            }
        }

        return sb.toString();
    }

    public class Response {

        private String response;
        private int responseCode;
        private String errorMessage;

        public Response(String response, int responseCode, String errorMessage) {

            this.errorMessage = errorMessage;
            this.response = response;
            this.responseCode = responseCode;
        }

        public String getErrorMessage() {

            return errorMessage;
        }


        public void setErrorMessage(String message) {

            this.errorMessage = message;
        }


        public String getResponse() {

            return response;
        }


        public void setResponse(String response) {

            this.response = response;
        }


        public int getResponseCode() {

            return responseCode;
        }


        public void setResponseCode(int responseCode) {

            this.responseCode = responseCode;
        }
    }
}
