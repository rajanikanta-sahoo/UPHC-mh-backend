/**
 * 
 */
package org.sdrc.dga.model;

/**
 * @author Harsh Pratyush (harsh@sdrc.co.in)
 * This model will contain all the selected data from crosstab page
 * 
 *
 */
public class CrossTabDataModel {
	
	private int colIndicatorFormXpathMappingId;

	private String colDispansaryXpath;// will be zero in case of indicator is not applicable for DH

	private String colHealthPostXpath;// will be zero in case of indicator is not applicable for CHC

	private String colMaternityHomeXpath;// will be zero in case of indicator is not applicable for PHC
	
	private String colUphcXpath;// will be zero in case of indicator is not applicable for HSC

	private String coLabel;
	
	private int rowIndicatorFormXpathMappingId;

	private String rowDispansaryXpath;// will be zero in case of indicator is not applicable for DH

	private String rowHealthPostXpath;// will be zero in case of indicator is not applicable for CHC

	private String rowMaternityHomeXpath;// will be zero in case of indicator is not applicable for PHC
	
	private String rowUphcXpath;// will be zero in case of indicator is not applicable for HSC

	private String rowLabel;
	
	private int facilityTypeId;// will be zero in case of Facility Type-All
	
	private int districtId;// will be zero in case of state selection
	
	private int timePeriodId;// will be zero in case of Timeperiod-All
	
	private String rowIndicatorFormXpathMappingType;
	
	private String colIndicatorFormXpathMappingType;

	public int getColIndicatorFormXpathMappingId() {
		return colIndicatorFormXpathMappingId;
	}

	public void setColIndicatorFormXpathMappingId(int colIndicatorFormXpathMappingId) {
		this.colIndicatorFormXpathMappingId = colIndicatorFormXpathMappingId;
	}

	
	public String getCoLabel() {
		return coLabel;
	}

	public void setCoLabel(String coLabel) {
		this.coLabel = coLabel;
	}

	public int getRowIndicatorFormXpathMappingId() {
		return rowIndicatorFormXpathMappingId;
	}

	public void setRowIndicatorFormXpathMappingId(int rowIndicatorFormXpathMappingId) {
		this.rowIndicatorFormXpathMappingId = rowIndicatorFormXpathMappingId;
	}

	

	
	
	

	public String getRowUphcXpath() {
		return rowUphcXpath;
	}

	public void setRowUphcXpath(String rowUphcXpath) {
		this.rowUphcXpath = rowUphcXpath;
	}

	public String getRowLabel() {
		return rowLabel;
	}

	public void setRowLabel(String rowLabel) {
		this.rowLabel = rowLabel;
	}

	public int getFacilityTypeId() {
		return facilityTypeId;
	}

	public void setFacilityTypeId(int facilityTypeId) {
		this.facilityTypeId = facilityTypeId;
	}

	public int getDistrictId() {
		return districtId;
	}

	public void setDistrictId(int districtId) {
		this.districtId = districtId;
	}

	public int getTimePeriodId() {
		return timePeriodId;
	}

	public void setTimePeriodId(int timePeriodId) {
		this.timePeriodId = timePeriodId;
	}

	public String getRowIndicatorFormXpathMappingType() {
		return rowIndicatorFormXpathMappingType;
	}

	public void setRowIndicatorFormXpathMappingType(
			String rowIndicatorFormXpathMappingType) {
		this.rowIndicatorFormXpathMappingType = rowIndicatorFormXpathMappingType;
	}

	public String getColIndicatorFormXpathMappingType() {
		return colIndicatorFormXpathMappingType;
	}

	public void setColIndicatorFormXpathMappingType(
			String colIndicatorFormXpathMappingType) {
		this.colIndicatorFormXpathMappingType = colIndicatorFormXpathMappingType;
	}

	public String getColDispansaryXpath() {
		return colDispansaryXpath;
	}

	public void setColDispansaryXpath(String colDispansaryXpath) {
		this.colDispansaryXpath = colDispansaryXpath;
	}

	public String getColHealthPostXpath() {
		return colHealthPostXpath;
	}

	public void setColHealthPostXpath(String colHealthPostXpath) {
		this.colHealthPostXpath = colHealthPostXpath;
	}

	public String getColMaternityHomeXpath() {
		return colMaternityHomeXpath;
	}

	public void setColMaternityHomeXpath(String colMaternityHomeXpath) {
		this.colMaternityHomeXpath = colMaternityHomeXpath;
	}

	public String getColUphcXpath() {
		return colUphcXpath;
	}

	public void setColUphcXpath(String colUphcXpath) {
		this.colUphcXpath = colUphcXpath;
	}

	public String getRowDispansaryXpath() {
		return rowDispansaryXpath;
	}

	public void setRowDispansaryXpath(String rowDispansaryXpath) {
		this.rowDispansaryXpath = rowDispansaryXpath;
	}

	public String getRowHealthPostXpath() {
		return rowHealthPostXpath;
	}

	public void setRowHealthPostXpath(String rowHealthPostXpath) {
		this.rowHealthPostXpath = rowHealthPostXpath;
	}

	public String getRowMaternityHomeXpath() {
		return rowMaternityHomeXpath;
	}

	public void setRowMaternityHomeXpath(String rowMaternityHomeXpath) {
		this.rowMaternityHomeXpath = rowMaternityHomeXpath;
	}
	
	

}
