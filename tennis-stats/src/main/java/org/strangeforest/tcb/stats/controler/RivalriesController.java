package org.strangeforest.tcb.stats.controler;

import java.time.*;
import java.util.*;
import java.util.stream.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.format.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.util.*;

import com.google.common.collect.*;

import static java.util.stream.Collectors.*;

@Controller
public class RivalriesController {

	@Autowired private RivalriesService rivalriesService;
	@Autowired private PlayerService playerService;

	@RequestMapping("/rivalryClusterTable")
	public ModelAndView rivalryClusterTable(
		@RequestParam(value = "players") String playersCSV,
		@RequestParam(value = "fromDate", required = false) @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate fromDate,
		@RequestParam(value = "toDate", required = false) @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate toDate,
		@RequestParam(value = "level", required = false) String level,
		@RequestParam(value = "surface", required = false) String surface
	) {
		Range<LocalDate> dateRange = DateUtil.toRange(fromDate, toDate);
		List<String> players = Stream.of(playersCSV.split(",")).map(String::trim).collect(toList());

		List<Integer> playerIds = playerService.findPlayerIds(players);
		RivalryCluster rivalryCluster = rivalriesService.getRivalryCluster(playerIds, dateRange, level, surface);

		return new ModelAndView("rivalryClusterTable", "rivalryCluster", rivalryCluster);
	}
}
