package org.sdrc.dga.model;

import java.util.List;

public class FipDistrict {

	private String areaName;
	private int areaId;
	private String areaCode;
	private List<FipFacility> facilites;
	public String getAreaName() {
		return areaName;
	}
	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}
	public int getAreaId() {
		return areaId;
	}
	public void setAreaId(int areaId) {
		this.areaId = areaId;
	}
	public List<FipFacility> getFacilites() {
		return facilites;
	}
	public void setFacilites(List<FipFacility> facilites) {
		this.facilites = facilites;
	}
	public String getAreaCode() {
		return areaCode;
	}
	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}
	
}
