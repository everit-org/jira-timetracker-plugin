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
function callUpdateNotifierRest() {
	jQuery.ajax({
		async : true,
		type : 'GET',
		url : contextPath + "/rest/jttp-rest/1/update-notifier/cancel-update",
		data : [],
		success : function(result) {
			jQuery(".update-notification-banner").remove();
		},
		error : function(XMLHttpRequest, status, error) {
		}
	});
}
