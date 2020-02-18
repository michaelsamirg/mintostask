package com.mintos.weather.task.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mintos.weather.task.model.WeatherHistoryBE;
import com.mintos.weather.task.service.WeatherSerivce;
import com.mintos.weather.task.util.Util;

@RestController
public class WeatherController {

	@Autowired
	private WeatherSerivce weatherService;
	
	@RequestMapping(method = RequestMethod.GET, value="/weather")
	@ResponseBody
	public Map<String, Object> getWeather()
	{
		return weatherService.getWeather();
	}
	
	@RequestMapping(method = RequestMethod.GET, value="/weather/history/all")
	@ResponseBody
	public List<WeatherHistoryBE> getWeatherHistoryAll()
	{
		return weatherService.findHistoryAll();
	}
	
	@RequestMapping(method = RequestMethod.GET, value="/weather/history/byIP")
	@ResponseBody
	public Map<String, Object> getWeatherHistoryByIP(@RequestParam(name = "ip", required = true) String ip)
	{
		Map<String, Object> map = new HashMap<String, Object>();
		
		if(ip != null && ip.length() == 0)
		{
			map.put("message", "IP address must be entered!");
			return map;
		}
		else if(!Util.validateIPAddress(ip))
		{
			map.put("message", "IP address is invalid!");
			return map;
		}
			
		map.put("message", "Success");
		map.put("list", weatherService.findByIP(ip));
		
		return map;
	}
	
	@RequestMapping(method = RequestMethod.GET, value="/weather/history/byDateRange")
	@ResponseBody
	public Map<String, Object> getWeatherrHistoryByDateRange(@RequestParam(name = "fromDate", required = false) String fromDate, 
			@RequestParam(name = "toDate", required = false) String toDate)
	{
		Map<String, Object> map = new HashMap<String, Object>();
		
		if(fromDate == null && toDate == null)
		{
			map.put("message", "From or to date must be entered!");
			return map;
		}
		else if(fromDate != null && Util.returnDate(fromDate, true) == null)
		{
			map.put("message", "From date is invalid!");
			return map;
		}
		else if(toDate != null && Util.returnDate(toDate, false) == null)
		{
			map.put("message", "To date is invalid!");
			return map;
		}
		
		map.put("message", "Success");
		map.put("list", weatherService.findByDateRange(Util.returnDate(fromDate, true), Util.returnDate(toDate, false)));
		
		return map;
	}
}
