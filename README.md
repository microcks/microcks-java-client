# Microcks Java Client

A Java Client or SDK that allows you to interact with the Microcks API. It has minimal dependencies and is easy to use.

[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/microcks/microcks-java-client/build-verify.yml?logo=github&style=for-the-badge)](https://github.com/microcks/microcks-java-client/actions)
[![Version](https://img.shields.io/maven-central/v/io.github.microcks/microcks-java-client?color=blue&style=for-the-badge)]((https://search.maven.org/artifact/io.github.microcks/microcks-java-client))
[![License](https://img.shields.io/github/license/microcks/microcks-java-client?style=for-the-badge&logo=apache)](https://www.apache.org/licenses/LICENSE-2.0)
[![Project Chat](https://img.shields.io/badge/discord-microcks-pink.svg?color=7289da&style=for-the-badge&logo=discord)](https://microcks.io/discord-invite/)
[![Artifact HUB](https://img.shields.io/endpoint?url=https://artifacthub.io/badge/repository/microcks&style=for-the-badge)](https://artifacthub.io/packages/search?repo=microcks)
[![CNCF Landscape](https://img.shields.io/badge/CNCF%20Landscape-5699C6?style=for-the-badge&logo=cncf)](https://landscape.cncf.io/?item=app-definition-and-development--application-definition-image-build--microcks)

## Build Status

Latest released version is `0.0.4`.

Current development version is `0.0.5-SNAPSHOT`.

#### Fossa license and security scans

[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fmicrocks%2Fmicrocks-java-client.svg?type=shield&issueType=license)](https://app.fossa.com/projects/git%2Bgithub.com%2Fmicrocks%2Fmicrocks-java-client?ref=badge_shield&issueType=license)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fmicrocks%2Fmicrocks-java-client.svg?type=shield&issueType=security)](https://app.fossa.com/projects/git%2Bgithub.com%2Fmicrocks%2Fmicrocks-java-client?ref=badge_shield&issueType=security)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fmicrocks%2Fmicrocks-java-client.svg?type=small)](https://app.fossa.com/projects/git%2Bgithub.com%2Fmicrocks%2Fmicrocks-java-client?ref=badge_small)

#### OpenSSF best practices on Microcks core

[![CII Best Practices](https://bestpractices.coreinfrastructure.org/projects/7513/badge)](https://bestpractices.coreinfrastructure.org/projects/7513)
[![OpenSSF Scorecard](https://api.securityscorecards.dev/projects/github.com/microcks/microcks/badge)](https://securityscorecards.dev/viewer/?uri=github.com/microcks/microcks)

## Community

* [Documentation](https://microcks.io/documentation/tutorials/getting-started/)
* [Microcks Community](https://github.com/microcks/community) and community meeting
* Join us on [Discord](https://microcks.io/discord-invite/), on [GitHub Discussions](https://github.com/orgs/microcks/discussions) or [CNCF Slack #microcks channel](https://cloud-native.slack.com/archives/C05BYHW1TNJ)

To get involved with our community, please make sure you are familiar with the project's [Code of Conduct](./CODE_OF_CONDUCT.md).

## Versions

| Java Client | Microcks Version |
|-------------|------------------|
| `0.0.2`     | `1.10.0` and +   |
| `0.0.3`     | `1.10.0` and +   |
| `0.0.4`     | `1.10.0` and +   |

## How to use it?

### Include it into your project dependencies

If you're using Maven:
```xml
<dependency>
  <groupId>io.github.microcks</groupId>
  <artifactId>microcks-java-client</artifactId>
  <version>0.0.4</version>
</dependency>
```

or if you're using Gradle:
```groovy  
dependencies {
    implementation 'io.github.microcks:microcks-java-client:0.0.4'
}
```

### Use it in your code

The API endpoints available on Microcks backend are split in different classes in the `io.github.microcks.client` package.
Each class represents a different part of the API but needs a common client configuration that is named `ApiClient`.

Here's the basic usage of the client where you configure the base URI of the Microcks API:

```java
ApiClient apiClient = new ApiClient();
apiClient.updateBaseUri("http://localhost:8585/api");
```

You can then easily use the client to interact with the different part of the API:

```java
ConfigApi configApi = new ConfigApi(apiClient);
KeycloakConfig config = configApi.getKeycloakConfig();
```

Check the [Microcks' OpenAPI reference](https://microcks.io/documentation/references/apis/open-api/) for comprehensive
list of available endpoints and their parameters.

### Access to authenticated endpoints

If your Microcks backend instance has enabled AuthN and AUthZ and if you need to access authenticated endpoints, 
we provide a `KeycloakClient` to first retrieve an oAuth token to use in following requests. You'll additionally need
the service account name and credentials to retrieve the token (see our documentation on 
[how Microcks is using Service Accounts](https://microcks.io/documentation/explanations/service-account/)).

The flow is as follows:

```java
KeycloakConfig keycloak = configApi.getKeycloakConfig();

// If Keycloak is enabled on target backend.
if (config.isEnabled()) {
   // Build the OAuth token endpoint - here using Keycloak public url but it could be another private one.
   String tokenEndpoint = keycloak.getAuthServerUrl() + "/realms/" + keycloak.getRealm() + "/protocol/openid-connect/token";
   final String oauthToken = KeycloakClient.connectAndGetOAuthToken("<service-account>", "<service-account-credentials>", tokenEndpoint);
   
   // Set a new interceptor to add the Authorization header with the OAuth token.
   apiClient.setRequestInterceptor(request -> request.header("Authorization", "Bearer " + oauthToken));
}

// Now you can use the client to create a protected resource.
JobApi jobApi = new JobApi(apiClient);

ImportJob importJob = new ImportJob();
importJob.setName("Hello Soap Service");
importJob.setRepositoryUrl("https://raw.githubusercontent.com/microcks/microcks/master/samples/HelloService-soapui-project.xml");

ImportJob result = jobApi.createImportJob(importJob);
```
