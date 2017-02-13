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
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.everit.jira.analytics.AnalyticsDTO;
import org.everit.jira.analytics.AnalyticsSender;
import org.everit.jira.analytics.event.CreateReportEvent;
import org.everit.jira.core.util.TimetrackerUtil;
import org.everit.jira.reporting.plugin.ReportingCondition;
import org.everit.jira.reporting.plugin.ReportingPlugin;
import org.everit.jira.reporting.plugin.column.WorklogDetailsColumns;
import org.everit.jira.reporting.plugin.dto.ConvertedSearchParam;
import org.everit.jira.reporting.plugin.dto.FilterCondition;
import org.everit.jira.reporting.plugin.dto.IssueSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.OrderBy;
import org.everit.jira.reporting.plugin.dto.ProjectSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.ReportingSessionData;
import org.everit.jira.reporting.plugin.dto.UserForPickerDTO;
import org.everit.jira.reporting.plugin.dto.UserPickerContainerDTO;
import org.everit.jira.reporting.plugin.dto.UserSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.WorklogDetailsReportDTO;
import org.everit.jira.reporting.plugin.exception.JTRPException;
import org.everit.jira.reporting.plugin.util.ConverterUtil;
import org.everit.jira.reporting.plugin.util.PermissionUtil;
import org.everit.jira.settings.TimeTrackerSettingsHelper;
import org.everit.jira.settings.dto.TimeTrackerUserSettings;
import org.everit.jira.timetracker.plugin.DurationFormatter;
import org.everit.jira.timetracker.plugin.JiraTimetrackerAnalytics;
import org.everit.jira.timetracker.plugin.PluginCondition;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.everit.jira.timetracker.plugin.util.PiwikPropertiesUtil;
import org.everit.jira.timetracker.plugin.util.PropertiesUtil;
import org.everit.jira.updatenotifier.UpdateNotifier;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.Avatar.Size;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.filter.DefaultSearchRequestService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.fields.renderer.JiraRendererPlugin;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.user.UserHistoryStore;
import com.atlassian.jira.user.util.UserManager;
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

  private static final int MAXIMUM_HISTORY = 5;

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

  private boolean defaultCommand = false;

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

  private TimeTrackerSettingsHelper settingsHelper;

  private String stacktrace = "";

  private UserPickerContainerDTO userPicker = new UserPickerContainerDTO();

  private TimeTrackerUserSettings userSettings;

  private UserSummaryReportDTO userSummaryReport = new UserSummaryReportDTO();

  private List<String> worklogDetailsAllColumns = WorklogDetailsColumns.ALL_COLUMNS;

  private boolean worklogDetailsEmpty = false;

  private WorklogDetailsReportDTO worklogDetailsReport = new WorklogDetailsReportDTO();

  /**
   * Simple constructor.
   */
  public ReportingWebAction(final ReportingPlugin reportingPlugin,
      final AnalyticsSender analyticsSender,
      final TimeTrackerSettingsHelper settingsHelper) {
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

  private void addUserHistory() {
    UserHistoryManager userHistoryManager =
        ComponentAccessor.getComponent(UserHistoryManager.class);
    JiraAuthenticationContext jiraAuthenticationContext =
        ComponentAccessor.getJiraAuthenticationContext();
    ApplicationUser user = jiraAuthenticationContext.getUser();
    for (String userKey : filterCondition.getUsers()) {
      userHistoryManager.addItemToHistory(UserHistoryItem.USED_USER, user, userKey);
    }
  }

  private String checkConditions() {
    boolean isUserLogged = TimetrackerUtil.isUserLogged();
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

    addUserHistory();
    return SUCCESS;
  }

  private List<UserForPickerDTO> createSuggestedUsers(final ApplicationUser loggedUser) {
    UserHistoryManager userHistoryManager =
        ComponentAccessor.getComponent(UserHistoryManager.class);
    List<UserHistoryItem> usedUserHistories =
        userHistoryManager.getHistory(UserHistoryItem.USED_USER, loggedUser);
    ComponentAccessor.getComponent(UserHistoryStore.class);
    usedUserHistories =
        usedUserHistories.subList(0,
            usedUserHistories.size() > MAXIMUM_HISTORY
                ? MAXIMUM_HISTORY
                : usedUserHistories.size());

    UserManager userManager = ComponentAccessor.getUserManager();
    AvatarService avatarService = ComponentAccessor.getAvatarService();
    List<UserForPickerDTO> suggestedUsers = new ArrayList<>();
    for (UserHistoryItem userHistoryItem : usedUserHistories) {
      ApplicationUser historyUser = userManager.getUserByKey(userHistoryItem.getEntityId());
      if (historyUser == null) {
        continue;
      }

      URI avatarUri =
          avatarService.getAvatarAbsoluteURL(loggedUser, historyUser, Avatar.Size.SMALL);
      UserForPickerDTO userForPickerDTO = new UserForPickerDTO(avatarUri.toString(),
          historyUser.getDisplayName(),
          historyUser.getKey());
      suggestedUsers.add(userForPickerDTO);
    }
    return suggestedUsers;
  }

  private void createUserPickersValue() {
    ApplicationUser loggedUser = ComponentAccessor.getJiraAuthenticationContext().getUser();
    userPicker.setSuggestedUsers(createSuggestedUsers(loggedUser));
    userPicker.setUsers(createUsers(loggedUser, filterCondition.getUsers()));
    userPicker.setIssueReporters(createUsers(loggedUser, filterCondition.getIssueReporters()));
    userPicker.setIssueAssignees(createUsers(loggedUser, filterCondition.getIssueAssignees()));

    AvatarService avatarService = ComponentAccessor.getAvatarService();
    URI avatarURI = avatarService.getAvatarAbsoluteURL(loggedUser, loggedUser, Size.SMALL);
    userPicker.setCurrentUser(new UserForPickerDTO(avatarURI.toString(),
        TimetrackerUtil.getI18nText(UserForPickerDTO.CURRENT_USER_DISPLAY_NAME),
        UserForPickerDTO.CURRENT_USER_KEY));

    String defaultAvaratar =
        avatarService.getProjectDefaultAvatarAbsoluteURL(Size.SMALL).toString();

    userPicker.setNoneUser(new UserForPickerDTO(defaultAvaratar,
        TimetrackerUtil.getI18nText(UserForPickerDTO.NONE_DISPLAY_NAME),
        UserForPickerDTO.NONE_USER_KEY));
    userPicker.setUnassigedUser(new UserForPickerDTO(defaultAvaratar,
        TimetrackerUtil.getI18nText(UserForPickerDTO.UNASSIGNED_DISPLAY_NAME),
        UserForPickerDTO.UNASSIGNED_USER_KEY));

  }

  private List<UserForPickerDTO> createUsers(final ApplicationUser loggedUser,
      final List<String> usersForIteration) {
    List<UserForPickerDTO> users = new ArrayList<>();
    UserManager userManager = ComponentAccessor.getUserManager();
    AvatarService avatarService = ComponentAccessor.getAvatarService();
    for (String userKey : usersForIteration) {
      UserForPickerDTO userForPickerDTO = null;
      if (UserForPickerDTO.NONE_USER_KEY.equals(userKey)) {
        userForPickerDTO = new UserForPickerDTO("",
            TimetrackerUtil.getI18nText(UserForPickerDTO.NONE_DISPLAY_NAME),
            "none");
      } else if (UserForPickerDTO.CURRENT_USER_KEY.equals(userKey)) {
        userForPickerDTO = new UserForPickerDTO("",
            TimetrackerUtil.getI18nText(UserForPickerDTO.CURRENT_USER_DISPLAY_NAME),
            "currentUser");
      } else if (UserForPickerDTO.UNASSIGNED_USER_KEY.equals(userKey)) {
        userForPickerDTO = new UserForPickerDTO("",
            TimetrackerUtil.getI18nText(UserForPickerDTO.UNASSIGNED_DISPLAY_NAME),
            "empty");
      } else {
        ApplicationUser historyUser = userManager.getUserByKey(userKey);
        if (historyUser == null) {
          continue;
        }
        URI avatarUri =
            avatarService.getAvatarAbsoluteURL(loggedUser, historyUser, Avatar.Size.SMALL);
        userForPickerDTO = new UserForPickerDTO(avatarUri.toString(),
            historyUser.getDisplayName(),
            historyUser.getKey());
      }
      users.add(userForPickerDTO);
    }
    return users;
  }

  private void defaultInitalizeData() {
    selectedMore = new ArrayList<>();
    filterCondition = new FilterCondition();
    if (!hasBrowseUsersPermission) {
      filterCondition.setUsers(Arrays.asList(UserForPickerDTO.CURRENT_USER_KEY));
    }
    String[] selectedWorklogDetailsColumnsArray =
        gson.fromJson(userSettings.getUserSelectedColumns(), String[].class);
    selectedWorklogDetailsColumns = Arrays.asList(selectedWorklogDetailsColumnsArray);
    initDatesIfNecessary();
  }

  @Override
  public String doDefault() throws ParseException {
    defaultCommand = true;
    String checkConditionsResult = checkConditions();
    if (checkConditionsResult != null) {
      return checkConditionsResult;
    }
    loadSettings();

    loadFavoriteFilters();

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
    loadSettings();
    normalizeContextPath();

    loadFavoriteFilters();
    hasBrowseUsersPermission =
        PermissionUtil.hasBrowseUserPermission(getLoggedInApplicationUser(), settingsHelper);

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

    createUserPickersValue();

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

  public DateTimeFormatter getDateTimeFormatterDate() {
    return super.getDateTimeFormatter().withStyle(DateTimeStyle.DATE).withSystemZone();
    // TODO with SystemZone?
  }

  public DateTimeFormatter getDateTimeFormatterDateTime() {
    return super.getDateTimeFormatter().withStyle(DateTimeStyle.COMPLETE).withSystemZone();
    // TODO with SystemZone?
  }

  public DurationFormatter getDurationFormatter() {
    return durationFormatter;
  }

  /**
   * Get end date for date picker.
   */
  public String getEndDateInJSDatePickerFormat() {
    return super.getDateTimeFormatter().withStyle(DateTimeStyle.DATE_PICKER)
        .withZone(DateTimeZone.UTC.toTimeZone())
        .format(new Date(filterCondition.getWorklogEndDate()));
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
    return isShowTutorialDialog && defaultCommand;
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

  /**
   * Get issue creation date for date picker.
   */
  public String getIsueCreateDateInJSFormat() {
    if (filterCondition.getIssueCreateDate() == null) {
      return "";
    } else {
      return super.getDateTimeFormatter().withStyle(DateTimeStyle.DATE_PICKER)
          .withZone(DateTimeZone.UTC.toTimeZone())
          .format(new Date(filterCondition.getIssueCreateDate()));
    }

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

  public String getStacktrace() {
    return stacktrace;
  }

  /**
   * Get start date for date picker.
   */
  public String getStartDateInJSDatePickerFormat() {
    return super.getDateTimeFormatter().withStyle(DateTimeStyle.DATE_PICKER)
        .withZone(DateTimeZone.UTC.toTimeZone())
        .format(new Date(filterCondition.getWorklogStartDate()));
  }

  public UserPickerContainerDTO getUserPicker() {
    return userPicker;
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
      DateTime dateFrom = new DateTime(TimetrackerUtil.getLoggedUserTimeZone());
      dateFrom = dateFrom.minusWeeks(1);
      filterCondition
          .setWorklogStartDate(DateTimeConverterUtil.convertDateTimeToDate(dateFrom).getTime());
    }
    if (filterCondition.getWorklogEndDate() == null) {
      DateTime dateTo = new DateTime(TimetrackerUtil.getLoggedUserTimeZone());
      filterCondition
          .setWorklogEndDate(DateTimeConverterUtil.convertDateTimeToDate(dateTo).getTime());
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

    createUserPickersValue();
  }

  public boolean isCollapsedDetailsModule() {
    return collapsedDetailsModule;
  }

  public boolean isCollapsedSummaryModule() {
    return collapsedSummaryModule;
  }

  public boolean isDefaultCommand() {
    return defaultCommand;
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

  private void loadIssueCollectorSrc() {
    Properties properties = PropertiesUtil.getJttpBuildProperties();
    issueCollectorSrc = properties.getProperty(PropertiesUtil.ISSUE_COLLECTOR_SRC);
  }

  private void loadSettings() {
    userSettings = settingsHelper.loadUserSettings();
    isShowTutorialDialog = userSettings.getIsShowTutorialDialog();
    pageSizeLimit = userSettings.getPageSize();
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
