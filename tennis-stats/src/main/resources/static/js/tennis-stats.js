// Autocomplete player

function autocompletePlayer(id) {
	$(function () {
		$("#" + id).autocomplete({
			source: "/autocompletePlayer",
			minLength: 2,
			select: function (event, ui) {
				if (ui.item)
					window.location = "/playerProfile?playerId=" + ui.item.id;
			}
		});
	});
}

function split(val) {
	return val.split(/,\s*/);
}

function extractLast(term) {
	return split(term).pop();
}

function autocompletePlayers(id, func) {
	$("#" + id).bind("keydown", function (event) {
		if (event.keyCode === $.ui.keyCode.ENTER && !$(this).autocomplete("instance").menu.active)
			func();
		else if (event.keyCode === $.ui.keyCode.TAB && $(this).autocomplete("instance").menu.active)
			event.preventDefault();
	}).autocomplete({
		source: function (request, response) {
			$.getJSON("/autocompletePlayer", {
				term: extractLast(request.term)
			}, response);
		},
		search: function () {
			var term = extractLast(this.value);
			if (term.length < 2) {
				return false;
			}
		},
		focus: function () {
			return false;
		},
		select: function (event, ui) {
			var terms = split(this.value);
			terms.pop();
			terms.push(ui.item.value);
			terms.push("");
			this.value = terms.join(", ");
			return false;
		}
	});
}

function getPlayerCount(players) {
	var a = split(players);
	var count = 0;
	for (var i = 0; i < a.length; i++) {
		if ($.trim(a[i]) != "")
			count++;
	}
	return count;
}


// Dates

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

function dateRangePicker(fromId, toId) {
	var $from = $("#" + fromId);
	var $to = $("#" + toId);
	$from.datepicker({
		defaultDate: "-1y", maxDate: "0", changeMonth: true, changeYear: true, showWeek: true, firstDay: 1, dateFormat: "dd-mm-yy",
		onClose: function (selectedDate) {
			$to.datepicker("option", "minDate", selectedDate);
		}
	});
	$to.datepicker({
		defaultDate: "0", maxDate: "0", changeMonth: true, changeYear: true, showWeek: true, firstDay: 1, dateFormat: "dd-mm-yy",
		onClose: function (selectedDate) {
			$from.datepicker("option", "maxDate", selectedDate);
		}
	});
	$("div.ui-datepicker").css({fontSize: "12px"});
}


// Bootgrid

function setBootgridTitles($gridTable, titles) {
	for (var i = 0, count = titles.length; i < count; i++) {
		var title = titles[i];
		$gridTable.find("th[data-column-id='" + title.id + "'] > a > span[class='text']").attr("title", title.title);
	}
}

// Date Formatter
function dateFormatter(column, row) {
	return $.datepicker.formatDate("dd-mm-yy", new Date(row.date));
}

// Country Formatter
function countryFormatter(column, row) {
	return "<img src='/images/flags/" + row.countryCode + ".png' title='" + row.countryId + "' width='24' height='20'/> " + row.countryId;
}

// Player Formatter
function playerFormatter(column, row) {
	return "<a href='/playerProfile?playerId=" + row.playerId + "' title='Show profile'>" + row.name + "</a>";
}

function playerCountryFormatter(column, row) {
	return "<img src='/images/flags/" + row.countryCode + ".png' title='" + row.countryId + "' width='24' height='20'/> " +
		"<a href='/playerProfile?playerId=" + row.playerId + "' title='Show profile'>" + row.name + "</a>";
}

function rivalryFormatter(column, row) {
	return playerCountryFormatter(column, row.player1) + " - " + playerCountryFormatter(column, row.player2);
}


// Level Formatter
function levelFormatter(column, row) {
	return "<span class='label label-" + levelClassSuffix(row.level) + "'>" + levelName(row.level) + "</span>";
}
function levelClassSuffix(level) {
	switch (level) {
		case "G": return "danger";
		case "F": return "warning";
		case "M": return "info";
		case "O": return "success";
		default: return "default";
	}
}
function levelName(level) {
	switch (level) {
		case "G": return "Grand Slam";
		case "F": return "Tour Finals";
		case "M": return "Masters";
		case "A": return "ATP";
		case "O": return "Olympics";
		case "D": return "Davis Cup";
		default: return level;
	}
}

// Surface Formatter
function surfaceFormatter(column, row) {
	if (row.surface)
		return "<span class='label label-" + surfaceClassSuffix(row.surface) + "'>" + surfaceName(row.surface) + "</span>";
}
function surfaceClassSuffix(surface) {
	switch (surface) {
		case "H": return "primary";
		case "C": return "danger";
		case "G": return "success";
		case "P": return "warning";
		default: return "default";
	}
}
function surfaceName(surface) {
	switch (surface) {
		case "H": return "Hard";
		case "C": return "Clay";
		case "G": return "Grass";
		case "P": return "Carpet";
		default: return surface;
	}
}

// Indoor Formatter
function indoorFormatter(column, row) {
	return row.indoor ? "Indoor" : "Outdoor";
}

// Tournament Formatter
function tournamentFormatter(column, row) {
	return "<span class='label label-" + levelClassSuffix(row.level) + "'>" + (row.tournament ? row.tournament : row.name) + "</span>";
}

// Result Formatter
function resultFormatter(column, row) {
	return "<span class='label black bg-result-" + row.result + "'>" + row.result + "</span>"
}

// Match Formatter
function matchFormatter(column, row) {
	return formatMatchPlayer(row.winner) + " d. " + formatMatchPlayer(row.loser);
}

function formatMatchPlayer(player) {
	return "<a href='/playerProfile?playerId=" + player.id + "' title='Show profile'>" + player.name
		+ (player.seed ? (" (" + player.seed  + (player.entry ? " " + player.entry : "") + ")") : (player.entry ? " (" + player.entry + ")" : "")) + "</a>";
}

// Misc Formatter
function appendGoatPointsTitle(title, row, propertyName, propertyTitle) {
	var points = row[propertyName];
	if (points > 0) {
		if (title)
			title += ", ";
		title += propertyTitle + ": " + points;
	}
	return title;
}
