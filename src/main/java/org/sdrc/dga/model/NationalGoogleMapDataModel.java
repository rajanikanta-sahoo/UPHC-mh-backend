package org.sdrc.dga.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NationalGoogleMapDataModel {

//	private String areaID;
	private String dataValue;
	private String longitude;
	private String latitude;
	private String title;
	private String icon;
	private String lable;
	private Integer sector;
	private Integer stateId;
	private Integer timePeriod;
	private Integer formId;
	private Boolean clickablel;
	private Integer aspDists;
	private String areaCode;
	private Integer facilityCovered;
}
