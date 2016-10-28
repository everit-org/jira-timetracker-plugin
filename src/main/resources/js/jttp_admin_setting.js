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
everit.jttp.admin = everit.jttp.admin || {};

(function(jttpadmin, jQuery) { 
	     
  jQuery(document).ready(function() {
    var calFrom = Calendar.setup({
      firstDay : jttpadmin.options.firstDay,
      inputField : jQuery("#excludeDate"),
      button : jQuery("#excludedate_trigger_from"),
      ifFormat: jttpadmin.options.dateFormat,
      align : 'Br',
      electric : false,
      singleClick : true,
      showOthers : true,
      useISO8601WeekNumbers : false,
    });
    var calFrom = Calendar.setup({
        firstDay : jttpadmin.options.firstDay,
        inputField : jQuery("#includeDate"),
        button : jQuery("#includedate_trigger_from"),
        ifFormat: jttpadmin.options.dateFormat,
        align : 'Br',
        electric : false,
        singleClick : true,
        showOthers : true,
        useISO8601WeekNumbers : false,
      });
    jttpadmin.initDatesDiv(jttpadmin.options.excludeDates,'excludeDatesDiv','e');
    jttpadmin.initDatesDiv(jttpadmin.options.includeDates,'includeDatesDiv','i');
  });
  jttpadmin.initDatesDiv=function (dates, divId, diffCar){
	  for (var i in dates) {
	  	  var eDate=dates[i];
	  	
	    var includeDate= new Date(eDate); 
	    	var labelToAdd = aui.labels.label({
	  		text: includeDate.print(jttpadmin.options.dateFormat),
	  		id: diffCar+eDate,
	  		isCloseable: true
	  	});
	 	    jQuery('#'+divId).append(labelToAdd);
	     	jQuery('#'+ divId + ' #'+ diffCar+eDate + ' .aui-icon.aui-icon-close').click(function() {
	     			jQuery( this ).parent().remove();
	  	     });
	  }
  }
  jttpadmin.beforeSubmit= function (){
	  jQuery("#error_messageJS").hide();
	  jQuery("#error_messageJS").empty();
	  jQuery("#excludedatesHiddenSelect").empty();
	  jQuery("#includedatesHiddenSelect").empty();
	  var dates=[];
	  var errorDates=[];
	  var error="";
	  jQuery("#excludeDatesDiv .aui-label.aui-label-closeable").each(function() {
		  var actId= jQuery( this ).attr('id').substr(1);
		  dates.push(actId);
	  jQuery("#excludedatesHiddenSelect").append( '<option value="'+ actId +'" selected></option>' );
	  });
	  jQuery("#includeDatesDiv .aui-label.aui-label-closeable").each(function() {
		  var actId= jQuery( this ).attr('id').substr(1);
		 if( jQuery.inArray(actId, dates) >-1){
			 errorDates.push(actId);
			 error="duplicateDate";
		 }
	  jQuery("#includedatesHiddenSelect").append( '<option value="'+ actId +'" selected></option>' );
	  });
	  if(error!=""){
		  jQuery("#error_messageJS").append(jttpadmin.options.duplicateDateMessage);
		  for(i in errorDates){
		  jQuery("#error_messageJS").append(' '
				  + new Date(new Number(errorDates[i])).print(jttpadmin.options.dateFormat));
		  }
		  jQuery("#error_messageJS").show();
		  return false;
	  }else {
		  return true;
	  }
  }
  
  jttpadmin.addExcludeDate= function (element){
	  jttpadmin.addDateAction("excludeDate","excludeDatesDiv","messageExclude",
			  jQuery(element).attr('data-jttp-error-msg'),'e');
  }
	  
  jttpadmin.addIncludeDate= function (element){
	  jttpadmin.addDateAction("includeDate","includeDatesDiv", "messageInclude", 
			  jQuery(element).attr('data-jttp-error-msg'),'i');
  }
  
  jttpadmin.addDateAction= function(dateInputFiledId, targetDivId, errorDiv,errorMsg,diffCar){
	  var dateValue=jQuery("#"+dateInputFiledId).val();
	  if(dateValue==""){
		  return;
	  }
	  jQuery('#'+errorDiv +' span').remove();
	  var dateInMilis=Date.parseDate(dateValue, jttpadmin.options.dateFormat);
	  if(jQuery('#' +targetDivId +' #'+diffCar+jttpadmin.getTimeWithoutTimezone(dateInMilis)).length >0){
		  jQuery('#'+errorDiv).append('<span>'+ errorMsg +' '+dateValue+' </span>');
		  return;
	  }
	   var labelToAdd = aui.labels.label({
	     text: dateValue,
	     id: diffCar+jttpadmin.getTimeWithoutTimezone(dateInMilis),
	     isCloseable: true
	   });
	  jQuery('#' +targetDivId).append(labelToAdd);
	  jQuery('#' +targetDivId +' #'+diffCar+jttpadmin.getTimeWithoutTimezone(dateInMilis) 
			  + ' .aui-icon.aui-icon-close').click(function() {
	  		jQuery( this ).parent().remove();
	  	});
  }

  jttpadmin.handleNonEstClick= function(element){
  if(element.value=="nonEstSelected"){
	  jQuery( "#issueSelect_collector_container").show();
   } else {
	  jQuery( "#issueSelect_collector_container").hide();
   }
 }
    jttpadmin.getTimeWithoutTimezone= function(date){
	  return date.getTime()-(date.getTimezoneOffset()*60000);
}
})(everit.jttp.admin, jQuery);
