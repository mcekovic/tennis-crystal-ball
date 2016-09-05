package org.strangeforest.tcb.stats.controller;

import org.hamcrest.*;
import org.junit.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.*;
import org.springframework.http.*;
import org.springframework.test.context.junit4.*;
import org.springframework.test.web.servlet.*;
import org.strangeforest.tcb.stats.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class ControllerIT {

	@Autowired private MockMvc mvc;

	@Test
	public void goatListHtml() throws Exception {
		mvc.perform(get("/goatList").accept(MediaType.APPLICATION_XHTML_XML))
			.andExpect(status().isOk());
	}

	@Test
	public void goatListTable() throws Exception {
		mvc.perform(get("/goatListTable").param("current", "1").param("rowCount", "20").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.rows[*].name").value(allOf(hasSize(20), (Matcher)hasItems(PlayersFixture.PLAYERS.toArray()))));
	}
}
