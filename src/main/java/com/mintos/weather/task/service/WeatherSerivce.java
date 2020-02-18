package com.mintos.weather.task.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.mintos.weather.task.dao.WeatherHistoryDao;
import com.mintos.weather.task.enums.TempUnitEnum;
import com.mintos.weather.task.model.WeatherHistoryBE;
import com.mintos.weather.task.util.Util;

@Service
@PropertySource(ignoreResourceNotFound = true, value = "classpath:application.properties")
public class WeatherSerivce {
	
	@Autowired
    private Environment env;
	
	@Autowired
	private WeatherHistoryDao weatherHistoryDao;
	
	// Retrieve URLs for APIs
	private final String API_URL_IP = "api.url.ip";
	private final String API_URL_LOCATION = "api.url.location";
	private final String API_URL_WEATHER = "api.url.weather";
	
	// Define constant variables for resource file
	private final String API_PARAM_KEYS = "parameters.keys";
	private final String API_PARAM_VALUES = "parameters.values";
	private final String API_RESPONSE_KEYS = "response.keys";
	
	private final String API_KEY = "api.key";
	private final String API_KEY_VALUE = "api.value";
	private final String API_KEY_HEADER = "api.header";
	
	private final String TEMP_UNIT = "temp.unit";
	
	public Map<String, Object> getWeather()
	{
		WeatherHistoryBE weatherHistory = new WeatherHistoryBE();
		String message = "";
		
		try {
			// 1- Get current IP address
			// 1.a- Get json object
			JSONObject jsonObj = createConnection(API_URL_IP, null);
			// 1.b- Read data from response
			String[] data = retreiveData(API_URL_IP, jsonObj);
			
			// 1.c- Retrieve IP address
			String ip = data[0];
			weatherHistory.setIp(ip);
			
			// 2- get current location based on IP address 
			// 2.a- Get json object
			jsonObj = createConnection(API_URL_LOCATION, data);
			// 2.b- read data from response
			data = retreiveData(API_URL_LOCATION, jsonObj);
			
			// 2.c- Set Latitude and longitude
			String lat = "";
			String lon = "";
			lat = data[0];
			lon = data[1];
			weatherHistory.setLat(lat);
			weatherHistory.setLon(lon);
			
			// 2.d- Set country and City
			if(data.length == 4)
			{
				weatherHistory.setCountry(data[2]);
				weatherHistory.setCity(data[3]);
			}
			
			// 3- Get current weather condition based on current data (lon+lat, city)
			// 3.a- Get json object
			jsonObj = createConnection(API_URL_WEATHER, data);
			// 3.b- read data from response
			data = retreiveData(API_URL_WEATHER, jsonObj);
			
			// 3.c- Set temperature
			double temp = 0.0;

			String tempUnit = env.getProperty(API_URL_WEATHER +  "." + TEMP_UNIT);
			try {
				temp = Double.parseDouble(data[0]);
				
				// 3.d- Set C or F based on the retrieved data from API
				if(tempUnit.equalsIgnoreCase(TempUnitEnum.UINT_C.name()))
				{
					weatherHistory.setTempC(temp);
					weatherHistory.setTempC(temp);
					
					weatherHistory.setTempF(Math.round(convertToF(temp)));
					weatherHistory.setTempF(Math.round(convertToF(temp)));
				}
				else
				{
					weatherHistory.setTempC(Math.round(convertToC(temp)));
					weatherHistory.setTempC(Math.round(convertToF(temp)));
					
					weatherHistory.setTempF(Math.round(temp));
					weatherHistory.setTempF(temp);
				}
				
			} catch (NumberFormatException e) {
				// if Temperature is invalid
				message = "Temperature value is invalid!!";
			}
			
			weatherHistory.setDate(new Date());
			
			// 4- Save weather history
			saveNewHistory(weatherHistory);
			
			message = "Success";
		} 
		catch(RuntimeException e)
		{
			message = e.getMessage();
		}
		catch (Exception e) {
			e.printStackTrace();
			message = "Something went wrong!";
		}
		
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		//5- return information
		map.put("message", message);
		map.put("current_conditions", weatherHistory);
		
		return map;
	}
	
	private JSONObject createConnection(String api, String[] parameters) throws RuntimeException
	{
		// 1- prepare URL based on URL Address
		String urlStr = prepareURL(api, parameters);
		
		// 2- Get API key information
		String APIHeader = env.getProperty(api +  "." + API_KEY_HEADER);
		String APIKey = env.getProperty(api +  "." + API_KEY);
		String APIKeyValue = env.getProperty(api +  "." + API_KEY_VALUE);
		
		// 3- If API key is not in the header, append the key to URL parameters
		if(APIHeader == null || (APIHeader != null && APIHeader.equalsIgnoreCase("no")))
		{
			urlStr += APIKey + "=" + APIKeyValue;
		}
		
		HttpURLConnection conn;
		BufferedReader br;
		String result = "";
		
		try {
			// 4- Open connection
			URL url = new URL(urlStr);
			
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			
			// 5- If API key is in the header, add new header property with the API key
			if(APIHeader != null && APIHeader.equalsIgnoreCase("yes"))
			{
				conn.setRequestProperty(APIKey, APIKeyValue);
			}
			
			// 6- Check valid response
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			// 7- read response into String
			br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			
			String output;
			
			while ((output = br.readLine()) != null) {
				result += output;
			}

			conn.disconnect();
		} 
		catch(MalformedURLException e)
		{
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		//8- Convert response into Json Object
		try {
			JSONObject jsonObject = new JSONObject(result);
			
			return jsonObject;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		// return null if the response is invalid
		return null;
	}
	
	// Prepare the URL based on data stored in resource file
	private String prepareURL(String api, String[] parameters)
	{
		StringBuffer sb = new StringBuffer( env.getProperty(api));
		
		//1- retrieve parameters keys
		String[] keys = env.getProperty(api + "." + API_PARAM_KEYS) != null ? 
				env.getProperty(api +  "." + API_PARAM_KEYS).split(",") : null;
		
		if(keys!= null && keys.length > 0)
		{
			//2- replace values
			//2.a replace values in parameters list
			if(parameters != null)
			{
				for (int i = 0; i < keys.length; i++) {
					sb.append(keys[i] + "=" + parameters[i] + "&");
				}
			}
			//2.b if parameters list is null, then replace values stored in properties file
			else
			{
				String[] values = env.getProperty(api +  "." + API_PARAM_VALUES) != null ? 
						env.getProperty(api +  "." + API_PARAM_VALUES).split(",") : null;
				
				if(values != null && values.length > 0)
				{
					for (int i = 0; i < keys.length; i++) {
						sb.append(keys[i] + "=" + values[i] + "&");
					}
				}
			}
		}
		
		return sb.toString();
	}
	
	// Retrieve data from Json Object
	private String[] retreiveData(String api, JSONObject jsonObj)
	{
		List<String> data = new ArrayList<String>();
		
		// Get response keys stored in resource file (split by ",")
		String[] parameterKeys = env.getProperty(api +  "." + API_RESPONSE_KEYS).split(",");
		
		for (int i = 0; i < parameterKeys.length; i++) {
			
			// Parameter key path (split by ">") for example data.location.ip --> data>location>ip
			String[] keyPath = parameterKeys[i].split(">");
			
			Object currentObj = jsonObj;
			for (int j = 0; j < keyPath.length; j++) {
				
				// check if the key is json array, the value should be in the form [d]
				if(Util.validateJsonArrayParam(keyPath[j]))
				{
					JSONArray array = ((JSONArray)currentObj);
					try {
						currentObj = array.get(Integer.parseInt(keyPath[j]));
					} catch (NumberFormatException | JSONException e) {
						e.printStackTrace();
					}
				}
				else
				{
					//check if the key last element return String
					if((j+1) == keyPath.length)
					{
						try {
							data.add( String.valueOf( ((JSONObject)currentObj).get(keyPath[j]) ));
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					// if the key is not last element return Json Object
					else
					{
						try {
							currentObj = ((JSONObject)currentObj).get(keyPath[j]);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		// return list of parameters values
		String[] dataArray = new String[data.size()];
		return data.toArray(dataArray);
	}
	
	// Convert temp to C
	private double convertToC(double c)
	{
		return (c - 32.0) * 5.0/9.0;
	}
	
	// Convert temp to F
	private double convertToF(double f)
	{
		return (f * 9.0/5.0) + 32.0;
	}
	
	@Transactional
	// Save weather history
	private void saveNewHistory(WeatherHistoryBE weatherHistory) {
		weatherHistoryDao.save(weatherHistory);
	}
	
	// find weather history list by IP address
	public List<WeatherHistoryBE> findByIP(String ip)
	{
		return weatherHistoryDao.findByIP(ip);
	}
	
	// find weather history list by date range
	public List<WeatherHistoryBE> findByDateRange(Date fromDate, Date toDate)
	{
		return weatherHistoryDao.findByDateRange(fromDate, toDate);
	}
	
	// find all weather history list
	public List<WeatherHistoryBE> findHistoryAll()
	{
		return weatherHistoryDao.findAll();
	}
}
