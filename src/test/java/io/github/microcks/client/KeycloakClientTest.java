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

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * This is a unit test for KeycloakClient class.
 * @author laurent
 */
@Testcontainers
public class KeycloakClientTest {

   @Container
   public static KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:26.0.0")
         .withRealmImportFile("io/github/microcks/client/myrealm-realm.json");

   @Test
   void testConnectAndGetToken() throws IOException, ApiException {
      // Retrieve token endpoint.
      String tokenEndpoint = keycloak.getAuthServerUrl() + "/realms/myrealm/protocol/openid-connect/token";

      // Authenticate and get OAuth token.
      String oauthToken = KeycloakClient.connectAndGetOAuthToken("myrealm-serviceaccount",
            "ab54d329-e435-41ae-a900-ec6b3fe15c54",
            tokenEndpoint);

      assertNotNull(oauthToken);
   }
}
