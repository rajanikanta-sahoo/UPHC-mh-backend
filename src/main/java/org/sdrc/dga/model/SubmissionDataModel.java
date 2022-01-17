package org.sdrc.dga.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SubmissionDataModel {

	private int lastVisitDataId;
	private String instanceId;
	private Date markedAsCompleteDate;
	private String createdBy;
	private String district;
	private String facility;
	private String facilityType;
	
	private int formId;
	private int areaId;
	private int timePeriodId;
	private boolean isFinalized;
	
	private String areaName;
	private String formName;
	private String timePeriod;
	
//	
}
