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
everit.survey = everit.survey || {};
everit.survey.main = everit.survey.main || {};

function isOnline(yes) {
  var xhr = XMLHttpRequest ? new XMLHttpRequest() : new ActiveXObject('Microsoft.XMLHttp');
  xhr.onload = function() {
    if (yes instanceof Function) {
      yes();
    }
  }
  xhr.onerror = function() {
    // do nothing
  }
  xhr.open("GET", "anypage.php", true);
  xhr.send();
}

AJS.$(function() {
  setTimeout(isOnline(function() {
    var surveyDialogUrl = everit.survey.main.options.contextPath
        + "/secure/SurveyDialogWebAction!default.jspa?decorator=dialog&inline=true";

    AJS.$.get(surveyDialogUrl, function(data) {

      var $data = AJS.$(data);
      var surveyScriptUrl = $data.find('#surveyScriptUrl').val();

      AJS.$.getScript(surveyScriptUrl, function() {
        if (!showSurvey()) {
          return false;
        }

        var lastSavedSurveyDate = $data.find('#lastSavedSurveyDate').val();
        if (lastSavedSurveyDate == ""
            || new Date(lastSavedSurveyDate).getTime() < getSurveyStartDate().getTime()) {
          AJS.$('body').append(data);
          AJS.dialog2('#jttp-survey-dialog').show();
          AJS.$('#survey-frame-spinner').spin('large');
        }
      });
    });
  }), 0);
});

(function(survey, jQuery) {
  
  survey.finishSurvey = function() {
    var checked = jQuery('#finishSurvey_dns').attr('checked');
    if(checked){
      var surveyDialogUrl = everit.survey.main.options.contextPath
          + "/secure/SurveyDialogWebAction.jspa?action=save";
      AJS.$.get(surveyDialogUrl, function(data) {
        // do nothing
      });
    }
    AJS.dialog2('#jttp-survey-dialog').hide();
    return true;
  }
  
})(everit.survey.main, jQuery);
