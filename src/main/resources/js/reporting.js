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
everit.reporting = everit.reporting || {};
everit.reporting.main = everit.reporting.main || {};

(function(reporting, jQuery) {

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
    
    var opt = reporting.values;
     
    Date.parseDate = function(str, fmt){return fecha.parse(str, AJS.Meta.get("date-dmy").toUpperCase());};
    
    var calFrom = Calendar.setup({
      firstDay : opt.firstDay,
      inputField : jQuery("#dateFrom"),
      button : jQuery("#date_trigger_from"),
      date : opt.dateFromFormated,
      align : 'Br',
      electric : false,
      singleClick : true,
      showOthers : true,
      useISO8601WeekNumbers : opt.useISO8601,
      onSelect: reporting.onSelect,
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
      onSelect: reporting.onSelect,
    });

    jQuery('.aui-ss, .aui-ss-editing, .aui-ss-field').attr("style", "width: 300px;");
    
    initProjectSelect();
    initStatusSelect();
    initTypeSelect();
    initPrioritySelect();
    initResolutionSelect();
    initGroupSelect();
    initUserSelect();
    initAssigneSelect();
    initReporterSelect();
    initAffectedVersionSelect();
    initFixVersionSelect();
    initComponentSelect();
  });
  
  
  function initPrioritySelect(){
    var selectedArray =  jQuery.makeArray( reporting.values.selectedPriorities ); 
    jQuery.ajax({
      async: true,
      type: 'GET',
      url : contextPath + "/rest/api/2/priority",
      data : [],
      success : function(result){
        for( var i in result) {
          var obj = result[i];
          var selected = checkSelected(obj.id, selectedArray);
          var avatarId =  obj.iconUrl;
          jQuery("#priorityPicker").append("<option data-icon=" + avatarId + " value="+obj.id + " "+ selected + ">" +obj.name +"</option>");

        }
        var pp = new AJS.CheckboxMultiSelect({
            element:  jQuery("#priorityPicker"),
            submitInputVal: true,
        });
        updatePickerButtonText("#priorityPicker" , "#priorityPickerButton", "Priority: All");
        jQuery("#priorityPicker").on("change unselect", function() {
          updatePickerButtonText("#priorityPicker" , "#priorityPickerButton", "Priority: All");
        });
      },
      error : function(XMLHttpRequest, status, error){
      }
    });
  };
  
  function initProjectSelect(){
    var selectedArray =  jQuery.makeArray( reporting.values.selectedProjects ); 
    jQuery.ajax({
      async: true,
      type: 'GET',
      url : contextPath + "/rest/api/2/project",
      data : [],
      success : function(result){
        for( var i in result) {
          var obj = result[i];
          var selected = checkSelected(obj.id, selectedArray);
          var avatarId =  obj.avatarUrls["16x16"];
          jQuery("#projectPicker").append("<option data-icon=" + avatarId + " value=" + obj.id + " "+ selected +">" +obj.name+ "(" + obj.key + " )" +"</option>");
        }
        var pp = new AJS.CheckboxMultiSelect({
            element:  jQuery("#projectPicker"),
            submitInputVal: true,
        });
        updatePickerButtonText("#projectPicker" , "#projectPickerButton", "Project: All");
        jQuery("#projectPicker").on("change unselect", function() {
          updatePickerButtonText("#projectPicker" , "#projectPickerButton", "Project: All");
        });
      },
      error : function(XMLHttpRequest, status, error){
      }
    });
  };
  
  function initAssigneSelect(){
    var selectedArray =  jQuery.makeArray( reporting.values.selectedAssignes ); 
    jQuery.ajax({
      async: true,
      type: 'GET',
      url : contextPath + "/rest/api/2/user/picker?query=daniel.toth@everit.biz",
      data : [],
      success : function(result){
        for( var i in result.users) {
          var obj = result.users[i];
          var avatarId =  obj.avatarUrl;
          var selected = checkSelected(obj.id, selectedArray);
          jQuery("#assignePicker").append("<option data-icon=" + avatarId + " value="+obj.name + " "+ selected + ">" +obj.displayName +"</option>");
        }
        var pp = new AJS.CheckboxMultiSelect({
          element:  AJS.$("#assignePicker"),
          submitInputVal: true,
        });
        updatePickerButtonText("#assignePicker" , "#assignePickerButton", "Assigne: All");
        jQuery("#assignePicker").on("change unselect", function() {
          updatePickerButtonText("#assignePicker" , "#assignePickerButton", "Assigne: All");
        });
      },
      error : function(XMLHttpRequest, status, error){
      }
    });
  };
  
  function initReporterSelect(){
    var selectedArray =  jQuery.makeArray( reporting.values.selectedReportes ); 
    jQuery.ajax({
      async: true,
      type: 'GET',
      url : contextPath + "/rest/api/2/user/picker?query=daniel.toth@everit.biz",
      data : [],
      success : function(result){
        for( var i in result.users) {
          var obj = result.users[i];
          var avatarId =  obj.avatarUrl;
          var selected = checkSelected(obj.id, selectedArray);
          jQuery("#reporterPicker").append("<option data-icon=" + avatarId + " value="+obj.name + " "+ selected + ">" +obj.displayName +"</option>");
        }
        var pp = new AJS.CheckboxMultiSelect({
          element:  AJS.$("#reporterPicker"),
          submitInputVal: true,
        });
        updatePickerButtonText("#reporterPicker" , "#reporterPickerButton", "Reporter: All");
        jQuery("#reporterPicker").on("change unselect", function() {
          updatePickerButtonText("#reporterPicker" , "#reporterPickerButton", "Reporter: All");
        });
      },
      error : function(XMLHttpRequest, status, error){
      }
    });
  };
  
  function initUserSelect(){
    var selectedArray =  jQuery.makeArray( reporting.values.selectedUsers ); 
    jQuery.ajax({
      async: true,
      type: 'GET',
      url : contextPath + "/rest/api/2/user/picker?query=test",
      data : [],
      success : function(result){
        for( var i in result.users) {
          var obj = result.users[i];
          var avatarId =  obj.avatarUrl;
          var selected = checkSelected(obj.id, selectedArray);
          jQuery("#userPicker").append("<option data-icon=" + avatarId + " value="+obj.name + " "+ selected + ">" +obj.displayName +"</option>");
        }
        var pp = new AJS.CheckboxMultiSelect({
          element:  AJS.$("#userPicker"),
          submitInputVal: true,
        });
        updatePickerButtonText("#userPicker" , "#userPickerButton", "User: All");
        jQuery("#userPicker").on("change unselect", function() {
          updatePickerButtonText("#userPicker" , "#userPickerButton", "User: All");
        });
      },
      error : function(XMLHttpRequest, status, error){
      }
    });
  };
  
  function initGroupSelect(){
    var selectedArray =  jQuery.makeArray( reporting.values.selectedGroups ); 
    jQuery.ajax({
      async: true,
      type: 'GET',
      url : contextPath + "/rest/api/2/groups/picker",
      data : [],
      success : function(result){
        for( var i in result.groups) {
          var obj = result.groups[i];
          var selected = checkSelected(obj.id, selectedArray);
          jQuery("#groupPicker").append("<option value="+obj.name + " "+ selected + ">" +obj.name +"</option>");
        }
        var pp = new AJS.CheckboxMultiSelect({
              element:  AJS.$("#groupPicker"),
              submitInputVal: true,
        });
        updatePickerButtonText("#groupPicker" , "#groupPickerButton", "Group: All");
        jQuery("#groupPicker").on("change unselect", function() {
          updatePickerButtonText("#groupPicker" , "#groupPickerButton", "Group: All");
        });
      },
      error : function(XMLHttpRequest, status, error){
      }
    });
  };
  
  function initTypeSelect(){
    var selectedArray =  jQuery.makeArray( reporting.values.selectedTypes ); 
    jQuery.ajax({
      async: true,
      type: 'GET',
      url : contextPath + "/rest/api/2/issuetype",
      data : [],
      success : function(result){
        for( var i in result) {
          var obj = result[i];
          var avatarId =  obj.iconUrl;
          var selected = checkSelected(obj.id, selectedArray);
          jQuery("#typePicker").append("<option data-icon=" + avatarId + " value="+obj.id + " "+ selected + ">" +obj.name +"</option>");
        }
        var pp = new AJS.CheckboxMultiSelect({
              element:  AJS.$("#typePicker"),
              submitInputVal: true,
        });
        updatePickerButtonText("#typePicker" , "#typePickerButton", "Type: All");
        jQuery("#typePicker").on("change unselect", function() {
          updatePickerButtonText("#typePicker" , "#typePickerButton", "Type: All");
        });
      },
      error : function(XMLHttpRequest, status, error){
      }
    });
  };
  
  function initResolutionSelect(){
    var selectedArray =  jQuery.makeArray( reporting.values.selectedResolutions ); 
    jQuery.ajax({
      async: true,
      type: 'GET',
      url : contextPath + "/rest/api/2/resolution",
      data : [],
      success : function(result){
        for( var i in result) {
          var obj = result[i];
          var selected = checkSelected(obj.id, selectedArray);
          jQuery("#resolutionPicker").append("<option value="+obj.id+ " "+ selected + ">" +obj.name +"</option>");
        }
        var pp = new AJS.CheckboxMultiSelect({
              element:  AJS.$("#resolutionPicker"),
              submitInputVal: true,
        });
        updatePickerButtonText("#resolutionPicker" , "#resolutionPickerButton", "Resolution: All");
        jQuery("#resolutionPicker").on("change unselect", function() {
          updatePickerButtonText("#resolutionPicker" , "#resolutionPickerButton", "Resolution: All");
        });
      },
      error : function(XMLHttpRequest, status, error){
      }
    });
  };
  
  function initStatusSelect(){
    var selectedArray =  jQuery.makeArray( reporting.values.selectedStatus ); 
    jQuery.ajax({
      async: true,
      type: 'GET',
      url : contextPath + "/rest/api/2/status",
      data : [],
      success : function(result){
        for( var i in result) {
          var obj = result[i];
          var selected = checkSelected(obj.id, selectedArray);
          var lozengeStatus = JSON.stringify(obj).replace(/"/g, "&quot;");
          jQuery("#statusPicker").append('<option value="'+obj.id+ '" data-simple-status="' + lozengeStatus +'"  ' + selected + '>' +obj.name +'</option>');
        }
        var pp = new AJS.CheckboxMultiSelectStatusLozenge({
              element:  AJS.$("#statusPicker"),
              submitInputVal: true,
        });
        updatePickerButtonText("#statusPicker" , "#statusPickerButton", "Status: All");
        jQuery("#statusPicker").on("change unselect", function() {
          updatePickerButtonText("#statusPicker" , "#statusPickerButton", "Status: All");
        });
      },
      error : function(XMLHttpRequest, status, error){
      }
    });
  };
  
  function initAffectedVersionSelect(){
    var selectedArray =  jQuery.makeArray( reporting.values.selectedAffectedVersions ); 
    jQuery.ajax({
      async: true,
      type: 'GET',
      url : contextPath + "/rest/api/2/status", //TODO set the right rest api
      data : [],
      success : function(result){
        for( var i in result) {
          var obj = result[i];
          var selected = checkSelected(obj.id, selectedArray);
          jQuery("#affectedVersionPicker").append("<option value="+obj.id+ " "+ selected + ">" +obj.name +"</option>");
        }
        var pp = new AJS.CheckboxMultiSelect({
              element:  AJS.$("#affectedVersionPicker"),
              submitInputVal: true,
        });
        updatePickerButtonText("#affectedVersionPicker" , "#affectedVersionPickerButton", "Affects Version: All");
        jQuery("#affectedVersionPicker").on("change unselect", function() {
          updatePickerButtonText("#affectedVersionPicker" , "#affectedVersionPickerButton", "Affects Version: All");
        });
      },
      error : function(XMLHttpRequest, status, error){
      }
    });
  };
  
  function initFixVersionSelect(){
    var selectedArray =  jQuery.makeArray( reporting.values.selectedFixVersions ); 
    jQuery.ajax({
      async: true,
      type: 'GET',
      url : contextPath + "/rest/api/2/status", //TODO set the right rest api
      data : [],
      success : function(result){
        for( var i in result) {
          var obj = result[i];
          var selected = checkSelected(obj.id, selectedArray);
          jQuery("#fixVersionPicker").append("<option value="+obj.id+ " "+ selected + ">" +obj.name +"</option>");
        }
        var pp = new AJS.CheckboxMultiSelect({
              element:  AJS.$("#fixVersionPicker"),
              submitInputVal: true,
        });
        updatePickerButtonText("#fixVersionPicker" , "#fixVersionPickerButton", "Fix Version: All");
        jQuery("#fixVersionPicker").on("change unselect", function() {
          updatePickerButtonText("#fixVersionPicker" , "#fixVersionPickerButton", "Fix Version: All");
        });
      },
      error : function(XMLHttpRequest, status, error){
      }
    });
  };
  
  function initComponentSelect(){
    var selectedArray =  jQuery.makeArray( reporting.values.selectedComponent ); 
    jQuery.ajax({
      async: true,
      type: 'GET',
      url : contextPath + "/rest/api/2/status", //TODO set the right rest api
      data : [],
      success : function(result){
        for( var i in result) {
          var obj = result[i];
          var selected = checkSelected(obj.id, selectedArray);
          jQuery("#componentPicker").append("<option value="+obj.id+ " "+ selected + ">" +obj.name +"</option>");
        }
        var pp = new AJS.CheckboxMultiSelect({
              element:  AJS.$("#componentPicker"),
              submitInputVal: true,
        });
        updatePickerButtonText("#componentPicker" , "#componentPickerButton", "Component: All");
        jQuery("#componentPicker").on("change unselect", function() {
          updatePickerButtonText("#componentPicker" , "#componentPickerButton", "Component: All");
        });
      },
      error : function(XMLHttpRequest, status, error){
      }
    });
  };
  
  function checkSelected(id , selectedArray){
    var selected = "";
    for(var i in selectedArray){
      if(selectedArray[i] == id) selected = "selected";
    }
    return selected;
  };
  
  function updatePickerButtonText(picker, button, defaultText){ //Example vallues: "#projectPicker" , "#projectPickerButton", "Project: All"
    var newButtonText = "";
    jQuery(picker).find("option:selected").each(function() {
      var optionText = AJS.$(this).text();
      if (newButtonText === '') {
        newButtonText = optionText;
      } else {
        newButtonText = newButtonText + "," + optionText;
      }
    });
    if (newButtonText === '') {
      newButtonText = defaultText;
    }else if(newButtonText.length > 16){
      newButtonText = newButtonText.substring(0, 12) + "...";
    }
    jQuery(button).text(newButtonText);
  };
  
  reporting.onSelect = function(cal) {
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
  
  reporting.toggleModContent = function(type) {
    var module = jQuery("#" + type + "Module");
    var icon  = jQuery(".mod-header .aui-icon", module);
    
    jQuery(".mod-content", module).toggle(0, function() {
        module.toggleClass("collapsed");
        
        if(module.hasClass("collapsed")) {
            icon.removeClass("aui-iconfont-expanded").addClass("aui-iconfont-collapsed");
        } else {
            icon.removeClass("aui-iconfont-collapsed").addClass("aui-iconfont-expanded");
        }
        
    });
  }

  reporting.changeFilterType = function(type) {
    var searchWrap = jQuery(".search-wrap");
    var formType = jQuery("input[name=formType]");
    
    formType.attr("value", type);
    
    if(type === "basic") {
        searchWrap.removeClass("filter").addClass("basic");
    } else {
        searchWrap.removeClass("basic").addClass("filter");
    }
  }

})(everit.reporting.main, jQuery);