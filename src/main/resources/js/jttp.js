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

AJS.$(document).ready(function(){
	var excludeDays = AJS.$("#excludeDays");
	var isColoring = AJS.$("#isColoring").val() === "true";
	var loggedDays = AJS.$("#loggedDays");
	document.getElementById('inputfields').scrollIntoView(allignWithTop=false);
    setExcludeDaysToWeekend(excludeDays);
    setLoggedDaysDesign(isColoring, loggedDays);
    document.getElementById("startTime").focus();
    AJS.$('.aui-ss-editing').attr("style","width: 250px;");
    AJS.$('.aui-ss.aui-ss-editing .aui-ss-field').attr("style","width: 250px;");

    eventBinding();
	durationSelectionSetup();
	var isPopup = AJS.$("#isPopup").val();
	issuePickerSetup(isPopup);
	commentsCSSFormat(isPopup);
	
	var isEditAll = AJS.$("#isEditAll").val() === "true";
	if (isEditAll){
		disableInputFields();
	}
});

function dateChanged(calendar){
		AJS.$("date").val(calendar.date.print(calendar.params.ifFormat));
		AJS.$("date").change();
	}

function disableInputFields(){
	AJS.$("#startTime").prop("disabled", true);
	AJS.$("#startNow").prop("disabled", true);
	AJS.$("#endTime").prop("disabled", true);
	AJS.$("#endNow").prop("disabled", true);
	AJS.$("#durationTime").prop("disabled", true);
	AJS.$("#issueSelect-textarea").prop("disabled", true);
	AJS.$("#issueSelect-textarea").prop("disabled", true);
	AJS.$("#comments").prop("disabled", true);
}

function eventBinding(){
	AJS.$('.table-endtime').click(function(){
		var temp = new String(AJS.$(this).html());
		AJS.$('#startTime').val(temp.trim());
	});

	AJS.$('.table-starttime').click(function(){
		var temp = new String(AJS.$(this).html());
		AJS.$('#endTime').val(temp.trim());
	});

	AJS.$('.table-comment').click(function(){
		var temp = AJS.$(this).find('#hiddenWorklogBody').val();
		var temp2 = temp.replace(/(\\r\\n|\\n|\\r)/gm, "&#013;");
		var temp3 = temp.replace(/(\\r\\n|\\n|\\r)/gm, "\n");
	    AJS.$("#comments").html(temp2);
	    AJS.$("#comments").val(temp3);
	});

	AJS.$('.table-issue').click(function(){
		AJS.$('#issueSelect-textarea').parent().find('.item-delete').click();
		
		var temp = new String(AJS.$(this).find('a').html());
		AJS.$('#issueSelect-textarea').val(temp.trim());
		AJS.$('#issueSelect-textarea').focus();
		AJS.$('#Edit').focus();
		AJS.$('#Submitbutton').focus();
	});

	 AJS.$('.copy').click(function(){
	    AJS.$('#issueSelect-textarea').parent().find('.item-delete').click();
		var temp = AJS.$(this).parent().parent().find('.table-issue').find('a').html();
		AJS.$('#issueSelect-textarea').val(temp.trim());

		temp = AJS.$(this).parent().parent().find('#hiddenWorklogBody').val();
		var temp2 = temp.replace(/(\\r\\n|\\n|\\r)/gm, "&#013;");
		var temp3 = temp.replace(/(\\r\\n|\\n|\\r)/gm, "\n");

		AJS.$("#comments").html(temp2);
		AJS.$("#comments").val(temp3);

		AJS.$('#issueSelect-textarea').focus();
		AJS.$('#Submitbutton').focus();
	});

	AJS.$('#issueSelect-textarea').keydown(function(e){
	    var isEnter = e.keyCode == 10 || e.keyCode == 13;
	        if (isEnter && e.ctrlKey) {
	          AJS.$('#Submitbutton, #Edit').click();
	        }
	    });

	AJS.$('#comments').keydown(function(e){
	    if ((e.keyCode == 10 || e.keyCode == 13) && e.ctrlKey){
		    AJS.$('#Submitbutton, #Edit').click();
        }
	});

	AJS.$('#jttpForm').submit(function (){
	    AJS.$('#Submitbutton').prop("disabled", true);
	    return true;
	});
}

function durationSelectionSetup(){
	var isDurationSelected = (AJS.$("#isDurationSelected").val() === "true");
	if(isDurationSelected){
		AJS.$("#durationTimeInput").css("cursor","text").hide().prev("input[disabled]").prop("disabled", false).css("cursor","text").focus();
		AJS.$("#endTimeInput").css("cursor","pointer").show().prev("input").prop("disabled", true).css("cursor","pointer");
		AJS.$("#radioDuration").prop("checked", true);
	} else {
		AJS.$("#endTimeInput").css("cursor","text");
		AJS.$("#durationTimeInput").css("cursor","pointer");
	}
}

function issuePickerSetup(isPopup){
	var ip = new AJS.IssuePicker({
		element: AJS.$("#issueSelect"),
		userEnteredOptionsMsg: AJS.params.enterIssueKey,
		uppercaseUserEnteredOnSelect: true,
		singleSelectOnly: true,
		currentProjectId: AJS.$("#projectsId").val(),
	});

	var jiraMainVersion = AJS.$("#jiraMainVersion").val();
	var issueKey = AJS.$("#issueKey").val();
	AJS.$('.issue-picker-popup').attr("style","margin-bottom: 16px;");
	if(isPopup != 1){
	    if(jiraMainVersion < 6){
	        AJS.$("#issueSelect-multi-select").attr("style","width: 630px;");
	        AJS.$("#issueSelect-textarea").attr("style","width: 605px; height: 20px");
	    } else {
	        AJS.$("#issueSelect-multi-select").attr("style","width: 630px;");
	        AJS.$("#issueSelect-textarea").attr("style","width: 625px; height: 30px");
	    }
	}else{
	    if(jiraMainVersion < 6){
	        AJS.$("#issueSelect-multi-select").attr("style","width: 930px;");
	        AJS.$("#issueSelect-textarea").attr("style","width: 905px; height: 20px");
	    }else{
	        AJS.$("#issueSelect-multi-select").attr("style","width: 910px;");
	        AJS.$("#issueSelect-textarea").attr("style","width: 905px; height: 30px");
	    }
	}
	AJS.$("#issueSelect-textarea").attr("class","select2-choices");

	AJS.$("#issueSelect-textarea").append(issueKey);
	AJS.$("#issueSelect-textarea").attr("tabindex","3");
	ip.handleFreeInput();
}

function commentsCSSFormat(isPopup){
	var comment = AJS.$("#comment").val();
	if(isPopup != 1){
	    AJS.$("#comments").attr("style","width: 650px");
	}else{
	    AJS.$("#comments").attr("style","width: 99.4%");    
	}
	AJS.$("#comments").append(comment);
	AJS.$("#comments").attr("tabindex","4");
	AJS.$("#comments").attr("height","100px");
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
        var dayNumber =  loggedDays.size();
        for(var i = 0; i < dayNumber; i++){
            var theDay = loggedDays[i];
            var calendarDays = AJS.$('.day.day-'+theDay);
            for (var j=0;j<calendarDays.length;j++){
                if(!(AJS.$(calendarDays[j]).hasClass('selected') || AJS.$(calendarDays[j]).hasClass('othermonth') || AJS.$(calendarDays[j]).hasClass('logged'))){
                    calendarDays[j].className += " logged";
                }
            }
        }
    }
}

function setExcludeDaysToWeekend(excludeDays){
    var dayNumber =  excludeDays.size();
    for(var i = 0; i < dayNumber; i++){
        var theDay = excludeDays[i];
        var calendarDays = AJS.$('.day.day-'+theDay);
        for (var j=0;j<calendarDays.length;j++){
            if(!(AJS.$(calendarDays[j]).hasClass('selected') || AJS.$(calendarDays[j]).hasClass('othermonth') || AJS.$(calendarDays[j]).hasClass('weekend'))){
                calendarDays[j].className += " weekend";
            }
        }
    }
}

function handleEnterKey(e, setFocusTo){
    var isEnter = e.keyCode == 10 || e.keyCode == 13;
    if (!isEnter) {
        return;
    }
	if (e.ctrlKey){
		AJS.$('#Submitbutton, #Edit').click();
    } else {
        e.preventDefault ? e.preventDefault() : event.returnValue = false;
        AJS.$(setFocusTo).focus();
        return false;
    }
}

function setStartNow(){
    var currentTime = new Date();
    var hour = currentTime.getHours();
    var minute = currentTime.getMinutes();
    startState = 1;
    var now = calculateTimeForInputfileds(hour,minute);
    AJS.$("#startTime").val(now);
}

function setStartDecTemporary(startTimeChange){
    setStartNow();
    var startTimeValParts = AJS.$("#startTime").val().split(':');
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
    AJS.$("#startTime").val(time);
}

function setStartInc(startTimeChange){
    setStartNow();
    var startTimeValParts = AJS.$("#startTime").val().split(':');
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
    AJS.$("#startTime").val(time);
}

function setEndNow(){
    var currentTime = new Date();
    var hour = currentTime.getHours();
    var minute = currentTime.getMinutes();
    endState = 1;
    var now =calculateTimeForInputfileds(hour,minute);
    AJS.$("#endTime").val(now);
 }

function setEndDecTemporary(endTimeChange){
    setEndNow();
    var endTimeValParts = AJS.$("#endTime").val().split(':');
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
    AJS.$("#endTime").val(time);
}

function setEndInc(endTimeChange){
    setEndNow();
    var endTimeValParts = AJS.$("#endTime").val().split(':');
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
    AJS.$("#endTime").val(time);
}

function startNowClick(startTimeChange){
	var startState = 0;
    if(startState == 0){
        setStartNow();
    }else if(startState == 1){
        setStartInc(startTimeChange);
    }else if(startState == 2){
        setStartDecTemporary(startTimeChange);
    }
}

function endTimeInputClick(isEditAll){
	if(!isEditAll){
		AJS.$("#endTimeInput").css("cursor","text").hide().prev().prop("disabled", false).css("cursor","text").focus();
		AJS.$("#durationTimeInput").css("cursor","pointer").show().prev("input").prop("disabled", true).css("cursor","pointer");
		AJS.$("#radioEnd").prop("checked", true);
	}
}

function endNowClick(){
	var endState = 0;
    if(endState == 0){
        setEndNow();
    }else if(endState == 1){
        setEndInc($endTimeChange);
    }else if(endState == 2){
        setEndDecTemporary($endTimeChange);
    } 
}

function durationTimeInput(isEditAll){
	if(!isEditAll){
		AJS.$("#durationTimeInput").css("cursor","text").hide().prev("input[disabled]").prop("disabled", false).css("cursor","text").focus();
		AJS.$("#endTimeInput").css("cursor","pointer").show().prev("input").prop("disabled", true).css("cursor","pointer");
		AJS.$("#radioDuration").prop("checked", true);
	}
}

function submitButtonClick(){
	AJS.$('#Submit').val('true');
	AJS.$('#Submit').attr('disabled', false);
}

