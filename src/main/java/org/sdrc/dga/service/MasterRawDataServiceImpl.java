/**
 * 
 */
package org.sdrc.dga.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.kxml2.io.KXmlSerializer;
import org.opendatakit.briefcase.model.DocumentDescription;
import org.opendatakit.briefcase.model.ServerConnectionInfo;
import org.opendatakit.briefcase.model.TerminationFuture;
import org.opendatakit.briefcase.util.AggregateUtils;
import org.opendatakit.briefcase.util.WebUtils;
import org.sdrc.dga.domain.Area;
import org.sdrc.dga.domain.AreaLevel;
import org.sdrc.dga.domain.ChoicesDetails;
import org.sdrc.dga.domain.CollectUser;
import org.sdrc.dga.domain.FormXpathScoreMapping;
import org.sdrc.dga.domain.IndicatorFormXpathMapping;

import org.sdrc.dga.domain.LastVisitData;
import org.sdrc.dga.domain.RawDataScore;
import org.sdrc.dga.domain.RawFormXapths;
import org.sdrc.dga.domain.TimePeriod;
import org.sdrc.dga.domain.XForm;
import org.sdrc.dga.model.PostSubmissionModel;
import org.sdrc.dga.repository.AreaRepository;
import org.sdrc.dga.repository.ChoiceDetailsRepository;
import org.sdrc.dga.repository.FormXpathScoreMappingRepository;
import org.sdrc.dga.repository.IndicatorFormXpathMappingRepository;

import org.sdrc.dga.repository.LastVisitDataRepository;
import org.sdrc.dga.repository.RawDataScoreRepository;
import org.sdrc.dga.repository.RawFormXapthsRepository;
import org.sdrc.dga.repository.XFormRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlSerializer;

/**
 * @author Harsh Pratyush(harsh@sdrc.co.in)
 *
 */
@Service
public class MasterRawDataServiceImpl implements MasterRawDataService {

	@Autowired
	private XFormRepository xFormRepository;

	@Autowired
	private RawFormXapthsRepository rawFormXapthsRepository;

	@Autowired
	private AreaRepository areaRepository;

	@Autowired
	private LastVisitDataRepository lastVisitDataRepository;

	@Autowired
	private ChoiceDetailsRepository choiceDetailsRepository;

	@Autowired
	private RawDataScoreRepository rawDataScoreRepository;

	@Autowired
	private FormXpathScoreMappingRepository formXpathScoreMappingRepository;

	@Autowired
	private IndicatorFormXpathMappingRepository indicatorFormXpathMappingRepository;

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
	SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy");
	private static final Logger logger = LoggerFactory.getLogger(MasterRawDataServiceImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sdrc.dga.service.MasterRawDataService#generateXpath()
	 */
	@Transactional
	@Override
	public boolean generateXpath() throws Exception {

		List<XForm> xforms = xFormRepository.findAllByIsCompleteFalse();
		
		for (XForm xform : xforms) {
			String inputFilePath = "C:\\Users\\SDRC_DEV\\Documents\\sdrc\\uphc-mh\\xform\\" + xform.getxFormId()+ ".xlsx";
			FileInputStream fileInputStream = new FileInputStream(inputFilePath);
			XSSFWorkbook xssfWorkbook = new XSSFWorkbook(fileInputStream);

			XSSFSheet sheet = xssfWorkbook.getSheet("survey");

			StringBuilder queryString = new StringBuilder();
//		queryString.append(xform.getxFormId());
			String splittingStr = "";

//		if (doc_id_list != null) 
			{
//			doc_id_list.getDocumentElement().normalize();

//			NodeList nodeIdList = doc_id_list.getElementsByTagName("id");
				// LocalDateTime currentDate = LocalDateTime.now();
				// LocalDateTime dbMarkedAsCompleteDateTime = null;
				// String dbMarkedAsCompleteDate = null;
				for (int node_no = 0; node_no < 1; node_no++) {

					XSSFRow headRrow = sheet.getRow(0);

					for (int i = 1; i <= sheet.getLastRowNum(); i++) {
						RawFormXapths rawFormXapths = new RawFormXapths();

						XSSFRow row = sheet.getRow(i);
						if (null != row) {

							XSSFCell typeCell = row.getCell(0);
							XSSFCell nameCell = row.getCell(1);
							if (null != typeCell && !typeCell.getStringCellValue().isEmpty()) {

								if (typeCell.getStringCellValue().equalsIgnoreCase("begin group")
										|| typeCell.getStringCellValue().equalsIgnoreCase("begin_group")) {
									rawFormXapths.setType(typeCell.getStringCellValue());
									queryString = queryString.append("/" + nameCell.getStringCellValue());
								} else if (typeCell.getStringCellValue().equalsIgnoreCase("end group")
										|| typeCell.getStringCellValue().equalsIgnoreCase("end_group")) {
									rawFormXapths.setType(typeCell.getStringCellValue());
									splittingStr = queryString.toString()
											.split("/")[queryString.toString().split("/").length - 1];
									queryString = new StringBuilder(
											queryString.substring(0, queryString.lastIndexOf("/" + splittingStr)));
								}

								if (!(typeCell.getStringCellValue().equalsIgnoreCase("begin group")
										|| typeCell.getStringCellValue().equalsIgnoreCase("end group")
										|| typeCell.getStringCellValue().equalsIgnoreCase("begin_group")
										|| typeCell.getStringCellValue().equalsIgnoreCase("end_group"))) {

									queryString = queryString.append("/" + nameCell.getStringCellValue());

									rawFormXapths.setType(typeCell.getStringCellValue());

									rawFormXapths.setForm(xform);
									rawFormXapths.setXpath(queryString.toString());
									// formXpathScoreMapping.setLive(true);
									if (row.getCell(2) != null) {
										rawFormXapths.setLabel(row.getCell(2).toString());
									}
									// row.getCell(2).setCellValue(rawFormXapths.getXpath());
									System.out.println(rawFormXapths);
									rawFormXapthsRepository.save(rawFormXapths);

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
			XSSFSheet choicesSheet = xssfWorkbook.getSheet("choices");

			/*
			 * Map<String, String> nameLabelMap = new HashMap<String, String>(); Map<String,
			 * Map<String, String>> listNameLabelMap = new HashMap<String, Map<String,
			 * String>>();
			 */

			for (int i = 1; i <= choicesSheet.getLastRowNum(); i++) {
				XSSFRow row = choicesSheet.getRow(i);

				if (null != row) {
					XSSFCell listNameCell = row.getCell(0);
					XSSFCell nameCell = row.getCell(1);
					XSSFCell labelCell = row.getCell(2);

					if (null != listNameCell && null != labelCell && null != nameCell) {

						ChoicesDetails choicesDetails = new ChoicesDetails();
						String nameVal = nameCell.getCellType() == Cell.CELL_TYPE_STRING ? nameCell.getStringCellValue()
								: Integer.toString(((Double) nameCell.getNumericCellValue()).intValue());

						String labelVal = labelCell.getCellType() == Cell.CELL_TYPE_STRING
								? labelCell.getStringCellValue()
								: Integer.toString(((Double) labelCell.getNumericCellValue()).intValue());

						choicesDetails.setChoicName(listNameCell.getStringCellValue());
						choicesDetails.setLabel(nameVal);
						choicesDetails.setChoiceValue(labelVal);
						choicesDetails.setForm(xform);
						choiceDetailsRepository.save(choicesDetails);

					}

				}

			}
			xssfWorkbook.close();
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sdrc.dga.service.MasterRawDataService#persistRawData()
	 */
	@Override
	@Transactional
	public boolean persistRawData() throws Exception {

		List<Area> areaDetails = areaRepository.findAll();

//		List<RawFormXapths> rawFormXapths = rawFormXapthsRepository.findAll();

		Map<String, Area> areaMap = new HashMap<String, Area>();

		for (Area area : areaDetails) {
			areaMap.put(area.getAreaCode(), area);
		}

		for (Area area : areaDetails) {
			if (area.getAreaLevel().getAreaLevelId() >= 4) {
				areaMap.put(area.getParentAreaId() + "_" + area.getAreaName(), area);
			}
		}

		List<LastVisitData> lastVisitDatas = lastVisitDataRepository.findAll();
		Map<String, LastVisitData> lastVisitDataMap = new HashMap<String, LastVisitData>();
		for (LastVisitData lastVisitData : lastVisitDatas) {
			lastVisitDataMap.put(lastVisitData.getInstanceId(), lastVisitData);

		}
		List<XForm> xforms = xFormRepository.findAllByIsCompleteFalse();

		for (XForm xform : xforms) {
			String baseUrl = xform.getOdkServerURL().concat("view/submissionList");
			String serverURL = xform.getOdkServerURL();
			String userName = xform.getUsername();
			String password = xform.getPassword();
			String submission_xml_url = xform.getOdkServerURL().concat("view/downloadSubmission");
			String base_xml_download_url = xform.getOdkServerURL().concat("formXml?formId=");
			// String xFormId = xform.getxFormId();
			String rootElement = xform.getxFormId();

			StringWriter id_list = new StringWriter();
			AggregateUtils.DocumentFetchResult result = null;
			XmlSerializer serializer = new KXmlSerializer();

			String formRooTitle = "";

			StringWriter base_xlsForm = getXML(xform.getxFormId(), serverURL, userName, password,
					base_xml_download_url);

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			Document core_xml_doc = dBuilder
					.parse(new InputSource(new ByteArrayInputStream(base_xlsForm.toString().getBytes("utf-8"))));
			if (core_xml_doc != null) {
				core_xml_doc.getDocumentElement().normalize();
				Element eElement = (Element) core_xml_doc.getElementsByTagName("group").item(0);
				formRooTitle = eElement.getAttribute("ref").split("/")[1];
			}

			Map<String, String> params = new HashMap<String, String>();
			params.put("formId", xform.getxFormId());
			params.put("cursor", "");
			params.put("numEntries", "");
			String fullUrl = WebUtils.createLinkWithProperties(baseUrl, params);

			ServerConnectionInfo serverInfo = new ServerConnectionInfo(serverURL, userName, password.toCharArray());
			DocumentDescription submissionDescription = new DocumentDescription(
					"Fetch of manifest failed. Detailed reason: ", "Fetch of manifest failed ", "form manifest",
					new TerminationFuture());
			result = AggregateUtils.getXmlDocument(fullUrl, serverInfo, false, submissionDescription, null);
			serializer.setOutput(id_list);
			result.doc.write(serializer);

			Document doc_id_list = dBuilder
					.parse(new InputSource(new ByteArrayInputStream(id_list.toString().getBytes("utf-8"))));

			if (doc_id_list != null) {
				doc_id_list.getDocumentElement().normalize();

				NodeList nodeIdList = doc_id_list.getElementsByTagName("id");
				// LocalDateTime currentDate = LocalDateTime.now();
				// LocalDateTime dbMarkedAsCompleteDateTime = null;
				// String dbMarkedAsCompleteDate = null;
				for (int node_no = 0; node_no < nodeIdList.getLength(); node_no++) {
					String instance_id = nodeIdList.item(node_no).getFirstChild().getNodeValue();
					String instance_id1 = instance_id;
					if (lastVisitDataMap.containsKey(instance_id1)
							&& lastVisitDataMap.get(instance_id1).getRawDataScore().size() == 0) {
						String link_formID = generateFormID(xform.getxFormId(), formRooTitle, instance_id);
						Map<String, String> submiteParams = new HashMap<String, String>();
						submiteParams.put("formId", link_formID);
						String full_url = WebUtils.createLinkWithProperties(submission_xml_url, submiteParams);

						serializer = new KXmlSerializer();
						StringWriter data_writer = new StringWriter();
						try {
							result = AggregateUtils.getXmlDocument(full_url, serverInfo, false, submissionDescription,
									null);
							serializer.setOutput(data_writer);
							result.doc.write(serializer);
						} catch (Exception e) {

							System.out.println(xform.getFormId() + ":-" + instance_id + ":-" + e.getMessage());
							continue;
						}

						Document submission_doc = dBuilder.parse(
								new InputSource(new ByteArrayInputStream(data_writer.toString().getBytes("utf-8"))));
						XPath xPath = XPathFactory.newInstance().newXPath();
						submission_doc.getDocumentElement().normalize();

						String markedAsCompleteDate = xPath
								.compile("/submission/data/" + rootElement + "/@markedAsCompleteDate")
								.evaluate(submission_doc);

						LastVisitData lvd = new LastVisitData();
						if (!lastVisitDataMap.containsKey(instance_id1)) {
							lvd.setMarkedAsCompleteDate(new Timestamp((sdf.parse(markedAsCompleteDate)).getTime()));
							lvd.setInstanceId(instance_id1);

							{
								for (String areaPath : xform.getAreaXPath().split(",")) {
									String areaCode = xPath.compile("/submission/data/" + rootElement + areaPath)
											.evaluate(submission_doc);
									if (areaCode != null && !areaCode.equalsIgnoreCase("")) {
										if (areaMap
												.containsKey(xPath.compile("/submission/data/" + rootElement + areaPath)
														.evaluate(submission_doc))) {
											lvd.setArea(areaMap
													.get(xPath.compile("/submission/data/" + rootElement + areaPath)
															.evaluate(submission_doc)));
										} else {
											Area area = new Area();
											String blockName = xPath.compile(
													"/submission/data/" + rootElement + xform.getSecondaryAreaXPath())
													.evaluate(submission_doc);
											Area block = areaRepository.findByAreaCode(blockName);
											List<ChoicesDetails> choiceDetails = choiceDetailsRepository
													.findByFormFormIdAndLabel(xform.getFormId(), blockName);
											area.setAreaCode(xPath.compile("/submission/data/" + rootElement + areaPath)
													.evaluate(submission_doc));
											area.setParentAreaId(block.getAreaId());
											area.setIsLive(true);
											area.setCreatedDate(new Timestamp(new java.util.Date().getTime()));
											area.setAreaName(choiceDetails.get(0).getChoiceValue());
											area.setAreaCode(choiceDetails.get(0).getLabel());
											if (choiceDetails.get(0).getLabel().contains("DH"))
												area.setAreaLevel(new AreaLevel(6));
											else if (choiceDetails.get(0).getLabel().contains("CH"))
												area.setAreaLevel(new AreaLevel(7));
											else if (choiceDetails.get(0).getLabel().contains("PH"))
												area.setAreaLevel(new AreaLevel(8));

											area = areaRepository.save(area);
											lvd.setArea(area);

										}
									}
								}
							}
							lvd.setLive(true);
							// to be uncommented and images should be set into the
							// lvds
							List<String> imagexPaths = new ArrayList<String>();
							String images = null;
							for (String imagexPath : imagexPaths) {
								String mediaFiles = xPath.compile("/submission/data/" + rootElement + "/" + imagexPath)
										.evaluate(submission_doc);
								if (mediaFiles != null && !mediaFiles.trim().equalsIgnoreCase("")) {
									if (images == null) {
										images = mediaFiles;
									} else {
										images += "," + mediaFiles;
									}
								}

							}

							if (!xPath.compile("/submission/data/" + rootElement + xform.getLocationXPath())
									.evaluate(submission_doc).trim().equalsIgnoreCase("")) {
								lvd.setLatitude(
										xPath.compile("/submission/data/" + rootElement + xform.getLocationXPath())
												.evaluate(submission_doc).split(" ")[0]);
								lvd.setLongitude(
										xPath.compile("/submission/data/" + rootElement + xform.getLocationXPath())
												.evaluate(submission_doc).split(" ")[1]);
							}
							lvd.setxForm(xform);
							SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
							if (xPath.compile("/submission/data/" + rootElement + xform.getDateOfVisitXPath())
									.evaluate(submission_doc).trim() != "")
								lvd.setDateOfVisit(new Date(dateFormat.parse(
										xPath.compile("/submission/data/" + rootElement + xform.getDateOfVisitXPath())
												.evaluate(submission_doc))
										.getTime()));
							if (images != null) {
								lvd.setImageFileNames(images);
							} else {
								lvd.setImageFileNames("");
							}
							lvd.setSubmissionFileName("");
							lvd.setSubmissionFileURL("");

							CollectUser collectUser = new CollectUser();
							collectUser.setUserId(1);
							lvd.setUser(collectUser);
							lvd.setTimPeriod(xform.getTimePeriod());
							lvd = lastVisitDataRepository.save(lvd);

						} else {
							lvd = lastVisitDataMap.get(instance_id1);
						}
						for (RawFormXapths formXapths : xform.getRawXpaths()) {
							RawDataScore rawDataScore = new RawDataScore();
							String score = null;
							String xpathPath = formXapths.getXpath();

							if (xPath.compile("/submission/data/"  + xpathPath)
									.evaluate(submission_doc) == null) {
								score = "";
							} else
								score = xPath.compile("/submission/data/"  + xpathPath)
										.evaluate(submission_doc);

//							if(xpathPath.contains("bg_3")) {
							System.out.println("/submission/data/" + rootElement + xpathPath + ":-" + score);
							System.out.println("LVD->" + lvd.getLastVisitDataId() + ", " + lvd.getInstanceId()+", score->"+score);
//							}
							rawDataScore.setLastVisitData(lvd);
							rawDataScore.setRawFormXapths(formXapths);
							rawDataScore.setScore(score);
							rawDataScoreRepository.save(rawDataScore);
						}

						lastVisitDataMap.put(instance_id, lvd);
					}
				}

			}
		}
		System.out.println("complited");
		return true;
	}

	private String generateFormID(String getxFormId, String formRooTitle, String instance_id) {

		return getxFormId + "[@version=null and @uiVersion=null]/" + formRooTitle + "" + "[@key=" + instance_id + "]";
	}

	private StringWriter getXML(String Form, String serverURL, String userName, String password,
			String base_xml_download_url) throws Exception {
		AggregateUtils.DocumentFetchResult result = null;
		XmlSerializer serializer = new KXmlSerializer();
		StringWriter base_xml = new StringWriter();

		ServerConnectionInfo serverInfo = new ServerConnectionInfo(serverURL,

				userName, password.toCharArray());

		DocumentDescription submissionDescription = new DocumentDescription(
				"Fetch of manifest failed. Detailed reason: ", "Fetch of manifest failed ", "form manifest",
				new TerminationFuture());

		result = AggregateUtils.getXmlDocument(base_xml_download_url.concat(Form), serverInfo, false,
				submissionDescription, null);
		serializer.setOutput(base_xml);
		result.doc.write(serializer);

		return base_xml;
	}

	@Override
	public boolean persistData(PostSubmissionModel postSubmissionModel) throws Exception {
		List<Area> areaDetails = areaRepository.findAll();

		List<RawFormXapths> rawFormXapths = rawFormXapthsRepository
				.findByFormFormId(postSubmissionModel.getxFormModel().getFormId());

		Map<String, Area> areaMap = new HashMap<String, Area>();

		for (Area area : areaDetails) {
			areaMap.put(area.getAreaCode(), area);
		}
		for (Area area : areaDetails) {
			if (area.getAreaLevel().getAreaLevelId() == 4) {
				areaMap.put(area.getParentAreaId() + "_" + area.getAreaName(), area);
			}
		}
		XForm xform = xFormRepository.findByFormId(postSubmissionModel.getxFormModel().getFormId());
		{
			String baseUrl = xform.getOdkServerURL().concat("view/submissionList");
			String serverURL = xform.getOdkServerURL();
			String userName = xform.getUsername();
			String password = xform.getPassword();
			String submission_xml_url = xform.getOdkServerURL().concat("view/downloadSubmission");
			String base_xml_download_url = xform.getOdkServerURL().concat("formXml?formId=");
			// String xFormId = xform.getxFormId();

			StringWriter id_list = new StringWriter();
			AggregateUtils.DocumentFetchResult result = null;
			XmlSerializer serializer = new KXmlSerializer();

			String formRooTitle = "";
			StringWriter base_xlsForm = getXML(xform.getxFormId(), serverURL, userName, password,
					base_xml_download_url);

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			Document core_xml_doc = dBuilder
					.parse(new InputSource(new ByteArrayInputStream(base_xlsForm.toString().getBytes("utf-8"))));
			if (core_xml_doc != null) {
				core_xml_doc.getDocumentElement().normalize();
				Element eElement = (Element) core_xml_doc.getElementsByTagName("group").item(0);
				formRooTitle = eElement.getAttribute("ref").split("/")[1];
			}
			Map<String, String> params = new HashMap<String, String>();
			params.put("formId", xform.getxFormId());
			params.put("cursor", "");
			params.put("numEntries", "");
			String fullUrl = WebUtils.createLinkWithProperties(baseUrl, params);

			ServerConnectionInfo serverInfo = new ServerConnectionInfo(serverURL, userName, password.toCharArray());
			DocumentDescription submissionDescription = new DocumentDescription(
					"Fetch of manifest failed. Detailed reason: ", "Fetch of manifest failed ", "form manifest",
					new TerminationFuture());
			result = AggregateUtils.getXmlDocument(fullUrl, serverInfo, false, submissionDescription, null);
			serializer.setOutput(id_list);
			result.doc.write(serializer);

			Document doc_id_list = dBuilder
					.parse(new InputSource(new ByteArrayInputStream(id_list.toString().getBytes("utf-8"))));

			if (doc_id_list != null) {
				doc_id_list.getDocumentElement().normalize();
				NodeList nodeIdList = doc_id_list.getElementsByTagName("id");
				// LocalDateTime currentDate = LocalDateTime.now();
				// LocalDateTime dbMarkedAsCompleteDateTime = null;
				// String dbMarkedAsCompleteDate = null;
//				System.out.println("nodeList lenth -------------->"+nodeIdList.getLength());
//				logger.error("nodeList lenth -------------->"+nodeIdList.getLength());
//				logger.error("instancId from Odk->"+instance_id+", id from lvd-->"+postSubmissionModel.getInstanceId());
			List<String> logList = new ArrayList<>();
				for (int node_no = 0; node_no < nodeIdList.getLength(); node_no++) {
					
					String instance_id = nodeIdList.item(node_no).getFirstChild().getNodeValue();
					System.out.println("instancId from Odk->"+instance_id+", id from lvd-->"+postSubmissionModel.getInstanceId());
//					logger.error("instancId from Odk->"+instance_id+", id from lvd-->"+postSubmissionModel.getInstanceId());
//					logList.add("instancId from Odk->"+instance_id+", id from lvd-->"+postSubmissionModel.getInstanceId());
					if (instance_id.trim().equals(postSubmissionModel.getInstanceId().trim())) {
						String link_formID = generateFormID(xform.getxFormId(), formRooTitle, instance_id);
						Map<String, String> submiteParams = new HashMap<String, String>();
						submiteParams.put("formId", link_formID);
						String full_url = WebUtils.createLinkWithProperties(submission_xml_url, submiteParams);
//						logger.error("fullUrl--->"+full_url);
						serializer = new KXmlSerializer();
						StringWriter data_writer = new StringWriter();
						result = AggregateUtils.getXmlDocument(full_url, serverInfo, false, submissionDescription,
								null);
						serializer.setOutput(data_writer);
						result.doc.write(serializer);
//logger.error("resul uptend");
						Document submission_doc = dBuilder.parse(
								new InputSource(new ByteArrayInputStream(data_writer.toString().getBytes("utf-8"))));
						XPath xPath = XPathFactory.newInstance().newXPath();
//						logger.error("xpath"+xPath);
						submission_doc.getDocumentElement().normalize();
						LastVisitData lvd = new LastVisitData();
						lvd.setLastVisitDataId(postSubmissionModel.getLastVisitDataModel().getLastVisitDataId());
						for (RawFormXapths formXapths : rawFormXapths) {
							RawDataScore rawDataScore = new RawDataScore();
							String score = null;

							if (xPath.compile("/submission/data/" 
									+ formXapths.getXpath()).evaluate(submission_doc) == null) {
								score = "";
							} else
								score = xPath.compile("/submission/data/"
										+  formXapths.getXpath())
										.evaluate(submission_doc);

							rawDataScore.setLastVisitData(lvd);
							rawDataScore.setRawFormXapths(formXapths);
							rawDataScore.setScore(score);
							RawDataScore data = rawDataScoreRepository.save(rawDataScore);
							System.out.println("rawDataScore Saved for lvd->"+lvd.getLastVisitDataId()+", path-> "+formXapths.getxPathId()+", score->"+score+", scoreId->"+data.getRawDataScoreId());
							logger.info("rawDataScore Saved for lvd->"+lvd.getLastVisitDataId()+", path-> "+formXapths.getxPathId()+", score->"+score+", scoreId->"+data.getRawDataScoreId());
						}

						return true;
					}

				}
				
			}
			return true;

		}

	}

	@Override
	public boolean updateRawDataPhase1() throws Exception {

		List<Area> areaDetails = areaRepository.findAll();

//		List<RawFormXapths> rawFormXapths = rawFormXapthsRepository.findAll();

		Map<String, Area> areaMap = new HashMap<String, Area>();

		for (Area area : areaDetails) {
			areaMap.put(area.getAreaCode(), area);
		}

		for (Area area : areaDetails) {
			if (area.getAreaLevel().getAreaLevelId() >= 4) {
				areaMap.put(area.getParentAreaId() + "_" + area.getAreaName(), area);
			}
		}

		List<LastVisitData> lastVisitDatas = lastVisitDataRepository.findByTimPeriodTimePeriodId(1);
		Map<String, LastVisitData> lastVisitDataMap = new HashMap<String, LastVisitData>();
		for (LastVisitData lastVisitData : lastVisitDatas) {
			lastVisitDataMap.put(lastVisitData.getInstanceId(), lastVisitData);

		}
		List<XForm> xforms = xFormRepository.findAllByIsLiveTrue();

		for (XForm xform : xforms) {
			String baseUrl = xform.getOdkServerURL().concat("view/submissionList");
			String serverURL = xform.getOdkServerURL();
			String userName = xform.getUsername();
			String password = xform.getPassword();
			String submission_xml_url = xform.getOdkServerURL().concat("view/downloadSubmission");
			String base_xml_download_url = xform.getOdkServerURL().concat("formXml?formId=");
			// String xFormId = xform.getxFormId();
			String rootElement = xform.getxFormId();

			StringWriter id_list = new StringWriter();
			AggregateUtils.DocumentFetchResult result = null;
			XmlSerializer serializer = new KXmlSerializer();

			String formRooTitle = "";

			StringWriter base_xlsForm = getXML(xform.getxFormId(), serverURL, userName, password,
					base_xml_download_url);

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			Document core_xml_doc = dBuilder
					.parse(new InputSource(new ByteArrayInputStream(base_xlsForm.toString().getBytes("utf-8"))));
			if (core_xml_doc != null) {
				core_xml_doc.getDocumentElement().normalize();
				Element eElement = (Element) core_xml_doc.getElementsByTagName("group").item(0);
				formRooTitle = eElement.getAttribute("ref").split("/")[1];
			}

			Map<String, String> params = new HashMap<String, String>();
			params.put("formId", xform.getxFormId());
			params.put("cursor", "");
			params.put("numEntries", "");
			String fullUrl = WebUtils.createLinkWithProperties(baseUrl, params);

			ServerConnectionInfo serverInfo = new ServerConnectionInfo(serverURL, userName, password.toCharArray());
			DocumentDescription submissionDescription = new DocumentDescription(
					"Fetch of manifest failed. Detailed reason: ", "Fetch of manifest failed ", "form manifest",
					new TerminationFuture());
			result = AggregateUtils.getXmlDocument(fullUrl, serverInfo, false, submissionDescription, null);
			serializer.setOutput(id_list);
			result.doc.write(serializer);

			Document doc_id_list = dBuilder
					.parse(new InputSource(new ByteArrayInputStream(id_list.toString().getBytes("utf-8"))));

			if (doc_id_list != null) {
				doc_id_list.getDocumentElement().normalize();

				NodeList nodeIdList = doc_id_list.getElementsByTagName("id");
				// LocalDateTime currentDate = LocalDateTime.now();
				// LocalDateTime dbMarkedAsCompleteDateTime = null;
				// String dbMarkedAsCompleteDate = null;
				for (int node_no = 0; node_no < nodeIdList.getLength(); node_no++) {
					String instance_id = nodeIdList.item(node_no).getFirstChild().getNodeValue();
					String instance_id1 = instance_id;
					instance_id1 = instance_id1.replace(":", "");
					if (lastVisitDataMap.containsKey(instance_id1)
							&& lastVisitDataMap.get(instance_id1).getRawDataScore().size() == 0) {
						String link_formID = generateFormID(xform.getxFormId(), formRooTitle, instance_id);
						Map<String, String> submiteParams = new HashMap<String, String>();
						submiteParams.put("formId", link_formID);
						String full_url = WebUtils.createLinkWithProperties(submission_xml_url, submiteParams);

						serializer = new KXmlSerializer();
						StringWriter data_writer = new StringWriter();
						result = AggregateUtils.getXmlDocument(full_url, serverInfo, false, submissionDescription,
								null);
						serializer.setOutput(data_writer);
						result.doc.write(serializer);

						Document submission_doc = dBuilder.parse(
								new InputSource(new ByteArrayInputStream(data_writer.toString().getBytes("utf-8"))));
						XPath xPath = XPathFactory.newInstance().newXPath();
						submission_doc.getDocumentElement().normalize();

						String markedAsCompleteDate = xPath
								.compile("/submission/data/" + rootElement + "/@markedAsCompleteDate")
								.evaluate(submission_doc);

						LastVisitData lvd = new LastVisitData();
						if (!lastVisitDataMap.containsKey(instance_id1)) {
							lvd.setMarkedAsCompleteDate(new Timestamp((sdf.parse(markedAsCompleteDate)).getTime()));
							lvd.setInstanceId(instance_id1);

							{
								for (String areaPath : xform.getAreaXPath().split(",")) {
									String areaCode = xPath.compile("/submission/data/" + rootElement + areaPath)
											.evaluate(submission_doc);
									System.out.println("path---->"+"/submission/data/" + rootElement + areaPath+"areaCode---->"+areaCode);
									logger.info("path---->"+"/submission/data/" + rootElement + areaPath+"areaCode---->"+areaCode);
									if (areaCode != null && !areaCode.equalsIgnoreCase("")) {
										if (areaMap
												.containsKey(xPath.compile("/submission/data/" + rootElement + areaPath)
														.evaluate(submission_doc))) {
											lvd.setArea(areaMap
													.get(xPath.compile("/submission/data/" + rootElement + areaPath)
															.evaluate(submission_doc)));
										} else {
											Area area = new Area();
											String blockName = xPath.compile(
													"/submission/data/" + rootElement + xform.getSecondaryAreaXPath())
													.evaluate(submission_doc);
											Area block = areaRepository.findByAreaCode(blockName);
											List<ChoicesDetails> choiceDetails = choiceDetailsRepository
													.findByFormFormIdAndLabel(xform.getFormId(), blockName);
											area.setAreaCode(xPath.compile("/submission/data/" + rootElement + areaPath)
													.evaluate(submission_doc));
											area.setParentAreaId(block.getAreaId());
											area.setIsLive(true);
											area.setCreatedDate(new Timestamp(new java.util.Date().getTime()));
											area.setAreaName(choiceDetails.get(0).getChoiceValue());
											area.setAreaCode(choiceDetails.get(0).getLabel());
											if (choiceDetails.get(0).getLabel().contains("DH"))
												area.setAreaLevel(new AreaLevel(6));
											else if (choiceDetails.get(0).getLabel().contains("CH"))
												area.setAreaLevel(new AreaLevel(7));
											else if (choiceDetails.get(0).getLabel().contains("PH"))
												area.setAreaLevel(new AreaLevel(8));

											area = areaRepository.save(area);
											lvd.setArea(area);

										}
									}
								}
							}
							lvd.setLive(true);
							// to be uncommented and images should be set into the
							// lvds
							List<String> imagexPaths = new ArrayList<String>();
							String images = null;
							for (String imagexPath : imagexPaths) {
								String mediaFiles = xPath.compile("/submission/data/" + rootElement + "/" + imagexPath)
										.evaluate(submission_doc);
								if (mediaFiles != null && !mediaFiles.trim().equalsIgnoreCase("")) {
									if (images == null) {
										images = mediaFiles;
									} else {
										images += "," + mediaFiles;
									}
								}

							}

							if (!xPath.compile("/submission/data/" + rootElement + xform.getLocationXPath())
									.evaluate(submission_doc).trim().equalsIgnoreCase("")) {
								lvd.setLatitude(
										xPath.compile("/submission/data/" + rootElement + xform.getLocationXPath())
												.evaluate(submission_doc).split(" ")[0]);
								lvd.setLongitude(
										xPath.compile("/submission/data/" + rootElement + xform.getLocationXPath())
												.evaluate(submission_doc).split(" ")[1]);
							}
							lvd.setxForm(xform);
							SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
							if (xPath.compile("/submission/data/" + rootElement + xform.getDateOfVisitXPath())
									.evaluate(submission_doc).trim() != "")
								lvd.setDateOfVisit(new Date(dateFormat.parse(
										xPath.compile("/submission/data/" + rootElement + xform.getDateOfVisitXPath())
												.evaluate(submission_doc))
										.getTime()));
							if (images != null) {
								lvd.setImageFileNames(images);
							} else {
								lvd.setImageFileNames("");
							}
							lvd.setSubmissionFileName("");
							lvd.setSubmissionFileURL("");
							TimePeriod timePeriod = new TimePeriod();
							timePeriod.setTimePeriodId(1);
							CollectUser collectUser = new CollectUser();
							collectUser.setUserId(1);
							lvd.setUser(collectUser);
							lvd.setTimPeriod(timePeriod);
							lvd = lastVisitDataRepository.save(lvd);

						} else {
							lvd = lastVisitDataMap.get(instance_id1);
						}
						for (RawFormXapths formXapths : xform.getRawXpaths()) {
							RawDataScore rawDataScore = new RawDataScore();
							String score = null;
							String xpathPath = formXapths.getXpath();
							if (xpathPath.contains("DGA_CHC_Format_171117_V2")) {
								xpathPath = xpathPath.replace("DGA_CHC_Format_171117_V2", xform.getxFormId());
							} else if (xpathPath.contains("DGA_DH_Format_171117_V2")) {
								xpathPath = xpathPath.replace("DGA_DH_Format_171117_V2", xform.getxFormId());
							} else if (xpathPath.contains("DGA_PHC_Format_171117_V2")) {
								xpathPath = xpathPath.replace("DGA_PHC_Format_171117_V2", xform.getxFormId());
							} else if (xpathPath.contains("Fire_and_Electrical_Assessment_Checklist_171117_V2")) {
								xpathPath = xpathPath.replace("Fire_and_Electrical_Assessment_Checklist_171117_V2",
										xform.getxFormId());
							}

							if (xPath.compile(xpathPath).evaluate(submission_doc) == null) {
								score = "";
							} else
								score = xPath.compile(xpathPath).evaluate(submission_doc);

							rawDataScore.setLastVisitData(lvd);
							rawDataScore.setRawFormXapths(formXapths);
							rawDataScore.setScore(score);
							rawDataScoreRepository.save(rawDataScore);
						}

						lastVisitDataMap.put(instance_id, lvd);
					}
				}

			}
		}
		return true;

	}

	@Override
	@Transactional
	public String getAllFormXpathScoreMappingUptoQuestionLevel() throws Exception {
		List<XForm> xfroms = xFormRepository.findAllByIsLiveTrue();

		int maxId = formXpathScoreMappingRepository.findLastId();
		for (XForm xform : xfroms) {
			Map<String, FormXpathScoreMapping> formMap = new LinkedHashMap<String, FormXpathScoreMapping>();
			List<FormXpathScoreMapping> formXpathScoreMappings = formXpathScoreMappingRepository
					.findByFormIdAndWithNoChildren(xform.getFormId());
			for (FormXpathScoreMapping formXpathScoreMapping : formXpathScoreMappings) {
				formMap.put(formXpathScoreMapping.getxPath().split(",")[0], formXpathScoreMapping);
			}
			List<String> xpathTypes = new ArrayList<String>();
			xpathTypes.add("select_one yes_no");
			xpathTypes.add("integer");
			List<RawFormXapths> rawFormXapths = rawFormXapthsRepository
					.findByFormFormIdAndTypeIgnoreCaseIn(xform.getFormId(), xpathTypes);

			for (RawFormXapths rawXapth : rawFormXapths) {
				String excelXpath = rawXapth.getXpath().replace("/submission/data/" + xform.getxFormId() + "/", "");
				for (String key : formMap.keySet()) {
					String modifiedKey = key.replace(key.split("/")[key.split("/").length - 1], "");
					if (excelXpath.startsWith(modifiedKey) && !formMap.containsKey(excelXpath)) {

						FormXpathScoreMapping formXpathScoreMapping = new FormXpathScoreMapping();

						formXpathScoreMapping.setFormXpathScoreId(++maxId);
						formXpathScoreMapping.setForm(xform);
						formXpathScoreMapping.setLabel(rawXapth.getLabel());
						formXpathScoreMapping.setParentXpathId(formMap.get(key).getFormXpathScoreId());
						formXpathScoreMapping.setType(rawXapth.getType());
						formXpathScoreMapping.setxPath(excelXpath);

						FormXpathScoreMapping formXpath = formXpathScoreMappingRepository.save(formXpathScoreMapping);
						formMap.put(formXpath.getxPath(), formXpath);

						break;
					}
				}
			}

		}

		return "D://Xpaths.xslx";
	}

	@Override
	public boolean insertCrossTabIndicatorXpath() throws Exception {

		FileInputStream fis = new FileInputStream("C:/Users/SDRC_DEV/Desktop/DgaIndia/DGA_Cross_Tab_r3_23092019.xlsx");

		List<RawFormXapths> rawFormXapths = rawFormXapthsRepository.findAll();

		Map<String, RawFormXapths> rawFormXapthMap = new LinkedHashMap<String, RawFormXapths>();
		for (RawFormXapths rawFormXapth : rawFormXapths) {
			rawFormXapthMap.put(
					rawFormXapth.getForm().getXform_meta_id() + "_"
							+ rawFormXapth.getXpath().split("/")[rawFormXapth.getXpath().split("/").length - 1],
					rawFormXapth);
		}
		System.out.println(rawFormXapthMap);
		XSSFWorkbook xssfWorkbook = new XSSFWorkbook(fis);

		XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(0);
		List<IndicatorFormXpathMapping> indicatorFormXpathMappings = new ArrayList<IndicatorFormXpathMapping>();
		for (int i = 1; i < xssfSheet.getLastRowNum(); i++) {
//			System.out.println("Sheet Reading Started");
			Row row = xssfSheet.getRow(i);

			if (row == null)
				break;

			Cell sectorCell = row.getCell(1);
			Cell subSectorCell = row.getCell(2);
			Cell labelCell = row.getCell(3);
			Cell subgroupCell = row.getCell(4);
			Cell chcCell = row.getCell(7);
			Cell dhCell = row.getCell(6);
			Cell phcCell = row.getCell(8);
			Cell hscCell = row.getCell(5);
			Cell typeCell = row.getCell(0);
			if (!labelCell.getStringCellValue().trim().equalsIgnoreCase("")) {

				IndicatorFormXpathMapping indicatorFormXpathMapping = new IndicatorFormXpathMapping();

				indicatorFormXpathMapping.setLabel(labelCell.getStringCellValue());

				if (chcCell != null && chcCell.getCellType() != Cell.CELL_TYPE_BLANK)
					indicatorFormXpathMapping
							.setChcXpath(rawFormXapthMap.get(1 + "_" + chcCell.getStringCellValue()).getXpath());

				if (dhCell != null && dhCell.getCellType() != Cell.CELL_TYPE_BLANK)
					indicatorFormXpathMapping
							.setDhXpath(rawFormXapthMap.get(2 + "_" + dhCell.getStringCellValue()).getXpath());

				if (phcCell != null && phcCell.getCellType() != Cell.CELL_TYPE_BLANK)
					indicatorFormXpathMapping
							.setPhcXpath(rawFormXapthMap.get(3 + "_" + phcCell.getStringCellValue()).getXpath());

			if(hscCell!=null && hscCell.getCellType()!=Cell.CELL_TYPE_BLANK)
				indicatorFormXpathMapping.setHscXpath(rawFormXapthMap.get(5+"_"+hscCell.getStringCellValue()).getXpath());

				indicatorFormXpathMapping.setSector(sectorCell.getStringCellValue());

				if (subSectorCell != null && subSectorCell.getCellType() != Cell.CELL_TYPE_BLANK)
					indicatorFormXpathMapping.setSubSector(subSectorCell.getStringCellValue());

				indicatorFormXpathMapping.setSubGroup(subgroupCell.getStringCellValue());

				indicatorFormXpathMapping.setType(typeCell.getStringCellValue());

				indicatorFormXpathMappings.add(indicatorFormXpathMapping);
				System.out.println(indicatorFormXpathMapping.getChcXpath());
			}
		}

		indicatorFormXpathMappingRepository.save(indicatorFormXpathMappings);
		xssfWorkbook.close();
		return false;
	}

	@Override
	public boolean createFoldersOfImages() {

		List<XForm> xforms = xFormRepository.findAllByIsLiveTrue();
		File mainDirectory = new File("C://ODKAggregateSubmissionForms//Forms");
		if (!mainDirectory.exists()) {
			mainDirectory.mkdir();
		}

		for (XForm xform : xforms) {
			File formDirectory = new File(mainDirectory.getAbsolutePath() + "//" + xform.getxFormId());
			if (!formDirectory.exists()) {
				formDirectory.mkdir();
			}

			File definationfile = new File(formDirectory.getAbsolutePath() + "//definationfile");
			if (!definationfile.exists()) {
				definationfile.mkdir();
			}

			File submissionfile = new File(formDirectory.getAbsolutePath() + "//submissionfile");
			if (!submissionfile.exists()) {
				submissionfile.mkdir();
			}

		}

//		List<Area> areas=areaRepository.findByAreaLevelAreaLevelId(4);
//		areas.addAll(areaRepository.findByAreaLevelAreaLevelId(5));
//		Map<Integer,Area> areaMap=new HashMap<Integer,Area>();
//		
//		for(Area area:areas)
//		{
//			areaMap.put(area.getAreaId(), area);
//		}
//		Set<Integer> ids=new HashSet<Integer>();
//		ids.add(Constants.CHC_ID);
//		ids.add(Constants.PHC_ID);
//		ids.add(Constants.DH_ID);
//		List<LastVisitData> lastVisitDatas=lastVisitDataRepository.findByIsLiveTrueAndxFormFormIdIn(ids);
//		
//		for (LastVisitData lastVisitData : lastVisitDatas)
//		{
//			File dir = new File("D://DGA V2//ODK Briefcase Storage//forms//"+lastVisitData.getxForm().getxFormIdTitle()+"//instances//"+lastVisitData.getInstanceId().replace(":", ""));
//			if(!dir.exists())
//			{
//			
//				continue;
//			}
//			
//			// to be removed *** Added for ripon sir
////			if(lastVisitData.getTimPeriod().getTimePeriodId()==1)
////			{
////				continue;
////			}
//			
//			String folder="";
//			String districtName="";
//			int signatureId=0;
//			switch(lastVisitData.getxForm().getFormId())
//			{
//			case Constants.DH_ID: folder="DH";
//				districtName=areaMap.get(lastVisitData.getArea().getParentAreaId()).getAreaName();
//				signatureId=2267;
//				break;
//			case Constants.CHC_ID: folder="CHC";
//			districtName=areaMap.get(areaMap.get(lastVisitData.getArea().getParentAreaId()).getParentAreaId()).getAreaName();
//			signatureId=1156;
//			break;
//			case Constants.PHC_ID: folder="PHC";
//			districtName=areaMap.get(areaMap.get(lastVisitData.getArea().getParentAreaId()).getParentAreaId()).getAreaName();
//			signatureId=3091;
//			break;
//			}
//			
//		RawDataScore signatureScore=rawDataScoreRepository.findByLastVisitDataLastVisitDataIdAndRawFormXapthsXPathId(lastVisitData.getLastVisitDataId(),signatureId);
//			File mainDirectory=new File("D://DGA V2//images");
//			if(!mainDirectory.exists())
//			{
//				mainDirectory.mkdir();
//			}
//			
//			File fromMainDirectory=new File(mainDirectory.getAbsolutePath()+"//"+folder.trim());
//			if(!fromMainDirectory.exists())
//			{
//				fromMainDirectory.mkdir();
//			}
//			
//			File districtFolderName=new File(fromMainDirectory.getAbsolutePath()+"//"+districtName.trim());
//			if(!districtFolderName.exists())
//			{
//				districtFolderName.mkdir();
//			}
//			
//			File timeFolderName = new File(districtFolderName.getAbsolutePath()+"//"+lastVisitData.getTimPeriod().getTimeperiod().trim());
//			if(!timeFolderName.exists())
//			{
//				if(timeFolderName.mkdir());
//			}
//			
////			File facilityFolderName = new File(timeFolderName.getAbsolutePath()+"//"+lastVisitData.getArea().getAreaName().trim());
////			if(!facilityFolderName.exists())
////			{
////				if(facilityFolderName.mkdir());
////			}
//			for(File files:dir.listFiles())
//			{
//				BufferedImage img = null;
//
//                try {
//                    img = ImageIO.read(files);
//                    if(img==null || signatureScore.getScore().equals(files.getName()))
//                    {
//                    	continue;
//                    }
//                    File file =new File(timeFolderName.getAbsolutePath()+"//"+lastVisitData.getArea().getAreaName()+"_"+files.getName());
//                    ImageIO.write(img, "jpg", file);
////                    System.out.println(file.getAbsolutePath()+" SIZE "+files.getUsableSpace());
//                } catch (Exception e) {
//                   e.printStackTrace();
//                }
//			}
//		}
//		

		return true;
	}

	@Override
	@Transactional
	public boolean updateLatitudeLogitudeOfSubmission() {

		List<LastVisitData> lastVisitDatas = lastVisitDataRepository.findByLatitudeIsNullAndIsLiveTrue();

		for (LastVisitData lastVisitData : lastVisitDatas) {
			LastVisitData lasVisitDataOfLastTime = lastVisitDataRepository
					.findByAreaAreaIdAndTimPeriodTimePeriodIdAndIsLiveTrueAndLatitudeIsNotNull(
							lastVisitData.getArea().getAreaId(), 1);
			if (lasVisitDataOfLastTime != null) {
				lastVisitData.setLatitude(lasVisitDataOfLastTime.getLatitude());
				lastVisitData.setLongitude(lasVisitDataOfLastTime.getLongitude());
				lastVisitDataRepository.save(lastVisitData);
			}
		}
		return true;
	}

	@Override
	@Transactional
	public boolean updateArea() throws Exception {

		String inputFilePath = "C:\\Users\\SDRC_DEV\\Documents\\sdrc\\uphc-mh\\lagacy-data\\areaTamplate_r4.xlsx";
		FileInputStream fileInputStream = new FileInputStream(inputFilePath);
		XSSFWorkbook xssfWorkbook = new XSSFWorkbook(fileInputStream);

		XSSFSheet sheet = xssfWorkbook.getSheet("Sheet1");
		List<Area> areas = areaRepository.findAll();
		Map<String, Area> areaMap = new HashMap<String, Area>();

		for (Area area : areas) {
			areaMap.put(area.getAreaCode(), area);
		}
		for (int i = 1; i <= sheet.getLastRowNum(); i++) {
			Row row = sheet.getRow(i);

			Cell areaCode = row.getCell(1);
			Cell areaName = row.getCell(2);
			Cell parentCode = row.getCell(3);
			Cell areaLevel = row.getCell(4);
			
			System.out.println("from file ->"+areaCode + "-" + areaName);
			if (!areaMap.containsKey(areaCode.getStringCellValue())) {
				Area area = new Area();
				area.setAreaCode(areaCode.getStringCellValue());
				area.setAreaName(areaName.getStringCellValue());
				area.setParentAreaId(areaMap.get(parentCode.getStringCellValue()).getAreaId());
				area.setIsLive(true);
				area.setAreaLevel(new AreaLevel(Double.valueOf(areaLevel.getNumericCellValue()).intValue()));
				Area savedArea = areaRepository.save(area);
				System.out.println(area.getAreaCode() + "-" + area.getAreaName());
				areaMap.put(savedArea.getAreaCode(), savedArea);
			}
		}
		xssfWorkbook.close();
		return true;

	}

	@Override
	@Transactional
	public boolean updateRawXpaths() throws Exception {
		List<RawFormXapths> rawFormXpathsList = rawFormXapthsRepository.findAll();

		for (RawFormXapths rawFormXapths : rawFormXpathsList) {
			rawFormXapths.setXpath(
					rawFormXapths.getXpath().replace("/submission/data/" + rawFormXapths.getForm().getxFormId(), ""));
		}

		return true;
	}

	public boolean updateFacilityPlanning() throws Exception {
		int timePeriodId = 3;
		List<Area> areaList = areaRepository.findAll();
		List<XForm> xFormList = xFormRepository.findByIsLiveTrueAndTimePeriodTimePeriodId(timePeriodId);
		Map<String, XForm> xformMap = xFormList.stream().collect(Collectors.toMap(x -> x.getxFormIdTitle(), x -> x));
		Map<String, Area> areaMap = areaList.stream().collect(Collectors.toMap(x -> x.getAreaName(), x -> x));

		try {
			FileInputStream file = new FileInputStream(
					"F:\\DGA-2018\\Excel Sheet\\DGA_karnataka\\Excel Template\\userList.xlsx");
			XSSFWorkbook wb = new XSSFWorkbook(file);
			XSSFSheet sheet = wb.getSheetAt(0);
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				System.out.println("row->" + i);
				for (int j = 1; j <= row.getLastCellNum(); j++) {
					System.out.println("cell->" + j);
					switch (j) {

					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

//	@Override
//	public boolean generateIndicatorXpathMapping() throws Exception {
//		
//		String xpath="",metaIds="";
//		List<IndicatorFormXpathMapping> oldIndicators = indicatorFormXpathMappingRepository.findAll();
//		for(IndicatorFormXpathMapping oldIndicator:oldIndicators) {
//			IndicatorFormXpathMappingTbl indicatorFormXpathMappingTbl = new IndicatorFormXpathMappingTbl();
//			metaIds="";
//			xpath="";
//			indicatorFormXpathMappingTbl.setLabel(oldIndicator.getLabel());
//			indicatorFormXpathMappingTbl.setSector(oldIndicator.getSector());
//			indicatorFormXpathMappingTbl.setSubGroup(oldIndicator.getSubGroup());
//			indicatorFormXpathMappingTbl.setType(oldIndicator.getType());
//			indicatorFormXpathMappingTbl.setSubSector(oldIndicator.getSubSector());
//			if(oldIndicator.getChcXpath() != null) {
//				xpath= oldIndicator.getChcXpath();
//				metaIds = "1";
////				indicatorFormXpathMappingTbl.setXpath(oldIndicator.getChcXpath());
////				indicatorFormXpathMappingTbl.setXformMetaId(1);
//			}else if(oldIndicator.getDhXpath() != null) {
//				if(!xpath.isEmpty())
//					xpath= xpath+",";
//				if(!metaIds.isEmpty())
//					metaIds= metaIds+",";
//				xpath = xpath+oldIndicator.getDhXpath();
//				
//				metaIds = metaIds+"2";
////				indicatorFormXpathMappingTbl.setXpath(oldIndicator.getDhXpath());
////				indicatorFormXpathMappingTbl.setXformMetaId(2);
//			}else if(oldIndicator.getPhcXpath() != null) {
//				if(!xpath.isEmpty())
//					xpath= xpath+",";
//				if(!metaIds.isEmpty())
//					metaIds= metaIds+",";
//				xpath = xpath+oldIndicator.getPhcXpath();
//				metaIds = metaIds+"3";
////				indicatorFormXpathMappingTbl.setXpath(oldIndicator.getPhcXpath());
////				indicatorFormXpathMappingTbl.setXformMetaId(3);
//			}
//			indicatorFormXpathMappingTbl.setXpath(xpath);
//			indicatorFormXpathMappingTbl.setXformMetaId(metaIds);
//			
//			indicatorFormXpathMappingTbl=indicatorFormXpathMappingTblRepository.save(indicatorFormXpathMappingTbl);
//			System.out.println(indicatorFormXpathMappingTbl.getIndicatorFormXpathMappingId());
//			
//		}
//		
//		return true;
//	}
	@Override
	public boolean updateXpathIdOnRawDataScore() throws Exception
	{

		List<Object[]> datasList = new ArrayList<Object[]>();
		List<Integer> timePeriods = new ArrayList<Integer>();
		timePeriods.add(1);
		timePeriods.add(2);
		
//		datasList = rawDataScoreRepository.findRawDataAndXpathId(timePeriods);
//		List<RawDataScore> rawDataScore = rawDataScoreRepository.findByTimePeriods(timePeriods);
		
		Map<Integer,Object> datasMap = new HashMap<Integer,Object> ();
		Map<Integer,RawDataScore> rawDaataMap = new HashMap<Integer,RawDataScore>();
		
//		datasList.stream()
		
		return true;
	}
	
	@Override
	@Transactional
	public boolean updateFormXpathScoreMapping() throws Exception
	{
		String inputFilePath = "C:\\Users\\SDRC_DEV\\Desktop\\test2.xlsx";
		FileInputStream fileInputStream = new FileInputStream(inputFilePath);
		XSSFWorkbook xssfWorkbook = new XSSFWorkbook(fileInputStream);
		List<FormXpathScoreMapping> formScores = formXpathScoreMappingRepository.findAll();
		
		Map<Integer,FormXpathScoreMapping> formMap = new HashMap<Integer,FormXpathScoreMapping>();
		for(FormXpathScoreMapping formScore: formScores) {
			formMap.put(formScore.getFormXpathScoreId(), formScore);
		}
		List<FormXpathScoreMapping> updatedList = new ArrayList<FormXpathScoreMapping>();
		
		XSSFSheet sheet = xssfWorkbook.getSheet("Sheet1");
		for (int i = 0; i <= sheet.getLastRowNum(); i++) {
			Row row = sheet.getRow(i);
			
			Cell id = row.getCell(0);
			Cell parentId = row.getCell(3);
			Cell xpath = row.getCell(5);
			FormXpathScoreMapping newFormXpath = new FormXpathScoreMapping();
			if(formMap.containsKey((int)Double.parseDouble(id.toString()))) {
				newFormXpath = formMap.get((int)Double.parseDouble(id.toString()));
				newFormXpath.setParentXpathId((int)Double.parseDouble(parentId.toString()));
				newFormXpath.setxPath(xpath.toString());
				updatedList.add(newFormXpath);
			}
			
		}
		
		for(FormXpathScoreMapping form: updatedList) {
			formXpathScoreMappingRepository.save(form);
		}

		return true;
	}

	@Override
	public boolean updateAspirationalArea(List<Integer> areaIds) throws Exception {
		List<Area> areas = areaRepository.findFacilityAndDistrict(areaIds);
		areas.addAll(areaRepository.findByAreaLevelAreaLevelIdAndParentAreaIdIn(6, areaIds));
		areas.addAll(areaRepository.findByAreaIdIn(areaIds));
		
		for(Area area: areas) {
			area.setAspirational(true);
			areaRepository.save(area);
		}
		
//		System.out.println(areas);
		return true;
	}

	@Override
	public boolean updateAreaLevel() throws Exception {
		String inputFilePath = "C:\\Users\\Lulu\\Documents\\sdrc\\dga\\excels\\areaCheck_r1.xlsx";
		FileInputStream fileInputStream = new FileInputStream(inputFilePath);
		XSSFWorkbook xssfWorkbook = new XSSFWorkbook(fileInputStream);

		XSSFSheet sheet = xssfWorkbook.getSheet("Sheet1");
		List<Area> areas = areaRepository.findAll();
		Map<Integer, Area> areaMap = new HashMap<Integer, Area>();

		for (Area area : areas) {
			areaMap.put(area.getAreaId(), area);
		}
		for (int i = 1; i <= sheet.getLastRowNum(); i++) {
			Row row = sheet.getRow(i);

			Cell areaId = row.getCell(0);
			Cell areaCode = row.getCell(1);
			Cell areaName = row.getCell(2);
			Cell parentCode = row.getCell(3);
			Cell areaLevel = row.getCell(5);
			if (areaMap.containsKey((int)areaId.getNumericCellValue())) {
				Area area = areaMap.get((int)areaId.getNumericCellValue());
				area.setAreaLevel(new AreaLevel(Double.valueOf(areaLevel.getNumericCellValue()).intValue()));
				Area savedArea = areaRepository.save(area);
				System.out.println(area.getAreaId() + "-" + area.getAreaLevel().getAreaLevelId());
//				areaMap.put(savedArea.getAreaCode(), savedArea);
			}
		}
		xssfWorkbook.close();
		return true;
	}

}
