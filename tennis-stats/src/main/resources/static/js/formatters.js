// Date
function dateFormatter(column, row) {
	return $.datepicker.formatDate("dd-mm-yy", new Date(row.date));
}

// Country
function countryFormatter(column, row) {
	return "<img src='images/flags/" + row.countryCode + ".png' title='" + row.countryId + "' width='24' height='20'/> " + row.countryId;
}

// Player
function playerFormatter(column, row) {
	return "<a href='playerProfile?playerId=" + row.playerId + "' title='Show profile'>" + row.name + "</a>";
}

function playerCountryFormatter(column, row) {
	return "<img src='images/flags/" + row.countryCode + ".png' title='" + row.countryId + "' width='24' height='20'/> " +
	       "<a href='playerProfile?playerId=" + row.playerId + "' title='Show profile'>" + row.name + "</a>";
}

// Level
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

// Surface
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

// Tournament
function tournamentFormatter(column, row) {
	return "<span class='label label-" + levelClassSuffix(row.level) + "'>" + levelName(row.tournament) + "</span>";
}

// Result
function resultFormatter(column, row) {
	return "<span class='label black bg-result-" + row.result + "'>" + row.result + "</span>"
}

// Match
function matchFormatter(column, row) {
	return "<a href='playerProfile?playerId=" + row.winnerId + "' title='Show profile'>" + row.winner + "</a> d. <a href='playerProfile?playerId=" + row.loserId + "' title='Show profile'>" + row.loser + "</a>";
}