package com.mintos.weather.task.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity(name = "Weather_History")
public class WeatherHistoryBE {
	@Id
	@GeneratedValue
	@JsonIgnore
	private Long id;
	// IP address
	private String ip;
	
	// City name
	private String city;
	
	// Country name
	private String country;
	
	// Latitude
	private String lat;
	
	// Longitude
	private String lon;
	
	// Temperature in Celsius
	private double tempC;
	
	// Temperature in Fahrenheit
	private double tempF;
	
	private Date date;
	
	public WeatherHistoryBE() {
		super();
	}
	
	public WeatherHistoryBE(String ip, String city, String country, String lat, String lon, double tempC, double tempF) {
		super();
		this.ip = ip;
		this.city = city;
		this.country = country;
		this.tempC = tempC;
		this.tempF = tempF;
		this.lat = lat;
		this.lon = lon;
		this.date = new Date();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getLon() {
		return lon;
	}

	public void setLon(String lon) {
		this.lon = lon;
	}

	public double getTempC() {
		return tempC;
	}

	public void setTempC(double tempC) {
		this.tempC = tempC;
	}

	public double getTempF() {
		return tempF;
	}

	public void setTempF(double tempF) {
		this.tempF = tempF;
	}
}
