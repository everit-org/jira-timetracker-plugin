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
    
  Date.prototype.format = function (formatString) {
    return fecha.format(this, formatString);
  };
  
  jQuery(document).ready(function() {
    fecha.i18n = {
        dayNamesShort: Calendar._SDN,
        dayNames: Calendar._DN,
        monthNamesShort: Calendar._SMN,
        monthNames: Calendar._MN,
        amPm: ['am', 'pm'],
        // D is the day of the month, function returns something like...  3rd or 11th
        DoFn: function (D) {
            return D + [ 'th', 'st', 'nd', 'rd' ][ D % 10 > 3 ? 0 : (D - D % 10 !== 10) * D % 10 ];
        }
    }
    
    var opt = jttp.options;
    
    Date.parseDate = function(str, fmt){
      return fecha.parse(str, AJS.Meta.get("date-dmy").toUpperCase());
      };
      
    jQuery("#dateFrom").val(fecha.format(opt.dateFromFormated, AJS.Meta.get("date-dmy").toUpperCase()));
    jQuery("#dateTo").val(fecha.format(opt.dateToFormated, AJS.Meta.get("date-dmy").toUpperCase()));
    

    jttp.calFrom = Calendar.setup({
      firstDay : opt.firstDay,
      inputField : jQuery("#dateFrom"),
      button : jQuery("#date_trigger_from"),
      date : opt.dateFromFormated,
      align : 'Br',
      electric : false,
      singleClick : true,
      showOthers : true,
      useISO8601WeekNumbers : opt.useISO8601,
      onSelect: jttp.onSelect,
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
      onSelect: jttp.onSelect,
    });
    
    browsePermissionCheck();
    
    jQuery('.aui-ss, .aui-ss-editing, .aui-ss-field').attr("style", "width: 300px;");

  });
  
  function browsePermissionCheck(){
    if(!jttp.options.hasBrowseUsersPermission){
      jQuery("#userPicker-field").attr("aria-disabled", true);
      jQuery("#userPicker-field").attr("disabled", "disabled");
      jQuery("#userPicker-single-select").attr("title", AJS.I18n.getText("jtrp.plugin.no.browse.permission"));
      jQuery("#userPicker-single-select").attr("original-title", AJS.I18n.getText("jtrp.plugin.no.browse.permission"));
      
      var $groupPickerTooltip = AJS.$('#userPicker-single-select');
      if(!$groupPickerTooltip.hasClass('jtrp-tooltipped')) {
        $groupPickerTooltip.tooltip({gravity: 'w'});
        $groupPickerTooltip.addClass('jtrp-tooltipped');
      }
      
    }
  }
  
  jttp.onSelect = function(cal) {
    //Copy of the original onSelect. Only chacnge not use te p.ifFormat
    var p = cal.params;
    var update = (cal.dateClicked || p.electric);
    if (update && p.inputField) {
      var dmy = AJS.Meta.get("date-dmy").toUpperCase();
      p.inputField.value = cal.date.format(dmy);
      jQuery(p.inputField).change();            
    }
    if (update && p.displayArea)
      p.displayArea.innerHTML = cal.date.print(p.daFormat);
    if (update && typeof p.onUpdate == "function")
      p.onUpdate(cal);
    if (update && p.flat) {
      if (typeof p.flatCallback == "function")
        p.flatCallback(cal);
    }
        if (p.singleClick === "true") {
            p.singleClick = true;
        } else if (p.singleClick === "false") {
            p.singleClick = false;
        }
    if (update && p.singleClick && cal.dateClicked)
      cal.callCloseHandler();
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
     var dateFromMil = fecha.parse(dateFrom,  AJS.Meta.get("date-dmy").toUpperCase());
     jQuery('#dateFromMil').val(dateFromMil.getTime());
   }catch(err){
     showErrorMessage("error_message_label_df");
     return false;
   }
   try{
     var dateTo = jQuery('#dateTo').val();
     var dateToMil = fecha.parse(dateTo,  AJS.Meta.get("date-dmy").toUpperCase());
     jQuery('#dateToMil').val(dateToMil.getTime());
   }catch(err){
     showErrorMessage("error_message_label_dt");
     return false;
   }
   var selectedUser = jQuery('#userPicker').val();
   jQuery('#selectedUser').val(selectedUser);
 }


 
 function showErrorMessage(message_key){
   AJS.$('#error_message label').hide();
   var errorMessageLabel = AJS.$('#'+message_key);
   errorMessageLabel.show();
   var errorMessage = AJS.$('#error_message');
   errorMessage.show();
 }
 

})(everit.jttp.report_common_scripts, jQuery);