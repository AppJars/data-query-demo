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
package com.appjars.dataquery.demo.views;

import com.appjars.dataquery.demo.views.tour.DemoTours;
import com.appjars.dataquery.demo.views.tour.DemoTours.DemoTour;
import com.appjars.dataquery.flow.util.RouteConfigurer;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;
import java.util.Arrays;
import java.util.Optional;

/** Main layout for the demo views. */
@SuppressWarnings("serial")
public class MainLayout extends AppLayout implements AfterNavigationObserver {

  private H2 viewTitle;
  private SubMenu tourSubMenu;
  private final RouteConfigurer routeConfigurer;

  public MainLayout(RouteConfigurer routeConfigurer) {
    this.routeConfigurer = routeConfigurer;
    setPrimarySection(Section.DRAWER);
    addDrawerContent();
    addHeaderContent();
  }

  private void addHeaderContent() {
    DrawerToggle toggle = new DrawerToggle();
    toggle.setAriaLabel(getTranslation("appjars.dataquery.demo.layout.menutoggle"));

    viewTitle = new H2();
    viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

    HorizontalLayout titleLayout = new HorizontalLayout();
    titleLayout.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.Gap.XSMALL);
    titleLayout.setWidthFull();
    titleLayout.add(toggle, viewTitle, createTourMenu());
    titleLayout.expand(viewTitle);

    addToNavbar(titleLayout);
  }

  private MenuBar createTourMenu() {
    MenuBar menu = new MenuBar();
    menu.addClassName("navbar-tour-menu");
    menu.setOpenOnHover(true);
    menu.addThemeVariants(MenuBarVariant.LUMO_PRIMARY);
    tourSubMenu = menu
        .addItem(new Div(VaadinIcon.MAP_MARKER.create(),
            new Span(getTranslation("appjars.dataquery.demo.layout.tour"))))
        .getSubMenu();
    refreshTourMenu();
    return menu;
  }

  private void refreshTourMenu() {
    tourSubMenu.removeAll();
    MenuItem thisPage = tourSubMenu
        .addItem(getTranslation("appjars.dataquery.demo.layout.tour.thispage"),
            e -> startCurrentTour());
    thisPage.setEnabled(currentTour().isPresent());
    tourSubMenu.addSeparator();

    addTourItem("queries", DemoTour.QUERIES, VaadinIcon.TABLE);
    addTourItem("queryform", DemoTour.QUERY_FORM, VaadinIcon.EDIT);
    addTourItem("categories", DemoTour.CATEGORIES, VaadinIcon.TAGS);
    addTourItem("report", DemoTour.REPORT, VaadinIcon.CHART);
    addTourItem("designer", DemoTour.REPORT_DESIGNER, VaadinIcon.PAINTBRUSH);
    addTourItem("dashboards", DemoTour.DASHBOARDS, VaadinIcon.DASHBOARD);
    addTourItem("dashboardform", DemoTour.DASHBOARD_FORM, VaadinIcon.EDIT);
    addTourItem("dashboard", DemoTour.DASHBOARD, VaadinIcon.DASHBOARD);
  }

  private void addTourItem(String view, DemoTour tour, VaadinIcon icon) {
    Div content = new Div(icon.create(), new Span(tourLabel(view)));
    content.addClassName("tour-menu-item");
    tourSubMenu.addItem(content, e -> startTour(tour));
  }

  private String tourLabel(String view) {
    return getTranslation("appjars.dataquery.demo.home.tour." + view);
  }

  private void startTour(DemoTour tour) {
    if (DemoTours.viewOf(tour).equals(currentView())) {
      runTour(tour);
    } else {
      getUI().ifPresent(ui -> DemoTours.startLater(tour, ui, routeConfigurer));
    }
  }

  private void startCurrentTour() {
    currentTour().ifPresent(this::runTour);
  }

  private void runTour(DemoTour tour) {
    DemoTours.start(tour, this, this::getTranslation);
  }

  private Optional<DemoTour> currentTour() {
    Class<?> current = currentView();
    return Arrays.stream(DemoTour.values())
        .filter(tour -> DemoTours.viewOf(tour).equals(current)).findFirst();
  }

  private Class<?> currentView() {
    return getContent() == null ? null : getContent().getClass();
  }

  private void addDrawerContent() {
    Image logo = new Image("icons/icon.png", null);
    logo.setHeight("5vh");
    logo.setWidth("5vh");

    H1 appName = new H1(getTranslation("appjars.dataquery.demo.layout.drawertitle"));
    appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
    Header header = new Header(logo, appName);
    header.addClassNames(LumoUtility.Display.FLEX, LumoUtility.Gap.SMALL,
        LumoUtility.AlignItems.CENTER);
    header.getStyle().set("padding", "var(--lumo-space-m)");

    Scroller scroller = new Scroller(createNavigation());

    VerticalLayout drawerContainer = new VerticalLayout(header, scroller);
    drawerContainer.getStyle().set("position", "relative");
    drawerContainer.setSizeFull();
    drawerContainer.setAlignItems(Alignment.STRETCH);
    drawerContainer.getStyle().set("overflow", "hidden");
    drawerContainer.setPadding(false);
    drawerContainer.setSpacing(false);
    drawerContainer.setFlexGrow(1, scroller);

    addToDrawer(drawerContainer);
  }

  private SideNav createNavigation() {
    SideNav nav = new SideNav();

    SideNavItem homeItem =
        new SideNavItem(getTranslation("appjars.dataquery.demo.menuitem.home"), HomeView.class);
    homeItem.setPrefixComponent(VaadinIcon.HOME.create());

    SideNavItem dataQueryItem =
        new SideNavItem(getTranslation("appjars.dataquery.demo.menuitem.dataquery"));
    dataQueryItem.setPrefixComponent(VaadinIcon.DATABASE.create());
    dataQueryItem.setExpanded(true);

    dataQueryItem.addItem(navItem("queries", routeConfigurer.getQueriesUrl(), VaadinIcon.TABLE));
    dataQueryItem.addItem(
        navItem("queryform", routeConfigurer.getQueriesCreateUrl(), VaadinIcon.PLUS));
    dataQueryItem.addItem(
        navItem("categories", routeConfigurer.getCategoriesUrl(), VaadinIcon.TAGS));
    dataQueryItem.addItem(navItem("report",
        routeConfigurer.getReportBaseUrl() + "/" + DemoTours.SALES_BY_REGION_QUERY_KEY,
        VaadinIcon.CHART));
    dataQueryItem.addItem(navItem("designer",
        routeConfigurer.getReportDesignerBaseUrl() + "/" + DemoTours.SALES_BY_REGION_QUERY_KEY,
        VaadinIcon.PAINTBRUSH));
    dataQueryItem.addItem(
        navItem("dashboards", routeConfigurer.getDashboardsUrl(), VaadinIcon.DASHBOARD));
    dataQueryItem.addItem(
        navItem("dashboardform", routeConfigurer.getDashboardsCreateUrl(), VaadinIcon.PLUS));
    dataQueryItem.addItem(navItem("dashboard",
        routeConfigurer.getDashboardBaseUrl() + "/" + DemoTours.SALES_OVERVIEW_SLUG,
        VaadinIcon.DASHBOARD));

    nav.addItem(homeItem, dataQueryItem);

    return nav;
  }

  private SideNavItem navItem(String key, String path, VaadinIcon icon) {
    SideNavItem item = new SideNavItem(
        getTranslation("appjars.dataquery.demo.menuitem." + key), path);
    item.setPrefixComponent(icon.create());
    return item;
  }

  @Override
  public void afterNavigation(AfterNavigationEvent event) {
    viewTitle.setText(getCurrentPageTitle());
    refreshTourMenu();
    startPendingTour();
  }

  private String getCurrentPageTitle() {
    if (getContent() instanceof HasDynamicTitle titled) {
      return titled.getPageTitle();
    }
    PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
    return title == null ? "" : title.value();
  }

  private void startPendingTour() {
    VaadinSession session = VaadinSession.getCurrent();
    if (session.getAttribute(DemoTours.PENDING_TOUR_ATTRIBUTE) instanceof DemoTour pending
        && DemoTours.viewOf(pending).equals(currentView())) {
      session.setAttribute(DemoTours.PENDING_TOUR_ATTRIBUTE, null);
      runTour(pending);
    }
  }
}
