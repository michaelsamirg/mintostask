package com.mintos.weather.task.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity(name = "Weather_History")
public class WeatherHistoryBE {
	@Id
	@GeneratedValue
	private Long id;
	private String ip;
	private String city;
	private String country;
	private String lat;
	private String lon;
	private double temp;
	private String unit;
	private Date date;
	
	public WeatherHistoryBE() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public WeatherHistoryBE(String ip, String city, String country, String lat, String lon, double temp, String unit) {
		super();
		this.ip = ip;
		this.city = city;
		this.country = country;
		this.temp = temp;
		this.unit = unit;
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

	public double getTemp() {
		return temp;
	}

	public void setTemp(double temp) {
		this.temp = temp;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
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
	
}
