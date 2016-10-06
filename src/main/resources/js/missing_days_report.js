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
window.missing_days_report = window.missing_days_report || {}
everit.jttp.missing_days_report = everit.jttp.missing_days_report || {};

(function(jttp, jQuery) {

  jQuery(document).ready(function() {
    jttp.setCheckWorkedHours();
  });
  
  jttp.setCheckWorkedHours = function() {
    if (jQuery("#hour").is(":checked")) {
      document.getElementById("nonworking").disabled = false;
    } else {
      document.getElementById("nonworking").disabled = true;
    }
  }
  
  jttp.beforeSubmitMissingsReport = function() {
    try{
      var dateFrom = jQuery('#dateFrom').val();
      var dateFromMil = Date.parseDate(dateFrom, everit.jttp.report_common_scripts.options.dateFormat);
      if(dateFrom != dateFromMil.print(everit.jttp.report_common_scripts.options.dateFormat)){
        showErrorMessage("error_message_label_df");
        return false;
      }
      jQuery('#dateFromMil').val(dateFromMil.getTime());
    }catch(err){
      showErrorMessage("error_message_label_df");
      return false;
    }
    try{
      var dateTo = jQuery('#dateTo').val();
      var dateToMil = Date.parseDate(dateTo, everit.jttp.report_common_scripts.options.dateFormat);
      if(dateTo != dateToMil.print(everit.jttp.report_common_scripts.options.dateFormat)){
        showErrorMessage("error_message_label_dt");
        return false;
      }
      jQuery('#dateToMil').val(dateToMil.getTime());
    }catch(err){
      showErrorMessage("error_message_label_dt");
      return false;
    }
    if (jQuery("#hour").is(":checked")) {
      var hourClone = jQuery("#hour").clone();
      hourClone.attr("hidden", "hidden");
      jQuery("#reporting-form").append(hourClone);
    }
    if (jQuery("#nonworking").is(":checked")) {
      var noneworkingClone = jQuery("#nonworking").clone();
      noneworkingClone.attr("hidden", "hidden");
      jQuery("#reporting-form").append(noneworkingClone);
    }
  }

  jttp.beforeSubmitMissingsPagingReport = function() {
    if (jQuery("#hour").is(":checked")) {
      var hourClone = jQuery("#hour").clone();
      hourClone.attr("hidden", "hidden");
      jQuery("#paging-form").append(hourClone);
    }
    if (jQuery("#nonworking").is(":checked")) {
      var noneworkingClone = jQuery("#nonworking").clone();
      noneworkingClone.attr("hidden", "hidden");
      jQuery("#paging-form").append(noneworkingClone);
    }
    return true;
  }
  
  function showErrorMessage(message_key){
    AJS.$('#error_message label').hide();
    var errorMessageLabel = AJS.$('#'+message_key);
    errorMessageLabel.show();
    var errorMessage = AJS.$('#error_message');
    errorMessage.show();
  }
})(everit.jttp.missing_days_report, jQuery);