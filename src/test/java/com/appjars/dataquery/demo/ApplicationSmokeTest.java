/*-
 * #%L
 * Data Query AppJars - Demo
 * %%
 * Copyright (C) 2026 AppJars
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.appjars.dataquery.demo;

import static org.assertj.core.api.Assertions.assertThat;

import com.appjars.dataquery.flow.util.RouteConfigurer;
import com.appjars.dataquery.service.DashboardService;
import com.appjars.dataquery.service.QueryDefinitionService;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:data-query-smoke;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.h2.console.enabled=false"
})
class ApplicationSmokeTest {

  @LocalServerPort
  private int port;

  @Autowired
  private QueryDefinitionService queryDefinitionService;

  @Autowired
  private DashboardService dashboardService;

  @Autowired
  private RouteConfigurer routeConfigurer;

  @Test
  void startsSeedsShowcaseAndServesPublicRoutes() throws IOException, InterruptedException {
    assertThat(queryDefinitionService.findByKeyWithDetails("sales-by-region")).isPresent();
    assertThat(queryDefinitionService.findByKeyWithDetails("product-inventory")).isPresent();
    assertThat(queryDefinitionService.findByKeyWithDetails("fetch-todos")).isPresent();
    assertThat(queryDefinitionService.findByKeyWithDetails("employee-managers")).isPresent();
    assertThat(dashboardService.findByKeyWithWidgets("sales-overview")).isPresent();

    List<String> routes = List.of(
        "",
        routeConfigurer.getQueriesUrl(),
        routeConfigurer.getQueriesCreateUrl(),
        routeConfigurer.getCategoriesUrl(),
        routeConfigurer.getReportBaseUrl() + "/sales-by-region",
        routeConfigurer.getReportDesignerBaseUrl() + "/sales-by-region",
        routeConfigurer.getDashboardsUrl(),
        routeConfigurer.getDashboardsCreateUrl(),
        routeConfigurer.getDashboardBaseUrl() + "/sales-overview");

    try (HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()) {
      for (String route : routes) {
        HttpRequest request = HttpRequest.newBuilder(uri(route))
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build();
        HttpResponse<Void> response = client.send(request,
            HttpResponse.BodyHandlers.discarding());

        assertThat(response.statusCode())
            .as("HTTP status for /%s", route)
            .isEqualTo(200);
      }
    }
  }

  private URI uri(String route) {
    return URI.create("http://localhost:" + port + "/" + route);
  }
}
