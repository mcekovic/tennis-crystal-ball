function showRankingChart(chartData, elementId, width, bySeason, pointCount, isRank, useLogScale, legendPosition) {
	showChart(chartData, elementId, width, bySeason, pointCount, isRank ? -1 : 1, isRank ? 1 : undefined, useLogScale, false, !bySeason, 0, legendPosition);
}

function showPerformanceChart(chartData, elementId, width, pointCount, legendPosition) {
	showChart(chartData, elementId, width, true, pointCount, 1, 0, false, true, false, 0, legendPosition);
}

function showStatsChart(chartData, elementId, width, pointCount, isPct, legendPosition) {
	showChart(chartData, elementId, width, true, pointCount, 1, 0, false, isPct, false, 0, legendPosition);
}

function showResultsChart(chartData, elementId, width, bySeason, pointCount, legendPosition) {
	showChart(chartData, elementId, width, bySeason, pointCount, 1, 0, false, false, !bySeason, 5, legendPosition);
}

function showChart(chartData, elementId, width, bySeason, pointCount, vDir, vMin, useLogScale, isPct, interpolate, pointSize, legendPosition) {
	if (chartData !== undefined) {
		var options = {
			width: width,
			height: width / 2,
			chartArea: {left: 50, top: 20, height: "80%"},
			hAxis: {format: bySeason ? "####" : null, gridlines: pointCount < 5 ? { count: pointCount} : null},
			vAxis: {direction: vDir, viewWindow: {min: vMin}, logScale: useLogScale, format: isPct ? "percent" : null},
			interpolateNulls: interpolate,
			pointSize: pointSize,
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
				if (value) {
					min = Math.min(min, value);
					max = Math.max(max, value);
				}
			}
		}
	}
	return max - min >= 50;
}

function defaultChartSize(device) {
	switch (device) {
		case "xs": return 600;
		case "sm": return 750;
		case "md": return 950;
		default: return 1000;
	}
}

function defaultChartWOLegendSize(device) {
	return device == "xs" ? 750 : 1000;
}