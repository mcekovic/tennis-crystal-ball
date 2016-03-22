function showRankingChart(chartData, elementId, width, bySeason, pointCount, isRank, useLogScale, legendPosition) {
	showChart(chartData, elementId, width, bySeason, pointCount, isRank ? -1 : 1, isRank ? 1 : undefined, useLogScale, false, !bySeason, legendPosition);
}

function showPerformanceChart(chartData, elementId, width, pointCount, legendPosition) {
	showChart(chartData, elementId, width, true, pointCount, 1, 0, false, true, false, legendPosition);
}

function showStatsChart(chartData, elementId, width, pointCount, isPct, legendPosition) {
	showChart(chartData, elementId, width, true, pointCount, 1, 0, false, isPct, false, legendPosition);
}

function showChart(chartData, elementId, width, bySeason, pointCount, vDir, vMin, useLogScale, isPct, interpolate, legendPosition) {
	if (chartData != undefined) {
		var options = {
			width: width,
			height: width / 2,
			chartArea: {left: 50, top: 20, height: "80%"},
			hAxis: {format: bySeason ? "####" : null, gridlines: pointCount < 5 ? { count: pointCount} : null},
			vAxis: {direction: vDir, viewWindow: {min: vMin}, logScale: useLogScale, format: isPct ? "percent" : null},
			interpolateNulls: interpolate,
			legend: {position: legendPosition}
		};
		var chart = new google.visualization.LineChart(document.getElementById(elementId));
		chart.draw(chartData, options);
	}
}

function useLogScale(json) {
	var min = Number.MAX_VALUE;
	var max = 0;
	for (var i = 0, ilen = json.rows.length; i < ilen; i++) {
		var row = json.rows[i];
		for (var j = 0, jlen = row.c.length; j < jlen; j++) {
			if (j > 0) {
				var value = row.c[j].v;
				min = Math.min(min, value);
				max = Math.max(max, value);
			}
		}
	}
	return max - min >= 50;
}