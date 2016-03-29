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
everit.jttp.reporting_admin = everit.jttp.reporting_admin || {};

(function(jttp, jQuery) {

  jQuery(document).ready(function() {
    
    jQuery("#pagesize-dropdown2-checkbox-radio-interactive").find("a.aui-dropdown2-radio").each(function() {
      this.onclick = function(){  
          updatePageSizeButtonText(this); 
      }; 
    });
    
    jQuery("#pages_" + jttp.options.pageSize).addClass("checked aui-dropdown2-checked");
    updatePageSizeButtonText(jQuery("#pages_" + jttp.options.pageSize)[0]);

  });

  updatePageSizeButtonText = function(element){
    jQuery("#pageSizeInput").val(element.text);
    jQuery("#pageSizeButton").text(element.text);
  };

  
})(everit.jttp.reporting_admin, jQuery);