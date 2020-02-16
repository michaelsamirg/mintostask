package com.mintos.weather.task.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
	
	private final String API_URL_IP = "api.url.ip";
	private final String API_URL_LOCATION = "api.url.location";
	private final String API_URL_WEATHER = "api.url.weather";
	
	private final String API_PARAM_KEYS = "parameters.keys";
	private final String API_PARAM_VALUES = "parameters.values";
	private final String API_RESPONSE_KEYS = "response.keys";
	
	private final String API_KEY = "api.key";
	private final String API_KEY_VALUE = "api.value";
	private final String API_KEY_HEADER = "api.header";
	
	private final String TEMP_UNIT = "temp.unit";
	
	public String getWeather()
	{
		String message = "";
		
		try {
			//1- get ip address
			//1.a- get json object
			JSONObject jsonObj = createConnection(API_URL_IP, null);
			//2.a- read data
			String[] data = retreiveData(API_URL_IP, jsonObj);
			
			String ip = data[0];
			
			message += "You IP Address = " + ip;
			
			//2- get location 
			jsonObj = createConnection(API_URL_LOCATION, data);
			//2.a- read data
			data = retreiveData(API_URL_LOCATION, jsonObj);
			
			String country ="";
			String city = "";
			String lat = "";
			String lon = "";
			
			lat = data[0];
			lon = data[1];
			
			//Add country + City
			if(data.length == 4)
			{
				message += "<br/>Country = " + data[2] + " - City = " + data[3] + "<br/>";
				country = data[2];
				city = data[3];
			}
			
			//3- get weather
			jsonObj = createConnection(API_URL_WEATHER, data);
			//2.a- read data
			data = retreiveData(API_URL_WEATHER, jsonObj);
			
			double temp = 0.0;
			String tempUnit = env.getProperty(API_URL_WEATHER +  "." + TEMP_UNIT);
			try {
				temp = Double.parseDouble(data[0]);
				
				if(tempUnit.equalsIgnoreCase(TempUnitEnum.UINT_C.name()))
				{
					message += "Temprature in C = " + Math.round(temp);
					message += "<br>Temprature in F = " + Math.round(convertToF(temp));
				}
				else
				{
					message += "Temprature in C = " + Math.round(convertToC(temp));
					message += "<br>Temprature in F = " + Math.round(temp);
				}
				
			} catch (NumberFormatException e) {
				message = "Temperature value is invalid!!";
			}
			
			saveNewHistory(ip, city, country, lat, lon, temp, tempUnit);
			
		} 
		catch(RuntimeException e)
		{
			message = e.getMessage();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			message = "Something went wrong!";
		}
		
		
		return message;
	}
	
	private JSONObject createConnection(String api, String[] parameters) throws RuntimeException
	{
		String urlStr = prepareURL(api, parameters);
		
		//Check header
		String APIHeader = env.getProperty(api +  "." + API_KEY_HEADER);
		String APIKey = env.getProperty(api +  "." + API_KEY);
		String APIKeyValue = env.getProperty(api +  "." + API_KEY_VALUE);
		
		if(APIHeader == null || (APIHeader != null && APIHeader.equalsIgnoreCase("no")))
		{
			urlStr += APIKey + "=" + APIKeyValue;
		}
		
		HttpURLConnection conn;
		BufferedReader br;
		String result = "";
		
		try {
			URL url = new URL(urlStr);
			
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			
			//add api key in header
			if(APIHeader != null && APIHeader.equalsIgnoreCase("yes"))
			{
				conn.setRequestProperty(APIKey, APIKeyValue);
			}
			
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

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

		try {
			JSONObject jsonObject = new JSONObject(result);
			
			return jsonObject;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private String prepareURL(String api, String[] parameters)
	{
		StringBuffer sb = new StringBuffer( env.getProperty(api));
		
		//1- check keys
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
			//2.b replace values in properties file
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
	
	private String[] retreiveData(String api, JSONObject jsonObj)
	{
		List<String> data = new ArrayList<String>();
		
		//Get respose key
		String[] parameterKeys = env.getProperty(api +  "." + API_RESPONSE_KEYS).split(",");
		
		for (int i = 0; i < parameterKeys.length; i++) {
			
			String[] keyPath = parameterKeys[i].split(">");
			
			Object currentObj = jsonObj;
			for (int j = 0; j < keyPath.length; j++) {
				
				//check jsonarray
				if(Util.validateJsonArrayParam(keyPath[j]))
				{
					JSONArray array = ((JSONArray)currentObj);
					try {
						currentObj = array.get(Integer.parseInt(keyPath[j]));
					} catch (NumberFormatException | JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else
				{
					//check if last element
					if((j+1) == keyPath.length)
					{
						try {
							data.add( String.valueOf( ((JSONObject)currentObj).get(keyPath[j]) ));
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else
					{
						try {
							currentObj = ((JSONObject)currentObj).get(keyPath[j]);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		String[] dataArray = new String[data.size()];
		return data.toArray(dataArray);
	}
	
	private double convertToC(double c)
	{
		return (c - 32.0) * 5.0/9.0;
	}
	
	private double convertToF(double f)
	{
		return (f * 9.0/5.0) + 32.0;
	}
	
	@Transactional
	private void saveNewHistory(String ip, String city, String country, String lat, String lon, double temp, String tempUnit) {
		
		WeatherHistoryBE weatherHistory = new WeatherHistoryBE(ip, city, country, lat, lon, temp, tempUnit);
		
		weatherHistoryDao.save(weatherHistory);
	}
	
	public List<WeatherHistoryBE> findByIP(String ip)
	{
		return weatherHistoryDao.findByIP(ip);
	}
	
	public List<WeatherHistoryBE> findByDateRange(Date fromDate, Date toDate)
	{
		return weatherHistoryDao.findByDateRange(fromDate, toDate);
	}
	
	public List<WeatherHistoryBE> findHistoryAll()
	{
		return weatherHistoryDao.findAll();
	}
}
