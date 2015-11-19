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
    var original = Calendar.prototype.show;
	var excludeDays = AJS.$("#excludeDays");
	var isColoring = AJS.$("#isColoring");
	var loggedDays = AJS.$("#loggedDays");
    Calendar.prototype.show = function(){
        original.call(this);
        setExcludeDaysToWeekend(excludeDays);
        setLoggedDaysDesign(isColoring, loggedDays);
    }
	document.getElementById('inputfields').scrollIntoView(allignWithTop=false);
    setExcludeDaysToWeekend(excludeDays);
    setLoggedDaysDesign(isColoring, loggedDays);
    document.getElementById("startTime").focus();
    AJS.$('.aui-ss-editing').attr("style","width: 250px;");
    AJS.$('.aui-ss.aui-ss-editing .aui-ss-field').attr("style","width: 250px;");

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
	     
	     AJS.$('#jttpForm').submit(function () {
	        AJS.$('#Submitbutton').prop("disabled", true);
	        return true;
	     });
});



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

function startNowClick(){
	var startState = 0;
	var startTimeChange = AJS.$('#startTimeChange');
    if(startState == 0){
        setStartNow();
    }else if(startState == 1){
        setStartInc(startTimeChange);
    }else if(startState == 2){
        setStartDecTemporary(startTimeChange);
    }
}

function endTimeInputClick(){
	console.log('endTimeInputClick called');
	var isEditAll = AJS.$("#isEditAll");
	console.log('isEditAll: '+isEditAll);
	if(!isEditAll){
		console.log("inside if");
		AJS.$('#endTimeInput').css("cursor","text").hide().prev().prop("disabled", false).css("cursor","text").focus();
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

