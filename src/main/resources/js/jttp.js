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

function setLoggedDaysDesign(){
    if($isColoring){
        var dayNumber =  $loggedDays.size();
        for(var i = 0; i < dayNumber; i++){
            var theDay = $loggedDays[i];
            var calendarDays = AJS.$('.day.day-'+theDay);
            for (var j=0;j<calendarDays.length;j++){
                if(!(AJS.$(calendarDays[j]).hasClass('selected') || AJS.$(calendarDays[j]).hasClass('othermonth') || AJS.$(calendarDays[j]).hasClass('logged'))){
                    calendarDays[j].className += " logged";
                }
            }
        }
    }
}

function setExcludeDaysToWeekend(){
    var dayNumber =  $excludeDays.size();
    for(var i = 0; i < dayNumber; i++){
        var theDay = $excludeDays[i];
        var calendarDays = AJS.$('.day.day-'+theDay);
        for (var j=0;j<calendarDays.length;j++){
            if(!(AJS.$(calendarDays[j]).hasClass('selected') || AJS.$(calendarDays[j]).hasClass('othermonth') || AJS.$(calendarDays[j]).hasClass('weekend'))){
                calendarDays[j].className += " weekend";
            }
        }
    }
}

function handleEnterKey(e, setFocusTo){
    if ((e.keyCode == 10 || e.keyCode == 13) && e.ctrlKey){
        AJS.$('#Submitbutton, #Edit').click();
    } else if(e.keyCode == 13) { 
        AJS.$(setFocusTo).focus();
    } else {
        e.preventDefault ? e.preventDefault() : event.returnValue = false;
        AJS.$(setFocusTo).focus();
        return false;
    }
}

var original = Calendar.prototype.show;
    
    Calendar.prototype.show = function(){
        original.call(this);
        setExcludeDaysToWeekend();
        setLoggedDaysDesign();
    }

    function setStartNow(){
    var currentTime = new Date();
 var hour = currentTime.getHours();
 var minute = currentTime.getMinutes();
 startState = 1;
 var now = calculateTimeForInputfileds(hour,minute);
 AJS.$("#startTime").val(now);
}
function setStartDecTemporary(){
    setStartNow();
    var startTimeValParts = AJS.$("#startTime").val().split(':');
    var hour = parseInt(startTimeValParts[0]);
    var minString = startTimeValParts[1];
    var min = parseInt(minString);
    var minSubInt = parseInt(minString.substring(1,2));
    if((minSubInt != 0) && (minSubInt != 5)){
        min = min - $startTimeChange;
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
function setStartInc(){
    setStartNow();
    var startTimeValParts = AJS.$("#startTime").val().split(':');
    var hour = parseInt(startTimeValParts[0]);
    var min = parseInt(startTimeValParts[1]);
    min = min + $startTimeChange;
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

 function setEndDecTemporary(){
    setEndNow();
    var endTimeValParts = AJS.$("#endTime").val().split(':');
    var hour = parseInt(endTimeValParts[0]);
    var minString = endTimeValParts[1];
    var min = parseInt(minString);
    var minSubInt = parseInt(minString.substring(1,2));
    if((minSubInt != 0) && (minSubInt != 5)){
        min = min - $endTimeChange;
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
function setEndInc(){
    setEndNow();
    var endTimeValParts = AJS.$("#endTime").val().split(':');
    var hour = parseInt(endTimeValParts[0]);
    var min = parseInt(endTimeValParts[1]);
    min = min + $endTimeChange;
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
    