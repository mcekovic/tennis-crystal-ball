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

function split(val) {
	return val.split(/,\s*/);
}

function extractLast(term) {
	return split(term).pop();
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

function selectPlayers(a) {
	$("#players").val(a.title);
}


// Tabs

function tabClick(event) {
	event.preventDefault();
	var $pill = $(this);
	if ($pill.hasClass("loaded"))
		return;
	var url = $pill.attr("data-url");
	if (typeof url !== "undefined")
		loadTab($pill, url);
	else
		$pill.tab("show");
}

function loadTab($pill, url) {
	var $pane = $($pill.attr("href"));
	$pane.load(url, function () {
		if (!$pill.hasClass("loaded"))
			$pill.addClass("loaded");
		$pill.tab("show");
	});
}


// Collapse

function collapseClick(event) {
	event.preventDefault();
	var $button = $(this);
	if ($button.hasClass("loaded"))
		return;
	var url = $button.attr("data-url");
	if (typeof url !== "undefined")
		loadCollapse($button, url);
	else
		$($button.attr("data-target")).collapse("toggle");
}

function loadCollapse($button, url) {
	var $pane = $($button.attr("data-target"));
	$pane.load(url, function () {
		if (!$button.hasClass("loaded"))
			$button.addClass("loaded");
		$pane.collapse("show");
	});
}


// Dates

var date_format = "dd-mm-yy";

function formatDate(date) {
	return date ? $.datepicker.formatDate(date_format, new Date(date)) : "";
}

function getDate(id) {
	var $date = $("#" + id);
	var date = $date.val();
	if (date == "")
		return date;
	try {
		$.datepicker.parseDate(date_format, date);
		return date;
	}
	catch (err) {
		alert("Invalid " + id.substr(0, id.length - 4) + " date: " + date);
		$date.focus();
		return null;
	}
}

function datePicker(id) {
	$("#" + id).datepicker({
		defaultDate: "0", maxDate: "0", changeMonth: true, changeYear: true, showWeek: true, firstDay: 1, dateFormat: date_format
	});
	$("div.ui-datepicker").css({fontSize: "12px"});
}

function dateRangePicker(fromId, toId) {
	var $from = $("#" + fromId);
	var $to = $("#" + toId);
	$from.datepicker({
		defaultDate: "-1y", maxDate: "0", changeMonth: true, changeYear: true, showWeek: true, firstDay: 1, dateFormat: date_format,
		onClose: function (selectedDate) {
			$to.datepicker("option", "minDate", selectedDate);
		}
	});
	$to.datepicker({
		defaultDate: "0", maxDate: "0", changeMonth: true, changeYear: true, showWeek: true, firstDay: 1, dateFormat: date_format,
		onClose: function (selectedDate) {
			$from.datepicker("option", "maxDate", selectedDate);
		}
	});
	$("div.ui-datepicker").css({fontSize: "12px"});
}


// Bootgrid

function setBootgridColumnsVisible($gridTable, columns, visible) {
	for (var i = 0, count = columns.length; i < count; i++)
		$gridTable.find("th[data-column-id='" + columns[i] + "']").attr("data-visible", visible);
}
function setBootgridColumnsWidths($gridTable, columns, widths) {
	for (var i = 0, count = columns.length; i < count; i++)
		$gridTable.find("th[data-column-id='" + columns[i] + "']").attr("data-width", widths[i]);
}
function setBootgridTitle($gridTableHeader, $gridTableTitle) {
	$gridTableHeader.find("div.actionBar > *:first-child").before($gridTableTitle.remove());
}
function setBootgridTitles($gridTable, titles) {
	$gridTable.bootgrid().on("loaded.rs.jquery.bootgrid", function() {
		for (var i = 0, count = titles.length; i < count; i++) {
			var title = titles[i];
			$gridTable.find("th[data-column-id='" + title.id + "'] > a > span[class='text']").attr("title", title.title);
		}
	});
}
var bootgridTemplateLoading = "Loading... <img src='/images/ui-anim_basic_16x16.gif' width='16' height='16'/>"
/* Fixes Bootgrid Issue with no link cursors on pagination buttons */
var bootgridTemplatePaginationItem = "<li class=\"{{ctx.css}}\"><a href=\"#\" data-page=\"{{ctx.page}}\" class=\"{{css.paginationButton}}\">{{ctx.text}}</a></li>";

// Date Formatter
function dateFormatter(column, row) {
	return formatDate(row.date);
}

// Country Formatter
function countryFormatter(column, row) {
	return "<img src='/images/flags/" + row.country.code + ".png' title='" + row.country.id + "' width='24' height='20'/> " + row.country.id;
}

// Player Formatter
function playerFormatter(column, row) {
	return "<a href='/playerProfile?playerId=" + row.playerId + "' title='Show profile'>" + row.name + "</a>" +
		(row.active ? " <img src='/images/active.png' title='Active' width='12' height='12'/>" : "");
}

function playerCountryFormatter(column, row) {
	return "<img src='/images/flags/" + row.country.code + ".png' title='" + row.country.id + "' width='24' height='20'/> " + playerFormatter(column, row);
}

function formatFavorite(column, favorite) {
	return favorite ? playerCountryFormatter(column, favorite) + " " + ((100 * favorite.probability).toFixed(1)) + "%" : "";
}

// Level Formatter
function levelFormatter(column, row) {
	return "<span class='label label-" + row.level + "'>" + levelName(row.level) + "</span>";
}

function levelName(level) {
	switch (level) {
		case "G": return "Grand Slam";
		case "F": return "Tour Finals";
		case "M": return "Masters";
		case "O": return "Olympics";
		case "A": return "ATP 500";
		case "B": return "ATP 250";
		case "C": return "Challengers";
		case "U": return "Futures";
		case "E": return "Exhibitions";
		case "H": return "Others";
		case "D": return "Davis Cup";
		case "T": return "World Team Cup";
		default: return level;
	}
}

// Surface Formatter
function surfaceFormatter(column, row) {
	if (row.surface)
		return "<span class='label label-" + surfaceClassSuffix(row.surface) + (row.indoor != null ? "' title='" + (row.indoor ? "Indoor" : "Outdoor") : "") + "'>" + surfaceName(row.surface) + "</span>";
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

// Won/Lost Formatter
function wonLostFormatter(column, row) {
	return "<span title='" + row.wonLost + "'>" + row.wonPct + "</span>";
}

// Tournament Formatter
function tournamentFormatter(column, row) {
	return "<a href='/tournament?tournamentId=" + row.tournamentId + "' class='label label-" + row.level + "' title='" + levelName(row.level) + "'>" + row.tournament + "</a>";
}

// Tournament Event Formatter
function tournamentEventFormatter(column, row) {
	return formatTournamentEvent(row.tournamentEventId, row.level, row.tournament ? row.tournament : row.name);
}

function tournamentEventFormatterExtId(column, row) {
	return formatTournamentEvent(row.id, row.level, row.name, row.tournamentExtId);
}

function startTournamentEventFormatter(column, row) {
	return tournamentEventFormatter(column, row.startEvent);
}

function endTournamentEventFormatter(column, row) {
	return tournamentEventFormatter(column, row.endEvent);
}

function formatTournamentEvent(id, level, name, extId) {
	return "<a href='/tournamentEvent?tournamentEventId=" + id + "' class='label label-" + level + "' title='" + levelName(level) + (extId ? " - " +  extId : "") +"'>" + name + "</a>";
}

function participationFormatter(column, row) {
	return row.participationPct.toFixed(1) + "%";
}

// Match Formatter
function matchFormatter(playerId) {
	return function(column, row) {
		return formatMatchPlayer(row.winner, false, playerId) + " " + (row.outcome != "ABD" ? "d." : "vs") + " " + formatMatchPlayer(row.loser, false, playerId);
	};
}

function h2hMatchFormatter(column, row) {
	var victory = row.outcome != "ABD";
	return formatMatchPlayer(row.winner, victory) + " " + (victory ? "d." : "vs") + " " + formatMatchPlayer(row.loser);
}

function finalFormatter(column, row) {
	if (!row.winner && !row.runnerUp) return "";
	var victory = row.outcome != "ABD";
	return formatMatchPlayer(row.winner, victory) + " " + (victory ? "d." : "vs") + " " + formatMatchPlayer(row.runnerUp) + " " + row.score;
}

function formatMatchPlayer(player, winner, playerId) {
	var name = (winner ? "<strong>" : "") + player.name + (winner ? "</strong>" : "") + formatSeedEntry(player.seed, player.entry);
	return player.id == playerId ? name : "<a href='/playerProfile?playerId=" + player.id + "' title='Show profile'>" + name + "</a>";
}

function formatSeedEntry(seed, entry) {
	return (seed ? (" (" + seed + (entry ? " " + entry : "") + ")") : (entry ? " (" + entry + ")" : ""));
}

// Record Formatter
function recordFormatter(column, row) {
	return "<a href='/record?recordId=" + row.id + "' title='Show Record'>" + row.name + "</a>";
}

function recordHoldersFormatter(column, row) {
	var recordHolders = row.recordHolders;
	var len = recordHolders.length;
	var s = "";
	for (var i = 0; i < len; i++) {
		var recordHolder = recordHolders[i];
		s = s ? s + ", " : "";
		s += playerCountryFormatter(column, recordHolder);
		if (recordHolder.detail)
			s += " (" + recordHolder.detail + ")";
	}
	return s;
}


// Stats

function toggleStatsData(selector) {
	if (selector)
		$(selector).find(".pct-data, .raw-data").toggle();
	else
		$(".pct-data, .raw-data").toggle();
}

function compareStats(containerId, statsId, close) {
	var url = $("#" + statsId).data("statsURL");
	if (!close) {
		url += "&compare=true";
		var compareSelector = "#" + statsId + "Compare";
		var $compareSeason = $(compareSelector + "Season");
		if ($compareSeason.length)
			url += "&compareSeason=" + $compareSeason.val();
		var $compareLevel = $(compareSelector + "Level");
		if ($compareLevel.length)
			url += "&compareLevel=" + $compareLevel.val();
		var $compareSurface = $(compareSelector + "Surface");
		if ($compareSurface.length)
			url += "&compareSurface=" + $compareSurface.val();
	}
	$.get(url, function (data) {
		$("#" + containerId).html(data);
	});
}

function showMatchStats(matchId, event, disableCompare) {
	var $matchStats = $("#matchStats-" + matchId);
	if (!$matchStats.hasClass("loaded")) {
		event.preventDefault();
		var url = "matchStats?matchId=" + matchId;
		if (disableCompare)
			url += "&enableCompare=false";
		$.get(url, function(data) {
			$matchStats.addClass("loaded").popover({content: data, html: true, placement: "auto right"});
			$matchStats.on("show.bs.popover", function() { $(this).data("bs.popover").tip().css("max-width", "500px"); }).click();
			$matchStats.data("statsURL", url);
		});
	}
}

function compareMatchStats(matchId, close) {
	var url = $("#matchStats-" + matchId).data("statsURL");
	if (!close) {
		url += "&compare=true";
		var compareSelector = "#matchStats-" + matchId + "Compare";
		var $compareSeason = $(compareSelector + "Season");
		if ($compareSeason.length)
			url += "&compareSeason=" + $compareSeason.prop("checked");
		var $compareLevel = $(compareSelector + "Level");
		if ($compareLevel.length)
			url += "&compareLevel=" + $compareLevel.prop("checked");
		var $compareSurface = $(compareSelector + "Surface");
		if ($compareSurface.length)
			url += "&compareSurface=" + $compareSurface.prop("checked");
		var $compareRound = $(compareSelector + "Round");
		if ($compareRound.length)
			url += "&compareRound=" + $compareRound.prop("checked");
		var $compareOpponent = $(compareSelector + "Opponent");
		if ($compareOpponent.length)
			url += "&compareOpponent=" + $compareOpponent.prop("checked");
	}
	$.get(url, function (data) {
		$("#matchStatsPopover-" + matchId).html(data);
	});
}

function StatsFilter($category, $from, $to) {
	var category = $category.val();
	var type = $category.find(":selected").data("type");
	var from = $from.val();
	var to = $to.val();
	if (category && (from || to)) {
		this.category = category;
		if (type == "PERCENTAGE") {
			if (from) from /= 100.0;
			if (to) to /= 100.0;
		}
		this.from = from;
		this.to = to;
	}
	this.equals = function(o) {
		return this.category == o.category && this.from == o.from && this.to == o.to;
	};
	this.hasFilter = function() {
		return this.category && (this.from || this.to);
	};
}

function validateStatsFilter($matchesStatsFrom, $matchesStatsTo, $range) {
	if (!(validateNumber($matchesStatsFrom) && validateNumber($matchesStatsTo)))
		return false;
	var from = $matchesStatsFrom.val();
	var to = $matchesStatsTo.val();
	if ($.isNumeric(from) && $.isNumeric(to) && parseFloat(from) > parseFloat(to)) {
		$range.tooltip({title: "From is greater then to"}).tooltip("show");
		$matchesStatsTo.focus();
		return false;
	}
	else {
		$range.tooltip("destroy");
		return true;
	}
}


// Devices

var deviceMatrix = {"xs": ["xs", "sm", "md", "lg"], "sm": ["sm", "md", "lg"], "md": ["md", "lg"], "lg": ["lg"]};
function deviceGreaterOrEqual(device1, device2) {
	return deviceMatrix[device2].indexOf(device1) >= 0;
}
function detectDevice() {
	return $(".device-check:visible").attr("data-device");
}


// Misc

function loadRankingTopN(rankType, count) {
	$("#rankingTopN").load("/rankingTopN?rankType=" + rankType + (count ? "&count=" + count : ""));
}

function bindPopovers() {
	$("[data-toggle=popover]").popover({
		html: true,
		content: function () {
			var content = $(this).attr("data-popover");
			return $(content).children(".popover-content").html();
		},
		title: function () {
			var title = $(this).attr("data-popover");
			return $(title).children(".popover-title").html();
		}
	}).on("show.bs.popover", function () {
		$(this).data("bs.popover").tip().css("max-width", "900px");
	});
}

function validateNumber($selector) {
	var value = $selector.val();
	if (!value || $.isNumeric(value)) {
		$selector.tooltip("destroy");
		return true;
	}
	else {
		$selector.tooltip({title: "Invalid number"}).tooltip("show");
		$selector.focus();
		return false;
	}
}

function appendGoatPointsTitle(title, row, propertyName, propertyTitle) {
	var points = row[propertyName];
	if (points > 0) {
		if (title)
			title += ", ";
		title += propertyTitle + ": " + points;
	}
	return title;
}