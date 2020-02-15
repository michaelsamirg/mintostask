package com.mintos.weather.task.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mintos.weather.task.service.WeatherSerivce;

@RestController
public class WeatherController {

	@Autowired
	private WeatherSerivce weatherController;
	
	@RequestMapping(method = RequestMethod.GET, value="/weather")
	@ResponseBody
	public String getWeather()
	{
		return weatherController.getWeather();
	}
}
