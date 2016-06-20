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
everit.jttp.chart_report = everit.jttp.chart_report || {};

(function(jttp, jQuery) {

  jQuery(document).ready(function() {
    google.load('visualization', '1', {
      packages : [ 'controls' ],
      callback : drawChart
    });
  });

  
  function drawChart() {
    var dataArray = new Array();
    dataArray.push([ 'id', 'duration' ]);
    var initState = {
      selectedValues : []
    };
    var chartDataList = jttp.options.chartDataList;
    for (index = 0; index < chartDataList.length; index++) {
      var chartData = chartDataList[index];
      var projectId = new String(chartData.projectId);
      var duration = new Number(chartData.duration);
      duration = duration / (1000 * 60 * 60);
      duration = +duration.toFixed(2);
      dataArray.push([ projectId.toString(), duration ]);
      initState.selectedValues.push(projectId.toString());
    }

    var data = google.visualization.arrayToDataTable(dataArray);

    var control = new google.visualization.ControlWrapper({
      containerId : 'control_div',
      controlType : 'CategoryFilter',
      options : {
        filterColumnIndex : 0,
        ui : {
          'label' : '',
          'selectedValuesLayout' : 'belowStacked'
        }
      },
      state : initState
    });

    var chart = new google.visualization.ChartWrapper({
      containerId : 'chart_div',
      chartType : 'PieChart',
      options : {
        height : 500,
        width : 700,
        backgroundColor : 'transparent',
        pieSliceText : 'value',
        title : 'Hours worked by projects',
        legend : {
          position : 'right',
          alignment : 'center'
        }
      },
      view : {
        columns : [ 0, 1 ]
      }
    });

    var dashboard = new google.visualization.Dashboard(document.querySelector('#dashboard'));

    dashboard.bind([ control ], [ chart ]);
    dashboard.draw(data);
  }

})(everit.jttp.chart_report, AJS.$);
