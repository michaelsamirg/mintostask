package com.mintos.weather.task;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.text.SimpleDateFormat;
import java.util.List;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.mintos.weather.task.model.WeatherHistoryBE;
import com.mintos.weather.task.service.WeatherSerivce;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class TaskApplicationTests {

	private final String INVALID_IP = "A.A.A.A";
	private final String INVALID_DATE = "12-04-2020";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private WeatherSerivce weatherSerivce;

	@Test
	void testWeather() throws Exception {
		MvcResult mvcResult = null;
		try {
			mvcResult = mockMvc.perform(get("/weather").contentType("application/json"))
					.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

			assertNotNull(mvcResult);
			assertNotNull(mvcResult.getResponse());
			assertNotNull(mvcResult.getResponse().getContentAsString());

			assertTrue(mvcResult.getResponse().getContentAsString().contains("Temprature"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e);
		}
	}

	@Test
	void testWeatherHistory() throws Exception {
		MvcResult mvcResult = null;
		try {
			mockMvc.perform(get("/weather").contentType("application/json"))
					.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

			mvcResult = mockMvc.perform(get("/weather/history/all").contentType("application/json"))
					.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

			assertNotNull(mvcResult);
			assertNotNull(mvcResult.getResponse());
			assertNotNull(mvcResult.getResponse().getContentAsString());

			JSONArray array = new JSONArray(mvcResult.getResponse().getContentAsString());

			assertTrue(array.length() > 0);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e);
		}
	}

	@Test
	void testWeatherHistoryIP() throws Exception {
		MvcResult mvcResult = null;
		try {
			mockMvc.perform(get("/weather").contentType("application/json"))
					.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

			List<WeatherHistoryBE> list = weatherSerivce.findHistoryAll();

			assertNotNull(list);
			assertTrue(list.size() > 0);

			WeatherHistoryBE historyBE = list.get(0);

			mvcResult = mockMvc
					.perform(
							get("/weather/history/byIP").contentType("application/json").param("ip", historyBE.getIp()))
					.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

			assertNotNull(mvcResult);
			assertNotNull(mvcResult.getResponse());
			assertNotNull(mvcResult.getResponse().getContentAsString());

			JSONObject jsonObject = new JSONObject(mvcResult.getResponse().getContentAsString());

			assertTrue(jsonObject.getString("message").equalsIgnoreCase("Success"));

			JSONArray array = new JSONArray(String.valueOf(jsonObject.get("list")));

			assertTrue(array.length() > 0);

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e);
		}
	}

	@Test
	void testWeatherHistoryIPFail() throws Exception {
		MvcResult mvcResult = null;
		try {
			mvcResult = mockMvc
					.perform(get("/weather/history/byIP").contentType("application/json").param("ip", INVALID_IP))
					.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

			assertNotNull(mvcResult);
			assertNotNull(mvcResult.getResponse());
			assertNotNull(mvcResult.getResponse().getContentAsString());

			JSONObject jsonObject = new JSONObject(mvcResult.getResponse().getContentAsString());

			assertTrue(!jsonObject.getString("message").equalsIgnoreCase("Success"));

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e);
		}
	}

	@Test
	void testWeatherHistoryDate() throws Exception {
		MvcResult mvcResult = null;
		try {
			mockMvc.perform(get("/weather").contentType("application/json"))
					.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

			List<WeatherHistoryBE> list = weatherSerivce.findHistoryAll();

			assertNotNull(list);
			assertTrue(list.size() > 0);

			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyy");
			DateTime fromDate = new DateTime();
			DateTime toDate = fromDate.plusDays(1);

			mvcResult = mockMvc
					.perform(get("/weather/history/byDateRange").contentType("application/json")
							.param("fromDate", simpleDateFormat.format(fromDate.toDate()))
							.param("toDate", simpleDateFormat.format(toDate.toDate())))
					.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

			assertNotNull(mvcResult);
			assertNotNull(mvcResult.getResponse());
			assertNotNull(mvcResult.getResponse().getContentAsString());

			JSONObject jsonObject = new JSONObject(mvcResult.getResponse().getContentAsString());

			assertTrue(jsonObject.getString("message").equalsIgnoreCase("Success"));

			JSONArray array = new JSONArray(String.valueOf(jsonObject.get("list")));

			assertTrue(array.length() > 0);

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e);
		}
	}

	@Test
	void testWeatherHistoryDateFail() throws Exception {
		MvcResult mvcResult = null;
		try {

			mvcResult = mockMvc
					.perform(get("/weather/history/byDateRange").contentType("application/json")
							.param("fromDate", INVALID_DATE).param("toDate", INVALID_DATE))
					.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

			assertNotNull(mvcResult);
			assertNotNull(mvcResult.getResponse());
			assertNotNull(mvcResult.getResponse().getContentAsString());

			JSONObject jsonObject = new JSONObject(mvcResult.getResponse().getContentAsString());

			assertTrue(!jsonObject.getString("message").equalsIgnoreCase("Success"));

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e);
		}
	}

}
