/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.jira.reporting.plugin.web;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.everit.jira.analytics.AnalyticsDTO;
import org.everit.jira.analytics.AnalyticsSender;
import org.everit.jira.analytics.event.CreateReportEvent;
import org.everit.jira.reporting.plugin.ReportingCondition;
import org.everit.jira.reporting.plugin.ReportingPlugin;
import org.everit.jira.reporting.plugin.column.WorklogDetailsColumns;
import org.everit.jira.reporting.plugin.dto.ConvertedSearchParam;
import org.everit.jira.reporting.plugin.dto.FilterCondition;
import org.everit.jira.reporting.plugin.dto.IssueSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.OrderBy;
import org.everit.jira.reporting.plugin.dto.PickerUserDTO;
import org.everit.jira.reporting.plugin.dto.ProjectSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.ReportingSessionData;
import org.everit.jira.reporting.plugin.dto.UserSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.WorklogDetailsReportDTO;
import org.everit.jira.reporting.plugin.exception.JTRPException;
import org.everit.jira.reporting.plugin.util.ConverterUtil;
import org.everit.jira.reporting.plugin.util.PermissionUtil;
import org.everit.jira.settings.TimetrackerSettingsHelper;
import org.everit.jira.settings.dto.TimeTrackerUserSettings;
import org.everit.jira.timetracker.plugin.DurationFormatter;
import org.everit.jira.timetracker.plugin.JiraTimetrackerAnalytics;
import org.everit.jira.timetracker.plugin.PluginCondition;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.everit.jira.timetracker.plugin.util.JiraTimetrackerUtil;
import org.everit.jira.timetracker.plugin.util.PiwikPropertiesUtil;
import org.everit.jira.timetracker.plugin.util.PropertiesUtil;
import org.everit.jira.updatenotifier.UpdateNotifier;

import com.atlassian.jira.bc.filter.DefaultSearchRequestService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.fields.renderer.JiraRendererPlugin;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.google.gson.Gson;

/**
 * Repoting page web action.
 */
public class ReportingWebAction extends JiraWebActionSupport {

  private static final String HTTP_PARAM_COLLAPSED_DETAILS_MODULE = "collapsedDetailsModule";

  private static final String HTTP_PARAM_COLLAPSED_SUMMARY_MODULE = "collapsedSummaryModule";

  private static final String HTTP_PARAM_FILTER_CONDITION_JSON = "filterConditionJson";

  private static final String HTTP_PARAM_REPORTING_TUTORIAL_BUTTON = "reporting-tutorial-button";

  private static final String HTTP_PARAM_SELECTED_ACTIVE_TAB = "selectedActiveTab";

  private static final String HTTP_PARAM_SELECTED_MORE_JSON = "selectedMoreJson";

  private static final String HTTP_PARAM_SELECTED_WORKLOG_DETAILS_COLUMNS =
      "selectedWorklogDetailsColumns";

  private static final String HTTP_PARAM_TUTORIAL_DNS = "tutorial_dns";

  private static final String JIRA_HOME_URL = "/secure/Dashboard.jspa";

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;

  private static final String SESSION_KEY = "jtrpSessionDataKey";

  private AnalyticsDTO analyticsDTO;

  private AnalyticsSender analyticsSender;

  private JiraRendererPlugin atlassianWikiRenderer;

  private boolean collapsedDetailsModule = false;

  private boolean collapsedSummaryModule = false;

  private String contextPath;

  private Class<DateTimeConverterUtil> dateConverterUtil = DateTimeConverterUtil.class;

  private DurationFormatter durationFormatter = new DurationFormatter();

  public List<SearchRequest> favouriteFilters;

  private FilterCondition filterCondition;

  private String filterConditionJson = "";

  private Gson gson;

  public boolean hasBrowseUsersPermission = true;

  public boolean isShowTutorialDialog = false;

  private String issueCollectorSrc;

  private IssueRenderContext issueRenderContext;

  private IssueSummaryReportDTO issueSummaryReport = new IssueSummaryReportDTO();

  /**
   * The message.
   */
  private String message = "";

  private List<String> notBrowsableProjectKeys = Collections.emptyList();

  private String order = "ASC";

  private String orderColumn = WorklogDetailsColumns.ISSUE_KEY;

  private int pageSizeLimit;

  private PluginCondition pluginCondition;

  private ProjectSummaryReportDTO projectSummaryReport = new ProjectSummaryReportDTO();

  private ReportingCondition reportingCondition;

  private ReportingPlugin reportingPlugin;

  private String selectedActiveTab = "tabs-project";

  private List<String> selectedMore = Collections.emptyList();

  private List<String> selectedWorklogDetailsColumns = Collections.emptyList();

  private TimetrackerSettingsHelper settingsHelper;

  private UserSummaryReportDTO userSummaryReport = new UserSummaryReportDTO();

  private List<String> worklogDetailsAllColumns = WorklogDetailsColumns.ALL_COLUMNS;

  private boolean worklogDetailsEmpty = false;

  private WorklogDetailsReportDTO worklogDetailsReport = new WorklogDetailsReportDTO();

  /**
   * Simple constructor.
   */
  public ReportingWebAction(final ReportingPlugin reportingPlugin,
      final AnalyticsSender analyticsSender,
      final TimetrackerSettingsHelper settingsHelper) {
    this.reportingPlugin = reportingPlugin;
    reportingCondition = new ReportingCondition(settingsHelper);
    gson = new Gson();
    pluginCondition = new PluginCondition(settingsHelper);
    issueRenderContext = new IssueRenderContext(null);
    RendererManager rendererManager = ComponentAccessor.getRendererManager();
    atlassianWikiRenderer = rendererManager.getRendererForType("atlassian-wiki-renderer");
    this.settingsHelper = settingsHelper;
    this.analyticsSender = analyticsSender;
  }

  private String checkConditions() {
    boolean isUserLogged = JiraTimetrackerUtil.isUserLogged();
    if (!isUserLogged) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    if (!reportingCondition.shouldDisplay(getLoggedInApplicationUser(), null)) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    if (!pluginCondition.shouldDisplay(getLoggedInApplicationUser(), null)) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    return null;
  }

  private String createReport(final String selectedMoreJson, final String selectedActiveTab,
      final String filterConditionJsonValue, final String selectedWorklogDetailsColumnsJson,
      final String collapsedDetailsModuleVal, final String collapsedSummaryModuleVal) {

    morePickerParse(selectedMoreJson);
    setParametersActiveTab(selectedActiveTab, collapsedDetailsModuleVal, collapsedSummaryModuleVal);

    ConvertedSearchParam convertedSearchParam = null;
    filterConditionJson = filterConditionJsonValue;
    String[] selectedWorklogDetailsColumnsArray =
        gson.fromJson(selectedWorklogDetailsColumnsJson, String[].class);
    selectedWorklogDetailsColumns = Arrays.asList(selectedWorklogDetailsColumnsArray);

    try {
      filterCondition = ConverterUtil.convertJsonToFilterCondition(filterConditionJson);
      convertedSearchParam = ConverterUtil
          .convertFilterConditionToConvertedSearchParam(filterCondition, settingsHelper);
    } catch (IllegalArgumentException e) {
      message = e.getMessage();
      return INPUT;
    }

    try {
      worklogDetailsReport =
          reportingPlugin.getWorklogDetailsReport(convertedSearchParam.reportSearchParam,
              OrderBy.DEFAULT);
      if (worklogDetailsReport.getWorklogDetailsCount() == 0) {
        worklogDetailsEmpty = true;
      }
      projectSummaryReport =
          reportingPlugin.getProjectSummaryReport(convertedSearchParam.reportSearchParam);

      issueSummaryReport =
          reportingPlugin.getIssueSummaryReport(convertedSearchParam.reportSearchParam);

      userSummaryReport =
          reportingPlugin.getUserSummaryReport(convertedSearchParam.reportSearchParam);

      notBrowsableProjectKeys = convertedSearchParam.notBrowsableProjectKeys;
    } catch (JTRPException e) {
      message = e.getMessage();
      return INPUT;
    }

    return SUCCESS;
  }

  private void defaultInitalizeData() {
    selectedMore = new ArrayList<>();
    filterCondition = new FilterCondition();
    if (!hasBrowseUsersPermission) {
      filterCondition.setUsers(Arrays.asList(PickerUserDTO.CURRENT_USER_NAME));
    }
    String selectedWorklogDetailsColumnsJson = loadUserWorklogDetialsSelectedColumnsJson();
    String[] selectedWorklogDetailsColumnsArray =
        gson.fromJson(selectedWorklogDetailsColumnsJson, String[].class);
    selectedWorklogDetailsColumns = Arrays.asList(selectedWorklogDetailsColumnsArray);
    initDatesIfNecessary();
  }

  @Override
  public String doDefault() throws ParseException {
    String checkConditionsResult = checkConditions();
    if (checkConditionsResult != null) {
      return checkConditionsResult;
    }

    loadFavoriteFilters();

    loadPageSizeLimit();
    loadIsShowTutorial();

    loadIssueCollectorSrc();
    normalizeContextPath();
    hasBrowseUsersPermission =
        PermissionUtil.hasBrowseUserPermission(getLoggedInApplicationUser(), settingsHelper);

    analyticsDTO = JiraTimetrackerAnalytics
        .getAnalyticsDTO(PiwikPropertiesUtil.PIWIK_REPORTING_SITEID, settingsHelper);

    initializeData();

    return INPUT;
  }

  @Override
  public String doExecute() throws ParseException {
    String checkConditionsResult = checkConditions();
    if (checkConditionsResult != null) {
      return checkConditionsResult;
    }

    normalizeContextPath();

    loadFavoriteFilters();
    hasBrowseUsersPermission =
        PermissionUtil.hasBrowseUserPermission(getLoggedInApplicationUser(), settingsHelper);

    loadPageSizeLimit();
    loadIssueCollectorSrc();

    analyticsDTO = JiraTimetrackerAnalytics
        .getAnalyticsDTO(PiwikPropertiesUtil.PIWIK_REPORTING_SITEID, settingsHelper);

    HttpServletRequest httpRequest = getHttpRequest();

    if (httpRequest.getParameter(HTTP_PARAM_REPORTING_TUTORIAL_BUTTON) != null) {
      boolean isDoNotShow = true;
      if (httpRequest.getParameter(HTTP_PARAM_TUTORIAL_DNS) != null) {
        isDoNotShow = false;
      }
      saveIsShowTutorial(isDoNotShow);
      initializeData();
      return SUCCESS;
    }

    String selectedMoreJson = httpRequest.getParameter(HTTP_PARAM_SELECTED_MORE_JSON);
    String selectedActiveTab = httpRequest.getParameter(HTTP_PARAM_SELECTED_ACTIVE_TAB);
    String filterConditionJsonValue = httpRequest.getParameter(HTTP_PARAM_FILTER_CONDITION_JSON);
    String selectedWorklogDetailsColumnsJson =
        httpRequest.getParameter(HTTP_PARAM_SELECTED_WORKLOG_DETAILS_COLUMNS);
    String collapsedDetailsModuleVal =
        httpRequest.getParameter(HTTP_PARAM_COLLAPSED_DETAILS_MODULE);
    String collapsedSummaryModuleVal =
        httpRequest.getParameter(HTTP_PARAM_COLLAPSED_SUMMARY_MODULE);
    String createReportResult =
        createReport(selectedMoreJson, selectedActiveTab, filterConditionJsonValue,
            selectedWorklogDetailsColumnsJson, collapsedDetailsModuleVal,
            collapsedSummaryModuleVal);
    if (SUCCESS.equals(createReportResult)) {
      saveDataToSession(selectedMoreJson, selectedActiveTab, filterConditionJsonValue,
          selectedWorklogDetailsColumnsJson, collapsedDetailsModuleVal,
          collapsedSummaryModuleVal);
      saveUserWorklogDetialsSelectedColumns(selectedWorklogDetailsColumnsJson);
      CreateReportEvent analyticsEvent =
          new CreateReportEvent(analyticsDTO.getInstalledPluginId(), filterCondition,
              selectedWorklogDetailsColumns, selectedActiveTab);
      analyticsSender.send(analyticsEvent);
    }
    return createReportResult;
  }

  public AnalyticsDTO getAnalyticsDTO() {
    return analyticsDTO;
  }

  public JiraRendererPlugin getAtlassianWikiRenderer() {
    return atlassianWikiRenderer;
  }

  public String getContextPath() {
    return contextPath;
  }

  public Class<DateTimeConverterUtil> getDateConverterUtil() {
    return dateConverterUtil;
  }

  public DurationFormatter getDurationFormatter() {
    return durationFormatter;
  }

  public List<SearchRequest> getFavouriteFilters() {
    return favouriteFilters;
  }

  public FilterCondition getFilterCondition() {
    return filterCondition;
  }

  public String getFilterConditionJson() {
    return filterConditionJson;
  }

  public boolean getHasBrowseUsersPermission() {
    return hasBrowseUsersPermission;
  }

  public boolean getIsShowTutorialDialog() {
    return isShowTutorialDialog;
  }

  public String getIssueCollectorSrc() {
    return issueCollectorSrc;
  }

  public IssueRenderContext getIssueRenderContext() {
    return issueRenderContext;
  }

  public IssueSummaryReportDTO getIssueSummaryReport() {
    return issueSummaryReport;
  }

  public String getMessage() {
    return message;
  }

  public List<String> getNotBrowsableProjectKeys() {
    return notBrowsableProjectKeys;
  }

  public String getOrder() {
    return order;
  }

  public String getOrderColumn() {
    return orderColumn;
  }

  public int getPageSizeLimit() {
    return pageSizeLimit;
  }

  public ProjectSummaryReportDTO getProjectSummaryReport() {
    return projectSummaryReport;
  }

  public String getSelectedActiveTab() {
    return selectedActiveTab;
  }

  public List<String> getSelectedMore() {
    return selectedMore;
  }

  public List<String> getSelectedWorklogDetailsColumns() {
    return selectedWorklogDetailsColumns;
  }

  public UserSummaryReportDTO getUserSummaryReport() {
    return userSummaryReport;
  }

  public List<String> getWorklogDetailsAllColumns() {
    return worklogDetailsAllColumns;
  }

  public boolean getWorklogDetailsEmpty() {
    return worklogDetailsEmpty;
  }

  public WorklogDetailsReportDTO getWorklogDetailsReport() {
    return worklogDetailsReport;
  }

  private void initDatesIfNecessary() {
    if (filterCondition.getWorklogStartDate() == null) {
      Calendar calendarFrom = Calendar.getInstance();
      calendarFrom.add(Calendar.WEEK_OF_MONTH, -1);
      filterCondition.setWorklogStartDate(calendarFrom.getTimeInMillis());
    }
    if (filterCondition.getWorklogEndDate() == null) {
      Calendar calendarTo = Calendar.getInstance();
      // Date dateTo = calendarTo.getTime();
      filterCondition.setWorklogEndDate(calendarTo.getTimeInMillis());
    }
  }

  private void initializeData() {
    ReportingSessionData loadDataFromSession = loadDataFromSession();
    if (loadDataFromSession != null) {
      FilterCondition filterConditionFromSession =
          ConverterUtil.convertJsonToFilterCondition(loadDataFromSession.filterConditionJson);
      filterConditionFromSession.setLimit(Long.valueOf(pageSizeLimit));
      String filterConditionJsonFixedPageSize =
          ConverterUtil.convertFilterConditionToJson(filterConditionFromSession);
      // String createReportResult =
      createReport(loadDataFromSession.selectedMoreJson, loadDataFromSession.selectedActiveTab,
          filterConditionJsonFixedPageSize,
          loadDataFromSession.selectedWorklogDetailsColumnsJson,
          loadDataFromSession.collapsedDetailsModuleVal,
          loadDataFromSession.collapsedSummaryModuleVal);
      // FIXME This check is necessary because of the date parse errors not handeled well. In the
      // feature
      // try to avoid the formated dates store, better if we user timestamp
      // if (!SUCCESS.equals(createReportResult)) {
      // message = "";
      // defaultInitalizeData();
      // }
    } else {
      defaultInitalizeData();
    }
  }

  public boolean isCollapsedDetailsModule() {
    return collapsedDetailsModule;
  }

  public boolean isCollapsedSummaryModule() {
    return collapsedSummaryModule;
  }

  private ReportingSessionData loadDataFromSession() {
    HttpSession session = getHttpSession();
    Object data = session.getAttribute(SESSION_KEY);

    if (!(data instanceof ReportingSessionData)) {
      return null;
    }
    return (ReportingSessionData) data;
  }

  private void loadFavoriteFilters() {
    DefaultSearchRequestService defaultSearchRequestService =
        ComponentAccessor.getComponentOfType(DefaultSearchRequestService.class);
    favouriteFilters = new ArrayList<>(
        defaultSearchRequestService.getFavouriteFilters(getLoggedInApplicationUser()));
  }

  private void loadIsShowTutorial() {
    isShowTutorialDialog = settingsHelper.loadUserSettings().getIsShowTutorialDialog();
  }

  private void loadIssueCollectorSrc() {
    Properties properties = PropertiesUtil.getJttpBuildProperties();
    issueCollectorSrc = properties.getProperty(PropertiesUtil.ISSUE_COLLECTOR_SRC);
  }

  private void loadPageSizeLimit() {
    pageSizeLimit = settingsHelper.loadUserSettings().getPageSize();
  }

  private String loadUserWorklogDetialsSelectedColumnsJson() {
    return settingsHelper.loadUserSettings().getUserSelectedColumns();
  }

  private void morePickerParse(final String selectedMoreJson) {
    if (selectedMoreJson != null) {
      String[] selectedMore = gson.fromJson(selectedMoreJson, String[].class);
      this.selectedMore = Arrays.asList(selectedMore);
    }
  }

  private void normalizeContextPath() {
    String path = getHttpRequest().getContextPath();
    if ((path.length() > 0) && "/".equals(path.substring(path.length() - 1))) {
      contextPath = path.substring(0, path.length() - 1);
    } else {
      contextPath = path;
    }
  }

  private void readObject(final java.io.ObjectInputStream stream) throws IOException,
      ClassNotFoundException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }

  /**
   * Decide render or not the update information bar.
   *
   * @return true if bar should be render
   */
  public boolean renderUpdateNotifier() {
    return new UpdateNotifier(settingsHelper).isShowUpdater();
  }

  private void saveDataToSession(final String selectedMoreJson, final String selectedActiveTab,
      final String filterConditionJsonValue, final String selectedWorklogDetailsColumnsJson,
      final String collapsedDetailsModuleVal, final String collapsedSummaryModuleVal) {
    HttpSession session = getHttpSession();
    session.setAttribute(SESSION_KEY,
        new ReportingSessionData().selectedMoreJson(selectedMoreJson)
            .selectedActiveTab(selectedActiveTab).filterConditionJson(filterConditionJsonValue)
            .selectedWorklogDetailsColumnsJson(selectedWorklogDetailsColumnsJson)
            .collapsedDetailsModuleVal(collapsedDetailsModuleVal)
            .collapsedSummaryModuleVal(collapsedSummaryModuleVal));
  }

  private void saveIsShowTutorial(final boolean isDoNotShow) {
    TimeTrackerUserSettings userSettings =
        new TimeTrackerUserSettings().isShowTutorialDialog(isDoNotShow);
    settingsHelper.saveUserSettings(userSettings);
  }

  private void saveUserWorklogDetialsSelectedColumns(final String selectedColumnsJson) {
    TimeTrackerUserSettings userSettings =
        new TimeTrackerUserSettings().selectedColumnsJSon(selectedColumnsJson);
    settingsHelper.saveUserSettings(userSettings);
  }

  public void setContextPath(final String contextPath) {
    this.contextPath = contextPath;
  }

  public void setFavouriteFilters(final List<SearchRequest> favouriteFilters) {
    this.favouriteFilters = favouriteFilters;
  }

  public void setHasBrowseUsersPermission(final boolean hasBrowseUsersPermission) {
    this.hasBrowseUsersPermission = hasBrowseUsersPermission;
  }

  public void setIsShowTutorialDialog(final boolean isShowTutorialDialog) {
    this.isShowTutorialDialog = isShowTutorialDialog;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  private void setParametersActiveTab(final String selectedActiveTab,
      final String collapsedDetailsModuleVal, final String collapsedSummaryModuleVal) {
    if (selectedActiveTab != null) {
      this.selectedActiveTab = selectedActiveTab;
    }

    collapsedDetailsModule = Boolean.parseBoolean(collapsedDetailsModuleVal);
    collapsedSummaryModule = Boolean.parseBoolean(collapsedSummaryModuleVal);
  }

  public void setSelectedMore(final List<String> selectedMore) {
    this.selectedMore = selectedMore;
  }

  public void setWorklogDetailsEmpty(final boolean worklogDetailsEmpty) {
    this.worklogDetailsEmpty = worklogDetailsEmpty;
  }

  private void writeObject(final java.io.ObjectOutputStream stream) throws IOException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }

}
