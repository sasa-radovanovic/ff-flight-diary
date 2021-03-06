/**
 * angular-ui-chart - Add a jqPlot chart to your Angular application.
 * @version v0.0.1 - 2014-04-17
 * @link http://angular-ui.github.com
 * @license MIT
 */
angular.module("ui.chart",[]).directive("uiChart",function(){return{restrict:"EACM",template:"<div></div>",replace:!0,link:function(a,b,c){var d=function(){var d=a.$eval(c.uiChart);if(b.html(""),angular.isArray(d)){var e={};if(!angular.isUndefined(c.chartOptions)&&(e=a.$eval(c.chartOptions),!angular.isObject(e)))throw"Invalid ui.chart options attribute";b.jqplot(d,e)}};a.$watch(c.uiChart,function(){d()},!0),a.$watch(c.chartOptions,function(){d()})}}});
