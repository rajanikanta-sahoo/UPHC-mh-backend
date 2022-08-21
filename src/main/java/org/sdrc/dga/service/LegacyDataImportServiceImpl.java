package org.sdrc.dga.service;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdrc.dga.domain.Area;
import org.sdrc.dga.domain.ChoicesDetails;
import org.sdrc.dga.domain.CollectUser;
import org.sdrc.dga.domain.FacilityScore;
import org.sdrc.dga.domain.FormXpathScoreMapping;
import org.sdrc.dga.domain.LastVisitData;
import org.sdrc.dga.domain.RawDataScore;
import org.sdrc.dga.domain.RawFormXapths;
import org.sdrc.dga.domain.XForm;
import org.sdrc.dga.repository.AreaRepository;
import org.sdrc.dga.repository.ChoiceDetailsRepository;
import org.sdrc.dga.repository.CollectUserRepository;
import org.sdrc.dga.repository.FacilityScoreRepository;
import org.sdrc.dga.repository.FormXpathScoreMappingRepository;
import org.sdrc.dga.repository.LastVisitDataRepository;
import org.sdrc.dga.repository.RawDataScoreRepository;
import org.sdrc.dga.repository.RawFormXapthsRepository;
import org.sdrc.dga.repository.XFormRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LegacyDataImportServiceImpl implements LegacyDataImportService {

	@Autowired
	RawFormXapthsRepository rawFormXapthsRepository;

	@Autowired
	FormXpathScoreMappingRepository formXpathScoreMappingRepository;

	@Autowired
	XFormRepository xFormRepository;

	@Autowired
	AreaRepository areaRepository;

	@Autowired
	LastVisitDataRepository lastVisitDataRepository;

	@Autowired
	RawDataScoreRepository rawDataScoreRepository;

	@Autowired
	FacilityScoreRepository facilityScoreRepository;

	@Autowired
	ChoiceDetailsRepository choiceDetailsRepository;

	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private CollectUserRepository collectUserRepository;

//	@Autowired
//	private LegacyDataException legacyDataException;

	@Override
	@Transactional
	public Boolean importLegacyData(int timePeriod, int formId, MultipartFile file) {

		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
//		Map<String, LastVisitData> lvdMap = lastVisitDataRepository.findAll().stream().collect(Collectors.toMap(LastVisitData :: getInstanceId, a->a));
		List<String> lvdList = lastVisitDataRepository
				.findAll().stream()
				.map(a -> a.getInstanceId()).collect(Collectors.toList());
		List<Integer> lList = lastVisitDataRepository
				.findByIsLiveTrueAndTimPeriodTimePeriodIdAndXFormFormId(timePeriod, formId).stream()
				.map(a -> a.getArea().getAreaId()).collect(Collectors.toList());
		Map<String, RawFormXapths> rawFormXpathMap = rawFormXapthsRepository.findByFormFormId(formId).stream()
				.collect(Collectors.toMap(a -> a.getXpath().toLowerCase(), a -> a));

		Map<String, FormXpathScoreMapping> formXpathMap = new HashMap<>();
		for (FormXpathScoreMapping f : formXpathScoreMappingRepository.findByFormFormId(formId)) {
			formXpathMap.put(f.getxPath().toLowerCase(), f);
		}
//		Map<String, FormXpathScoreMapping> formXpathMap = formXpathScoreMappingRepository.findByFormFormId(formId)
//				.stream().collect(Collectors.toMap(FormXpathScoreMapping::getxPath, a -> a));


		XForm xForm = xFormRepository.findByFormId(formId);

		// chenge where areaCode is added
		Map<String, Area> areaMap = areaRepository.findAll().stream()
				.collect(Collectors.toMap(Area::getAreaCode, a -> a));

		Map<String, String> choiceMap = new HashMap<>();
		choiceDetailsRepository.findByFormFormId(formId).stream().filter(a -> !a.getChoiceValue().equals("0"))
				.collect(Collectors.toList()).forEach(a -> {
					if (true)
						choiceMap.put(a.getChoiceValue(), a.getLabel());
				});

		try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {

			XSSFSheet sheet = null;
			sheet = workbook.getSheetAt(0);

			XSSFRow xssfRow = sheet.getRow(0);
			Iterator<Cell> cellIterator = xssfRow.cellIterator();

			Cell cell = null;

			java.util.Date utilDate = new java.util.Date();
			java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

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

			StringBuilder xPath = new StringBuilder();
			String value = "";
			int areaId;
			for (int row = 2; row <= sheet.getLastRowNum(); row++) {
				int count = 0;
				xssfRow = sheet.getRow(row);
				cellIterator = xssfRow.cellIterator();
//				if(row==244)
				System.out.println(row);
				// here we are creating LastVisitData and setting all values
				xssfRow = sheet.getRow(row);
//				System.out.println("areaId--------------->"+xssfRow
//						.getCell(xPathMap2.get(xForm.getAreaXPath().subSequence(1, xForm.getAreaXPath().length())))
//						.getStringCellValue());
				areaId = areaMap.get(xssfRow
						.getCell(xPathMap2.get(xForm.getAreaXPath().subSequence(1, xForm.getAreaXPath().length())))
						.getStringCellValue()).getAreaId();
				lList.contains(areaId);

//				if (!lList.contains(areaId)
//						&& !lvdList.contains(xssfRow.getCell(xssfRow.getLastCellNum() - 1).getStringCellValue())) {
				if (!lList.contains(areaId)) {
					LastVisitData lvd = new LastVisitData();
					lvd.setArea(areaMap.get(xssfRow
							.getCell(xPathMap2.get(xForm.getAreaXPath().subSequence(1, xForm.getAreaXPath().length())))
							.getStringCellValue()));
					lList.add(areaMap.get(xssfRow
							.getCell(xPathMap2.get(xForm.getAreaXPath().subSequence(1, xForm.getAreaXPath().length())))
							.getStringCellValue()).getAreaId());
					lvd.setDateOfVisit(sqlDate);
					lvd.setInstanceId(xssfRow.getCell(xssfRow.getLastCellNum() - 1).getStringCellValue());
					lvd.setFinalized(true);
					lvd.setLive(true);
					if((xssfRow.getCell(xPathMap2.get(
							xForm.getLocationXPath().subSequence(1, xForm.getLocationXPath().length()) ))
							.toString().split("-")).length>0) {
					lvd.setLatitude(xssfRow.getCell(xPathMap2.get(
							xForm.getLocationXPath().subSequence(1, xForm.getLocationXPath().length()) ))
							.toString().split("-")[0]);
					lvd.setLongitude(xssfRow.getCell(xPathMap2.get(
							xForm.getLocationXPath().subSequence(1, xForm.getLocationXPath().length()) ))
							.toString().split("-")[1]);
					}else {
						lvd.setLatitude(null);
						lvd.setLongitude(null);
					}
					lvd.setTimPeriod(xForm.getTimePeriod());
					CollectUser collectUser = collectUserRepository.findByUsername("system");
//					CollectUser collectUser = collectUserRepository.findByUsername("nibedita");
//					collectUser.setUserId(1);
					lvd.setUser(collectUser);
					lvd.setxForm(xForm);
					lvd.setSubmissionFileName("LegacyData Insert");
					lvd.setSubmissionFileURL("LegacyData Insert");
					lvd = lastVisitDataRepository.save(lvd);
//					System.out.println("lvd ->" + lvd.getLastVisitDataId());
					while (cellIterator.hasNext()) {
						cell = cellIterator.next();
						if (xPath.length() > 0)
							xPath.delete(0, xPath.length());
						xPath = xPath.append(xPathMap.get(count));
//						System.out.println("xpath-> " + xPath);
						RawDataScore rwScore = new RawDataScore();
						FacilityScore fScore = new FacilityScore();
//						if(xPath.toString().equals("calc_hf"))
//							System.out.println("hi");
//						System.out.println(cell.getColumnIndex());
//					int type = cell.getCellType();
//						if(xPath.toString().toLowerCase().contains("bg_k/max_k"))
//							System.out.println("hi");
						
						if (!cell.toString().equals("null")) {
							if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC && HSSFDateUtil.isCellDateFormatted(cell))
								if (xPath.toString().contains("time"))
									value = timeFormatter.format(cell.getDateCellValue());
								else
									value = formatter.format(cell.getDateCellValue());
							else if (cell.getCellType() == Cell.CELL_TYPE_STRING)
								value = cell.getStringCellValue();
							else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC)
								value = ((int)Float.parseFloat(cell.getNumericCellValue() + ""))+"" ;
							else if(cell.getCellType() == Cell.CELL_TYPE_FORMULA)
								value = ((int)Float.parseFloat(cell.getNumericCellValue() + ""))+"";
							else if(cell.getCellType() == Cell.CELL_TYPE_BLANK)
								value = "";
						} else {
							value = "";
						}
//						System.out.println(cell.getColumnIndex()+"-value -> " + value);
						

						if (rawFormXpathMap.containsKey("/" + xPath.toString().toLowerCase())) {
							rwScore.setLastVisitData(lvd);
							rwScore.setRawFormXapths(rawFormXpathMap.get("/" + xPath.toString().toLowerCase()));
							value = value.replace("\n", ",");
							rwScore.setScore(value);
//							System.out.println("rwScore->" + xPath + ", score->" + value);
							rawDataScoreRepository.save(rwScore);
						}
						if (formXpathMap.containsKey(xPath.toString().toLowerCase())) {
							fScore.setFormXpathScoreMapping(formXpathMap.get(xPath.toString().toLowerCase()));
							fScore.setLastVisitData(lvd);
							fScore.setMaxScore(formXpathMap.get(xPath.toString().toLowerCase()).getMaxScore());
							
//							if(xPath.toString().equals("calc_hf"))
//								System.out.println("hi");
							fScore.setScore(checkNumberType(value) ? Double.parseDouble(value)
									: value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("no")
											? value.equalsIgnoreCase("yes") ? Double.parseDouble("1")
													: value.equalsIgnoreCase("no") ? Double.parseDouble("0")
															: Double.parseDouble(choiceMap.get(value))
											: Double.parseDouble("0"));
//							System.out.println("fscore->" + xPath + ", score->" + value);
							facilityScoreRepository.save(fScore);
						}

						count++;
					}
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	boolean checkNumberType(String val) {

		try {
			Double.parseDouble(val);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

}
