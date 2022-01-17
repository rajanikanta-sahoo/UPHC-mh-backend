package org.sdrc.dga.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.sdrc.dga.domain.Area;
import org.sdrc.dga.domain.TimePeriod;
import org.sdrc.dga.domain.XForm;
import org.sdrc.dga.model.AreaLevelModel;
import org.sdrc.dga.model.TimePeriodModel;
import org.sdrc.dga.model.XFormModel;
import org.sdrc.dga.repository.AreaRepository;
import org.sdrc.dga.repository.ProgrammRepository;
import org.sdrc.dga.repository.XFormRepository;
import org.sdrc.dga.util.DomainToModelConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RawDataReportServiceImpl implements RawDataReportService{
	
	@Autowired
	private XFormRepository xFormRepository;

	@Autowired
	private DomainToModelConverter domainToModelConverter;
	
	@Autowired
	private ProgrammRepository programmRepository;
	
	@Autowired
	private AreaRepository areaRepository;


	@Override
	public Map<String, List<XFormModel>> getFacilityFormADistrictForRawData(Integer stateId) {
		

		List<XFormModel> xformModels = new ArrayList<XFormModel>();
		List<XForm> xForms = xFormRepository.findAllByIsLiveTrueAndStateAndMaxTimePeriod(stateId);
		
		Map<String,List<XFormModel>> xformMap=new LinkedHashMap<String,List<XFormModel>>();

		for (XForm xForm : xForms) {
			
			if(xformMap.containsKey(xForm.getProgram_XForm_Mapping().getProgram().getProgramName()))
			{
				xformMap.get(xForm.getProgram_XForm_Mapping().getProgram().getProgramName()).add(domainToModelConverter.toXFormModel(xForm));
			}
			else
		{
			
			xformModels = new ArrayList<XFormModel>();
			xformModels.add(domainToModelConverter.toXFormModel(xForm));
			xformMap.put(xForm.getProgram_XForm_Mapping().getProgram().getProgramName(),xformModels);
			
			
		}
		}
		if(stateId!=2076) {
		AreaLevelModel areaLevelModel = new AreaLevelModel();
		XFormModel xformModel = new XFormModel();
		areaLevelModel.setAreaLevelName("Fire and Electrical Assessment");
		xformModel.setAreaLevelModel(areaLevelModel);
		xformModel.setFormId(15);
		xformModel.setxFormTitle("Fire and Electrical Assessment");
		xformModels = new ArrayList<XFormModel>();
		xformModels.add(xformModel);
		xformMap.put("Fire and Electrical Assessment",xformModels);
		}
		return xformMap;
	
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<TimePeriodModel> getAllPlanningTimePeriodForRawData(int stateId,String programId) {
		int pid = programmRepository.findByProgramName(programId).getProgramId();
		List<TimePeriodModel> timePeriodModels = new ArrayList<TimePeriodModel>();
		List<TimePeriod> timePeriods = xFormRepository.findDistinctTimPeriodByStateAreaIdAndProgramIdForRawData(stateId,pid);
		timePeriods.sort(new Comparator<TimePeriod>() {

			@Override
			public int compare(TimePeriod o1, TimePeriod o2) {
				return o2.getTimePeriodId()-o1.getTimePeriodId();
			}
		});
		for (TimePeriod timeperiod : timePeriods) {
			TimePeriodModel periodModel = new TimePeriodModel();
			periodModel.setTimePeriod(timeperiod.getTimeperiod());
			periodModel.setTimePeriod_Nid(timeperiod.getTimePeriodId());

			timePeriodModels.add(periodModel);
		}

		return timePeriodModels;
	}

	@Override
	public String getRawDataReportName(String programId, String facilityName, int timePeriodId,String stateId) {
		// TODO Auto-generated method stub
		String fileName="";
		String state = "";
		Area area = areaRepository.findByAreaId(Integer.parseInt(stateId));
		 fileName = programId+"_"+area.getAreaCode()+"_v"+timePeriodId+"_"+facilityName;
		 System.out.println(fileName);
		if(stateId.equals("2")) {
			state="cg";
		}
		String formatedFile = programId+"_"+state+"_v"+timePeriodId+"_"+facilityName;
		switch(formatedFile) {
		
		case "DGA_cg_v1_DH":
			fileName="DH_Raw_Data_r1.xlsx";
			break;
		case "DGA_cg_v2_DH":
			fileName="DGA_2_DH_Raw_Data_r1.xlsx";
			break;
		case "DGA_cg_v3_DH":
			fileName="DGA_3_DH_Raw_Data_r1.xlsx";
			break;
		case "DGA_cg_v1_CHC":
			fileName="CHC_Raw Data_r1.xlsx";
			break;
		case "DGA_cg_v2_CHC":
			fileName="DGA_2_CHC_Raw Data_r1.xlsx";
			break;
		case "DGA_v3_CHC":
			fileName="DGA_3_CHC_Raw_Data_r1.xlsx";
			break;
		case "DGA_cg_v1_PHC":
			fileName="PHC_Raw_Data_r1.xlsx";
			break;
		case "DGA_cg_v2_PHC":
			fileName="DGA_2_PHC_Raw_Data_r1.xlsx";
			break;
		case "DGA_cg_v3_PHC":
			fileName="DGA_3_PHC_Raw_Data_r1.xlsx";
			break;
		
		case "DGA_cg_v1_HSC":
		case "DGA_cg_v2_HSC":
		case "DGA_cg_v3_HSC":
			fileName="DGA_3_HSC_Raw_Data_r1.xlsx";
			break;
		case "HWC_cg_v3_HSC":
			fileName="HWC_HSC_Raw_Data_r1.xlsx";
			break;
		
		}
		
		return fileName;
	}

}
