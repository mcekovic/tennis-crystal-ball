// Cookies

function getCookie(name) {
	var s = name + "=";
	var cookie = decodeURIComponent(document.cookie).split(';');
	for (var i = 0; i <cookie.length; i++) {
		var c = cookie[i];
		while (c.charAt(0) === ' ')
			c = c.substring(1);
		if (c.indexOf(s) === 0)
			return c.substring(s.length, c.length);
	}
	return "";
}

function setCookie(name, value, days) {
	var cookie = name + "=" + value;
	if (days) {
		var d = new Date();
		d.setTime(d.getTime() + days * 86400000);
		cookie += "; expires=" + d.toUTCString();
	}
	document.cookie = cookie + "; path=/";
}

function initCookiesNotification() {
	if (!localStorage.getItem("cookiesNotification"))
		$("#cookiesNotification").show();
}

function agreeToUseCookies() {
	$("#cookiesNotification").hide()
	localStorage.setItem("cookiesNotification", "true");
}


// Autocomplete player

function autocompletePlayer(id) {
	$(function () {
		$("#" + id).autocomplete({
			source: "/autocompletePlayer",
			minLength: 2,
			select: function (event, ui) {
				if (ui.item)
					window.location.href = "/playerProfile?playerId=" + ui.item.id;
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
	var url = $pill.data("url");
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

function tabLoading($pill) {
	var $pane = $($pill.attr("href"));
	$pane.html("<div class='loading'></div>") ;
	$pill.tab("show");
}


// Collapse

function collapseClick(event) {
	event.preventDefault();
	var $button = $(this);
	if ($button.hasClass("loaded"))
		return;
	var url = $button.data("url");
	if (typeof url !== "undefined")
		loadCollapse($button, url, true);
	else
		$($button.data("target")).collapse("toggle");
}

function loadCollapse($button, url, show) {
	var $pane = $($button.data("target"));
	$pane.load(url, function () {
		if (!$button.hasClass("loaded"))
			$button.addClass("loaded");
		if (show)
			$pane.collapse("show");
	});
}


// Dates

var date_format = "dd-mm-yy";

function formatDate(date) {
	if (typeof date === "string" && date.length === 10 && date.charAt(4) === "-" && date.charAt(7) === "-")
		return date.substr(8, 2) + "-" + date.substr(5, 2) + "-" + date.substr(0, 4);
	else
		return date ? $.datepicker.formatDate(date_format, new Date(date)) : "";
}

function getDate(id, title) {
	var $date = $("#" + id);
	var date = $date.val();
	if (date == "") {
		$date.tooltip("destroy");
		return date;
	}
	try {
		$.datepicker.parseDate(date_format, date);
		$date.tooltip("destroy");
		return date;
	}
	catch (err) {
		$date.tooltip("destroy");
		$date.tooltip({title: "Invalid " + title + ": " + date, placement: "top"}).tooltip("show");
		$date.focus();
		return null;
	}
}

function datePicker(id) {
	var $input = $("#" + id);
	if ($input.attr("readonly"))
		return;
	$input.datepicker({
		defaultDate: "0", maxDate: "0", changeMonth: true, changeYear: true, showWeek: true, firstDay: 1, dateFormat: date_format
	});
	$("div.ui-datepicker").css({fontSize: "12px"});
}

function dateRangePicker(fromId, toId, yearRange) {
	var singleSeason = yearRange.indexOf(":") < 0;
	if (singleSeason) {
		var season = yearRange;
		yearRange = season + ":" + season;
	}
	var $from = $("#" + fromId);
	var $to = $("#" + toId);
	$from.datepicker({
		defaultDate: singleSeason ? "01-01-" + season : "-1y", maxDate: "0", changeMonth: true, changeYear: !singleSeason, yearRange: yearRange, showWeek: true, firstDay: 1, dateFormat: date_format,
		onClose: function (selectedDate) {
			$to.datepicker("option", "minDate", selectedDate);
			$to.tooltip("destroy");
		}
	});
	$to.datepicker({
		defaultDate: singleSeason ? "31-12-" + season : "0", maxDate: "0", changeMonth: true, changeYear: !singleSeason, yearRange: yearRange, showWeek: true, firstDay: 1, dateFormat: date_format,
		onClose: function (selectedDate) {
			$from.datepicker("option", "maxDate", selectedDate);
			$from.tooltip("destroy");
		}
	});
	$("div.ui-datepicker").css({fontSize: "12px"});
}


// Bootgrid

function addRequestParam(request, param, def) {
	var value = $("#" + param).val();
	if (value && !(def && value === def))
		request[param] = value;
}

function addRequestBooleanParam(request, param) {
	var value = $("#" + param).prop("checked");
	if (value)
		request[param] = value;
}

function setBootgridColumnsVisible($gridTable, columns, visible) {
	for (var i = 0, count = columns.length; i < count; i++)
		$gridTable.find("th[data-column-id='" + columns[i] + "']").data("visible", visible);
}
function setBootgridColumnsWidths($gridTable, columns, widths) {
	for (var i = 0, count = columns.length; i < count; i++)
		$gridTable.find("th[data-column-id='" + columns[i] + "']").data("width", widths[i]);
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
var bootgridTemplateLoading = "Loading... <img src='/images/ui-anim_basic_16x16.gif' width='16' height='16'/>";
/* Fixes Bootgrid Issue with no link cursors on pagination buttons */
var bootgridTemplatePaginationItem = "<li class=\"{{ctx.css}}\"><a href=\"#\" data-page=\"{{ctx.page}}\" class=\"{{css.paginationButton}}\">{{ctx.text}}</a></li>";

// Date Formatter
function dateFormatter(column, row) {
	return formatDate(row.date);
}

// Country Formatter
function countryFormatter(column, row) {
	return formatCountry(row) + " " + row.country.id;
}

function formatCountry(row) {
	return "<img src='/images/flags/" + row.country.code + ".png' title='" + row.country.id + "' width='24' height='20'/>";
}

// Player Formatter
function playerFormatter(column, row) {
	return "<a href='/playerProfile?playerId=" + row.playerId + "' title='Show " + row.name + "&apos;s profile'>" + row.name + "</a>" +
		(row.active ? " <img src='/images/active.png' title='Active' width='12' height='12' style='vertical-align: 0'/>" : "");
}

function playerCountryFormatter(column, row) {
	return formatCountry(row) + " " + playerFormatter(column, row);
}

function formatFavorite(column, favorite) {
	if (!favorite) return "";
	var price = favorite.price;
	return playerCountryFormatter(column, favorite) + " " + (price ? "<span title='Odds " + price + "'>" : "") + ((100 * favorite.probability).toFixed(1)) + "%" + (price ? "</span>" : "");
}

// Level Formatter
function levelFormatter(column, row) {
	return "<span class='label label-" + row.level + "'>" + levelName(row.level) + "</span>";
}

function levelName(level) {
	switch (level) {
		case "G": return "Grand Slam";
		case "F": return "Tour Finals";
		case "L": return "Alt. Finals";
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
	return formatSurface(row.surface, row.indoor);
}

function shortSurfaceFormatter(column, row) {
	return row.surface ? "<span class='label label-" + surfaceClassSuffix(row.surface) + "'><span title='" + surfaceName(row.surface) + "'>" + surfaceShortName(row.surface) + "</span>" + indoorMark(row.surface, row.indoor) + "</span>" : "";
}

function formatSurface(surface, indoor) {
	return surface ? "<span class='label label-" + surfaceClassSuffix(surface) + "'>" + surfaceName(surface) + indoorMark(surface, indoor) + "</span>" : "";
}

function indoorMark(surface, indoor) {
	return indoor && surface !== 'P' ? " <span title='Indoor'>(i)</span>" : "";
}

function surfaceClassSuffix(surface) {
	switch (surface) {
		case "H": return "primary";
		case "C": return "danger";
		case "G": return "success";
		case "P": return "warning";
		case undefined: return "default";
		default: return surface;
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

function surfaceShortName(surface) {
	switch (surface) {
		case "H": return "Hd";
		case "C": return "Cl";
		case "G": return "Gr";
		case "P": return "Cp";
		default: return surface;
	}
}

function decorateSurface(selector) {
	$(selector).each(function() {
		var $this = $(this);
		$this.addClass("label-" + surfaceClassSuffix($this.data("surface")));
	});
}

function speedFormatter(column, row) {
	return formatSpeed(row.speed, row.surface);
}

function formatSpeed(speed, surface) {
	if (!speed) return "";
	return "<span class='label points-" + speedClassSuffix(speed) + " points-" + surface + "' title='" + speedTitle(speed) + "'>" + speed + "</span>";
}

function speedsFormatter(column, row) {
	var surfaces = row.surfaces;
	var speeds = row.speeds;
	var s = "";
	for (var i = 0, count = Math.min(3, surfaces.length); i < count; i++) {
		if (s !== "") s += " ";
		var surface = surfaces[i];
		var speed = speeds[surface];
		if (speed)
			s += "<span class='label points-" + speedClassSuffix(speed) + " points-" + surface + "' title='" + speedTitle(speed) + " " + surfaceName(surface) + "'>" + speed + "</span>";
	}
	return s;
}

function speedClassSuffix(speed) {
	return Math.floor(speed / 10) * 10;
}

function speedTitle(speed) {
	if (speed < 30)
		return "Very slow";
	else if (speed < 40)
		return "Slow";
	else if (speed < 50)
		return "Medium slow";
	else if (speed < 60)
		return "Medium";
	else if (speed < 70)
		return "Medium fast";
	else if (speed < 80)
		return "Fast";
	return "Very fast";
}

function decorateSpeed(selector) {
	$(selector).each(function() {
		var $this = $(this);
		var speed = $this.data("court-speed");
		$this.addClass("points-" + speedClassSuffix(speed) + " points-" + $this.data("surface")).attr("title", speedTitle(speed));;
	});
}

// Tournament Formatter
function tournamentFormatter(column, row) {
	return "<a href='/tournament?tournamentId=" + row.id + "' title='Show tournament" + (row.extId ? " - " +  row.extId : "") + "'>" + row.name + "</a>";
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
	return (100 * row.participation).toFixed(1) + "%";
}

// Match Formatter
function matchFormatter(playerId) {
	return function(column, row) {
		return formatMatchPlayer(row.winner, false, playerId) + " " + (row.outcome !== "ABD" ? "d." : "vs") + " " + formatMatchPlayer(row.loser, false, playerId);
	};
}

function matchExFormatter(playerId) {
	return function(column, row) {
		return formatMatchExPlayer(row.winner, false, playerId) + " " + (row.outcome !== "ABD" ? "d." : "vs") + " " + formatMatchExPlayer(row.loser, false, playerId);
	};
}

function h2hMatchFormatter(column, row) {
	var victory = row.outcome !== "ABD";
	return formatMatchPlayer(row.winner, victory) + " " + (victory ? "d." : "vs") + " " + formatMatchPlayer(row.loser);
}

function finalFormatter(column, row) {
	if (!row.winner && !row.runnerUp) return "";
	var victory = row.outcome !== "ABD";
	return formatMatchPlayer(row.winner, victory) + " " + (victory ? "d." : "vs") + " " + formatMatchPlayer(row.runnerUp) + " " + formatScore(row.score);
}

function finalExFormatter(column, row) {
	if (!row.winner && !row.runnerUp) return "";
	var victory = row.outcome !== "ABD";
	return formatMatchExPlayer(row.winner, victory) + " " + (victory ? "d." : "vs") + " " + formatMatchExPlayer(row.runnerUp) + " " + formatScore(row.score);
}

function formatMatchPlayer(player, winner, playerId) {
	var name = (winner ? "<strong>" : "") + player.name + (winner ? "</strong>" : "") + formatSeedEntry(player.seed, player.entry);
	return player.id === playerId ? name : "<a href='/playerProfile?playerId=" + player.id + "' title='Show profile'>" + name + "</a>";
}

function formatMatchExPlayer(player, winner, playerId) {
	return formatCountry(player) + " "  + formatMatchPlayer(player, winner, playerId) + formatRanking(player);
}

function formatRanking(row) {
	if (row.rank || row.eloRating || row.eloRatingDelta)
		return " <div class='rankings-badge'>" + (row.rank ? "Rank " + row.rank : "")
			+ (row.eloRating || row.eloRatingDelta ? "<br/>Elo" + (row.eloRating ? " " + row.eloRating : "") + (row.eloRatingDelta ? (row.eloRatingDelta > 0 ? " (<span class='positive'>+" + row.eloRatingDelta + "</span>)" : " (<span class='negative'>" + row.eloRatingDelta + "</span>)") : "") : "") + "</div>";
	else
		return "";
}

function formatSeedEntry(seed, entry) {
	return (seed ? (" (" + seed + (entry ? " " + entry : "") + ")") : (entry ? " (" + entry + ")" : ""));
}

function scoreFormatter(column, row) {
	return formatScore(row.score);
}

function formatScore(score) {
	return score ? score.replace(/\(/g, "<sup>(").replace(/\)/g, ")</sup>") : score;
}

function wonLostFormatter(playerId) {
	return function(column, row) {
		return row.outcome !== "ABD" ? (row.winner.id === playerId ? "<label class='label label-won'>W</label>" : "<label class='label label-lost'>L</label>") : "<label class='label label-abd'>A</label>";
	}
}

// Seasons Formatter
function seasonsFormatter(column, row) {
	return row.seasons.length > 30 ? "<span style='font-size: 75%'>" + row.seasons + "</span>" : row.seasons;
}

// Record Formatter
function recordFormatter(column, row) {
	return "<a href='/record?recordId=" + row.id + "' title='Show Record'>" + row.name + "</a>";
}

function recordValueFormatter(column, row) {
	return formatRecordDetail(row.value, row.detailUrl);
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
			s += " (" + formatRecordDetail(recordHolder.detail, recordHolder.detailUrl) + ")";
	}
	return s;
}

function formatRecordDetail(detail, detailUrl) {
	return detailUrl ? "<a href='" + detailUrl + "' title='Show record detail'>" + detail + "</a>" : detail;
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
	var tab = $("#" + statsId + "Tabs").find("li.active a").attr("href");
	if (tab)
		url += "&tab=" + tab.substr(1);
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

function showMatchStats(matchId, event, container) {
	var $matchStats = $("#matchStats-" + matchId);
	if (!$matchStats.hasClass("loaded")) {
		event.preventDefault();
		var url = "matchStats?matchId=" + matchId;
		$.get(url, function(data) {
			$matchStats.addClass("loaded").popover({content: data, html: true, placement: "auto right", container: container});
			$matchStats.on("show.bs.popover", function() { $(this).data("bs.popover").tip().css("max-width", "600px"); }).click();
			$matchStats.data("statsURL", url);
		});
	}
}

function compareMatchStats(matchId, close) {
	var url = $("#matchStats-" + matchId).data("statsURL");
	var tab = $("#matchStats-" + matchId + "Tabs").find("li.active a").attr("href");
	if (tab)
		url += "&tab=" + tab.substr(1);
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
		$range.tooltip({title: "From is greater than to"}).tooltip("show");
		$matchesStatsTo.focus();
		return false;
	}
	else {
		$range.tooltip("destroy");
		return true;
	}
}


// Devices

var deviceMatrix = {"xs": ["xs", "sm", "md", "lg", "xl"], "sm": ["sm", "md", "lg", "xl"], "md": ["md", "lg", "xl"], "lg": ["lg", "xl"], "xl": ["xl"]};
function deviceGreaterOrEqual(device1, device2) {
	return deviceMatrix[device2].indexOf(device1) >= 0;
}
function deviceLessOrEqual(device1, device2) {
	return deviceMatrix[device2].indexOf(device1) <= 0;
}
function detectDevice() {
	return $(".device-check:visible").data("device");
}


// Perf/Stats

function performancePlayerMatchesUrl(playerId, outcome, prefix) {
	var url = "/playerProfile?playerId=" + playerId + "&tab=matches";
	var category = paramValue("category", prefix);
	var season = paramValue("season", prefix);
	if (season) url += "&season=" + season;
	var fromDate = paramValue("fromDate", prefix);
	if (fromDate) url += "&fromDate=" + fromDate;
	var toDate = paramValue("toDate", prefix);
	if (toDate) url += "&toDate=" + toDate;
	var level = paramValue("level", prefix);
	if (category == "grandSlamMatches") url += "&level=G";
	else if (category == "altFinalsMatches") url += "&level=L";
	else if (category == "tourFinalsMatches") url += "&level=F";
	else if (category == "mastersMatches") url += "&level=M";
	else if (category == "olympicsMatches") url += "&level=O";
	else if (level) url += "&level=" + level;
	var bestOf = paramValue("bestOf", prefix);
	if (bestOf) url += "&bestOf=" + bestOf;
	if (category == "hardMatches") url += "&surface=H";
	else if (category == "clayMatches") url += "&surface=C";
	else if (category == "grassMatches") url += "&surface=G";
	else if (category == "carpetMatches") url += "&surface=P";
	else {
		var surface = paramValue("surface", prefix);
		if (surface) url += "&surface=" + surface;
	}
	var indoor = paramValue("indoor", prefix);
	if (indoor) url += "&indoor=" + indoor;
	var speed = paramValue("speed", prefix);
	if (speed) url += "&speed=" + speed;
	if (category == "finals") {
		url += "&round=F";
		if (!level) url += "&level=GFLMOAB";
	}
	else {
		var round = paramValue("round", prefix);
		if (round) {
			url += "&round=" + encodeURIComponent(round);
			if (!level) url += "&level=GFLMOAB";
		}
	}
	var result = paramValue("result", prefix);
	if (result) {
		url += "&result=" + encodeURIComponent(result);
		if (!level) url += "&level=GFLMOAB";
	}
	var tournament = paramValue("tournament", prefix);
	if (tournament) url += "&tournamentId=" + tournament;
	if (category == "vsNo1") url += "&opponent=NO_1";
	else if (category == "vsTop5") url += "&opponent=TOP_5";
	else if (category == "vsTop10") url += "&opponent=TOP_10";
	else {
		var opponent = paramValue("opponent", prefix);
		if (opponent) url += "&opponent=" + opponent;
	}
	var country = paramValue("country", prefix);
	if (country) url += "&countryId=" + country;
	if (category == "decidingSets") url += "&score=*DS";
	else if (category == "fifthSets") url += "&score=" + encodeURIComponent("2:2+");
	else if (category == "afterWinningFirstSet") url += "&score=" + encodeURIComponent("1:0+");
	else if (category == "afterLosingFirstSet") url += "&score=" + encodeURIComponent("0:1+");
	else if (category == "tieBreaks") url += "&score=*TB" + outcome;
	else if (category == "decidingSetTBs") url += "&score=*DSTB";
	if (category != "tieBreaks") {
		if (outcome == "W") url += "&outcome=wonplayed";
		else if (outcome == "L") url += "&outcome=lostplayed";
		else url += "&outcome=played";
	}
	return url;
}

function statisticsPlayerMatchesUrl(playerId, prefix) {
	var url = "/playerProfile?playerId=" + playerId + "&tab=matches";
	var season = paramValue("season", prefix);
	if (season) url += "&season=" + season;
	var fromDate = paramValue("fromDate", prefix);
	if (fromDate) url += "&fromDate=" + fromDate;
	var toDate = paramValue("toDate", prefix);
	if (toDate) url += "&toDate=" + toDate;
	var level = paramValue("level", prefix);
	if (level) url += "&level=" + level;
	var bestOf = paramValue("bestOf", prefix);
	if (bestOf) url += "&bestOf=" + bestOf;
	var surface = paramValue("surface", prefix);
	if (surface) url += "&surface=" + surface;
	var indoor = paramValue("indoor", prefix);
	if (indoor) url += "&indoor=" + indoor;
	var speed = paramValue("speed", prefix);
	if (speed) url += "&speed=" + speed;
	var round = paramValue("round", prefix);
	if (round) {
		url += "&round=" + encodeURIComponent(round);
		if (!level) url += "&level=GFLMOAB";
	}
	var result = paramValue("result", prefix);
	if (result) {
		url += "&result=" + encodeURIComponent(result);
		if (!level) url += "&level=GFLMOAB";
	}
	var tournament = paramValue("tournament", prefix);
	if (tournament) url += "&tournamentId=" + tournament;
	var tournamentEvent = paramValue("tournamentEvent", prefix);
	if (tournamentEvent) url += "&tournamentEventId=" + tournamentEvent;
	var opponent = paramValue("opponent", prefix);
	if (opponent) url += "&opponent=" + opponent;
	var country = paramValue("country", prefix);
	if (country) url += "&countryId=" + country;
	url += "&outcome=played";
	return url;
}

function paramValue(name, prefix) {
	return $("#" + (prefix ? prefix + (name.charAt(0).toUpperCase() + name.substr(1)) : name)).val()
}


// Misc

function loadRankingTopN(rankType, count) {
	$("#rankingTopN").load("/rankingTopN?rankType=" + rankType + (count ? "&count=" + count : ""));
}

function bindPopovers(container) {
	$("[data-toggle=popover]").popover({
		html: true,
		container: container,
		content: function () {
			var content = $(this).data("popover");
			return $(content).children(".popover-content").html();
		},
		title: function () {
			var title = $(this).data("popover");
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
		$selector.tooltip({title: "Invalid number", placement: "bottom"}).tooltip("show");
		$selector.focus();
		return false;
	}
}

function appendPointsTitle(title, row, propertyName, propertyTitle) {
	var points = row[propertyName];
	if (points > 0) {
		if (title)
			title += ", ";
		title += propertyTitle + ": " + points;
	}
	return title;
}