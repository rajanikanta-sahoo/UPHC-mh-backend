package org.sdrc.dga.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.sdrc.dga.model.FipDistrict;
import org.sdrc.dga.model.FormModel;
import org.sdrc.dga.model.InitalDataModel;
import org.sdrc.dga.model.SubmissionDataModel;

public interface FinalizeService {

	List<SubmissionDataModel> getActiveSubmissions(int areaId,int timePeriodId, int formId);
	
	Boolean checkFinalize(int lastVisitDataId);
	
	Boolean makeSubmissionFinalize(int lastVisitDataId);
	
	Map<String, String> generateFIP(int lastVisitDataId) throws IOException;
	
	InitalDataModel getInitialData();
	
	List<FormModel> getXForms(int timePeriodId);
	
	Boolean rejectSubmission(int lastVisitData);
	
	Boolean acceptSubmission(int lastVisitData);
	
	Map<String,Object> getPreData();
	
	List<FipDistrict> getFinalizeDistrict(int stateId);
}
