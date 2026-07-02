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
package com.appjars.dataquery.demo.views.tour;

import com.appjars.dataquery.flow.util.RouteConfigurer;
import com.appjars.dataquery.flow.view.DashboardFormView;
import com.appjars.dataquery.flow.view.DashboardListView;
import com.appjars.dataquery.flow.view.DashboardView;
import com.appjars.dataquery.flow.view.QueryCategoryListView;
import com.appjars.dataquery.flow.view.QueryDefinitionFormView;
import com.appjars.dataquery.flow.view.QueryDefinitionListView;
import com.appjars.dataquery.flow.view.QueryReportView;
import com.appjars.dataquery.flow.view.ReportDesignerView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.VaadinSession;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.vaadin.addons.antlerflow.tour.EngineType;
import org.vaadin.addons.antlerflow.tour.Tour;
import org.vaadin.addons.antlerflow.tour.TourButton;
import org.vaadin.addons.antlerflow.tour.TourButtonType;
import org.vaadin.addons.antlerflow.tour.TourStep;

/** Factory for the guided tours offered by the demo. */
public final class DemoTours {

  public static final String PENDING_TOUR_ATTRIBUTE = DemoTours.class.getName() + ".pendingTour";

  static final String KEY_PREFIX = "appjars.dataquery.demo.tour.";

  private static final String TARGET_ATTR = "data-antler-target";

  /** Keeps tour targets attached to the first visible selector match. */
  private static final String RESOLVE_TARGETS_JS =
      """
      const MAP = JSON.parse($0);
      const ATTR = 'data-antler-target';
      const resolve = () => {
        Object.keys(MAP).forEach(id => {
          let pick = null;
          for (const el of document.querySelectorAll(MAP[id])) {
            const r = el.getBoundingClientRect();
            if (r.width > 4 && r.height > 4) { pick = el; break; }
          }
          document.querySelectorAll("[" + ATTR + "='" + id + "']")
              .forEach(el => { if (el !== pick) { el.removeAttribute(ATTR); } });
          if (pick && pick.getAttribute(ATTR) !== id) { pick.setAttribute(ATTR, id); }
        });
      };
      if (window.__antlerResolver) { window.__antlerResolver.stop(); }
      let scheduled = false;
      const schedule = () => { if (scheduled) return; scheduled = true;
        requestAnimationFrame(() => { scheduled = false; resolve(); }); };
      resolve();
      const obs = new MutationObserver(schedule);
      obs.observe(document.body, {childList: true, subtree: true, attributes: true,
          attributeFilter: ['hidden', 'style', 'class']});
      window.__antlerResolver = { stop() { obs.disconnect();
        document.querySelectorAll('[' + ATTR + ']').forEach(el => el.removeAttribute(ATTR));
        window.__antlerResolver = null; } };
      """;

  /** Prevents Driver from clipping content beside the highlighted component. */
  private static final String TOUR_CSS_JS =
      """
      if (!document.getElementById('demo-tour-css')) {
        const style = document.createElement('style');
        style.id = 'demo-tour-css';
        style.textContent =
            'body :not(body):has(> .driver-active-element) { overflow: visible !important; }';
        document.head.appendChild(style);
      }
      """;

  private static final String STOP_JS = """
      window.__antlerResolver?.stop();
      document.getElementById('demo-tour-css')?.remove();
      """;

  public static final String SALES_BY_REGION_QUERY_KEY = "sales-by-region";

  public static final String SALES_OVERVIEW_SLUG = "sales-overview";

  public enum DemoTour {
    QUERIES,
    QUERY_FORM,
    CATEGORIES,
    REPORT,
    REPORT_DESIGNER,
    DASHBOARDS,
    DASHBOARD_FORM,
    DASHBOARD
  }

  public static Class<? extends Component> viewOf(DemoTour tour) {
    return switch (tour) {
      case QUERIES -> QueryDefinitionListView.class;
      case QUERY_FORM -> QueryDefinitionFormView.class;
      case CATEGORIES -> QueryCategoryListView.class;
      case REPORT -> QueryReportView.class;
      case REPORT_DESIGNER -> ReportDesignerView.class;
      case DASHBOARDS -> DashboardListView.class;
      case DASHBOARD_FORM -> DashboardFormView.class;
      case DASHBOARD -> DashboardView.class;
    };
  }

  public static void navigateTo(DemoTour tour, UI ui, RouteConfigurer routes) {
    switch (tour) {
      case QUERIES -> ui.navigate(QueryDefinitionListView.class);
      case QUERY_FORM -> ui.navigate(routes.getQueriesCreateUrl());
      case CATEGORIES -> ui.navigate(QueryCategoryListView.class);
      case REPORT -> ui.navigate(QueryReportView.class,
          new RouteParameters(QueryReportView.QUERY_KEY_PARAMETER, SALES_BY_REGION_QUERY_KEY));
      case REPORT_DESIGNER -> ui.navigate(ReportDesignerView.class,
          new RouteParameters(ReportDesignerView.QUERY_KEY_PARAMETER, SALES_BY_REGION_QUERY_KEY));
      case DASHBOARDS -> ui.navigate(DashboardListView.class);
      case DASHBOARD_FORM -> ui.navigate(routes.getDashboardsCreateUrl());
      case DASHBOARD -> ui.navigate(DashboardView.class,
          new RouteParameters(DashboardView.SLUG_PARAMETER, SALES_OVERVIEW_SLUG));
    }
  }

  public static void startLater(DemoTour tour, UI ui, RouteConfigurer routes) {
    VaadinSession.getCurrent().setAttribute(PENDING_TOUR_ATTRIBUTE, tour);
    navigateTo(tour, ui, routes);
  }

  private record StepDef(String key, String selector, String position, boolean first,
      boolean last) {

    String id() {
      return key.replace('.', '-');
    }
  }

  private DemoTours() {}

  public static Tour create(DemoTour tour, SerializableFunction<String, String> translator) {
    // Driver.js is the permitted tour engine.
    return Tour.builder().engineType(EngineType.DRIVER)
        .steps(steps(tour).stream().map(def -> step(translator, def)).toList())
        .showCancelButton(true).allowClose(true).build();
  }

  public static void start(DemoTour tour, Component host,
      SerializableFunction<String, String> translator) {
    Tour t = create(tour, translator);
    host.getElement().appendChild(t.getElement());
    host.getElement().executeJs(TOUR_CSS_JS);
    host.getElement().executeJs(RESOLVE_TARGETS_JS, targetJson(steps(tour)));
    t.addTourCompletedListener(e -> stop(t, host));
    t.addTourCanceledListener(e -> stop(t, host));
    t.start();
  }

  private static void stop(Tour tour, Component host) {
    host.getElement().executeJs(STOP_JS);
    tour.getElement().removeFromParent();
  }

  private static List<StepDef> steps(DemoTour tour) {
    return switch (tour) {
      case QUERIES -> queriesSteps();
      case QUERY_FORM -> queryFormSteps();
      case CATEGORIES -> categorySteps();
      case REPORT -> reportSteps();
      case REPORT_DESIGNER -> designerSteps();
      case DASHBOARDS -> dashboardsSteps();
      case DASHBOARD_FORM -> dashboardFormSteps();
      case DASHBOARD -> dashboardSteps();
    };
  }

  private static List<StepDef> queriesSteps() {
    return List.of(
        new StepDef("queries.intro", null, null, true, false),
        new StepDef("queries.grid", "#query-definitions-grid", "top", false, false),
        new StepDef("queries.filters", "#key-filter", "bottom", false, false),
        new StepDef("queries.create", "#new-query-button", "bottom", false, false),
        new StepDef("queries.limits", "#restrictions-bar", "bottom", false, false),
        new StepDef("queries.finish", null, null, false, true));
  }

  private static List<StepDef> queryFormSteps() {
    return List.of(
        new StepDef("queryform.intro", null, null, true, false),
        new StepDef("queryform.tabs", "#query-definition-tabsheet", "bottom", false, false),
        new StepDef("queryform.connector", "#connector-type-field", "bottom", false, false),
        new StepDef("queryform.save", "#save-button", "bottom", false, false),
        new StepDef("queryform.finish", null, null, false, true));
  }

  private static List<StepDef> categorySteps() {
    return List.of(
        new StepDef("categories.intro", null, null, true, false),
        new StepDef("categories.grid", "#query-categories-grid", "top", false, false),
        new StepDef("categories.create", "#new-category-button", "bottom", false, false),
        new StepDef("categories.finish", null, null, false, true));
  }

  private static List<StepDef> reportSteps() {
    return List.of(
        new StepDef("report.intro", null, null, true, false),
        new StepDef("report.parameters", "[id^='parameter-']", "bottom", false, false),
        new StepDef("report.execute", "#execute-button", "bottom", false, false),
        new StepDef("report.results", "#report-chart-area", "top", false, false),
        new StepDef("report.drilldown", null, null, false, false),
        new StepDef("report.finish", null, null, false, true));
  }

  private static List<StepDef> designerSteps() {
    return List.of(
        new StepDef("designer.intro", null, null, true, false),
        new StepDef("designer.canvas", "#report-designer", "top", false, false),
        new StepDef("designer.save", "#save-report-button", "bottom", false, false),
        new StepDef("designer.finish", null, null, false, true));
  }

  private static List<StepDef> dashboardsSteps() {
    return List.of(
        new StepDef("dashboards.intro", null, null, true, false),
        new StepDef("dashboards.grid", "#dashboards-grid", "top", false, false),
        new StepDef("dashboards.create", "#new-dashboard-button", "bottom", false, false),
        new StepDef("dashboards.finish", null, null, false, true));
  }

  private static List<StepDef> dashboardFormSteps() {
    return List.of(
        new StepDef("dashboardform.intro", null, null, true, false),
        new StepDef("dashboardform.details", "#dashboard-key-field", "bottom", false, false),
        new StepDef("dashboardform.widgets", "#add-widget-button", "bottom", false, false),
        new StepDef("dashboardform.layout", "#dashboard-grid", "top", false, false),
        new StepDef("dashboardform.save", "#save-dashboard-button", "bottom", false, false),
        new StepDef("dashboardform.finish", null, null, false, true));
  }

  private static List<StepDef> dashboardSteps() {
    return List.of(
        new StepDef("dashboard.intro", null, null, true, false),
        new StepDef("dashboard.title", "#dashboard-title", "bottom", false, false),
        new StepDef("dashboard.grid", "#dashboard-grid", "top", false, false),
        new StepDef("dashboard.widget", "[id^='widget-']", "bottom", false, false),
        new StepDef("dashboard.finish", null, null, false, true));
  }

  private static String targetJson(List<StepDef> defs) {
    return defs.stream().filter(def -> def.selector() != null)
        .map(def -> "\"" + def.id() + "\":\"" + def.selector().replace("\\", "\\\\")
            .replace("\"", "\\\"") + "\"")
        .collect(Collectors.joining(",", "{", "}"));
  }

  private static TourStep step(SerializableFunction<String, String> t, StepDef def) {
    List<TourButton> buttons = new ArrayList<>();
    if (!def.first()) {
      buttons.add(TourButton.builder().label(t.apply(KEY_PREFIX + "btn.back")).secondary(true)
          .type(TourButtonType.PREVIOUS).build());
    }
    buttons
        .add(TourButton.builder().label(t.apply(KEY_PREFIX + (def.last() ? "btn.done" : "btn.next")))
            .type(TourButtonType.NEXT).build());
    String attachTo =
        def.selector() == null ? null : "[" + TARGET_ATTR + "='" + def.id() + "']";
    return TourStep.builder().id(def.id()).attachTo(attachTo).position(def.position())
        .title(t.apply(KEY_PREFIX + def.key() + ".title"))
        .content(t.apply(KEY_PREFIX + def.key() + ".desc")).buttons(buttons).build();
  }
}
