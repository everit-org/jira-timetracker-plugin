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

  function jttpSearchOnClick(reportName){
    var loggedUserIn = AJS.params.loggedInUser;
    var selectedUser = document.getElementById("userPicker").value;
    if(loggedUserIn == selectedUser){
      _paq.push(['trackEvent', 'User', 'My'+reportName]);
    }else{
      _paq.push(['trackEvent', 'User', 'Others'+reportName]);
    }
  }

  function jttpReportDialogShow() {
    var currentHash = window.location.hash;
    if (currentHash=="#reporting-dialog") {
        window.location.hash = "";
        AJS.dialog2("#reporting-dialog").show();
        AJS.$("#reportingError").hide();
        _paq.push(['trackEvent', 'User', 'Reporting']); 
    }
  }
  
window.everit = window.everit || {};
everit.jttp = everit.jttp || {};
everit.jttp.main = everit.jttp.main || {};

(function(jttp, jQuery) {
 
jQuery(document).ready(function(){
	document.getElementById('inputfields').scrollIntoView(allignWithTop=false);
    setExcludeDaysToWeekend(jttp.options.excludeDays);
    setLoggedDaysDesign(jttp.options.isColoring, jttp.options.loggedDays);
    document.getElementById("startTime").focus();
    jQuery('.aui-ss-editing').attr("style","width: 250px;");
    jQuery('.aui-ss.aui-ss-editing .aui-ss-field').attr("style","width: 250px;");

    eventBinding();
	durationSelectionSetup();
	issuePickerSetup(jttp.options.isPopup);
	commentsCSSFormat(jttp.options.isPopup);
	
	var isEditAll = jttp.options.isEditAll === "true";
	if (isEditAll){
		disableInputFields();
	}
});

jttp.dateChanged = function(calendar){
	jQuery("date").val(calendar.date.print(calendar.params.ifFormat));
	jQuery("date").change();
}
	
jttp.startNowClick = function(startTimeChange){
	var startState = 0;
    if(startState == 0){
        setStartNow();
    }else if(startState == 1){
        setStartInc(startTimeChange);
    }else if(startState == 2){
        setStartDecTemporary(startTimeChange);
    }
}

jttp.endTimeInputClick = function(isEditAll){
	if(!isEditAll){
		jQuery("#endTimeInput").css("cursor","text").hide().prev().prop("disabled", false).css("cursor","text").focus();
		jQuery("#durationTimeInput").css("cursor","pointer").show().prev("input").prop("disabled", true).css("cursor","pointer");
		jQuery("#radioEnd").prop("checked", true);
	}
}

jttp.endNowClick = function(){
	var endState = 0;
    if(endState == 0){
        setEndNow();
    }else if(endState == 1){
        setEndInc($endTimeChange);
    }else if(endState == 2){
        setEndDecTemporary($endTimeChange);
    } 
}

jttp.durationTimeInput = function(isEditAll){
	if(!isEditAll){
		jQuery("#durationTimeInput").css("cursor","text").hide().prev("input[disabled]").prop("disabled", false).css("cursor","text").focus();
		jQuery("#endTimeInput").css("cursor","pointer").show().prev("input").prop("disabled", true).css("cursor","pointer");
		jQuery("#radioDuration").prop("checked", true);
	}
}

jttp.submitButtonClick = function(){
	jQuery('#Submit').val('true');
	jQuery('#Submit').attr('disabled', false);
}

jttp.handleEnterKey = function(e, setFocusTo){
    var isEnter = e.keyCode == 10 || e.keyCode == 13;
    if (!isEnter) {
        return;
    }
	if (e.ctrlKey){
		jQuery('#Submitbutton, #Edit').click();
    } else {
        e.preventDefault ? e.preventDefault() : event.returnValue = false;
        jQuery(setFocusTo).focus();
        return false;
    }
}

function disableInputFields(){
	jQuery("#startTime").prop("disabled", true);
	jQuery("#startNow").prop("disabled", true);
	jQuery("#endTime").prop("disabled", true);
	jQuery("#endNow").prop("disabled", true);
	jQuery("#durationTime").prop("disabled", true);
	jQuery("#issueSelect-textarea").prop("disabled", true);
	jQuery("#issueSelect-textarea").prop("disabled", true);
	jQuery("#comments").prop("disabled", true);
}

function eventBinding(){
	jQuery('.table-endtime').click(function(){
		var temp = new String(jQuery(this).html());
		jQuery('#startTime').val(temp.trim());
	});

	jQuery('.table-starttime').click(function(){
		var temp = new String(jQuery(this).html());
		jQuery('#endTime').val(temp.trim());
	});

	jQuery('.table-comment').click(function(){
		var temp = jQuery(this).find('#hiddenWorklogBody').val();
		var temp2 = temp.replace(/(\\r\\n|\\n|\\r)/gm, "&#013;");
		var temp3 = temp.replace(/(\\r\\n|\\n|\\r)/gm, "\n");
	    jQuery("#comments").html(temp2);
	    jQuery("#comments").val(temp3);
	});

	jQuery('.table-issue').click(function(){
		jQuery('#issueSelect-textarea').parent().find('.item-delete').click();
		
		var temp = new String(jQuery(this).find('a').html());
		jQuery('#issueSelect-textarea').val(temp.trim());
		jQuery('#issueSelect-textarea').focus();
		jQuery('#Edit').focus();
		jQuery('#Submitbutton').focus();
	});

	 jQuery('.copy').click(function(){
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

	jQuery('#issueSelect-textarea').keydown(function(e){
	    var isEnter = e.keyCode == 10 || e.keyCode == 13;
	        if (isEnter && e.ctrlKey) {
	          jQuery('#Submitbutton, #Edit').click();
	        }
	    });

	jQuery('#comments').keydown(function(e){
	    if ((e.keyCode == 10 || e.keyCode == 13) && e.ctrlKey){
		    jQuery('#Submitbutton, #Edit').click();
        }
	});

	jQuery('#jttpForm').submit(function (){
	    jQuery('#Submitbutton').prop("disabled", true);
	    return true;
	});
}

function durationSelectionSetup(){
	var isDurationSelected = (jttp.options.isDurationSelected === "true");
	if(isDurationSelected){
		jQuery("#durationTimeInput").css("cursor","text").hide().prev("input[disabled]").prop("disabled", false).css("cursor","text").focus();
		jQuery("#endTimeInput").css("cursor","pointer").show().prev("input").prop("disabled", true).css("cursor","pointer");
		jQuery("#radioDuration").prop("checked", true);
	} else {
		jQuery("#endTimeInput").css("cursor","text");
		jQuery("#durationTimeInput").css("cursor","pointer");
	}
}

function issuePickerSetup(isPopup){
	var ip = new AJS.IssuePicker({
		element: jQuery("#issueSelect"),
		userEnteredOptionsMsg: AJS.params.enterIssueKey,
		uppercaseUserEnteredOnSelect: true,
		singleSelectOnly: true,
		currentProjectId: jttp.options.projectsId,
	});

	var jiraMainVersion = jttp.options.jiraMainVersion;
	var issueKey = jttp.options.issueKey;
	jQuery('.issue-picker-popup').attr("style","margin-bottom: 16px;");
	if(isPopup != 1){
	    if(jiraMainVersion < 6){
	        jQuery("#issueSelect-multi-select").attr("style","width: 630px;");
	        jQuery("#issueSelect-textarea").attr("style","width: 605px; height: 20px");
	    } else {
	        jQuery("#issueSelect-multi-select").attr("style","width: 630px;");
	        jQuery("#issueSelect-textarea").attr("style","width: 630px; height: 30px");
	    }
	}else{
	    if(jiraMainVersion < 6){
	        jQuery("#issueSelect-multi-select").attr("style","width: 930px;");
	        jQuery("#issueSelect-textarea").attr("style","width: 910px; height: 20px");
	    }else{
	        jQuery("#issueSelect-multi-select").attr("style","width: 910px;");
	        jQuery("#issueSelect-textarea").attr("style","width: 910px; height: 30px");
	    }
	}
	jQuery("#issueSelect-textarea").attr("class","select2-choices");

	jQuery("#issueSelect-textarea").append(issueKey);
	jQuery("#issueSelect-textarea").attr("tabindex","3");
	ip.handleFreeInput();
}

function commentsCSSFormat(isPopup){
	var comment = jttp.options.comment;
	if(isPopup != 1){
	    jQuery("#comments").attr("style","width: 650px; resize: vertical;");
	}else{
	    jQuery("#comments").attr("style","width: 99.4%; resize: vertical;");    
	}
	jQuery("#comments").append(comment);
	jQuery("#comments").attr("tabindex","4");
	jQuery("#comments").attr("height","100px");
}


function calculateTimeForInputfileds(hour,min){
    if (hour < 10){
        hour = "0" + hour
    }
    if (min < 10){
        min = "0" + min
    }
    var time = hour + ':' + min;
    return time;
}

function setLoggedDaysDesign(isColoring, loggedDays){
    if(isColoring){
        var dayNumber =  loggedDays.length;
        for(var i = 0; i < dayNumber; i++){
            var theDay = loggedDays[i];
            var calendarDays = jQuery('.day.day-'+theDay);
            for (var j=0;j<calendarDays.length;j++){
                if(!(jQuery(calendarDays[j]).hasClass('selected') || jQuery(calendarDays[j]).hasClass('othermonth') || jQuery(calendarDays[j]).hasClass('logged'))){
                    calendarDays[j].className += " logged";
                }
            }
        }
    }
}

function setExcludeDaysToWeekend(excludeDays){
    var dayNumber =  excludeDays.length;
    for(var i = 0; i < dayNumber; i++){
        var theDay = excludeDays[i];
        var calendarDays = jQuery('.day.day-'+theDay);
        for (var j=0;j<calendarDays.length;j++){
            if(!(jQuery(calendarDays[j]).hasClass('selected') || jQuery(calendarDays[j]).hasClass('othermonth') || jQuery(calendarDays[j]).hasClass('weekend'))){
                calendarDays[j].className += " weekend";
            }
        }
    }
}

function setStartNow(){
    var currentTime = new Date();
    var hour = currentTime.getHours();
    var minute = currentTime.getMinutes();
    startState = 1;
    var now = calculateTimeForInputfileds(hour,minute);
    jQuery("#startTime").val(now);
}

function setStartDecTemporary(startTimeChange){
    setStartNow();
    var startTimeValParts = jQuery("#startTime").val().split(':');
    var hour = parseInt(startTimeValParts[0]);
    var minString = startTimeValParts[1];
    var min = parseInt(minString);
    var minSubInt = parseInt(minString.substring(1,2));
    if((minSubInt != 0) && (minSubInt != 5)){
        min = min - startTimeChange;
        if(minSubInt < 5){
            min = min + (5-minSubInt);
        }else if(minSubInt > 5){
            min = min + (10-minSubInt);
        }
        startState = 0;
    }
    var time = calculateTimeForInputfileds(hour,min);
    jQuery("#startTime").val(time);
}

function setStartInc(startTimeChange){
    setStartNow();
    var startTimeValParts = jQuery("#startTime").val().split(':');
    var hour = parseInt(startTimeValParts[0]);
    var min = parseInt(startTimeValParts[1]);
    min = min + startTimeChange;
    if(min >= 60){
        min = min - 60;
        hour = hour + 1;
        if(hour > 23){
            hour = 0;
        }
    }
    var minString = min.toString();
    var minSubInt;
    if(minString.length > 1){
        minSubInt = parseInt(minString.substring(1,2));
    }else{
        minSubInt = min;
    }
    if(minSubInt >= 5){
        min = min - (minSubInt - 5);
    }else{
        min = min - minSubInt;
    }
    startState = 2;
    var time = calculateTimeForInputfileds(hour,min);
    jQuery("#startTime").val(time);
}

function setEndNow(){
    var currentTime = new Date();
    var hour = currentTime.getHours();
    var minute = currentTime.getMinutes();
    endState = 1;
    var now =calculateTimeForInputfileds(hour,minute);
    jQuery("#endTime").val(now);
 }

function setEndDecTemporary(endTimeChange){
    setEndNow();
    var endTimeValParts = jQuery("#endTime").val().split(':');
    var hour = parseInt(endTimeValParts[0]);
    var minString = endTimeValParts[1];
    var min = parseInt(minString);
    var minSubInt = parseInt(minString.substring(1,2));
    if((minSubInt != 0) && (minSubInt != 5)){
        min = min - endTimeChange;
        var checkHour = false;
        if(min < 0){
            min = 60 + min;
            checkHour = true;
            minSubInt =  parseInt(min.toString().substring(1,2));;
        }
        if(minSubInt < 5){
            min = min + (5-minSubInt);
        }else if(minSubInt > 5){
            min = min + (10-minSubInt);
        }
        if(checkHour){
            if(min != 60){
                hour = hour - 1;
                if(hour < 0){
                    hour = 23;
                }
            }else{
                min = 0;
            } 
        }
        endState = 0;
    }
    var time = calculateTimeForInputfileds(hour,min);
    jQuery("#endTime").val(time);
}

function setEndInc(endTimeChange){
    setEndNow();
    var endTimeValParts = jQuery("#endTime").val().split(':');
    var hour = parseInt(endTimeValParts[0]);
    var min = parseInt(endTimeValParts[1]);
    min = min + endTimeChange;
    if(min >= 60){
        min = min - 60;
        hour = hour + 1;
        if(hour > 23){
            hour = 0;
        }
    }
    var minString = min.toString();
    var minSubInt;
    if(minString.length > 1){
        minSubInt = parseInt(minString.substring(1,2));
    }else{
        minSubInt = min;
    }
    if(minSubInt >= 5){
        min = min - (minSubInt - 5);
    }else{
        min = min - minSubInt;
    }
    endState = 2;
    var time = calculateTimeForInputfileds(hour,min);
    jQuery("#endTime").val(time);
}

function analitycs(){
	
}

})(everit.jttp.main, jQuery);
