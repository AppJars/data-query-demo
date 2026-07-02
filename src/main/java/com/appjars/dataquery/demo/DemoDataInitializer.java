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

import com.appjars.dataquery.model.ColumnLinkDefinitionDto;
import com.appjars.dataquery.model.ColumnLinkParameterMappingDto;
import com.appjars.dataquery.model.ColumnType;
import com.appjars.dataquery.model.ConnectorType;
import com.appjars.dataquery.model.DashboardDto;
import com.appjars.dataquery.model.DashboardWidgetDto;
import com.appjars.dataquery.model.DashboardWidgetParameterDto;
import com.appjars.dataquery.model.WidgetDisplayMode;
import com.appjars.dataquery.model.LinkType;
import com.appjars.dataquery.model.ParameterType;
import com.appjars.dataquery.model.AxisRole;
import com.appjars.dataquery.model.ChartAxisMappingDto;
import com.appjars.dataquery.model.ChartDrillDownDefinitionDto;
import com.appjars.dataquery.model.ChartDrillDownParameterMappingDto;
import com.appjars.dataquery.model.QueryCategoryDto;
import com.appjars.dataquery.model.DependencyParameterMappingDto;
import com.appjars.dataquery.model.QueryDefinitionDto;
import com.appjars.dataquery.model.QueryDependencyDto;
import com.appjars.dataquery.model.QueryParameterDefinitionDto;
import com.appjars.dataquery.model.QueryResultColumnDefinitionDto;
import com.appjars.dataquery.model.QueryVisualizationDefinitionDto;
import com.appjars.dataquery.model.ReportTemplateDto;
import com.appjars.dataquery.model.VisualizationType;
import com.appjars.dataquery.model.query.QueryDefinitionFilter;
import com.appjars.dataquery.service.DashboardService;
import com.appjars.dataquery.service.QueryCategoryService;
import com.appjars.dataquery.service.QueryDefinitionService;
import com.appjars.dataquery.service.ReportTemplateService;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Populates representative content within the free license limits. */
@Component
public class DemoDataInitializer implements CommandLineRunner {

  private static final Logger logger = LoggerFactory.getLogger(DemoDataInitializer.class);

  private final QueryCategoryService queryCategoryService;
  private final QueryDefinitionService queryDefinitionService;
  private final ReportTemplateService reportTemplateService;
  private final DashboardService dashboardService;

  public DemoDataInitializer(QueryCategoryService queryCategoryService,
      QueryDefinitionService queryDefinitionService,
      ReportTemplateService reportTemplateService,
      DashboardService dashboardService) {
    this.queryCategoryService = queryCategoryService;
    this.queryDefinitionService = queryDefinitionService;
    this.reportTemplateService = reportTemplateService;
    this.dashboardService = dashboardService;
  }

  @Override
  public void run(String... args) {
    if (queryDefinitionService.count(QueryDefinitionFilter.builder().build()) == 0) {
      seedBaseData();
    }
    ensureDrillDownLinks();
    ensureChainedQueries();
    ensureRestShowcase();
    ensureHqlQuery();
    ensureChartVisualizations();
    ensurePhase6Showcase();
    ensureReportTemplate();
    ensureDashboard();
    ensureInternationalizedShowcase();
  }

  /** Seeds the Sales Overview dashboard with SQL, chained and HQL widgets. */
  private void ensureDashboard() {
    if (dashboardService.findByKeyWithWidgets("sales-overview").isPresent()) {
      return;
    }
    List<DashboardWidgetDto> widgets = new ArrayList<>();
    widgetFor("sales-by-region", "Sales by Region", WidgetDisplayMode.CHART, 0, 0, 6, 4, 1,
        List.of(fixedParameter("fromDate", "2025-01-01"),
            generatedParameter("toDate", "currentDate")))
        .ifPresent(widgets::add);
    widgetFor("product-inventory", "Stock by Product", WidgetDisplayMode.CHART, 6, 0, 6, 4, 2,
        List.of(fixedParameter("currency", "USD")))
        .ifPresent(widgets::add);
    widgetFor("employee-managers", "Employees and Managers", WidgetDisplayMode.TABLE,
        0, 4, 12, 5, 3, List.of(fixedParameter("department", "Engineering")))
        .ifPresent(widgets::add);

    if (widgets.isEmpty()) {
      return;
    }
    dashboardService.save(DashboardDto.builder()
        .key("sales-overview")
        .slug("sales-overview")
        .title("Sales Overview")
        .description("Sales, stock and order details at a glance")
        .enabled(true)
        .gridColumns(12)
        .widgets(widgets)
        .build());
    logger.info("Seeded the sales-overview dashboard");
  }

  /** Stores selected entity texts as translation keys. */
  private void ensureInternationalizedShowcase() {
    queryCategoryService.findByKey("operations")
        .filter(category -> !Boolean.TRUE.equals(category.getInternationalized()))
        .ifPresent(category -> {
          category.setDisplayName("demo.dataquery.category.operations");
          category.setDescription("demo.dataquery.category.operations.description");
          category.setInternationalized(true);
          queryCategoryService.update(category);
          logger.info("The operations category now stores translation keys");
        });

    dashboardService.findByKeyWithWidgets("sales-overview")
        .filter(dashboard -> !Boolean.TRUE.equals(dashboard.getInternationalized()))
        .ifPresent(dashboard -> {
          dashboard.setTitle("demo.dataquery.dashboard.sales.title");
          dashboard.setDescription("demo.dataquery.dashboard.sales.description");
          dashboard.setInternationalized(true);
          dashboard.getWidgets().forEach(widget -> widget.setTitle(widgetTitleKey(widget)));
          dashboardService.update(dashboard);
          logger.info("The sales-overview dashboard now stores translation keys");
        });

    translateQuery("sales-by-region", "demo.dataquery.query.salesbyregion",
        "demo.dataquery.chart.salesbyregion");
    translateQuery("product-inventory", "demo.dataquery.query.productinventory",
        "demo.dataquery.chart.productinventory");
  }

  /** Replaces charting query texts with translation keys. */
  private void translateQuery(String queryKey, String queryKeyPrefix, String chartKeyPrefix) {
    queryDefinitionService.findByKeyWithDetails(queryKey)
        .filter(query -> !Boolean.TRUE.equals(query.getInternationalized()))
        .ifPresent(query -> {
          query.setDisplayName(queryKeyPrefix);
          query.setDescription(queryKeyPrefix + ".description");
          query.setInternationalized(true);
          if (query.getVisualization() != null) {
            query.getVisualization().setTitle(chartKeyPrefix + ".title");
            query.getVisualization().setSubtitle(chartKeyPrefix + ".subtitle");
          }
          queryDefinitionService.update(query);
          logger.info("The {} query now stores translation keys", queryKey);
        });
  }

  private String widgetTitleKey(DashboardWidgetDto widget) {
    return switch (widget.getQueryKey() != null ? widget.getQueryKey() : "") {
      case "sales-by-region" -> "demo.dataquery.widget.salesbyregion";
      case "product-inventory" -> "demo.dataquery.widget.stockbyproduct";
      case "employee-managers" -> "demo.dataquery.widget.employeemanagers";
      default -> widget.getTitle();
    };
  }

  private Optional<DashboardWidgetDto> widgetFor(String queryKey, String title,
      WidgetDisplayMode displayMode, int posX, int posY, int width, int height, int displayOrder,
      List<DashboardWidgetParameterDto> parameters) {
    return queryDefinitionService.findByKeyWithDetails(queryKey)
        .map(query -> DashboardWidgetDto.builder()
            .queryDefinitionId(query.getId())
            .queryKey(query.getKey())
            .title(title)
            .displayMode(displayMode)
            .posX(posX)
            .posY(posY)
            .width(width)
            .height(height)
            .minWidth(2)
            .minHeight(2)
            .displayOrder(displayOrder)
            .parameters(new ArrayList<>(parameters))
            .build());
  }

  private DashboardWidgetParameterDto fixedParameter(String parameterKey, String value) {
    return DashboardWidgetParameterDto.builder().parameterKey(parameterKey).value(value).build();
  }

  private DashboardWidgetParameterDto generatedParameter(String parameterKey,
      String valueGeneratorKey) {
    return DashboardWidgetParameterDto.builder().parameterKey(parameterKey)
        .valueGeneratorKey(valueGeneratorKey).build();
  }

  /** Seeds grouped PDF templates for the two report examples. */
  private void ensureReportTemplate() {
    seedReportTemplate("region-order-details", "/reports/region-order-details.jrxml");
    seedReportTemplate("product-inventory", "/reports/product-inventory.jrxml");
  }

  private void seedReportTemplate(String queryKey, String resource) {
    queryDefinitionService.findByKeyWithDetails(queryKey).ifPresent(query -> {
      if (reportTemplateService.findByQueryDefinitionId(query.getId()).isEmpty()) {
        reportTemplateService.save(ReportTemplateDto.builder()
            .queryDefinitionId(query.getId())
            .content(readResource(resource))
            .enabled(true)
            .build());
        logger.info("Seeded the report template of {}", queryKey);
      }
    });
  }

  private String readResource(String path) {
    try (InputStream in = getClass().getResourceAsStream(path)) {
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Showcases the Phase 6 features on both fresh and pre-existing demo databases: a value
   * generator pre-filling the To Date of Sales by Region with the current date, and filterable
   * columns on Region Order Details.
   */
  private void ensurePhase6Showcase() {
    queryDefinitionService.findByKeyWithDetails("sales-by-region").ifPresent(query ->
        query.getParameters().stream()
            .filter(parameter -> "toDate".equals(parameter.getKey()))
            .filter(parameter -> parameter.getValueGeneratorKey() == null)
            .findFirst()
            .ifPresent(parameter -> {
              parameter.setValueGeneratorKey("currentDate");
              queryDefinitionService.update(query);
              logger.info("Configured the currentDate generator on sales-by-region.toDate");
            }));

    queryDefinitionService.findByKeyWithDetails("region-order-details").ifPresent(query -> {
      List<QueryResultColumnDefinitionDto> filterableColumns = query.getResultColumns().stream()
          .filter(column -> List.of("customer_name", "product").contains(column.getKey()))
          .filter(column -> !Boolean.TRUE.equals(column.getFilterable()))
          .toList();
      if (!filterableColumns.isEmpty()) {
        filterableColumns.forEach(column -> column.setFilterable(true));
        queryDefinitionService.update(query);
        logger.info("Made region-order-details columns filterable");
      }
    });
  }

  private void seedBaseData() {
    logger.info("Populating Data Query demo data");

    QueryCategoryDto salesReports = category("sales-reports", "Sales Reports",
        "Sales-related queries", "chart", 1);
    category("hr-reports", "HR Reports", "Human resources queries", "group", 2);
    category("operations", "Operations", "Operational queries", "cogs", 3);

    QueryDefinitionDto salesByRegion = QueryDefinitionDto.builder()
        .key("sales-by-region")
        .displayName("Sales by Region")
        .description("Total sales and order count per region within a date range")
        .categoryId(salesReports.getId())
        .connectorType(ConnectorType.SQL)
        .queryBody("""
            SELECT region, SUM(quantity * unit_price) AS total_sales, COUNT(*) AS order_count
            FROM demo_orders
            WHERE order_date BETWEEN :fromDate AND :toDate
            GROUP BY region
            ORDER BY total_sales DESC""")
        .enabled(true)
        .parameters(List.of(
            parameter("fromDate", "From Date", ParameterType.DATE, true, 1),
            parameter("toDate", "To Date", ParameterType.DATE, true, 2)))
        .resultColumns(List.of(
            column("region", "Region", ColumnType.STRING, null, 1),
            column("total_sales", "Total Sales", ColumnType.DOUBLE, "$#,##0.00", 2),
            column("order_count", "Orders", ColumnType.INTEGER, "#,##0", 3)))
        .build();
    salesByRegion.setId(queryDefinitionService.save(salesByRegion));

    QueryDefinitionDto regionOrderDetails = QueryDefinitionDto.builder()
        .key("region-order-details")
        .displayName("Region Order Details")
        .description("Orders of a given region")
        .categoryId(salesReports.getId())
        .connectorType(ConnectorType.SQL)
        .queryBody("""
            SELECT id AS order_id, customer_name, product, quantity, order_date
            FROM demo_orders
            WHERE region = :region""")
        .enabled(true)
        .parameters(List.of(
            parameter("region", "Region", ParameterType.STRING, true, 1)))
        .resultColumns(List.of(
            column("order_id", "Order Id", ColumnType.INTEGER, null, 1),
            column("customer_name", "Customer", ColumnType.STRING, null, 2),
            column("product", "Product", ColumnType.STRING, null, 3),
            column("quantity", "Quantity", ColumnType.INTEGER, "#,##0", 4),
            column("order_date", "Order Date", ColumnType.DATE, "yyyy-MM-dd", 5)))
        .build();
    queryDefinitionService.save(regionOrderDetails);

    logger.info("Data Query demo data created");
  }

  /**
   * Configures the drill-down chain Sales by Region -> Region Order Details on both fresh and
   * pre-existing demo databases.
   */
  private void ensureDrillDownLinks() {
    ensureColumnLink("sales-by-region", "region",
        queryLink("region-order-details", "region", "region"));
  }

  private void ensureColumnLink(String queryKey, String columnKey,
      ColumnLinkDefinitionDto link) {
    queryDefinitionService.findByKeyWithDetails(queryKey).ifPresent(
        query -> query.getResultColumns().stream()
            .filter(column -> columnKey.equals(column.getKey()))
            .filter(column -> column.getLink() == null)
            .findFirst()
            .ifPresent(column -> {
              column.setLink(link);
              queryDefinitionService.update(query);
              logger.info("Added drill-down link from {}.{} to {}", queryKey, columnKey,
                  link.getTargetQueryKey());
            }));
  }

  /**
   * Seeds the chained query pair Fetch Exchange Rates -> Product Inventory with External
   * Pricing on both fresh and pre-existing demo databases. Department Salary Summary and
   * Employee Salaries are discontinued HQL queries that a pre-existing demo database may still
   * carry from an earlier iteration of this showcase; they are removed here so upgrading
   * installations converge on the current five-query shape.
   */
  private void ensureChainedQueries() {
    queryDefinitionService.findByKey("department-salary-summary").ifPresent(query -> {
      queryDefinitionService.delete(query);
      logger.info("Removed the discontinued department-salary-summary query");
    });
    queryDefinitionService.findByKey("employee-salaries").ifPresent(query -> {
      queryDefinitionService.delete(query);
      logger.info("Removed the discontinued employee-salaries query");
    });

    if (queryDefinitionService.findByKey("product-inventory").isPresent()) {
      return;
    }

    Integer operationsCategoryId = queryCategoryService.findAll().stream()
        .filter(category -> "operations".equals(category.getKey()))
        .map(QueryCategoryDto::getId)
        .findFirst().orElse(null);

    QueryParameterDefinitionDto ratesCurrency =
        parameter("currency", "Currency", ParameterType.ENUM, true, 1);
    ratesCurrency.setEnumValues("USD,EUR,GBP");
    ratesCurrency.setDefaultValue("USD");

    QueryDefinitionDto exchangeRates = QueryDefinitionDto.builder()
        .key("fetch-exchange-rates")
        .displayName("Fetch Exchange Rates")
        .description("Loads the exchange rate of the given currency into the tmp_rates "
            + "temporary table")
        .categoryId(operationsCategoryId)
        .connectorType(ConnectorType.SQL)
        .queryBody("""
            SELECT currency, rate
            FROM demo_exchange_rates
            WHERE currency = :currency""")
        .storesInTemporaryTable(true)
        .temporaryTableName("tmp_rates")
        .enabled(true)
        .parameters(List.of(ratesCurrency))
        .resultColumns(List.of(
            column("currency", "Currency", ColumnType.STRING, null, 1),
            column("rate", "Rate", ColumnType.DOUBLE, "#,##0.0000", 2)))
        .build();
    exchangeRates.setId(queryDefinitionService.save(exchangeRates));
    logger.info("Created fetch-exchange-rates query");

    QueryParameterDefinitionDto inventoryCurrency =
        parameter("currency", "Currency", ParameterType.ENUM, true, 1);
    inventoryCurrency.setEnumValues("USD,EUR,GBP");
    inventoryCurrency.setDefaultValue("USD");

    QueryDefinitionDto productInventory = QueryDefinitionDto.builder()
        .key("product-inventory")
        .displayName("Product Inventory with External Pricing")
        .description("Product stock with prices converted through the tmp_rates temporary "
            + "table populated by the Fetch Exchange Rates dependency")
        .categoryId(operationsCategoryId)
        .connectorType(ConnectorType.SQL)
        .queryBody("""
            SELECT p.name AS product_name, p.category, p.price,
                   p.price * r.rate AS price_converted, p.stock_quantity
            FROM demo_products p
            CROSS JOIN tmp_rates r""")
        .enabled(true)
        .parameters(List.of(inventoryCurrency))
        .resultColumns(List.of(
            column("product_name", "Product", ColumnType.STRING, null, 1),
            column("category", "Category", ColumnType.STRING, null, 2),
            column("price", "Price (USD)", ColumnType.DOUBLE, "$#,##0.00", 3),
            column("price_converted", "Converted Price", ColumnType.DOUBLE, "#,##0.00", 4),
            column("stock_quantity", "Stock", ColumnType.INTEGER, "#,##0", 5)))
        .dependencies(List.of(QueryDependencyDto.builder()
            .dependsOnQueryDefinitionId(exchangeRates.getId())
            .dependsOnQueryKey("fetch-exchange-rates")
            .dependsOnQueryDisplayName("Fetch Exchange Rates")
            .executionOrder(1)
            .parameterMappings(new ArrayList<>(List.of(
                DependencyParameterMappingDto.builder()
                    .sourceParameterKey("currency")
                    .targetParameterKey("currency")
                    .build())))
            .build()))
        .build();
    queryDefinitionService.save(productInventory);
    logger.info("Created product-inventory query chained to fetch-exchange-rates");
  }

  /** Seeds a REST-to-SQL chain and removes its discontinued predecessor. */
  private void ensureRestShowcase() {
    queryDefinitionService.findByKeyWithDetails("region-order-details").ifPresent(query ->
        query.getResultColumns().stream()
            .filter(column -> "customer_name".equals(column.getKey()))
            .filter(column -> column.getLink() != null
                && "customer-orders".equals(column.getLink().getTargetQueryKey()))
            .findFirst()
            .ifPresent(column -> {
              column.setLink(null);
              queryDefinitionService.update(query);
              logger.info("Removed the customer-orders drill-down link from "
                  + "region-order-details.customer_name");
            }));

    queryDefinitionService.findByKey("customer-orders").ifPresent(query -> {
      queryDefinitionService.delete(query);
      logger.info("Removed the discontinued customer-orders query");
    });

    if (queryDefinitionService.findByKey("todos-summary").isPresent()) {
      return;
    }

    Integer operationsCategoryId = queryCategoryService.findAll().stream()
        .filter(category -> "operations".equals(category.getKey()))
        .map(QueryCategoryDto::getId)
        .findFirst().orElse(null);

    QueryDefinitionDto fetchTodos = QueryDefinitionDto.builder()
        .key("fetch-todos")
        .displayName("Fetch Todos")
        .description("Loads sample todo items from the public jsonplaceholder.typicode.com "
            + "REST API into the tmp_todos temporary table")
        .categoryId(operationsCategoryId)
        .connectorType(ConnectorType.REST_API)
        .queryBody("""
            {
              "method": "GET",
              "url": "https://jsonplaceholder.typicode.com/todos"
            }""")
        .storesInTemporaryTable(true)
        .temporaryTableName("tmp_todos")
        .enabled(true)
        .resultColumns(List.of(
            column("userid", "User Id", ColumnType.LONG, "#,##0", 1),
            column("id", "Todo Id", ColumnType.LONG, "#,##0", 2),
            column("title", "Title", ColumnType.STRING, null, 3),
            column("completed", "Completed", ColumnType.BOOLEAN, null, 4)))
        .build();
    fetchTodos.setId(queryDefinitionService.save(fetchTodos));
    logger.info("Created fetch-todos query");

    QueryDefinitionDto todosSummary = QueryDefinitionDto.builder()
        .key("todos-summary")
        .displayName("Todos Summary by User")
        .description("Count of completed and pending todo items per user, aggregated from the "
            + "tmp_todos temporary table populated by the Fetch Todos REST dependency")
        .categoryId(operationsCategoryId)
        .connectorType(ConnectorType.SQL)
        .queryBody("""
            SELECT userid AS user_id,
                   COUNT(*) AS todo_count,
                   SUM(CASE WHEN completed THEN 1 ELSE 0 END) AS completed_count,
                   SUM(CASE WHEN completed THEN 0 ELSE 1 END) AS pending_count
            FROM tmp_todos
            GROUP BY userid
            ORDER BY userid""")
        .enabled(true)
        .resultColumns(List.of(
            column("user_id", "User Id", ColumnType.LONG, "#,##0", 1),
            column("todo_count", "Todos", ColumnType.LONG, "#,##0", 2),
            column("completed_count", "Completed", ColumnType.LONG, "#,##0", 3),
            column("pending_count", "Pending", ColumnType.LONG, "#,##0", 4)))
        .dependencies(List.of(QueryDependencyDto.builder()
            .dependsOnQueryDefinitionId(fetchTodos.getId())
            .dependsOnQueryKey("fetch-todos")
            .dependsOnQueryDisplayName("Fetch Todos")
            .executionOrder(1)
            .build()))
        .build();
    queryDefinitionService.save(todosSummary);
    logger.info("Created todos-summary query chained to fetch-todos");
  }

  /**
   * Seeds the Employees and Managers query on fresh demo databases: an HQL query that traverses
   * the self-referencing {@code manager} association of {@code DemoEmployeeEntity} through
   * {@code e.manager.name}. It is shown as a table widget in the sales-overview dashboard,
   * showcasing the HQL connector alongside the SQL-based queries.
   */
  private void ensureHqlQuery() {
    if (queryDefinitionService.findByKey("employee-managers").isPresent()) {
      return;
    }

    Integer hrCategoryId = queryCategoryService.findAll().stream()
        .filter(category -> "hr-reports".equals(category.getKey()))
        .map(QueryCategoryDto::getId)
        .findFirst().orElse(null);

    QueryParameterDefinitionDto departmentParameter =
        parameter("department", "Department", ParameterType.ENUM, true, 1);
    departmentParameter.setEnumValues("Engineering,Sales,Marketing,HR");
    departmentParameter.setDefaultValue("Engineering");

    QueryDefinitionDto employeeManagers = QueryDefinitionDto.builder()
        .key("employee-managers")
        .displayName("Employees and Managers")
        .description("Employees of a department together with their manager, traversing the "
            + "manager association through HQL")
        .categoryId(hrCategoryId)
        .connectorType(ConnectorType.HQL)
        .queryBody("""
            select e.name as employee_name, e.department as department,
                   e.manager.name as manager_name
            from DemoEmployeeEntity e
            where e.department = :department
            order by e.name""")
        .enabled(true)
        .parameters(List.of(departmentParameter))
        .resultColumns(List.of(
            column("employee_name", "Employee", ColumnType.STRING, null, 1),
            column("department", "Department", ColumnType.STRING, null, 2),
            column("manager_name", "Manager", ColumnType.STRING, null, 3)))
        .build();
    queryDefinitionService.save(employeeManagers);
    logger.info("Created employee-managers HQL query");
  }

  /** Configures the seeded bar and donut charts. */
  private void ensureChartVisualizations() {
    ensureVisualization("sales-by-region", QueryVisualizationDefinitionDto.builder()
        .visualizationType(VisualizationType.BAR_CHART)
        .title("Total Sales by Region")
        .showLegend(false)
        .axisMappings(new ArrayList<>(List.of(
            axisMapping(AxisRole.CATEGORY, "region", "Region"),
            axisMapping(AxisRole.VALUE, "total_sales", "Total Sales"))))
        .drillDown(ChartDrillDownDefinitionDto.builder()
            .drillDownType(LinkType.QUERY_LINK)
            .targetQueryKey("region-order-details")
            .parameterMappings(new ArrayList<>(List.of(
                ChartDrillDownParameterMappingDto.builder()
                    .targetParameterKey("region")
                    .sourceAxisRole(AxisRole.CATEGORY)
                    .build())))
            .build())
        .build());

    ensureVisualization("product-inventory", QueryVisualizationDefinitionDto.builder()
        .visualizationType(VisualizationType.DONUT_CHART)
        .title("Stock by Product")
        .showLegend(true)
        .axisMappings(new ArrayList<>(List.of(
            axisMapping(AxisRole.CATEGORY, "product_name", "Product"),
            axisMapping(AxisRole.VALUE, "stock_quantity", "Stock"))))
        .build());
  }

  private void ensureVisualization(String queryKey,
      QueryVisualizationDefinitionDto visualization) {
    queryDefinitionService.findByKeyWithDetails(queryKey)
        .filter(query -> query.getVisualization() == null)
        .ifPresent(query -> {
          query.setVisualization(visualization);
          queryDefinitionService.update(query);
          logger.info("Added {} visualization to {}",
              visualization.getVisualizationType(), queryKey);
        });
  }

  private ChartAxisMappingDto axisMapping(AxisRole axisRole, String columnKey, String label) {
    return ChartAxisMappingDto.builder()
        .axisRole(axisRole)
        .columnKey(columnKey)
        .label(label)
        .build();
  }

  private ColumnLinkDefinitionDto queryLink(String targetQueryKey, String targetParameterKey,
      String sourceColumnKey) {
    return ColumnLinkDefinitionDto.builder()
        .linkType(LinkType.QUERY_LINK)
        .targetQueryKey(targetQueryKey)
        .openInNewTab(false)
        .parameterMappings(new ArrayList<>(List.of(ColumnLinkParameterMappingDto.builder()
            .targetParameterKey(targetParameterKey)
            .sourceColumnKey(sourceColumnKey)
            .build())))
        .build();
  }

  private QueryCategoryDto category(String key, String displayName, String description,
      String icon, int displayOrder) {
    QueryCategoryDto category = QueryCategoryDto.builder()
        .key(key)
        .displayName(displayName)
        .description(description)
        .icon(icon)
        .displayOrder(displayOrder)
        .build();
    category.setId(queryCategoryService.save(category));
    return category;
  }

  private QueryParameterDefinitionDto parameter(String key, String label,
      ParameterType dataType, boolean required, int displayOrder) {
    return QueryParameterDefinitionDto.builder()
        .key(key)
        .label(label)
        .dataType(dataType)
        .required(required)
        .displayOrder(displayOrder)
        .build();
  }

  private QueryResultColumnDefinitionDto column(String key, String label, ColumnType dataType,
      String format, int displayOrder) {
    return QueryResultColumnDefinitionDto.builder()
        .key(key)
        .label(label)
        .dataType(dataType)
        .format(format)
        .sortable(true)
        .filterable(false)
        .visible(true)
        .displayOrder(displayOrder)
        .build();
  }
}
