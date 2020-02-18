package com.mintos.weather.task;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.tomcat.util.codec.binary.Base64;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

	// Simple authentication header
	private String basicDigestHeaderValue = "Basic " + new String(Base64.encodeBase64(("admin:password").getBytes()));
	
	// Test /weather API
	@Test
	void testWeather() throws Exception {
		MvcResult mvcResult = null;
		try {
			mvcResult = mockMvc.perform(get("/weather")
					.contentType("application/json")
					.header("Authorization", basicDigestHeaderValue))
					.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

			// Check existing results
			assertNotNull(mvcResult);
			assertNotNull(mvcResult.getResponse());
			assertNotNull(mvcResult.getResponse().getContentAsString());

			JSONObject jsonObject = new JSONObject(mvcResult.getResponse().getContentAsString());

			// Check message = success
			assertTrue(jsonObject.getString("message").equalsIgnoreCase("Success"));
			// Check current_conditions is not null
			assertNotNull(jsonObject.getJSONObject("current_conditions"));
			
			JSONObject jsonObjectCurrent = jsonObject.getJSONObject("current_conditions");
			Double tempC = jsonObjectCurrent.getDouble("tempC");
			Double tempF = jsonObjectCurrent.getDouble("tempF");
			
			// Check temp is not nul
			assertNotNull(tempC);
			assertNotNull(tempF);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e);
		}
	}

	// Test /weather/history/all
	@Test
	void testWeatherHistory() throws Exception {
		MvcResult mvcResult = null;
		try {
			// Call /weather to save some data
			mockMvc.perform(get("/weather")
					.contentType("application/json")
					.header("Authorization", basicDigestHeaderValue))
					.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

			mvcResult = mockMvc.perform(get("/weather/history/all")
					.contentType("application/json")
					.header("Authorization", basicDigestHeaderValue))
					.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

			assertNotNull(mvcResult);
			assertNotNull(mvcResult.getResponse());
			assertNotNull(mvcResult.getResponse().getContentAsString());

			JSONArray array = new JSONArray(mvcResult.getResponse().getContentAsString());

			// check data is retrieved
			assertTrue(array.length() > 0);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e);
		}
	}

	// Test /weather/history/byIP
	@Test
	void testWeatherHistoryIP() throws Exception {
		MvcResult mvcResult = null;
		try {
			// Call /weather to save some data
			mockMvc.perform(get("/weather")
					.contentType("application/json")
					.header("Authorization", basicDigestHeaderValue))
					.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

			List<WeatherHistoryBE> list = weatherSerivce.findHistoryAll();

			assertNotNull(list);
			assertTrue(list.size() > 0);

			WeatherHistoryBE historyBE = list.get(0);

			mvcResult = mockMvc.perform(get("/weather/history/byIP")
					.contentType("application/json")
					.header("Authorization", basicDigestHeaderValue)
					.param("ip", historyBE.getIp()))
					.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

			assertNotNull(mvcResult);
			assertNotNull(mvcResult.getResponse());
			assertNotNull(mvcResult.getResponse().getContentAsString());

			JSONObject jsonObject = new JSONObject(mvcResult.getResponse().getContentAsString());

			assertTrue(jsonObject.getString("message").equalsIgnoreCase("Success"));

			JSONArray array = new JSONArray(String.valueOf(jsonObject.get("list")));
			// check data is retrieved
			assertTrue(array.length() > 0);

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e);
		}
	}

	// Test /weather/history/byIP with invalid IP address
	@Test
	void testWeatherHistoryIPFail() throws Exception {
		MvcResult mvcResult = null;
		try {
			mvcResult = mockMvc.perform(get("/weather/history/byIP")
					.contentType("application/json")
					.header("Authorization", basicDigestHeaderValue)
					.param("ip", INVALID_IP))
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

	// Test /weather/history/byDateRange
	@Test
	void testWeatherHistoryDate() throws Exception {
		MvcResult mvcResult = null;
		try {
			// Call /weather to save some data
			mockMvc.perform(get("/weather")
					.contentType("application/json")
					.header("Authorization", basicDigestHeaderValue))
					.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

			List<WeatherHistoryBE> list = weatherSerivce.findHistoryAll();

			assertNotNull(list);
			assertTrue(list.size() > 0);

			// Set current date
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyy");
			DateTime fromDate = new DateTime();
			DateTime toDate = fromDate.plusDays(1);

			mvcResult = mockMvc.perform(get("/weather/history/byDateRange")
					.contentType("application/json")
					.header("Authorization", basicDigestHeaderValue)
					.param("fromDate", simpleDateFormat.format(fromDate.toDate()))
					.param("toDate", simpleDateFormat.format(toDate.toDate())))
					.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

			assertNotNull(mvcResult);
			assertNotNull(mvcResult.getResponse());
			assertNotNull(mvcResult.getResponse().getContentAsString());

			JSONObject jsonObject = new JSONObject(mvcResult.getResponse().getContentAsString());

			assertTrue(jsonObject.getString("message").equalsIgnoreCase("Success"));

			JSONArray array = new JSONArray(String.valueOf(jsonObject.get("list")));
			// check data is retrieved
			assertTrue(array.length() > 0);

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e);
		}
	}

	// Test /weather/history/byDateRange with invalid date format
	@Test
	void testWeatherHistoryDateFail() throws Exception {
		MvcResult mvcResult = null;
		try {

			mvcResult = mockMvc.perform(get("/weather/history/byDateRange")
					.contentType("application/json")
					.header("Authorization", basicDigestHeaderValue)
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
