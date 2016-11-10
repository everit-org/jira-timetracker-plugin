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
everit.jttp.main = everit.jttp.main || {};

(function(jttp, jQuery) {
  
  jQuery(document).ready(function() {

    jQuery('.aui-ss-editing').attr("style", "width: 250px;");
    jQuery('.aui-ss.aui-ss-editing .aui-ss-field').attr("style", "width: 250px;");

    durationSelectionSetup();
    issuePickerSetup();
    eventBinding();
    commentsCSSFormat();

    if(!jQuery( ".aui-message-error" ).length 
        && window.location.search.indexOf('date') > -1
        && !isContainsAchorExlucdingParts(window.location.search)){
      document.getElementById("issueSelect-textarea").focus();
      var anchorDiv = document.getElementById("buttons-container");
      jQuery(window).scrollTop( anchorDiv.offsetTop);
    }else{
      jQuery("#jttp-headline-day-calendar").blur();
    }

    var formatedDate =  new Date(jttp.options.dateFormatted).print(jttp.options.dateFormat);  

    jQuery("#dateHidden").val(formatedDate);

    popupCalendarsSetup();
    setExcludeDaysToWeekend(jttp.options.excludeDays);
    setLoggedDaysDesign(jttp.options.isColoring, jttp.options.loggedDays);
    
    if (jttp.options.actionFlag == "editAll") {
      disableInputFields();
    }
    if(jttp.options.worklogSize == 0){
      AJS.messages.info({
        title: AJS.I18n.getText("plugin.no.worklogs.title"),
        body: AJS.I18n.getText("plugin.no.worklogs"),
        closeable: false,
      });
    }

    addTooltips();
    headlineProgressIndicator();
    initProgrssIndicators();
    initTooltipsForIndicators();
    var original = Calendar.prototype.show;
    Calendar.prototype.show = function() {
      original.call(this);
      setExcludeDaysToWeekend(jttp.options.excludeDays);
      setLoggedDaysDesign(jttp.options.isColoring, jttp.options.loggedDays);
    }
    if(location.href.indexOf('showWarning')>=0){
    	jQuery("#futorelog-warning").slideToggle("slow");
    }
  });
  
  function isContainsAchorExlucdingParts(search){
    var exlucdingParts = ["datesubmit", "dayBack", "dayNext","today", "actionFlag=delete","actionFlag=copy", "lw_chgdate" ];
    var contains = false;
    exlucdingParts.forEach(function(item){
      if(search.indexOf(item) != -1){
        contains = true;
      }
    });
    return contains;
  }
  
  function addTooltips(){
    var $issueTypeTooltip = jQuery('#jttp-worklog-issue-type');
    if(!$issueTypeTooltip.hasClass('jtrp-tooltipped')) {
      $issueTypeTooltip.tooltip({gravity: 'w'});
      $issueTypeTooltip.addClass('jtrp-tooltipped');
    }
    
    var $datePickerTooltip = jQuery('#jttp-headline-day-calendar');
    if(!$datePickerTooltip.hasClass('jtrp-tooltipped')) {
      $datePickerTooltip.tooltip();
      $datePickerTooltip.addClass('jtrp-tooltipped');
    }
    
    jQuery('.tooltip-left').each(function() {
      var $element = jQuery(this);
      if(!$element.hasClass('jtrp-tooltipped')) {
        $element.tooltip({gravity: 'e'});
        $element.addClass('jtrp-tooltipped');
      }
    });
    
    jQuery('.tooltip-bottom').each(function() {
      var $element = jQuery(this);
      if(!$element.hasClass('jtrp-tooltipped')) {
        $element.tooltip();
        $element.addClass('jtrp-tooltipped');
      }
    });
    
    jQuery('.img-tooltip').each(function() {
      var $element = jQuery(this);
      if(!$element.hasClass('jtrp-tooltipped')) {
        $element.tooltip({gravity: 'w'});
        $element.addClass('jtrp-tooltipped');
      }
    });
  }
  
  
  jttp.startState = 0;
  jttp.endState = 0;

  jttp.dateChanged = function(calendar) {
    var dmy = AJS.Meta.get("date-dmy").toUpperCase();
    jQuery("#dateHidden").val(calendar.date.format(dmy));
    jQuery("#dateHidden").change();
  }

  jttp.startNowClick = function(startTimeChange) {
    if (jttp.startState == 0) {
      setStartNow();
    } else if (jttp.startState == 1) {
      setStartInc(startTimeChange);
    } else if (jttp.startState == 2) {
      setStartDecTemporary(startTimeChange);
    }
  }

  jttp.endTimeInputClick = function() {
    if (jttp.options.actionFlag != "editAll") {
      jQuery("#endTimeInput").css("cursor", "text").hide().prev().prop("disabled", false).css(
          "cursor", "text").focus();
      jQuery("#durationTimeInput").css("cursor", "pointer").show().prev("input").prop("disabled",
          true).css("cursor", "pointer");
      jQuery("#radioEnd").prop("checked", true);
    }
  }

  jttp.endNowClick = function(endTimeChange) {
    if (jttp.endState == 0) {
      setEndNow();
    } else if (jttp.endState == 1) {
      setEndInc(endTimeChange);
    } else if (jttp.endState == 2) {
      setEndDecTemporary(endTimeChange);
    }
  }

  jttp.durationTimeInput = function() {
    if (jttp.options.actionFlag != "editAll") {
      jQuery("#durationTimeInput").css("cursor", "text").hide().prev("input[disabled]").prop(
          "disabled", false).css("cursor", "text").focus();
      jQuery("#endTimeInput").css("cursor", "pointer").show().prev("input").prop("disabled", true)
          .css("cursor", "pointer");
      jQuery("#radioDuration").prop("checked", true);
    }
  }
  
  jttp.beforeSubmit = function() {
    var dateHidden = jQuery('#dateHidden').val();
    var dateInMil = Date.parseDate(dateHidden, jttp.options.dateFormat);
    var date = jQuery('#date');
    date.val(dateInMil.getTime());
    
    var worklogValues = getWorklogValuesJson();
    var json = JSON.stringify(worklogValues);
    var worklogValuesJson = jQuery('#worklogValuesJson');
    worklogValuesJson.val(json);
    
    return true;
  }
    
  jttp.setActionFlag = function(flag, id) {
    var actionFlag = jQuery('.actionFlag_'+id);
    actionFlag.val(flag);
    actionFlag.change();
  }
  
  jttp.actionSubmitClick  = function(id) {
    jQuery("#actionSubmit_"+id).click();
  }
  
  jttp.beforeSubmitEditAll = function(){
    var dateHidden = jQuery('#dateHidden').val();
    var dateInMil = Date.parseDate(dateHidden, jttp.options.dateFormat);
    var date = jQuery('#date');
    date.val(dateInMil.getTime());
    jQuery("#jttp-editall-form").append(date);
    
    return true;
  }
  
  jttp.beforeSubmitAction = function(id) {
    var dateHidden = jQuery('#dateHidden').val();
    var dateInMil = Date.parseDate(dateHidden, jttp.options.dateFormat);
    var date = jQuery('#date');
    date.val(dateInMil.getTime());
    jQuery(".actionForm_"+id).append(date);
    
    return true;
  }
   
 jttp.cancelClick = function(){
   var dateHidden = jQuery('#dateHidden').val();
   var dateInMil = Date.parseDate(dateHidden, jttp.options.dateFormat);
   window.location = "JiraTimetrackerWebAction.jspa?date="+dateInMil.getTime();
 }
 
  jttp.beforeSubmitChangeDate = function() {
    var dateHidden = jQuery('#dateHidden').val();
    var dateInMil = Date.parseDate(dateHidden, jttp.options.dateFormat);
    var date = jQuery('#date');
    date.val(dateInMil.getTime());
    jQuery("#jttp-datecahnge-form").append(date);
    
    var worklogValues = getWorklogValuesJson();
    var json = JSON.stringify(worklogValues);
    var worklogValuesJson = jQuery('#worklogValuesJson');
    worklogValuesJson.val(json);
    jQuery("#jttp-datecahnge-form").append(worklogValuesJson);
    
    var actionWorklogId = jQuery('#jttp-logwork-form #actionWorklogId');
    jQuery("#jttp-datecahnge-form").append(actionWorklogId);
    
    var editAll = jQuery('#jttp-logwork-form #editAll');
    jQuery("#jttp-datecahnge-form").append(editAll);
    
    var actionFlag = jQuery('#jttp-logwork-form #actionFlag');
    jQuery("#jttp-datecahnge-form").append(actionFlag);
    
    return true;
  }
  
  jttp.handleEnterKey = function(e, setFocusTo) {
    var isEnter = e.keyCode == 10 || e.keyCode == 13;
    if (!isEnter) {
      return;
    }
    if (e.ctrlKey) {
      jQuery('#jttp-logwork-save, #Edit').click();
    } else {
      e.preventDefault ? e.preventDefault() : event.returnValue = false;
      jQuery(setFocusTo).focus();
      return false;
    }
  }

  function disableInputFields() {
	jQuery("#dummyFormWrapper").hide();
	jQuery("#wokrlogChangeDateMessage").show();
    jQuery("#startTime").prop("disabled", true);
    jQuery("#startNow").prop("disabled", true);
    jQuery("#endTime").prop("disabled", true);
    jQuery("#endNow").prop("disabled", true);
    jQuery("#durationTime").prop("disabled", true);
    jQuery("#issueSelect-textarea").prop("disabled", true);
    jQuery("#issueSelect-textarea").prop("disabled", true);
    jQuery("#comments").prop("disabled", true);
  }
  
  function headlineProgressIndicator() {
    var jttp_progress_red = '#d04437';
    var jttp_progress_green = '#14892c';
    var jttp_progress_yellow = '#f6c342';
    var $indicator = jQuery('#jttp-headline-progress-indicator');
    var dailyPercent = parseFloat($indicator.attr('data-jttp-percent'));
    if(dailyPercent > 1.0) {
      dailyPercent = 1;
    }
    AJS.progressBars.update($indicator, dailyPercent);
    var $progressIndicator = jQuery('#jttp-headline-progress-indicator .aui-progress-indicator-value');
    if (dailyPercent <= 0.2) {
      jQuery($progressIndicator).css("background-color", jttp_progress_red);
    } else if (dailyPercent >= 1.0){
      jQuery($progressIndicator).css("background-color", jttp_progress_green); 
    } else {
      jQuery($progressIndicator).css("background-color", jttp_progress_yellow);  
    }
  }
  
  function initProgrssIndicators(){
		  jQuery('.progress').each(function(i, obj) {
			  var width = 0;
			  jQuery( obj ).children('.progress-bar').each(function(i, obj) {
				   width+=parseInt(jQuery( obj ).css( "width" ));
			   });
			   var widthInprecent = (width) / parseInt(jQuery( obj ).css( "width" ));
				  if(widthInprecent < 0.2){
					  jQuery( obj ).children('.progress-bar').each(function(i, obj) {
						  jQuery( obj ).addClass( "progress-bar-danger" );   
					   });
				  } else if(widthInprecent < 1){
					  jQuery( obj ).children('.progress-bar').each(function(i, obj) {
						  jQuery( obj ).addClass( "progress-bar-warning" );   
					   });
				  }else {
					  jQuery( obj ).children('.progress-bar').each(function(i, obj) {
						  jQuery( obj ).addClass( "progress-bar-success" );  
					   });
				  }
		  });
  }
  function initTooltipsForIndicators(){
	  jQuery('.jttpTooltip').each(function(i, obj) {
		  jQuery(obj).tooltip({
		      title: function () {
		          return jQuery( obj ).children('.jttpTooltiptext').html();
		      },
		    html: true 
		  });  
		  });
  }
  function eventBinding() {
    jQuery('.table-endtime').click(function() {
      var temp = new String(jQuery(this).html());
      jQuery('#startTime').val(temp.trim());
    });

    jQuery('.table-starttime').click(function() {
      var temp = new String(jQuery(this).html());
      jQuery('#endTime').val(temp.trim());
    });

    jQuery('.table-comment').click(function() {
      var temp = jQuery(this).find('#hiddenWorklogBody').val();
      var temp2 = temp.replace(/(\\r\\n|\\n|\\r)/gm, "&#013;");
      var temp3 = temp.replace(/(\\r\\n|\\n|\\r)/gm, "\n");
      jQuery("#comments").html(temp2);
      jQuery("#comments").val(temp3);
    });

    jQuery('.table-issue').click(function() {
      jQuery('#issueSelect-textarea').parent().find('.item-delete').click();

      var temp = new String(jQuery(this).find('a').html());
      jQuery('#issueSelect-textarea').val(temp.trim());
      jQuery('#issueSelect-textarea').focus();
      jQuery('#Edit').focus();
      jQuery('#jttp-logwork-save').focus();
    });

    jQuery('.copy').click(function() {
      jQuery('#issueSelect-textarea').parent().find('.item-delete').click();
      var temp = jQuery(this).parent().parent().find('.table-issue').find('a').html();
      jQuery('#issueSelect-textarea').val(temp.trim());

      temp = jQuery(this).parent().parent().find('#hiddenWorklogBody').val();
      var temp2 = temp.replace(/(\\r\\n|\\n|\\r)/gm, "&#013;");
      var temp3 = temp.replace(/(\\r\\n|\\n|\\r)/gm, "\n");

      jQuery("#comments").html(temp2);
      jQuery("#comments").val(temp3);

      jQuery('#issueSelect-textarea').focus();
      jQuery('#jttp-logwork-save').focus();
    });

    jQuery('#issueSelect-textarea').keydown(function(e) {
      var isEnter = e.keyCode == 10 || e.keyCode == 13;
      if (isEnter && e.ctrlKey) {
        jQuery('#jttp-logwork-save, #Edit').click();
      }
    });

    jQuery('#comments').keydown(function(e) {
      if ((e.keyCode == 10 || e.keyCode == 13) && e.ctrlKey) {
        jQuery('#jttp-logwork-save, #Edit').click();
      }
    });

    jQuery('#jttp-logwork-form').submit(function() {
    	 jQuery('#jttp-logwork-save').prop("disabled", true);
    	 jQuery('#lw_save').val('true');
    	 jQuery('#lw_save').attr('disabled', false);
      return true;
    });
  }

  function durationSelectionSetup() {
    if (jttp.options.isDurationSelected) {
      jQuery("#durationTimeInput").css("cursor", "text").hide().prev("input[disabled]").prop(
          "disabled", false).css("cursor", "text").focus();
      jQuery("#endTimeInput").css("cursor", "pointer").show().prev("input").prop("disabled", true)
          .css("cursor", "pointer");
      jQuery("#radioDuration").prop("checked", true);
    } else {
      jQuery("#endTimeInput").css("cursor", "text");
      jQuery("#durationTimeInput").css("cursor", "pointer");
    }
  }

  function issuePickerSetup() {
    var ip = new AJS.IssuePicker({
      element : jQuery("#issueSelect"),
      userEnteredOptionsMsg : AJS.params.enterIssueKey,
      uppercaseUserEnteredOnSelect : true,
      singleSelectOnly : true,
      currentProjectId : jttp.options.projectsId,
    });

    var issueKey = jttp.options.issueKey;
    jQuery("#issueSelect-textarea").attr("class", "select2-choices");

    jQuery("#issueSelect-textarea").append(issueKey);
    jQuery("#issueSelect-textarea").attr("tabindex", "1");
    ip.handleFreeInput();
  }
  
  function popupCalendarsSetup() {
    var original = Calendar.prototype.show;
    Calendar.prototype.show = function() {
      original.call(this);
      setExcludeDaysToWeekend(jttp.options.excludeDays);
      setLoggedDaysDesign(jttp.options.isColoring, jttp.options.loggedDays);
    }
      var calPop = Calendar.setup({
        firstDay : jttp.options.firstDay,
        inputField : jQuery("#dateHidden"),
        button : jQuery("#jttp-headline-day-calendar"),
        date : new Date(jttp.options.dateFormatted).print(jttp.options.dateFormat),
        ifFormat: jttp.options.dateFormat,
        align : 'Br',
        electric : false,
        singleClick : true,
        showOthers : true,
        useISO8601WeekNumbers : jttp.options.useISO8601,
      });
  }
  
  jttp.toggleSummary = function() {
    var module = jQuery("#summaryModule");
    jQuery(".mod-content", module).toggle(0, function() {
        module.toggleClass("collapsed");
    });
  }
  
  function getWorklogValuesJson(){
    var issueKey = (!jQuery('#issueSelect').val() || jQuery('#issueSelect').val())[0] || "";
    var startTime = jQuery('#startTime').val() || "";
    var endOrDuration = jQuery('input[name="endOrDuration"]:checked').val();
    var endTime = jQuery('#endTime').val() || "";
    var durationTime = jQuery('#durationTime').val() || "";
    var comment = jQuery('#comments').val() ||"";
    var isDurationSelect = true;
    if(endOrDuration == "end"){
      isDurationSelect = false;
    }
    
    var worklogValues = {
      "startTime": startTime,
      "endTime": endTime,
      "durationTime": durationTime,
      "isDuration": isDurationSelect,
      "comment": comment,
      "issueKey": issueKey,
    }
    return worklogValues;
  }
  
  
  function commentsCSSFormat() {
    var comment = jttp.options.comment;
    jQuery("#comments").append(comment);
    jQuery("#comments").attr("tabindex", "4");
    jQuery('#comments').attr("style","height: 85px;");
  }

  function calculateTimeForInputfileds(hour, min) {
    if (hour < 10) {
      hour = "0" + hour
    }
    if (min < 10) {
      min = "0" + min
    }
    var time = hour + ':' + min;
    return time;
  }

  function setLoggedDaysDesign(isColoring, loggedDays) {
    if (isColoring) {
      var dayNumber = loggedDays.length;
      for (var i = 0; i < dayNumber; i++) {
        var theDay = loggedDays[i];
        var calendarDays = jQuery('.day.day-' + theDay);
        for (var j = 0; j < calendarDays.length; j++) {
          if (!(jQuery(calendarDays[j]).hasClass('selected')
              || jQuery(calendarDays[j]).hasClass('othermonth') || jQuery(calendarDays[j])
              .hasClass('logged'))) {
            calendarDays[j].className += " logged";
          }
        }
      }
    }
  }

  function setExcludeDaysToWeekend(excludeDays) {
    var dayNumber = excludeDays.length;
    for (var i = 0; i < dayNumber; i++) {
      var theDay = excludeDays[i];
      var calendarDays = jQuery('.day.day-' + theDay);
      for (var j = 0; j < calendarDays.length; j++) {
        if (!(jQuery(calendarDays[j]).hasClass('selected')
            || jQuery(calendarDays[j]).hasClass('othermonth') || jQuery(calendarDays[j]).hasClass(
            'weekend'))) {
          calendarDays[j].className += " weekend";
        }
      }
    }
  }

  function setStartNow() {
    var currentTime = new Date();
    var hour = currentTime.getHours();
    var minute = currentTime.getMinutes();
    jttp.startState = 1;
    var now = calculateTimeForInputfileds(hour, minute);
    jQuery("#startTime").val(now);
  }

  function setStartDecTemporary(startTimeChange) {
    setStartNow();
    var startTimeValParts = jQuery("#startTime").val().split(':');
    var hour = parseInt(startTimeValParts[0]);
    var minString = startTimeValParts[1];
    var min = parseInt(minString);
    var minSubInt = parseInt(minString.substring(1, 2));
    if ((minSubInt != 0) && (minSubInt != 5)) {
      min = min - startTimeChange;
      if (minSubInt < 5) {
        min = min + (5 - minSubInt);
      } else if (minSubInt > 5) {
        min = min + (10 - minSubInt);
      }
      jttp.startState = 0;
    }
    var time = calculateTimeForInputfileds(hour, min);
    jQuery("#startTime").val(time);
  }

  function setStartInc(startTimeChange) {
    setStartNow();
    var startTimeValParts = jQuery("#startTime").val().split(':');
    var hour = parseInt(startTimeValParts[0]);
    var min = parseInt(startTimeValParts[1]);
    min = min + startTimeChange;
    if (min >= 60) {
      min = min - 60;
      hour = hour + 1;
      if (hour > 23) {
        hour = 0;
      }
    }
    var minString = min.toString();
    var minSubInt;
    if (minString.length > 1) {
      minSubInt = parseInt(minString.substring(1, 2));
    } else {
      minSubInt = min;
    }
    if (minSubInt >= 5) {
      min = min - (minSubInt - 5);
    } else {
      min = min - minSubInt;
    }
    jttp.startState = 2;
    var time = calculateTimeForInputfileds(hour, min);
    jQuery("#startTime").val(time);
  }

  function setEndNow() {
    var currentTime = new Date();
    var hour = currentTime.getHours();
    var minute = currentTime.getMinutes();
    jttp.endState = 1;
    var now = calculateTimeForInputfileds(hour, minute);
    jQuery("#endTime").val(now);
  }

  function setEndDecTemporary(endTimeChange) {
    setEndNow();
    var endTimeValParts = jQuery("#endTime").val().split(':');
    var hour = parseInt(endTimeValParts[0]);
    var minString = endTimeValParts[1];
    var min = parseInt(minString);
    var minSubInt = parseInt(minString.substring(1, 2));
    if ((minSubInt != 0) && (minSubInt != 5)) {
      min = min - endTimeChange;
      var checkHour = false;
      if (min < 0) {
        min = 60 + min;
        checkHour = true;
        minSubInt = parseInt(min.toString().substring(1, 2));
        ;
      }
      if (minSubInt < 5) {
        min = min + (5 - minSubInt);
      } else if (minSubInt > 5) {
        min = min + (10 - minSubInt);
      }
      if (checkHour) {
        if (min != 60) {
          hour = hour - 1;
          if (hour < 0) {
            hour = 23;
          }
        } else {
          min = 0;
        }
      }
      jttp.endState = 0;
    }
    var time = calculateTimeForInputfileds(hour, min);
    jQuery("#endTime").val(time);
  }

  function setEndInc(endTimeChange) {
    setEndNow();
    var endTimeValParts = jQuery("#endTime").val().split(':');
    var hour = parseInt(endTimeValParts[0]);
    var min = parseInt(endTimeValParts[1]);
    min = min + endTimeChange;
    if (min >= 60) {
      min = min - 60;
      hour = hour + 1;
      if (hour > 23) {
        hour = 0;
      }
    }
    var minString = min.toString();
    var minSubInt;
    if (minString.length > 1) {
      minSubInt = parseInt(minString.substring(1, 2));
    } else {
      minSubInt = min;
    }
    if (minSubInt >= 5) {
      min = min - (minSubInt - 5);
    } else {
      min = min - minSubInt;
    }
    jttp.endState = 2;
    var time = calculateTimeForInputfileds(hour, min);
    jQuery("#endTime").val(time);
  }
  
  jttp.selectCalculationType = function(){
    var $endInput = jQuery('#endTime');
    if($endInput.attr("disabled")){
      jttp.calculateEndTime();
    }else{
      jttp.calculateDuration();
    }
  }
  
  jttp.calculateDuration = function(){
    var $startInput = jQuery('#startTime');
    var $endInput = jQuery('#endTime');
    var $durationInput = jQuery('#durationTime');
    
    var startTime = $startInput.val();
    var endTime = $endInput.val();
    
    if(startTime.length != 5 || endTime.length != 5) {
      return false;
    }
    
    var endTimeParts = endTime.split(':');
    var endTimeHour = parseInt(endTimeParts[0]);
    var endTimeMin = parseInt(endTimeParts[1]);
    
    var startTimeParts = startTime.split(':');
    var startTimeHour = parseInt(startTimeParts[0]);
    var startTimeMin = parseInt(startTimeParts[1]);
    
    var durationHour = endTimeHour - startTimeHour;
    var durationMin = endTimeMin - startTimeMin;
    
    if(durationMin < 0) {
      durationHour = durationHour - 1;
      durationMin = 60 + durationMin;
    }

    // startime is after endtime
    if(durationHour < 0) {
      $durationInput.val("");
      return false;
    }

    var durationHourString = String(durationHour);
    if(durationHour < 10) {
      durationHourString = "0" + String(durationHour);
    }
    
    var durationMinString = String(durationMin);
    if(durationMin < 10) {
      durationMinString = "0" + String(durationMin);
    }
    
    $durationInput.val(durationHourString + ":" + durationMinString);
  }

  jttp.calculateEndTime = function(){
    var $startInput = jQuery('#startTime');
    var $endInput = jQuery('#endTime');
    var $durationInput = jQuery('#durationTime');
    
    var startTime = $startInput.val();
    var durationTime = $durationInput.val();
    
    if(startTime.length != 5 || durationTime.length != 5) {
      return false;
    }
    
    var durationTimeParts = durationTime.split(':');
    var durationTimeHour = parseInt(durationTimeParts[0]);
    var durationTimeMin = parseInt(durationTimeParts[1]);
    
    var startTimeParts = startTime.split(':');
    var startTimeHour = parseInt(startTimeParts[0]);
    var startTimeMin = parseInt(startTimeParts[1]);
    
    var endHour = durationTimeHour + startTimeHour;
    var endMin = durationTimeMin + startTimeMin;
    
    if(endMin > 59) {
      endHour = endHour + 1;
      endMin = endMin - 60;
    }

    // startime is after endtime
    if(endHour > 23) {
      endHour = endHour - 24;
    }

    var endHourString = String(endHour);
    if(endHour < 10) {
      endHourString = "0" + String(durationHour);
    }
    
    var endMinString = String(endMin);
    if(endMin < 10) {
      endMinString = "0" + String(endMin);
    }
    
    $endInput.val(endHourString + ":" + endMinString);
  }

})(everit.jttp.main, jQuery);
