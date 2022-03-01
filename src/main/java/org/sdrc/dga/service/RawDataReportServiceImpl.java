package org.sdrc.dga.service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.POIXMLProperties;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdrc.dga.domain.Area;
import org.sdrc.dga.domain.LastVisitData;
import org.sdrc.dga.domain.RawDataScore;
import org.sdrc.dga.domain.TimePeriod;
import org.sdrc.dga.domain.XForm;
import org.sdrc.dga.model.AreaLevelModel;
import org.sdrc.dga.model.TimePeriodModel;
import org.sdrc.dga.model.XFormModel;
import org.sdrc.dga.repository.AreaRepository;
import org.sdrc.dga.repository.ProgrammRepository;
import org.sdrc.dga.repository.XFormRepository;
import org.sdrc.dga.util.Constants;
import org.sdrc.dga.util.DomainToModelConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

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
	
	@Autowired
	private MessageSource messages;
	


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

	@Override
	public String getRawDataReport(String programId, String facilityName, int timePeriodId, String stateId) {

//		List<LastVisitData> lvds = lastVisitDataRepository
//				.findByAreaAreaCodeAndIsLiveTrueAndXFormMetaIdOrderByTimPeriodTimePeriodIdAsc(areaCode, formMetaId);
		
//		boolean dataAvalable= false;
//		List<Integer> avlTimePeriod = new ArrayList();
//		TimePeriod latestTimeperiod = timePeriodRepository.findTop1ByOrderByTimePeriodIdDesc();
//		for(int i=lvds.size();i>0;i-- ) { 
//			avlTimePeriod.add(lvds.get(i-1).getTimPeriod().getTimePeriodId());
//		if(latestTimeperiod.getTimePeriodId() == lvds.get(i-1).getTimPeriod().getTimePeriodId()) {
//			 dataAvalable = true;
//			 break;
//		 }
//		}
		
		if (true) {
			List<TimePeriod> timePeriods = null;
			 timePeriods = xFormRepository.findDistinctTimePeriodBySateIdAndFormMetaId(formMetaId,
					stateId);
				

			// THE XLSX FILE TO WRITE
			// -------------------------KEEP TEMPLATE NAME SAME AS FORMID + .XLSX
			// -----------------ALWAYS................
			FileInputStream fileInputStream = new FileInputStream(
					ResourceUtils.getFile("classpath:"+(messages.getMessage(Constants.TEMPLATE_EXCEL_PATH, null, null))
							+ lvds.get(lvds.size() - 1).getxForm().getxFormId() + ".xlsx"));

			XSSFWorkbook xssfWorkbook = new XSSFWorkbook(fileInputStream);

			XSSFSheet sheet = xssfWorkbook.getSheet(messages.getMessage(Constants.SHEET_WRITE_NAME, null, null));
			POIXMLProperties xmlProps = xssfWorkbook.getProperties();
			POIXMLProperties.CoreProperties coreProps = xmlProps.getCoreProperties();
			coreProps.setCreator("dgaindia.org");

			String splittingStr = "";

			XSSFCellStyle cellStyle = xssfWorkbook.createCellStyle();
			cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
			cellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
			cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			cellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
			cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
			cellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
			cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
			cellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
			cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
			cellStyle.setVerticalAlignment(CellStyle.ALIGN_CENTER);
			cellStyle.setLocked(true);
			cellStyle.setWrapText(true);

			XSSFCellStyle blankCellStyle = xssfWorkbook.createCellStyle();
			blankCellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
			blankCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
			blankCellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			blankCellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
			blankCellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
			blankCellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
			blankCellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
			blankCellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
			blankCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
			blankCellStyle.setVerticalAlignment(CellStyle.ALIGN_CENTER);
			blankCellStyle.setLocked(false);

			XSSFCellStyle beginCellStyle = xssfWorkbook.createCellStyle();
			beginCellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
			beginCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
			beginCellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			beginCellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
			beginCellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
			beginCellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
			beginCellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
			beginCellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
			beginCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
			beginCellStyle.setFillForegroundColor(HSSFColor.PALE_BLUE.index);
			beginCellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
			beginCellStyle.setVerticalAlignment(CellStyle.ALIGN_CENTER);

			XSSFCellStyle noCellStyle = xssfWorkbook.createCellStyle();
			noCellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
			noCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
			noCellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			noCellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
			noCellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
			noCellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
			noCellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
			noCellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
			noCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
			noCellStyle.setVerticalAlignment(CellStyle.ALIGN_CENTER);
			noCellStyle.setFillForegroundColor(HSSFColor.YELLOW.index);
			noCellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);

			///// create map of choices list name---name and label-----------

			XSSFSheet choicesSheet = xssfWorkbook
					.getSheet(messages.getMessage(Constants.SHEET_CHOICES_NAME, null, null));

			Map<String, String> nameLabelMap = new HashMap<String, String>();
			Map<String, Map<String, String>> listNameLabelMap = new HashMap<String, Map<String, String>>();

			for (int i = 1; i <= choicesSheet.getLastRowNum(); i++) {
				XSSFRow row = choicesSheet.getRow(i);

				if (null != row) {
					XSSFCell listNameCell = row.getCell(0);
					XSSFCell nameCell = row.getCell(1);
					XSSFCell labelCell = row.getCell(2);

					if (null != listNameCell && null != labelCell && null != nameCell) {
						String nameVal = nameCell.getCellType() == Cell.CELL_TYPE_STRING ? nameCell.getStringCellValue()
								: Integer.toString(((Double) nameCell.getNumericCellValue()).intValue());

						String labelVal = labelCell.getCellType() == Cell.CELL_TYPE_STRING
								? labelCell.getStringCellValue()
								: Integer.toString(((Double) labelCell.getNumericCellValue()).intValue());

						if (listNameLabelMap.containsKey(listNameCell.getStringCellValue())) {

							listNameLabelMap.get(listNameCell.getStringCellValue()).put(nameVal.trim(), labelVal);
						} else {
							nameLabelMap = new HashMap<String, String>();

							nameLabelMap.put(nameVal.trim(), labelVal);

							listNameLabelMap.put(listNameCell.getStringCellValue().trim(), nameLabelMap);
						}
					}
				}
			}
			
			
			if(xssfWorkbook.getSheet(messages.getMessage(
					Constants.SHEET_EXTERNAL_CHOICES_NAME, null, null))!=null)
			{
			XSSFSheet externalChoicesSheet = xssfWorkbook.getSheet(messages.getMessage(
					Constants.SHEET_EXTERNAL_CHOICES_NAME, null, null));
			for (int i = 1; i <= externalChoicesSheet.getLastRowNum(); i++) {
				XSSFRow row = externalChoicesSheet.getRow(i);
				
				if(null!=row){
					XSSFCell listNameCell = row.getCell(0);
					XSSFCell nameCell = row.getCell(1);
					XSSFCell labelCell = row.getCell(2);
					
					if(null!=listNameCell && null!=labelCell && null!=nameCell){
						String nameVal =nameCell.getCellType() == Cell.CELL_TYPE_STRING ? nameCell.getStringCellValue() :
							Integer.toString(((Double)nameCell.getNumericCellValue()).intValue()) ;
						
						String labelVal =labelCell.getCellType() == Cell.CELL_TYPE_STRING ? labelCell.getStringCellValue() :
							Integer.toString(((Double)labelCell.getNumericCellValue()).intValue()) ;
					
					if(listNameLabelMap.containsKey(listNameCell.getStringCellValue() )){
						
						listNameLabelMap.get(listNameCell.getStringCellValue() )
							.put(nameVal.trim(), labelVal);
					}else{
						nameLabelMap = new HashMap<String, String>();
						
						nameLabelMap.put(nameVal.trim(), labelVal);
						
						listNameLabelMap.put(listNameCell.getStringCellValue().trim(), nameLabelMap);
					}
					}
				}
			}
			}
			/// end

			// iterate through all the sheet rows
			int colId1 = 3;
			int majorGap = colId1 + timePeriods.size(), activity = colId1 + timePeriods.size() + 1,
					timeline = colId1 + timePeriods.size() + 2, resposible = colId1 + timePeriods.size() + 3;

			for (TimePeriod time : timePeriods) {
				XSSFRow row = sheet.getRow(0);

				XSSFCell majorGapCell = row.createCell(majorGap);
				XSSFCell activityCell = row.createCell(activity);
				XSSFCell timelineCell = row.createCell(timeline);
				XSSFCell resposibleCell = row.createCell(resposible);

				// create response cell for that corresponding xpath
				XSSFCell valueCell = row.createCell(colId1);
				valueCell.setCellValue(time.getTimeperiod());
				valueCell.setCellStyle(cellStyle);
				sheet.setColumnWidth(valueCell.getColumnIndex(), 8630);
				majorGapCell.setCellValue("Major Gap");
				sheet.setColumnWidth(majorGapCell.getColumnIndex(), 8630);
				activityCell.setCellValue("Activity Planned");
				sheet.setColumnWidth(activityCell.getColumnIndex(), 8630);
				timelineCell.setCellValue("Timeline");
				sheet.setColumnWidth(timelineCell.getColumnIndex(), 8630);
				resposibleCell.setCellValue("Responsible Person");
				sheet.setColumnWidth(resposibleCell.getColumnIndex(), 8630);
				activityCell.setCellStyle(cellStyle);
				timelineCell.setCellStyle(cellStyle);
				resposibleCell.setCellStyle(cellStyle);
				majorGapCell.setCellStyle(cellStyle);

				colId1++;
			}
			int colId = 3, valueCol = 3;
			for (int tpIndex =0; tpIndex< timePeriods.size();tpIndex++) {
				
				LastVisitData lvd = null;
				boolean tpExists = false;
				for(LastVisitData lvd1 : lvds) {
					if (lvd1.getTimPeriod().getTimePeriodId() == timePeriods.get(tpIndex).getTimePeriodId()) {
						tpExists = true;
						lvd = lvd1;
						break;
					}
					
				}
				if(!tpExists) {
					colId++;
					continue;
				}
				
				
				StringBuilder queryString = new StringBuilder();
				Map<String, String> xPath = new LinkedHashMap<String, String>();
				for (RawDataScore rawData : lvd.getRawDataScore()) {
					xPath.put(rawData.getRawFormXapths().getXpath(), rawData.getScore());
				}
				for (int i = 1; i <= sheet.getLastRowNum(); i++) {

					XSSFRow row = sheet.getRow(i);
					if (null != row) {

						XSSFCell typeCell = row.getCell(0);
						XSSFCell nameCell = row.getCell(1);
						XSSFCell majorGapCell = row.createCell(majorGap);

						XSSFCell activityCell = row.createCell(activity);
						XSSFCell timelineCell = row.createCell(timeline);
						XSSFCell resposibleCell = row.createCell(resposible);
						activityCell.setCellStyle(blankCellStyle);
						timelineCell.setCellStyle(blankCellStyle);
						resposibleCell.setCellStyle(blankCellStyle);
						majorGapCell.setCellStyle(blankCellStyle);

						// create response cell for that corresponding xpath
						XSSFCell valueCell = row.createCell(colId);

						{

							// setting column width
							valueCell.setCellStyle(cellStyle);
							for (int k = 0; k < timePeriods.size(); k++) {
								if (row.getCell(valueCol + k) == null) {
									XSSFCell otherCell = row.createCell(valueCol + k);
									otherCell.setCellStyle(cellStyle);
								}

							}
							if (null != typeCell && !typeCell.getStringCellValue().isEmpty()) {

//					if(typeCell.getStringCellValue().equalsIgnoreCase(messages.getMessage(
//							Constants.NOTE_XPATH, null, null)))
//						continue;

								if (typeCell.getStringCellValue()
										.equalsIgnoreCase(messages.getMessage(Constants.BEGIN_GROUP_XPATH, null, null))
										|| typeCell.getStringCellValue().equalsIgnoreCase(
												messages.getMessage(Constants.BEGIN_REPEAT_XPATH, null, null))||typeCell
										.getStringCellValue().equalsIgnoreCase(
												"begin_group")) {
									valueCell.setCellStyle(beginCellStyle);
									for (int k = 0; k < timePeriods.size(); k++) {
										XSSFCell otherCell = row.createCell(valueCol + k);
										otherCell.setCellStyle(beginCellStyle);
									}
//						XSSFCell otherCell = row.createCell(col2Id);
//						otherCell.setCellStyle(beginCellStyle);
									queryString = queryString.append("/" + nameCell.getStringCellValue());
								}
								if (typeCell.getStringCellValue()
										.equalsIgnoreCase(messages.getMessage(Constants.END_GROUP_XPATH, null, null))
										|| typeCell.getStringCellValue().equalsIgnoreCase(
												messages.getMessage(Constants.END_REPEAT_XPATH, null, null))||typeCell
										.getStringCellValue().equalsIgnoreCase(
												"begin_group")||typeCell
										.getStringCellValue().equalsIgnoreCase(
												"end_group")) {
									splittingStr = queryString.toString()
											.split("/")[queryString.toString().split("/").length - 1];
									queryString = new StringBuilder(
											queryString.substring(0, queryString.lastIndexOf("/" + splittingStr)));
								}

								if (!(typeCell.getStringCellValue()
										.equalsIgnoreCase(messages.getMessage(Constants.BEGIN_GROUP_XPATH, null, null))
										|| typeCell.getStringCellValue().equalsIgnoreCase(
												messages.getMessage(Constants.END_GROUP_XPATH, null, null))||typeCell
										.getStringCellValue().equalsIgnoreCase(
												"begin_group")||typeCell
										.getStringCellValue().equalsIgnoreCase(
												"end_group"))) {

									if (!(nameCell.getStringCellValue().equals("")
											|| nameCell.getStringCellValue() == null)) {

										queryString = queryString.append("/" + nameCell.getStringCellValue());


										String response = xPath.get( queryString.toString());
										if (response == null) {
											response = "";
										}

										if (!xPath.containsKey(queryString.toString())
												&& lvd.getxForm().getAreaLevel().getAreaLevelId() == 4
												&& queryString.toString().endsWith("cal_d1")) {
											String modifiedXpath = queryString.toString();
											modifiedXpath = modifiedXpath.replaceAll("bg_d_2", "bg_d_1");
											response = xPath.get( modifiedXpath);
											if (response == null) {
												response = "";
											}
										}
										String type[] = typeCell.getStringCellValue().split("\\s+");
										if (!response.equals("") && typeCell.getStringCellValue().contains(
												messages.getMessage(Constants.SELECT_ONE_XPATH, null, null))) {

											if (response.startsWith("IND") && !listNameLabelMap.get(
													typeCell.getStringCellValue().split("\\s+")[type.length - 1].trim())
													.containsKey(response)) {
												valueCell.setCellValue(
														areaRepository.findByAreaCode(response).getAreaName());
											} else {
												valueCell
														.setCellValue(listNameLabelMap
																.get(typeCell.getStringCellValue()
																		.split("\\s+")[type.length - 1].trim())
																.get(response));
											}

										} else if (!response.equals("") && typeCell.getStringCellValue().contains(
												messages.getMessage(Constants.SELECT_MULTIPLE_XPATH, null, null))) {
											StringBuilder multipleVal = new StringBuilder();

											for (int m = 0; m < response.split("\\s+").length; m++) {
												multipleVal.append(m == 0
														? listNameLabelMap.get(type[type.length - 1])
																.get(response.split("\\s+")[m])
														: ", " + listNameLabelMap.get(type[type.length - 1])
																.get(response.split("\\s+")[m]));
											}

											valueCell.setCellValue(multipleVal.toString());
										} else if (!response.equals("")
												&& typeCell.getStringCellValue().contains("decimal")) {

											valueCell.setCellValue(df.format(Double.parseDouble(response)));
										} else
											valueCell.setCellValue(response);
										// valueCell.setCellStyle(valueCell.getCellStyle());
										valueCell.setCellStyle(cellStyle);
										if (valueCell.getCellType() == XSSFCell.CELL_TYPE_STRING
												&& valueCell.getStringCellValue().equalsIgnoreCase("no"))
											valueCell.setCellStyle(noCellStyle);

										splittingStr = queryString.toString()
												.split("/")[queryString.toString().split("/").length - 1];
										queryString = new StringBuilder(
												queryString.substring(0, queryString.lastIndexOf("/" + splittingStr)));
									}

								}

							}
						}
					}

				}
				colId++;
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-SSS");

			String filepath =

					messages.getMessage("outputPath", null, null) + lvds.get(lvds.size() - 1).getxForm().getxFormId() + "_"
							+ lvds.get(0).getArea().getAreaName() + "_" + sdf.format(new Date()) + ".xlsx";

//			sheet.createFreezePane(0, 1);
			FileOutputStream fileOutputStream = new FileOutputStream(filepath);
			xssfWorkbook.write(fileOutputStream);
			xssfWorkbook.close();

			return filepath;
		} else
			return "";
		
	}

}
