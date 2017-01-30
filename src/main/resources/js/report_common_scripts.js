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

  var opt;
  
  jQuery(document).ready(function() {
    opt = jttp.options;
      
    jQuery("#dateFrom").val(opt.dateFromInJSFormat);
    jQuery("#dateTo").val(opt.dateToInJSFormat);

    jttp.calFrom = Calendar.setup({
      firstDay : opt.firstDay,
      inputField : jQuery("#dateFrom"),
      button : jQuery("#date_trigger_from"),
      date : opt.dateFromInJSFormat,
      ifFormat: opt.dateFormat,
      align : 'Br',
      electric : false,
      singleClick : true,
      showOthers : true,
      useISO8601WeekNumbers : opt.useISO8601,
    });

    var calTo = Calendar.setup({
      firstDay : opt.firstDay,
      inputField : jQuery("#dateTo"),
      button : jQuery("#date_trigger_to"),
      date : opt.dateToInJSFormat,
      ifFormat: opt.dateFormat,
      align : 'Br',
      electric : false,
      singleClick : true,
      showOthers : true,
      useISO8601WeekNumbers : opt.useISO8601,
    });
    
    browsePermissionCheck();
    
    jQuery('.aui-ss, .aui-ss-editing, .aui-ss-field').attr("style", "width: 300px;");

  });
  
  function millisTimeZoneCorrection(mil){
    var osTimeZoneOffset = new Date().getTimezoneOffset() * -60000;
    var correctMil = mil + osTimeZoneOffset;
    return correctMil;
  }
  
  function timeZoneCorrection(date){
    var osTimeZoneOffset = date.getTimezoneOffset() * -60000;
    var correctMil = date.getTime() + osTimeZoneOffset;
    return correctMil;
  }
  
  function browsePermissionCheck(){
    if(!jttp.options.hasBrowseUsersPermission){
      jQuery("#userPicker-field").attr("aria-disabled", true);
      jQuery("#userPicker-field").attr("disabled", "disabled");
      jQuery("#userPicker-single-select").attr("title", AJS.I18n.getText("jtrp.plugin.no.browse.permission"));
      jQuery("#userPicker-single-select").attr("original-title", AJS.I18n.getText("jtrp.plugin.no.browse.permission"));
      
      var $groupPickerTooltip = jQuery('#userPicker-single-select');
      if(!$groupPickerTooltip.hasClass('jtrp-tooltipped')) {
        $groupPickerTooltip.tooltip({gravity: 'w'});
        $groupPickerTooltip.addClass('jtrp-tooltipped');
      }
      
    }
  }

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
 
 
 jttp.beforeSubmitReport = function() {
   try{
     var dateFrom = jQuery('#dateFrom').val();
     var dateFromMil = Date.parseDate(dateFrom, jttp.options.dateFormat);
     if(dateFrom != dateFromMil.print(jttp.options.dateFormat)){
       showErrorMessage("error_message_label_df");
       return false;
     }
     jQuery('#dateFromMil').val(timeZoneCorrection(dateFromMil));
   }catch(err){
     showErrorMessage("error_message_label_df");
     return false;
   }
   try{
     var dateTo = jQuery('#dateTo').val();
     var dateToMil =  Date.parseDate(dateTo, jttp.options.dateFormat);
     if(dateTo != dateToMil.print(jttp.options.dateFormat)){
       showErrorMessage("error_message_label_dt");
       return false;
     }
     jQuery('#dateToMil').val(timeZoneCorrection(dateToMil));
   }catch(err){
     showErrorMessage("error_message_label_dt");
     return false;
   }
   var selectedUser = jQuery('#userPicker').val();
   jQuery('#selectedUser').val(selectedUser);
 }


 
 function showErrorMessage(message_key){
   jQuery('#error_message label').hide();
   var errorMessageLabel = jQuery('#'+message_key);
   errorMessageLabel.show();
   var errorMessage = jQuery('#error_message');
   errorMessage.show();
 }
 

})(everit.jttp.report_common_scripts, jQuery);