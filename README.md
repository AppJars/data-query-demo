# Data Query AppJar Demo

This Spring Boot and Vaadin application demonstrates the full Data Query AppJar experience. It includes stored SQL, HQL and REST API queries, typed parameters, charts, drill-down navigation, query chaining, PDF reports, dashboards and internationalized entity texts. Learn more at [AppJars](https://www.appjars.com) and the [AppJars documentation](https://docs.appjars.com).

## Preloaded showcase

The first startup creates generic sales, employee, product and exchange-rate data. It also creates five user-facing queries, two intermediate query-chain steps, three categories, two designed PDF reports and the Sales Overview dashboard. The examples stay within the free license limits of five user-facing queries and one dashboard.

The seeded queries cover all three connectors:

| Query | Connector | What it demonstrates |
|---|---|---|
| Sales by Region | SQL | Parameters, bar chart and drill-down |
| Region Order Details | SQL | Column filters, link navigation and designed PDF |
| Product Inventory with External Pricing | SQL chain | Temporary tables, dependencies, chart and grouped PDF |
| Todos Summary | REST API chain | Public API input and query chaining |
| Employees and Managers | HQL | Entity queries and self-referencing relationships |

## Requirements

- JDK 21
- Maven 3.9 or newer
- Internet access on the first build to download Data Query `1.0.0` from the AppJars Maven repository and the remaining dependencies

Docker is not required.

## Run the demo

1. Clone this repository.
2. Start the application:

```bash
mvn clean spring-boot:run
```

3. Open [http://localhost:8080](http://localhost:8080). No login is required.

The H2 database is stored in `./data`. The local H2 console is available at [http://localhost:8080/h2-console](http://localhost:8080/h2-console) with JDBC URL `jdbc:h2:./data/dataquerydb`, user `sa` and an empty password.

## Explore the demo

Use the Data Query submenu to open every AppJar view:

| View | What to try |
|---|---|
| Query Definitions | Search the seeded catalog, run a query, edit its parameters and inspect connector settings |
| New Query Definition | Build a query across the General, Parameters, Result Columns, Dependencies, Visualization and Report tabs |
| Query Categories | Create, edit and order categories |
| Sales by Region Report | Execute a parameterized query, switch between chart and grid, follow drill-down links and export results |
| Report Designer | Edit the seeded PDF report on the visual canvas |
| Dashboards | Manage dashboard definitions and open their runtime views |
| New Dashboard | Add query widgets, drag them and resize the grid layout |
| Sales Overview Dashboard | View the saved chart and table widgets in read-only mode |

The **Guided tour** menu in the navigation bar contains one tour for each view and remains available throughout the application.

## Run the automated checks

```bash
mvn verify
```

The smoke test starts the complete application with an isolated in-memory database, verifies that the representative showcase data was seeded and checks every public demo route over HTTP.

## Data Query configuration

### Routes

| Property | Demo value | Purpose |
|---|---|---|
| `com.appjars.dataquery.url.admin.queries` | `data-query/admin/queries` | Query definition list |
| `com.appjars.dataquery.url.admin.queries-create` | `data-query/admin/queries/create` | Query creation form |
| `com.appjars.dataquery.url.admin.queries-edit` | `data-query/admin/queries/edit` | Query editing form |
| `com.appjars.dataquery.url.admin.categories` | `data-query/admin/categories` | Query category list |
| `com.appjars.dataquery.url.report` | `data-query/report` | Query report base route |
| `com.appjars.dataquery.url.report-designer` | `data-query/report-designer` | Report designer base route |
| `com.appjars.dataquery.url.admin.dashboards` | `data-query/admin/dashboards` | Dashboard list |
| `com.appjars.dataquery.url.admin.dashboards-create` | `data-query/admin/dashboards/create` | Dashboard creation form |
| `com.appjars.dataquery.url.admin.dashboards-edit` | `data-query/admin/dashboards/edit` | Dashboard editing form |
| `com.appjars.dataquery.url.dashboard` | `data-query/dashboards` | Read-only dashboard base route |

### HQL connector

| Property | Demo value | Purpose |
|---|---|---|
| `com.appjars.dataquery.hql.allowed-entities` | `DemoEmployeeEntity` | Limits HQL queries to the seeded employee entity |

### REST API connector

| Property | Demo value | Purpose |
|---|---|---|
| `com.appjars.dataquery.rest.allowed-hosts` | `jsonplaceholder.typicode.com` | Allows the seeded public REST API query |
| `com.appjars.dataquery.rest.max-rows` | `10000` | Limits accepted response rows |
| `com.appjars.dataquery.rest.connect-timeout` | `5s` | Sets the connection timeout |
| `com.appjars.dataquery.rest.read-timeout` | `30s` | Sets the read timeout |
| `com.appjars.dataquery.rest.max-response-bytes` | `8388608` | Limits the response body size |

## Production build

```bash
mvn clean package -Pproduction
```

## Stop and reset

Stop the running application with `Ctrl+C`. To reset all seeded data, stop the application, delete the `data` directory and start it again.
