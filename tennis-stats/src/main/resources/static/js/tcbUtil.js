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
