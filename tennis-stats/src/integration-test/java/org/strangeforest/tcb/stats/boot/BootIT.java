package org.strangeforest.tcb.stats.boot;

import java.util.*;

import org.junit.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.http.*;
import org.springframework.test.context.junit4.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.*;
import org.springframework.web.context.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.service.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class BootIT {

	@Autowired private PlayerService playerService;
	@Autowired private GOATListService goatListService;
	@Autowired private WebApplicationContext wac;

	private MockMvc mvc;

	private static final Iterable<String> PLAYERS = Arrays.asList("Roger Federer", "Novak Djokovic", "Rafael Nadal");

	@Before
	public void setUp() {
		mvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	@Test
	public void playerExists() {
		for (String player : PLAYERS)
			playerExists(player);
	}

	private void playerExists(String playerName) {
		Optional<Player> player = playerService.getPlayer(playerName);

		assertThat(player).withFailMessage("Player %1$s does not exist", playerName).isNotEmpty();
	}

	@Test
	public void goatList() {
		BootgridTable<GOATListRow> goatList = goatListService.getGOATListTable(1000, new PlayerListFilter(""), "goat_points", 20, 1);

		assertThat(goatList.getRowCount()).isEqualTo(20);
		assertThat(goatList.getTotal()).isGreaterThanOrEqualTo(500);
	}

	@Test
	public void goatListHtml() throws Exception {
		mvc.perform(get("/goatList").accept(MediaType.APPLICATION_XHTML_XML))
			.andExpect(status().isOk());
	}

	@Test
	public void goatListTable() throws Exception {
		String response = mvc.perform(get("/goatListTable").param("current", "1").param("rowCount", "20").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
         .andReturn().getResponse().getContentAsString();
		assertThat(response).contains(PLAYERS);
	}
}
