function showChart(chartData, elementId, width, isRank, useLogScale, legendPosition) {
	if (chartData != undefined) {
		var options = {
			width: width,
			height: width / 2,
			chartArea: {left: 50, top: 20, height: "80%"},
			vAxis: {direction: isRank ? -1 : 1, viewWindow: {min: isRank ? 1 : undefined}, logScale: useLogScale},
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

function getDate(id) {
	var $date = $("#" + id);
	var date = $date.val();
	if (date == "")
		return date;
	try {
		$.datepicker.parseDate("dd-mm-yy", date);
		return date;
	}
	catch (err) {
		alert("Invalid " + id.substr(0, id.length - 4) + " date: " + date);
		$date.focus();
		return null;
	}
}
