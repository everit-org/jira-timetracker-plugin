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

window.everit = window.everit || {};
everit.jttp = everit.jttp || {};
everit.jttp.report_common_scripts = everit.jttp.report_common_scripts || {};

(function(jttp, jQuery) {

  jQuery(document).ready(function() {

    var opt = jttp.options;

    var calFrom = Calendar.setup({
      firstDay : opt.firstDay,
      inputField : jQuery("#dateFrom"),
      button : jQuery("#date_trigger_from"),
      date : opt.dateFromFormated,
      align : 'Br',
      electric : false,
      singleClick : true,
      showOthers : true,
      useISO8601WeekNumbers : opt.useISO8601,
      ifFormat : '%Y-%m-%d'
    });

    var calTo = Calendar.setup({
      firstDay : opt.firstDay,
      inputField : jQuery("#dateTo"),
      button : jQuery("#date_trigger_to"),
      date : opt.dateToFormated,
      align : 'Br',
      electric : false,
      singleClick : true,
      showOthers : true,
      useISO8601WeekNumbers : opt.useISO8601,
      ifFormat : '%Y-%m-%d'
    });

    jQuery('.aui-ss, .aui-ss-editing, .aui-ss-field').attr("style", "width: 300px;");

  });

  jttp.jttpSearchOnClick = function(reportName) {
    var loggedUserIn = AJS.params.loggedInUser;
    var selectedUser = document.getElementById("userPicker").value;
    if (loggedUserIn == selectedUser) {
      _paq.push([ 'trackEvent', 'User', 'My' + reportName ]);
    } else {
      _paq.push([ 'trackEvent', 'User', 'Others' + reportName ]);
    }
  }

 jttp.checkToEnter = function(event) {
    if (event.keyCode == 13) {
      jQuery("#search").click();
      return false;
    }
  }

 jttp.checkEnter = function(event) {
    if (event.keyCode == 13) {
      jQuery("#dateTo").focus();
      return false;
    }
  }

})(everit.jttp.report_common_scripts, jQuery);