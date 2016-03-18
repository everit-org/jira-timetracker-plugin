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