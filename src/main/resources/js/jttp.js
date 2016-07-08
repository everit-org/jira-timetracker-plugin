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
  
  Calendar.prototype.parseDate = function(str, fmt) {
    this.setDate(fecha.parse(str, AJS.Meta.get("date-dmy").toUpperCase()));
  };
  Date.parseDate = function(str, fmt){
    return fecha.parse(str, AJS.Meta.get("date-dmy").toUpperCase());
  };
  
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
    
    document.getElementById("startTime").focus();
    jQuery('.aui-ss-editing').attr("style", "width: 250px;");
    jQuery('.aui-ss.aui-ss-editing .aui-ss-field').attr("style", "width: 250px;");
   
    durationSelectionSetup();
    issuePickerSetup(jttp.options.isPopup);
    eventBinding();
    commentsCSSFormat();
    var fechaFormatedDate = fecha.format(jttp.options.dateFormatted, AJS.Meta.get("date-dmy").toUpperCase());
    jQuery("#date-span").text(fechaFormatedDate);
    jQuery("#dateHidden").val(fechaFormatedDate);
    
   
    popupCalendarsSetup(jttp.options.isPopup);
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
    
    var original = Calendar.prototype.show;
    Calendar.prototype.show = function() {
      original.call(this);
      setExcludeDaysToWeekend(jttp.options.excludeDays);
      setLoggedDaysDesign(jttp.options.isColoring, jttp.options.loggedDays);
    }
  });
  
  function addTooltips(){
    var $issueTypeTooltip = AJS.$('#jttp-worklog-issue-type');
    if(!$issueTypeTooltip.hasClass('jtrp-tooltipped')) {
      $issueTypeTooltip.tooltip({gravity: 'w'});
      $issueTypeTooltip.addClass('jtrp-tooltipped');
    }
    
    var $datePickerTooltip = AJS.$('#jttp-headline-day-calendar');
    if(!$datePickerTooltip.hasClass('jtrp-tooltipped')) {
      $datePickerTooltip.tooltip();
      $datePickerTooltip.addClass('jtrp-tooltipped');
    }
    
    AJS.$('.tooltip-left').each(function() {
      var $element = AJS.$(this);
      if(!$element.hasClass('jtrp-tooltipped')) {
        $element.tooltip({gravity: 'e'});
        $element.addClass('jtrp-tooltipped');
      }
    });
    
    AJS.$('.tooltip-bottom').each(function() {
      var $element = AJS.$(this);
      if(!$element.hasClass('jtrp-tooltipped')) {
        $element.tooltip();
        $element.addClass('jtrp-tooltipped');
      }
    });
    
    AJS.$('.img-tooltip').each(function() {
      var $element = AJS.$(this);
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

  jttp.submitButtonClick = function() {
    jQuery('#Submit').val('true');
    jQuery('#Submit').attr('disabled', false);
  }
  
  jttp.beforeSubmit = function() {
    var dateHidden = jQuery('#dateHidden').val();
    var dateInMil = fecha.parse(dateHidden,  AJS.Meta.get("date-dmy").toUpperCase());
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
    var dateInMil = fecha.parse(dateHidden,  AJS.Meta.get("date-dmy").toUpperCase());
    var date = jQuery('#date');
    date.val(dateInMil.getTime());
    jQuery("#jttp-editall-form").append(date);
    
    return true;
  }
  
  jttp.beforeSubmitAction = function(id) {
    var dateHidden = jQuery('#dateHidden').val();
    var dateInMil = fecha.parse(dateHidden,  AJS.Meta.get("date-dmy").toUpperCase());
    var date = jQuery('#date');
    date.val(dateInMil.getTime());
    jQuery(".actionForm_"+id).append(date);
    
    return true;
  }
   
 jttp.cancelClick = function(){
   var dateHidden = jQuery('#dateHidden').val();
   var dateInMil = fecha.parse(dateHidden,  AJS.Meta.get("date-dmy").toUpperCase());
   window.location = "JiraTimetrackerWebAction!default.jspa?date="+dateInMil.getTime();
 }
 
  jttp.beforeSubmitChangeDate = function() {
    var dateHidden = jQuery('#dateHidden').val();
    var dateInMil = fecha.parse(dateHidden,  AJS.Meta.get("date-dmy").toUpperCase());
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
      jQuery('#Submitbutton, #Edit').click();
    } else {
      e.preventDefault ? e.preventDefault() : event.returnValue = false;
      jQuery(setFocusTo).focus();
      return false;
    }
  }

  function disableInputFields() {
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
      AJS.$($progressIndicator).css("background-color", jttp_progress_red);
    } else if (dailyPercent >= 1.0){
      AJS.$($progressIndicator).css("background-color", jttp_progress_green); 
    } else {
      AJS.$($progressIndicator).css("background-color", jttp_progress_yellow);  
    }
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
      jQuery('#Submitbutton').focus();
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
      jQuery('#Submitbutton').focus();
    });

    jQuery('#issueSelect-textarea').keydown(function(e) {
      var isEnter = e.keyCode == 10 || e.keyCode == 13;
      if (isEnter && e.ctrlKey) {
        jQuery('#Submitbutton, #Edit').click();
      }
    });

    jQuery('#comments').keydown(function(e) {
      if ((e.keyCode == 10 || e.keyCode == 13) && e.ctrlKey) {
        jQuery('#Submitbutton, #Edit').click();
      }
    });

    jQuery('#jttpForm').submit(function() {
      jQuery('#Submitbutton').prop("disabled", true);
      return true;
    });
  }

  function durationSelectionSetup() {
    var isDurationSelected = (jttp.options.isDurationSelected === "true");
    if (isDurationSelected) {
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

  function issuePickerSetup(isPopup) {
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
    jQuery("#issueSelect-textarea").attr("tabindex", "3");
    ip.handleFreeInput();
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
  
  function popupCalendarsSetup(isPopup) {
    if (isPopup != 2) {
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
        date : jttp.options.dateFormatted,
        align : 'Br',
        electric : false,
        singleClick : true,
        showOthers : true,
        useISO8601WeekNumbers : jttp.options.useISO8601,
        onSelect: jttp.onSelect
      });
    }
  }
  
  jttp.standCalendarSetup = function(isPopup){
    if (isPopup != 1) {
      
      var original = Calendar.prototype.show;
      Calendar.prototype.show = function() {
        original.call(this);
        setExcludeDaysToWeekend(jttp.options.excludeDays);
        setLoggedDaysDesign(jttp.options.isColoring, jttp.options.loggedDays);
      }
      
      var cal = Calendar.setup({
        firstDay : jttp.options.firstDay,
        date : jttp.options.dateFormatted,
        align : 'Br',
        singleClick : true,
        showOthers : true,
        flat : 'not_popup_calendar',
        flatCallback : jttp.dateChanged,
        useISO8601WeekNumbers : jttp.options.useISO8601,
        onSelect: jttp.onSelect
      });
    }
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
    jQuery("#comments").attr("height", "100px");
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
  
  jttp.calculateDuration = function(){
    var $startInput = jQuery('#startTime');
    var $endInput = jQuery('#endTime');
    var $durationInput = jQuery('#durationTime');
    
    var startTime = $startInput.val();
    var endTime = $endInput.val();
    
    if(startTime.length != 5 || endTime.length != 5) {
      $durationInput.val("");
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
      $endInput.val("");
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
