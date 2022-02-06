package org.sdrc.dga.service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.sdrc.devinfo.repository.TimeperiodRepository;
import org.sdrc.dga.domain.Area;
import org.sdrc.dga.domain.FormXpathScoreMapping;
import org.sdrc.dga.domain.LastVisitData;
import org.sdrc.dga.domain.RawDataScore;
import org.sdrc.dga.model.AreaModel;
import org.sdrc.dga.model.FipDistrict;
import org.sdrc.dga.model.FipFacility;
import org.sdrc.dga.model.FormModel;
import org.sdrc.dga.model.InitalDataModel;
import org.sdrc.dga.model.SubmissionDataModel;
import org.sdrc.dga.model.TimePeriodModel;
import org.sdrc.dga.model.XFormModel;
import org.sdrc.dga.repository.AreaRepository;
import org.sdrc.dga.repository.FormXpathScoreMappingRepository;
import org.sdrc.dga.repository.LastVisitDataRepository;
import org.sdrc.dga.repository.RawDataScoreRepository;
import org.sdrc.dga.repository.TimePeriodRepository;
import org.sdrc.dga.repository.XFormRepository;
import org.sdrc.dga.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

@Service
public class FinalizeServiceImpl implements FinalizeService {

	@Autowired
	private LastVisitDataRepository lastVisitDataRepository;

	@Autowired
	private AreaRepository areaRepository;

	@Autowired
	private TimePeriodRepository timeperiodRepository;

	@Autowired
	private XFormRepository xFormRepository;

	@Autowired
	private ODKService odkService;

	@Autowired
	private RawDataScoreRepository rawDataScoreRepository;

	@Autowired
	private FormXpathScoreMappingRepository formXpathScoreMappingRepository;

	@Autowired
	private MessageSource messages;

	private DecimalFormat df = new DecimalFormat("0.00");

	@Override
	public List<SubmissionDataModel> getActiveSubmissions(int areaId, int timePeriodId, int formId) {

		List<LastVisitData> allData = lastVisitDataRepository.findByDistrictId(areaId, timePeriodId, formId);

		Map<Integer, Object[]> areaMap = areaRepository.getFacilityDistrics().stream()
				.collect(Collectors.toMap(a -> (Integer) a[0], a -> a));

		Map<Integer, RawDataScore> fasilityNameMap = rawDataScoreRepository.findByLastVisitDataAndXpath(formId).stream()
				.collect(Collectors.toMap(a -> a.getLastVisitData().getLastVisitDataId(), a -> a));

		List<FormXpathScoreMapping> facilityList = formXpathScoreMappingRepository.findByParentXpathId(-1).stream()
				.filter(a -> a.getForm().getFormId() == formId).collect(Collectors.toList());

		List<SubmissionDataModel> modelList = new ArrayList<>();
		
		Map<String, Area> facilityMap = areaRepository.findByAreaLevelAreaLevelIdIn(Arrays.asList(5,6,7,8,9,10)).parallelStream().collect(Collectors.toMap(Area :: getAreaCode, a->a));

		allData.forEach(a -> modelList.add(new SubmissionDataModel(a.getLastVisitDataId(), a.getInstanceId(),
				a.getMarkedAsCompleteDate(), a.getUser().getName(), areaMap.get(a.getArea().getAreaId())[3].toString(),
				facilityMap.get(fasilityNameMap.get(a.getLastVisitDataId()).getScore()).getAreaName(),
				facilityList.size() > 0 ? facilityList.get(0).getLabel().substring(16) : "", a.getxForm().getFormId(),
				a.getArea().getAreaId(), a.getTimPeriod().getTimePeriodId(), a.isFinalized(), a.getArea().getAreaName(),
				a.getxForm().getxFormIdTitle(), a.getTimPeriod().getShortName())));

		return modelList;
	}

	@Override
	public Boolean checkFinalize(int lastVisitDataId) {

		LastVisitData lvd = lastVisitDataRepository.findByLastVisitDataIdAndIsLiveTrue(lastVisitDataId);

		return !lastVisitDataRepository
				.findByAreaAndTimPeriodAndXForm(lvd.getArea(), lvd.getTimPeriod(), lvd.getxForm()).isEmpty();
	}

	/*
	 * when user submit form for a area,form, and timePeriod then as usual form data is inserted on lastVisit
	 * and rawDataScore table, when one record comes with 1st unick combination will send to generate for facilityScore 
	 * and makes that submission as finalize on lastVisitData table.
	 * After 1st submission all other submission comes for that combination facilityScore 
	 * will not generate and finalize field of lastvisitData will stay false.
	 * When admin wants to make any other submission finalize the this method will call and facilityScore will generated for that submission
	*/
	@Override
	public Boolean makeSubmissionFinalize(int lastVisitDataId) {

		LastVisitData lvd = lastVisitDataRepository.findByLastVisitDataIdAndIsLiveTrue(lastVisitDataId);
//		lastVisitDataRepository.updateLastVisitDataForFinalize(lvd.getxForm().getFormId(),
//				lvd.getTimPeriod().getTimePeriodId(), lvd.getArea().getAreaId(), lastVisitDataId);
		LastVisitData lvd2 = lastVisitDataRepository.getByXFormTimPeriodAreaIsFinalized(lvd.getxForm().getFormId(),
				lvd.getTimPeriod().getTimePeriodId(), lvd.getArea().getAreaId());

		if (lvd2 != null) {
			lvd2.setFinalized(false);
			lastVisitDataRepository.save(lvd2);
		}
		
		

		return odkService.aproveAndFinalizeLastVisitData(lastVisitDataId);
	}
	
	

	@Override
	@Transactional(readOnly = true)
	public Map<String, String> generateFIP(int lastVisitDataId) throws IOException {

		List<LastVisitData> lvds = new ArrayList<>();
		lvds.add(lastVisitDataRepository.findByLastVisitDataIdAndIsLiveTrue(lastVisitDataId));

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

		if (lvds.size() > 0) {

			FileInputStream fileInputStream = new FileInputStream(ResourceUtils
					.getFile("classpath:" + (messages.getMessage(Constants.TEMPLATE_EXCEL_PATH, null, null))
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

			if (xssfWorkbook.getSheet(messages.getMessage(Constants.SHEET_EXTERNAL_CHOICES_NAME, null, null)) != null) {
				XSSFSheet externalChoicesSheet = xssfWorkbook
						.getSheet(messages.getMessage(Constants.SHEET_EXTERNAL_CHOICES_NAME, null, null));
				for (int i = 1; i <= externalChoicesSheet.getLastRowNum(); i++) {
					XSSFRow row = externalChoicesSheet.getRow(i);

					if (null != row) {
						XSSFCell listNameCell = row.getCell(0);
						XSSFCell nameCell = row.getCell(1);
						XSSFCell labelCell = row.getCell(2);

						if (null != listNameCell && null != labelCell && null != nameCell) {
							String nameVal = nameCell.getCellType() == Cell.CELL_TYPE_STRING
									? nameCell.getStringCellValue()
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
			}
			/// end

			// iterate through all the sheet rows
//			int colId1 = 3;
//			int majorGap = colId1 + timePeriods.size(), activity = colId1 + timePeriods.size() + 1,
//					timeline = colId1 + timePeriods.size() + 2, resposible = colId1 + timePeriods.size() + 3;

//			for (TimePeriod time : timePeriods) {
//				XSSFRow row = sheet.getRow(0);
//
//				XSSFCell majorGapCell = row.createCell(majorGap);
//				XSSFCell activityCell = row.createCell(activity);
//				XSSFCell timelineCell = row.createCell(timeline);
//				XSSFCell resposibleCell = row.createCell(resposible);
//
//				// create response cell for that corresponding xpath
//				XSSFCell valueCell = row.createCell(colId1);
//				valueCell.setCellValue(time.getTimeperiod());
//				valueCell.setCellStyle(cellStyle);
//				sheet.setColumnWidth(valueCell.getColumnIndex(), 8630);
//				majorGapCell.setCellValue("Major Gap");
//				sheet.setColumnWidth(majorGapCell.getColumnIndex(), 8630);
//				activityCell.setCellValue("Activity Planned");
//				sheet.setColumnWidth(activityCell.getColumnIndex(), 8630);
//				timelineCell.setCellValue("Timeline");
//				sheet.setColumnWidth(timelineCell.getColumnIndex(), 8630);
//				resposibleCell.setCellValue("Responsible Person");
//				sheet.setColumnWidth(resposibleCell.getColumnIndex(), 8630);
//				activityCell.setCellStyle(cellStyle);
//				timelineCell.setCellStyle(cellStyle);
//				resposibleCell.setCellStyle(cellStyle);
//				majorGapCell.setCellStyle(cellStyle);
//
//				colId1++;
//			}
			int colId = 3, valueCol = 3;
			for (int tpIndex = 0; tpIndex < 1; tpIndex++) {

				LastVisitData lvd = lvds.get(0);
//				boolean tpExists = false;
//				for (LastVisitData lvd1 : lvds) {
//					if (lvd1.getTimPeriod().getTimePeriodId() == timePeriods.get(tpIndex).getTimePeriodId()) {
//						tpExists = true;
//						lvd = lvd1;
//						break;
//					}
//
//				}
//				if (!tpExists) {
//					colId++;
//					continue;
//				}

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
//						XSSFCell majorGapCell = row.createCell(majorGap);
//
//						XSSFCell activityCell = row.createCell(activity);
//						XSSFCell timelineCell = row.createCell(timeline);
//						XSSFCell resposibleCell = row.createCell(resposible);
//						activityCell.setCellStyle(blankCellStyle);
//						timelineCell.setCellStyle(blankCellStyle);
//						resposibleCell.setCellStyle(blankCellStyle);
//						majorGapCell.setCellStyle(blankCellStyle);

						// create response cell for that corresponding xpath
						XSSFCell valueCell = row.createCell(colId);

						{

							// setting column width
							valueCell.setCellStyle(cellStyle);
							for (int k = 0; k < 1; k++) {
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
												messages.getMessage(Constants.BEGIN_REPEAT_XPATH, null, null))
										|| typeCell.getStringCellValue().equalsIgnoreCase("begin_group")) {
									valueCell.setCellStyle(beginCellStyle);
									for (int k = 0; k < 1; k++) {
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
												messages.getMessage(Constants.END_REPEAT_XPATH, null, null))
										|| typeCell.getStringCellValue().equalsIgnoreCase("begin_group")
										|| typeCell.getStringCellValue().equalsIgnoreCase("end_group")) {
									splittingStr = queryString.toString()
											.split("/")[queryString.toString().split("/").length - 1];
									queryString = new StringBuilder(
											queryString.substring(0, queryString.lastIndexOf("/" + splittingStr)));
								}

								if (!(typeCell.getStringCellValue()
										.equalsIgnoreCase(messages.getMessage(Constants.BEGIN_GROUP_XPATH, null, null))
										|| typeCell.getStringCellValue().equalsIgnoreCase(
												messages.getMessage(Constants.END_GROUP_XPATH, null, null))
										|| typeCell.getStringCellValue().equalsIgnoreCase("begin_group")
										|| typeCell.getStringCellValue().equalsIgnoreCase("end_group"))) {

									if (!(nameCell.getStringCellValue().equals("")
											|| nameCell.getStringCellValue() == null)) {

										queryString = queryString.append("/" + nameCell.getStringCellValue());

										String response = xPath.get(queryString.toString());
										if (response == null) {
											response = "";
										}

										if (!xPath.containsKey(queryString.toString())
												&& lvd.getxForm().getAreaLevel().getAreaLevelId() == 4
												&& queryString.toString().endsWith("cal_d1")) {
											String modifiedXpath = queryString.toString();
											modifiedXpath = modifiedXpath.replaceAll("bg_d_2", "bg_d_1");
											response = xPath.get(modifiedXpath);
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

			String filepath = messages.getMessage("outputPath", null, null)
					+ lvds.get(lvds.size() - 1).getxForm().getxFormId() + "_" + lvds.get(0).getArea().getAreaName()
					+ "_" + sdf.format(new Date()) + ".xlsx";

//			sheet.createFreezePane(0, 1);
			FileOutputStream fileOutputStream = new FileOutputStream(filepath);
			xssfWorkbook.write(fileOutputStream);
			xssfWorkbook.close();
//System.out.println("path->"+filepath);
			Map<String, String> map = new HashMap<String, String>();
			map.put("File", filepath);
			return map;
			
		} else
			return null;

	}

	@Override
	public InitalDataModel getInitialData() {

		List<AreaModel> areaModels = new ArrayList<>();
		List<FormModel> formModel = new ArrayList<>();
		List<TimePeriodModel> timeModel = new ArrayList<>();

		areaRepository.findByAreaLevelAreaLevelId(4).forEach(a -> {
			areaModels.add(new AreaModel(a.getAreaId(), a.getAreaName(), a.getParentAreaId(), null,
					a.getAreaLevel().getAreaLevelId(), a.getIsLive(), a.getAreaCode()));
		});

		timeperiodRepository.findAll().forEach(a -> {
			timeModel.add(new TimePeriodModel(a.getTimePeriodId(), a.getShortName(), "", "", ""));
		});

//		xFormRepository.findAllByIsLiveTrue().forEach(a -> {
//			formModel.add(new FormModel(a.getFormId(), a.getXform_meta_id(), a.getTimePeriod().getTimePeriodId(),
//					a.getxFormIdTitle()));
//		});

		InitalDataModel inModel = new InitalDataModel();
		inModel.setDistricts(areaModels);
		inModel.setForms(formModel);
		inModel.setTimePeriods(timeModel);

		return inModel;
	}

	@Override
	public Boolean rejectSubmission(int lastVisitDataId) {

		LastVisitData lvd = lastVisitDataRepository.findByLastVisitDataIdAndIsLiveTrue(lastVisitDataId);
		lvd.setLive(false);
		lastVisitDataRepository.save(lvd);

		return true;
	}

	@Override
	public List<FormModel> getXForms(int timePeriodId) {
		// TODO Auto-generated method stub
		List<FormModel> formModel = new ArrayList<>();
		xFormRepository.findByIsLiveTrueAndTimePeriodTimePeriodId(timePeriodId).forEach(a -> {
			formModel.add(new FormModel(a.getFormId(), a.getXform_meta_id(), a.getTimePeriod().getTimePeriodId(),
					a.getxFormIdTitle()));
		});

		return formModel;
	}

	@Override
	public Map<String, Object> getPreData() {
		timeperiodRepository.findAll();
		areaRepository.findAll();
		return null;
	}

	@Override
	public Boolean acceptSubmission(int lastVisitDataId) {
		LastVisitData lvd = lastVisitDataRepository.findByLastVisitDataIdAndIsLiveTrue(lastVisitDataId);
//		lastVisitDataRepository.updateLastVisitDataForFinalize(lvd.getxForm().getFormId(),
//				lvd.getTimPeriod().getTimePeriodId(), lvd.getArea().getAreaId(), lastVisitDataId);
		LastVisitData lvd2 = lastVisitDataRepository.getByXFormTimPeriodAreaIsFinalized(lvd.getxForm().getFormId(),
				lvd.getTimPeriod().getTimePeriodId(), lvd.getArea().getAreaId());

		if (lvd2 != null) {
			lvd2.setFinalized(false);
			lastVisitDataRepository.save(lvd2);
			lvd.setFinalized(false);
			lastVisitDataRepository.save(lvd);
		}

		return true;
	}

	@Override
	public List<FipDistrict> getFinalizeDistrict(int stateId) {

		List<FipDistrict> fipDistricts = new ArrayList<FipDistrict>();
		List<Area> districts = areaRepository.findDistrictWithStateId(stateId);
		List<Area> blockFacilites = areaRepository.findAreaDataModelWithDistrict(stateId);
		List<Integer> blocks;
		Map<Integer,List<Area>> blockFacilitesMap = new HashMap<>();
		ArrayList<Area> areasList;
		for(Area area : blockFacilites) {
			if(blockFacilitesMap.containsKey(area.getParentAreaId())) {
				areasList = (ArrayList<Area>) blockFacilitesMap.get(area.getParentAreaId());
				areasList.add(area);
				blockFacilitesMap.put(area.getParentAreaId(),areasList);
			}else {
				areasList = new ArrayList<Area>();
				areasList.add(area);
				blockFacilitesMap.put(area.getParentAreaId(),areasList);
			}
			
		}
		
		
		List<Integer> list = new ArrayList<>();
		
		Map<Integer , List<Area>> map = new HashMap<>();
		
		districts.forEach(v->{
	
			list.add(v.getAreaId());
		});
		
		List<Area> areaList = new ArrayList<>();
		List<Area> facilitieList = areaRepository.findByParentAreaIdIn(list);
		
		for(Area a : facilitieList) {
			
			if(map.containsKey(a.getParentAreaId())) {
				map.get(a.getParentAreaId()).add(a);
			}else {
				
				areaList = new ArrayList<>();
				areaList.add(a);
				map.put(a.getParentAreaId(), areaList);
			}
				
		}
		ArrayList<Area> fasilityAreas;
		for (Area district : districts) {
			FipDistrict fipDistrict = new FipDistrict();
			
			List<FipFacility> fipFasilites = new ArrayList<FipFacility>();

			List<Area> facilities = map.get(district.getAreaId());
			blocks = new ArrayList<Integer>();
			if(facilities !=null) {
			for (Area facility : facilities) {
				FipFacility fipFacility = new FipFacility();
				
				if (facility.getAreaLevel().getAreaLevelId() == 5) {
					blocks.add(facility.getAreaId());
					if(blockFacilitesMap.containsKey(facility.getAreaId())) {
						fasilityAreas = (ArrayList<Area>) blockFacilitesMap.get(facility.getAreaId());
						
						for(Area blockFasilties : fasilityAreas) {
							FipFacility fipFacility1 = new FipFacility();
							fipFacility1.setAreaId(blockFasilties.getAreaId());
							fipFacility1.setAreaLevelId(blockFasilties.getAreaLevel().getAreaLevelId());
							fipFacility1.setFacilityCode(blockFasilties.getAreaCode());
							fipFacility1.setFacilityName(blockFasilties.getAreaName());
							fipFasilites.add(fipFacility1);
						}
						
					}

				} else {
					fipFacility.setAreaId(facility.getAreaId());
					fipFacility.setAreaLevelId(facility.getAreaLevel().getAreaLevelId());
					fipFacility.setFacilityCode(facility.getAreaCode());
					fipFacility.setFacilityName(facility.getAreaName());
					fipFasilites.add(fipFacility);
				}
				

			}}

			if(fipFasilites.size()>0) {
			fipDistrict.setAreaId(district.getAreaId());
			fipDistrict.setAreaName(district.getAreaName());
			fipDistrict.setFacilites(fipFasilites);
			fipDistricts.add(fipDistrict);
			}
		}

		return fipDistricts;
	
	}

}
