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
	var currentCalendar = AJS.$("#currentCalendar").val();
	var dateFormatted = AJS.$("#dateFormatted").val();
	var firstDay = AJS.$("#firstDay").val();
	
	var calFrom= Calendar.setup({
		firstDay : firstDay,
		inputField : AJS.$("#dateFrom"),
		button : AJS.$("#date_trigger_from"),
		date : dateFormatted,
		align : 'Br',
		electric: false,
		singleClick : true,
		showOthers: true,
		useISO8601WeekNumbers : currentCalendar.useISO8601,
		ifFormat : '%Y-%m-%d'
	});

	var calTo= Calendar.setup({
		firstDay : firstDay,
		inputField : AJS.$("#dateTo"),
		button : AJS.$("#date_trigger_to"),
		date : dateFormatted,
		align : 'Br',
		electric: false,
		singleClick : true,
		showOthers: true,
		useISO8601WeekNumbers : currentCalendar.useISO8601,
		ifFormat : '%Y-%m-%d'
	});
	
	var hourElement = AJS.$("#hour");
	if (hourElement){
		setCheckWorkedHours();
	}
	
});

function checkToEnter(event) {
	if (event.keyCode == 13) {
		AJS.$("#search").click();
		return false;
	}
}

function checkEnter(event){
    if (event.keyCode == 13) {
      AJS.$("#dateTo").focus();
      return false;
    }
}

function setCheckWorkedHours() {
	if (AJS.$("#hour").is(":checked")) {
		document.getElementById("nonworking").disabled = false;
	} else {
		document.getElementById("nonworking").disabled = true;
	}
}