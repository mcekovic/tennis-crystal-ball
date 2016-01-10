function showChart(chartData, elementId, width, isRank, bySeason, useLogScale, pointCount, legendPosition) {
	if (chartData != undefined) {
		var options = {
			width: width,
			height: width / 2,
			chartArea: {left: 50, top: 20, height: "80%"},
			vAxis: {direction: isRank ? -1 : 1, viewWindow: {min: isRank ? 1 : undefined}, logScale: useLogScale},
			hAxis: {format: bySeason ? "#" : null, gridlines: pointCount < 5 ? { count: pointCount}: null},
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