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

import io.github.microcks.client.api.JobApi;
import io.github.microcks.client.model.ImportJob;
import io.github.microcks.testcontainers.MicrocksContainer;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * This is a unit test for JobApi class.
 * @author laurent
 */
@Testcontainers
class JobApiTest {

   @Container
   public static MicrocksContainer microcks = new MicrocksContainer("quay.io/microcks/microcks-uber:1.12.1-native");

   @Test
   void testManageImportJob() throws ApiException {
      ApiClient apiClient = new ApiClient();
      apiClient.updateBaseUri("http://localhost:" + microcks.getMappedPort(MicrocksContainer.MICROCKS_HTTP_PORT) + "/api");

      JobApi jobApi = new JobApi(apiClient);

      ImportJob importJob = new ImportJob();
      importJob.setName("Hello Soap Service");
      importJob.setRepositoryUrl("https://raw.githubusercontent.com/microcks/microcks/master/samples/HelloService-soapui-project.xml");

      ImportJob result = jobApi.createImportJob(importJob);
      assertNotNull(result);
      assertNotNull(result.getId());
      assertEquals("Hello Soap Service", result.getName());

      // Get import job by id.
      ImportJob job = jobApi.getImportJob(result.getId());
      assertNotNull(job);
      assertEquals(result.getId(), job.getId());
      assertEquals("Hello Soap Service", job.getName());

      // List import jobs.
      List<ImportJob> jobs = jobApi.getImportJobs(0, 20, "Hello Soap Service");
      assertNotNull(jobs);
      assertEquals(1, jobs.size());
      assertEquals("Hello Soap Service", jobs.get(0).getName());

      // Now delete import job.
      jobApi.deleteImportJob(result.getId());
      jobs = jobApi.getImportJobs(0, 20, null);
      assertEquals(0, jobs.size());
   }
}
