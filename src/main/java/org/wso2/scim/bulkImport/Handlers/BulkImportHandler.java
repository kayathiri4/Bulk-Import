package org.wso2.scim.bulkImport.Handlers;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.scim.bulkImport.Exceptions.BulkImportException;
import org.wso2.scim.bulkImport.Utils.FileHandler;
import org.wso2.scim.bulkImport.Utils.JSONCreator;
import org.wso2.scim.bulkImport.Utils.Summary;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class BulkImportHandler {

    private static final Logger logger = LoggerFactory.getLogger(JSONCreator.class.getName());

    public static String getTemporaryPassword(String accessToken) throws URISyntaxException, BulkImportException {

        String temPass = "Pass@1234";
        URIBuilder builder = new URIBuilder("https://api.asg.io/api/asgardeo-worker/v1/users/generate-password");
        try {
            HttpGet request = new HttpGet(builder.build());
            request.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
            request.addHeader(HttpHeaders.ACCEPT, "*/*");
            String authHeader = "Bearer " + accessToken;
            request.addHeader(HttpHeaders.AUTHORIZATION, authHeader);

            try (CloseableHttpClient client = HttpClientBuilder.
                    create()
                    .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build()) {
                HttpResponse response = client.execute(request);

                if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                    temPass = EntityUtils.toString(response.getEntity(), "UTF-8");
                } else if (HttpStatus.SC_UNAUTHORIZED == response.getStatusLine().getStatusCode()) {
                    logger.error("Unauthorized request to get temporary password.");
                } else {
                    logger.error("There is an error in getting temporary password.");
                }
            } catch (IOException e) {
                throw new BulkImportException("Error in getting temporary password.", e);
            }
        } catch (URISyntaxException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new BulkImportException("Error in getting temporary password.", e);
        }
        return temPass;
    }

    /**
     *
     * @param builder URI builder with endpoint URL.
     * @param operations Bulk operations.
     * @param accessToken Access Token.
     * @throws BulkImportException - Exception for Bulk Import.
     */
    public static void importBulkUser(URIBuilder builder, JSONArray operations, String accessToken)
            throws BulkImportException {

        try {
            JSONObject body = new JSONObject();
            JSONArray schemas = new JSONArray();
            schemas.add("urn:ietf:params:scim:api:messages:2.0:BulkRequest");
            body.put("failOnErrors", 0);
            body.put("schemas", schemas);
            body.put("Operations", operations);

            HttpPost request = new HttpPost(builder.build());
            request.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
            request.addHeader(HttpHeaders.ACCEPT, "*/*");
            String authHeader = "Bearer " + accessToken;
            request.addHeader(HttpHeaders.AUTHORIZATION, authHeader);

            StringEntity entity;
            entity = new StringEntity(body.toJSONString(), ContentType.APPLICATION_JSON);

            request.setEntity(entity);
            try (CloseableHttpClient client = HttpClientBuilder.
                    create()
                    .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build()) {
                HttpResponse response = client.execute(request);

                String stringResponse;

                if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                    stringResponse = EntityUtils.toString(response.getEntity(), "UTF-8");
                    JSONParser parser = new JSONParser();
                    JSONObject summary = (JSONObject) parser.parse(stringResponse);

                    // Display response details
                    Summary.displayUserImportSummary(summary, operations);
                    FileHandler.createOutputFile(operations, summary);
                    logger.info("Import process finished");
                } else if (HttpStatus.SC_UNAUTHORIZED == response.getStatusLine().getStatusCode()) {
                    logger.error("Unauthorized request to import bulk users.");
                } else {
                    logger.error("There is an error in importing bulk users.");
                }
            } catch (IOException | ParseException e) {
                throw new BulkImportException("Error in importing bulk users.", e);
            }
        } catch (URISyntaxException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new BulkImportException("Error in importing bulk users.", e);
        }
    }
}
