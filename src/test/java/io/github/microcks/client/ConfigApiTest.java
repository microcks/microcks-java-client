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

import io.github.microcks.client.api.ConfigApi;
import io.github.microcks.client.model.KeycloakConfig;
import io.github.microcks.testcontainers.MicrocksContainer;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is a unit test for ConfigApi class.
 * @author laurent
 */
@Testcontainers
class ConfigApiTest {

   @Container
   public static MicrocksContainer microcks = new MicrocksContainer("quay.io/microcks/microcks-uber:1.11.2-native");

   @Test
   void testGetKeycloakConfig() throws ApiException {
      ApiClient apiClient = new ApiClient();
      apiClient.updateBaseUri("http://localhost:" + microcks.getMappedPort(MicrocksContainer.MICROCKS_HTTP_PORT) + "/api");

      ConfigApi configApi = new ConfigApi(apiClient);
      KeycloakConfig config = configApi.getKeycloakConfig();

      assertFalse(config.getEnabled());
      assertEquals("microcks", config.getRealm());
      assertEquals("http://localhost:8180", config.getAuthServerUrl());
   }
}
