package com.mintos.weather.task.enums;

public enum TempUnitEnum {
	UINT_C("c"),
	UNIT_F("f");
	
	private String unit;
	
	private TempUnitEnum(String unit)
	{
		this.unit = unit;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}
}
