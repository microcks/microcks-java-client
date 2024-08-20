/*
 * Copyright The Microcks Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.microcks.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

/**
 * A basic client to connect to Keycloak and retrieve an OAuth token for a Service Account.
 * @author laurent
 */
public class KeycloakClient {

   /** Get a commons logger. */
   private static final Log log = LogFactory.getLog(KeycloakClient.class);

   private KeycloakClient() {
      // Private constructor to hide the default implicit one.
   }

   /**
    * Connecto to a remote Kaycloent token endpoint and retrieve an OAuth token for a Service Account.
    * @param serviceAccount The Service account to retrieve token for.
    * @param saCredentials  The Service account credentials.
    * @param tokenEndpoint  The Keycloak token endpoint (ends with /realms/<myrealm>/protocol/openid-connect/token)
    * @return The OAuth token string representation
    * @throws ApiException If token cannot be retrieved.
    * @throws IOException If http connection cannot be established or closed.
    */
   public static String connectAndGetOAuthToken(String serviceAccount, String saCredentials, String tokenEndpoint) throws ApiException, IOException {
      CloseableHttpClient httpClient = null;

      try {
         // Start creating a SSL Context that accepts all because we may have self-signed certs.
         TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
         SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
         SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);

         // Configuring a httpClient that disables host name validation for certificates.
         httpClient = HttpClients.custom().setSSLHostnameVerifier((String s, SSLSession sslSession) -> true)
               .setSSLSocketFactory(sslsf).build();
      } catch (GeneralSecurityException gse) {
         log.error("Caught a SecurityException when building the SSL Context", gse);
         throw new ApiException("SSLContext cannot be created to reach Keycloak endpoint: " + gse.getMessage());
      }

      try {
         // Prepare a post request with grant_type client credentials flow.
         HttpPost tokenRequest = new HttpPost(tokenEndpoint);
         tokenRequest.addHeader("Content-Type", "application/x-www-form-urlencoded");
         tokenRequest.addHeader("Accept", "application/json");
         tokenRequest.addHeader("Authorization",
               "Basic " + Base64.getEncoder().encodeToString((serviceAccount + ":" + saCredentials).getBytes(StandardCharsets.UTF_8)));
         tokenRequest.setEntity(new StringEntity("grant_type=client_credentials"));

         // Execute request and retrieve content as string.
         CloseableHttpResponse tokenResponse = httpClient.execute(tokenRequest);

         if (tokenResponse.getStatusLine().getStatusCode() != 200) {
            log.error("OAuth token cannot be retrieved for Keycloak server, check serviceaccount configuration");
            log.error("  tokenResponse.statusLine: " + tokenResponse.getStatusLine().toString());
            log.error("  tokenResponse.statusCode: " + tokenResponse.getStatusLine().getStatusCode());
            throw new ApiException("OAuth token cannot be retrieved for Microcks. Check serviceaccount.");
         }

         String result = EntityUtils.toString(tokenResponse.getEntity());
         log.debug("Result: " + result);

         // This should be a JSON token so parse it with Jackson.
         ObjectMapper mapper = new ObjectMapper();
         JsonNode jsonToken = mapper.readTree(result);

         // Retrieve and return access_token.
         return jsonToken.path("access_token").asText();
      } finally {
         httpClient.close();
      }
   }
}
