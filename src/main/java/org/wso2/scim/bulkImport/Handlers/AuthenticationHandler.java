/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.scim.bulkImport.Handlers;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;

import org.wso2.scim.bulkImport.Exceptions.AuthenticationException;

public class AuthenticationHandler {

    private final Logger logger = LoggerFactory.getLogger(AuthenticationHandler.class.getName());

    /**
     *
     * @param builder URI builder with endpoint URL.
     * @param username Username of the user.
     * @param password Password of the user.
     * @return String access Token.
     * @throws AuthenticationException - AuthenticationException
     */
    public String authenticate(
            URIBuilder builder, String username, String password) throws AuthenticationException {

        logger.info("Authenticating the user...");
        String accessToken = null;

        try {
            HttpPost request = new HttpPost(builder.build());
            request.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
            request.addHeader(HttpHeaders.ACCEPT, "*/*");
            String data = "grant_type=asg_api&client_id=ASG_API_CLIENT&username=" + username +
                    "&password=" + password + "&scope=SYSTEM openid";

            StringEntity entity;
            entity = new StringEntity(data);
            request.setEntity(entity);
            final SSLContext sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(null, (x509CertChain, authType) -> true)
                    .build();
            try (CloseableHttpClient client = HttpClientBuilder.create()
                    .setSSLContext(sslContext)
                    .build()) {
                HttpResponse response = client.execute(request);

                String stringResponse;

                if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                    System.out.println("***Authenticating success");
                    stringResponse = EntityUtils.toString(response.getEntity(), "UTF-8");
                    JSONParser parser = new JSONParser();
                    JSONObject json = (JSONObject) parser.parse(stringResponse);
                    accessToken = (String) json.get("access_token");
                } else {
//                    System.out.println(response);
                    logger.error("The username or password is invalid. Please try again!!!");
                }
            }
        } catch (IOException | URISyntaxException | NoSuchAlgorithmException | KeyStoreException |
                ParseException | KeyManagementException e) {
            throw new AuthenticationException("Error in authenticating the user,", e);
        }

        return accessToken;
    }
}
