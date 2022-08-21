package org.sdrc.dga.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
import org.sdrc.dga.domain.Area;
import org.sdrc.dga.domain.LastVisitData;
import org.sdrc.dga.domain.RawDataScore;
import org.sdrc.dga.domain.TimePeriod;
import org.sdrc.dga.domain.XForm;
import org.sdrc.dga.model.AreaLevelModel;
import org.sdrc.dga.model.TimePeriodModel;
import org.sdrc.dga.model.XFormModel;
import org.sdrc.dga.repository.AreaRepository;
import org.sdrc.dga.repository.ChoiceDetailsRepository;
import org.sdrc.dga.repository.LastVisitDataRepository;
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
public class RawDataReportServiceImpl implements RawDataReportService {

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

	@Autowired
	private LastVisitDataRepository lastVisitDataRepository;

	@Autowired
	ChoiceDetailsRepository choiceDetailsRepository;

	@Override
	public Map<String, List<XFormModel>> getFacilityFormADistrictForRawData(Integer stateId) {

		List<XFormModel> xformModels = new ArrayList<XFormModel>();
		List<XForm> xForms = xFormRepository.findAllByIsLiveTrueAndStateAndMaxTimePeriod(stateId);

		Map<String, List<XFormModel>> xformMap = new LinkedHashMap<String, List<XFormModel>>();

		for (XForm xForm : xForms) {

			if (xformMap.containsKey(xForm.getProgram_XForm_Mapping().getProgram().getProgramName())) {
				xformMap.get(xForm.getProgram_XForm_Mapping().getProgram().getProgramName())
						.add(domainToModelConverter.toXFormModel(xForm));
			} else {

				xformModels = new ArrayList<XFormModel>();
				xformModels.add(domainToModelConverter.toXFormModel(xForm));
				xformMap.put(xForm.getProgram_XForm_Mapping().getProgram().getProgramName(), xformModels);

			}
		}
//		if(stateId!=2076) {
//		AreaLevelModel areaLevelModel = new AreaLevelModel();
//		XFormModel xformModel = new XFormModel();
//		areaLevelModel.setAreaLevelName("Fire and Electrical Assessment");
//		xformModel.setAreaLevelModel(areaLevelModel);
//		xformModel.setFormId(15);
//		xformModel.setxFormTitle("Fire and Electrical Assessment");
//		xformModels = new ArrayList<XFormModel>();
//		xformModels.add(xformModel);
//		xformMap.put("Fire and Electrical Assessment",xformModels);
//		}
		return xformMap;

	}

	@Override
	@Transactional(readOnly = true)
	public List<TimePeriodModel> getAllPlanningTimePeriodForRawData(int stateId, String programId) {
		int pid = programmRepository.findByProgramName(programId).getProgramId();
		List<TimePeriodModel> timePeriodModels = new ArrayList<TimePeriodModel>();
		List<TimePeriod> timePeriods = xFormRepository.findDistinctTimPeriodByStateAreaIdAndProgramIdForRawData(stateId,
				pid);
		timePeriods.sort(new Comparator<TimePeriod>() {

			@Override
			public int compare(TimePeriod o1, TimePeriod o2) {
				return o2.getTimePeriodId() - o1.getTimePeriodId();
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
	public String getRawDataReportName(String programId, String facilityName, int timePeriodId, String stateId) {
		// TODO Auto-generated method stub
		String fileName = "";
		String state = "";
		Area area = areaRepository.findByAreaId(Integer.parseInt(stateId));
		fileName = programId + "_" + area.getAreaCode() + "_v" + timePeriodId + "_" + facilityName;
		System.out.println(fileName);
		if (stateId.equals("2")) {
			state = "cg";
		}
		String formatedFile = programId + "_" + state + "_v" + timePeriodId + "_" + facilityName;
		switch (formatedFile) {

		case "DGA_cg_v1_DH":
			fileName = "DH_Raw_Data_r1.xlsx";
			break;
		case "DGA_cg_v2_DH":
			fileName = "DGA_2_DH_Raw_Data_r1.xlsx";
			break;
		case "DGA_cg_v3_DH":
			fileName = "DGA_3_DH_Raw_Data_r1.xlsx";
			break;
		case "DGA_cg_v1_CHC":
			fileName = "CHC_Raw Data_r1.xlsx";
			break;
		case "DGA_cg_v2_CHC":
			fileName = "DGA_2_CHC_Raw Data_r1.xlsx";
			break;
		case "DGA_v3_CHC":
			fileName = "DGA_3_CHC_Raw_Data_r1.xlsx";
			break;
		case "DGA_cg_v1_PHC":
			fileName = "PHC_Raw_Data_r1.xlsx";
			break;
		case "DGA_cg_v2_PHC":
			fileName = "DGA_2_PHC_Raw_Data_r1.xlsx";
			break;
		case "DGA_cg_v3_PHC":
			fileName = "DGA_3_PHC_Raw_Data_r1.xlsx";
			break;

		case "DGA_cg_v1_HSC":
		case "DGA_cg_v2_HSC":
		case "DGA_cg_v3_HSC":
			fileName = "DGA_3_HSC_Raw_Data_r1.xlsx";
			break;
		case "HWC_cg_v3_HSC":
			fileName = "HWC_HSC_Raw_Data_r1.xlsx";
			break;

		}

		return fileName;
	}

	@Override
	public String getRawDataReport(String programId, String facilityName, int timePeriodId, String stateId)
			throws Exception {

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

		int formMetaId = getFormMetaId(facilityName);

		XForm xform = xFormRepository.findByFormMetaDataIdAndStateId(formMetaId, Integer.parseInt(stateId));

		List<LastVisitData> lvds = lastVisitDataRepository
				.findByIsLiveTrueAndXFormMetaIdOrderByTimPeriodTimePeriodIdAsc(formMetaId);

		Map<String, String> choiceMap = new HashMap<>();
		choiceDetailsRepository.findByFormFormId(xform.getFormId()).stream()
				.filter(a -> !a.getChoiceValue().equals("0")).collect(Collectors.toList()).forEach(a -> {
					if (true)
						choiceMap.put(a.getChoiceValue(), a.getLabel());
				});

		Map<String, String> areaNameMap = areaRepository.findAll().stream()
				.collect(Collectors.toMap(Area::getAreaCode, a -> a.getAreaName()));

		if (lvds.size()>0) {
			List<TimePeriod> timePeriods = null;
			timePeriods = xFormRepository.findDistinctTimePeriodBySateIdAndFormMetaId(formMetaId,
					Integer.parseInt(stateId));

			// THE XLSX FILE TO WRITE
			// -------------------------KEEP TEMPLATE NAME SAME AS FORMID + .XLSX
			// -----------------ALWAYS................
			FileInputStream fileInputStream = new FileInputStream(ResourceUtils
					.getFile("classpath:" + (messages.getMessage(Constants.RAW_TEMPLATE_EXCEL_PATH, null, null))
							+ xform.getxFormId() + ".xlsx"));

			XSSFWorkbook xssfWorkbook = new XSSFWorkbook(fileInputStream);

			XSSFSheet sheet = xssfWorkbook.getSheet("Sheet1");
			POIXMLProperties xmlProps = xssfWorkbook.getProperties();
			POIXMLProperties.CoreProperties coreProps = xmlProps.getCoreProperties();
			coreProps.setCreator("dgaindia.org");

			String splittingStr = "";

			XSSFRow xssfRow = sheet.getRow(0);
			Iterator<Cell> cellIterator = xssfRow.cellIterator();

			Map<Integer, String> xPathMap = new HashMap<Integer, String>();
			Map<String, Integer> xPathMap2 = new HashMap<String, Integer>();
			int key = 0;
			String fpath = "";
			while (cellIterator.hasNext()) {

				fpath = cellIterator.next().getStringCellValue();
//				System.out.println("-->" + fpath);
				xPathMap.put(key, fpath);
				xPathMap2.put(fpath, key);
				key++;
			}

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
//			noCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
			noCellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
//			noCellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
			noCellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
//			noCellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
			noCellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
//			noCellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
			noCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
			noCellStyle.setVerticalAlignment(CellStyle.ALIGN_CENTER);
//			noCellStyle.setFillForegroundColor(HSSFColor.YELLOW.index);
//			noCellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);


			for (int tpIndex = 0; tpIndex < lvds.size(); tpIndex++) {

				LastVisitData lvd = lvds.get(tpIndex);

//				System.out.println("lvd->"+lvd.getLastVisitDataId());
				Map<String, RawDataScore> scoresMap = lvd.getRawDataScore().stream()
						.collect(Collectors.toMap(a -> a.getRawFormXapths().getXpath().toLowerCase(), a -> a));

				StringBuilder queryString = new StringBuilder();
				Map<String, String> xPath = new LinkedHashMap<String, String>();
				XSSFRow row;
				for (RawDataScore rawData : lvd.getRawDataScore()) {
					xPath.put(rawData.getRawFormXapths().getXpath(), rawData.getScore());
				}
				// insert data from here
				int count = 0;
				StringBuilder xpath = new StringBuilder();

				XSSFCell cell;
				row = sheet.createRow(tpIndex + 2);
				for (int k = 0; k < xPathMap.keySet().size(); k++) {
					if (xpath.length() > 0)
						xpath.delete(0, xpath.length());
					xpath = xpath.append("/" + xPathMap.get(k));
//if(xpath.toString().equalsIgnoreCase("/calc_uphc"))
//	System.out.println("hello");
					cell = row.createCell(k);
					cell.setCellStyle(noCellStyle);
					cell.setCellValue(scoresMap.get(xpath.toString().toLowerCase()) != null
							? scoresMap.get(xpath.toString().toLowerCase()).getScore().contains("IND")
									? areaNameMap.get(scoresMap.get(xpath.toString().toLowerCase()).getScore())
									: scoresMap.get(xpath.toString().toLowerCase()).getScore()
							: "");
				}

//				colId++;
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-SSS");

			String filepath =

					messages.getMessage("outputPath", null, null) + lvds.get(lvds.size() - 1).getxForm().getxFormId()
							+ "_" + sdf.format(new Date()) + ".xlsx";

//			sheet.createFreezePane(0, 1);
			FileOutputStream fileOutputStream = new FileOutputStream(filepath);
			xssfWorkbook.write(fileOutputStream);
			xssfWorkbook.close();
			return filepath;
		} else
			return "";

	}

	int getFormMetaId(String facilityType) {

		int id = 0;
		switch (facilityType.toLowerCase()) {
		case "uphc":
			id = 1;
			break;
		case "dispensary":
			id = 2;
			break;
		case "health post":
			id = 3;
			break;
		case "maternity home":
			id = 4;
			break;
		case "covid":
			id = 5;
			break;

		}

		return id;
	}

}
