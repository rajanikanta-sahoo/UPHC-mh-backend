
package org.sdrc.dga.service;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
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
import org.sdrc.dga.domain.FacilityScore;
import org.sdrc.dga.domain.FormXpathScoreMapping;
import org.sdrc.dga.domain.LastVisitData;
import org.sdrc.dga.domain.TimePeriod;
import org.sdrc.dga.domain.XForm;
import org.sdrc.dga.repository.AreaRepository;
import org.sdrc.dga.repository.ChoiceDetailsRepository;
import org.sdrc.dga.repository.FacilityScoreRepository;
import org.sdrc.dga.repository.FormXpathScoreMappingRepository;
import org.sdrc.dga.repository.LastVisitDataRepository;
import org.sdrc.dga.repository.TimePeriodRepository;
import org.sdrc.dga.repository.XFormRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlSerializer;

/**
 * @author Harsh Pratyush
 *
 */
@Service
public class ODKServiceImpl implements ODKService {

	@Autowired
	AreaRepository areaDetailsRepository;

	@Autowired
	LastVisitDataRepository lastVisitDataRepository;

	@Autowired
	FacilityScoreRepository facilityScoreRepository;

	@Autowired
	XFormRepository xFormRepository;

	@Autowired
	private ChoiceDetailsRepository choiceDetailsRepository;

	@Autowired
	private FormXpathScoreMappingRepository formXpathScoreMappingRepository;

	@Autowired
	private TimePeriodRepository timePeriodRepository;

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sdrc.slr.service.ODKService#updateFacilityScore()
	 */
	@Override
	@Transactional
	public boolean updateFacilityScore() throws Exception {

		try {
			System.out.println("Starts");
			List<Area> areaDetails = areaDetailsRepository.findAll();

			Map<String, Area> areaMap = new HashMap<String, Area>();

			for (Area area : areaDetails) {
				areaMap.put(area.getAreaCode(), area);
			}

			for (Area area : areaDetails) {
				if (area.getAreaLevel().getAreaLevelId() == 4) {
					areaMap.put(area.getParentAreaId() + "_" + area.getAreaName(), area);
				}
			}

			Map<String, Area> areaMap2 = areaDetailsRepository.findAll().stream()
					.collect(Collectors.toMap(Area::getAreaName, a -> a));

			List<LastVisitData> lastVisitDatas = lastVisitDataRepository.findAll();
			Map<String, LastVisitData> lastVisitDataMap = new HashMap<String, LastVisitData>();
			for (LastVisitData lastVisitData : lastVisitDatas) {
				lastVisitDataMap.put(lastVisitData.getInstanceId(), lastVisitData);

			}
			List<XForm> xfroms = xFormRepository.findAllByIsCompleteFalse();

			for (XForm xform : xfroms) {
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
						String instance_id = nodeIdList.item(node_no).getFirstChild().getNodeValue().trim();

						if (!lastVisitDataMap.containsKey(instance_id)
								|| lastVisitDataMap.get(instance_id).getFacilityScores() == null
								|| lastVisitDataMap.get(instance_id).getFacilityScores().size() < 1
								|| lastVisitDataMap.get(instance_id).getFacilityScores().isEmpty()) {
							String link_formID = generateFormID(xform.getxFormId(), formRooTitle, instance_id);
							Map<String, String> submiteParams = new HashMap<String, String>();
							submiteParams.put("formId", link_formID);
							String full_url = WebUtils.createLinkWithProperties(submission_xml_url, submiteParams);

							serializer = new KXmlSerializer();
							StringWriter data_writer = new StringWriter();
							try {
								result = AggregateUtils.getXmlDocument(full_url, serverInfo, false,
										submissionDescription, null);
								serializer.setOutput(data_writer);
								result.doc.write(serializer);
							} catch (Exception e) {

								System.out.println(xform.getFormId() + ":-" + instance_id + ":-" + e.getMessage());
								continue;
							}

							Document submission_doc = dBuilder.parse(new InputSource(
									new ByteArrayInputStream(data_writer.toString().getBytes("utf-8"))));
							XPath xPath = XPathFactory.newInstance().newXPath();
							submission_doc.getDocumentElement().normalize();

							String markedAsCompleteDate = xPath.compile("/submission/data/" + "/@markedAsCompleteDate")
									.evaluate(submission_doc);

							LastVisitData lvd = new LastVisitData();
							if (!lastVisitDataMap.containsKey(instance_id)) {
								lvd.setMarkedAsCompleteDate(new Timestamp((sdf.parse(markedAsCompleteDate)).getTime()));
								lvd.setInstanceId(instance_id);
								String areaCode = null;
								for (String areaPath : xform.getAreaXPath().split(",")) {
									if (!xPath.compile("/submission/data/" + areaPath).evaluate(submission_doc).trim()
											.equalsIgnoreCase("")) {
										areaCode = xPath.compile("/submission/data/" + areaPath)
												.evaluate(submission_doc);
//areaCode = areaMap2.get(areaCode).getAreaCode();
										if (areaMap.containsKey(xPath.compile("/submission/data/" + areaPath)
												.evaluate(submission_doc))) {

											lvd.setArea(areaMap.get(areaCode));
										} else {
											Area area = new Area();

											String blockName = xPath
													.compile("/submission/data/" + xform.getSecondaryAreaXPath())
													.evaluate(submission_doc);
											Area block = areaDetailsRepository.findByAreaCode(blockName);
											List<ChoicesDetails> choiceDetails = choiceDetailsRepository
													.findByFormFormIdAndLabel(xform.getFormId(), areaCode);
											area.setAreaCode(areaCode);
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

											area = areaDetailsRepository.save(area);
											areaMap.put(area.getAreaCode(), area);

											lvd.setArea(area);
										}
										break;
									}
								}

								lvd.setLive(true);
								// to be uncommented and images should be set into
								// the
								// lvds

								if (!xPath.compile("/submission/data/" + xform.getLocationXPath())
										.evaluate(submission_doc).trim().equalsIgnoreCase("")) {
									lvd.setLatitude(xPath.compile("/submission/data/"

											+ xform.getLocationXPath()).evaluate(submission_doc).split(" ")[0]);
									lvd.setLongitude(xPath.compile("/submission/data/"

											+ xform.getLocationXPath()).evaluate(submission_doc).split(" ")[1]);
								}
								lvd.setxForm(xform);
								SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
								lvd.setDateOfVisit(new Date(dateFormat.parse(xPath.compile("/submission/data/"

										+ xform.getDateOfVisitXPath()).evaluate(submission_doc)).getTime()));
								lvd.setSubmissionFileName("");
								lvd.setSubmissionFileURL("");
								lvd.setImageFileNames("");
								lvd.setImageFileNames("");
								CollectUser collectUser = new CollectUser();
								collectUser.setUserId(1);
								lvd.setUser(collectUser);
								lvd.setTimPeriod(xform.getTimePeriod());
								if (lvd.getArea() == null) {
									continue;

								}
								LastVisitData lvd1 = lastVisitDataRepository
										.findByxFormFormIdAndAreaAreaIdAndTimPeriodTimePeriodId(xform.getFormId(),
												lvd.getArea().getAreaId(), 1);

//							if(lvd1!=null)
//							{
//								if(lvd1.getMarkedAsCompleteDate().before(lvd.getMarkedAsCompleteDate()))
//								{
//								lvd1.setLive(false);
//								lastVisitDataRepository.save(lvd);
//								}
//								else if(lvd1.getMarkedAsCompleteDate().after(lvd.getMarkedAsCompleteDate()))
//								{
//									lvd.setLive(false);
//								}
//								
//							}
								lvd = lastVisitDataRepository.save(lvd);

								lastVisitDataMap.put(lvd.getInstanceId(), lvd);

							} else {
								lvd = lastVisitDataMap.get(instance_id);
							}
//						String s = xPath.compile(
//								"/submission/data/" + rootElement
//										+ "/bg_a/facility").evaluate(
//								submission_doc);
							for (FormXpathScoreMapping formXpathScoreMapping : xform.getFormXpathScoreMappings()) {
								System.out.println("/submission/data/"

										+ "/" + formXpathScoreMapping.getxPath() + "  ,type:-"
										+ formXpathScoreMapping.getType());
								FacilityScore facilityScore = new FacilityScore();
								facilityScore.setFormXpathScoreMapping(formXpathScoreMapping);
								if (formXpathScoreMapping.getType().contains("split")) {
									for (String path : formXpathScoreMapping.getxPath().split(",")) {
										if (xPath.compile("/submission/data/" + "/" + path).evaluate(submission_doc)
												.trim().equalsIgnoreCase("")) {

										} else {
											facilityScore.setScore(Double.parseDouble(xPath.compile("/submission/data/"

													+ "/" + path).evaluate(submission_doc)));

										}
									}

								}

								else {
									String subXpath = "";
									if (formXpathScoreMapping.getxPath().substring(0, 1).equals("/")) {
										subXpath = formXpathScoreMapping.getxPath().substring(1);
									} else {
										subXpath = formXpathScoreMapping.getxPath();
									}
									if (xPath.compile("/submission/data/"

											+ "/" + subXpath).evaluate(submission_doc).trim().equalsIgnoreCase("")) {

									} else {
										String valueOf = xPath.compile("/submission/data/"

												+ "/" + subXpath).evaluate(submission_doc);
										if (valueOf.toLowerCase().contains("yes")
												|| valueOf.toLowerCase().contains("no")) {
											facilityScore.setScore(
													valueOf.equalsIgnoreCase("yes") ? (double) 1 : (double) 0);
//										System.out.println("yes_no score-->"+(valueOf.equalsIgnoreCase("yes")?(double)1:(double)0));
										} else if (formXpathScoreMapping.getType()
												.equalsIgnoreCase("select_one yes_no")) {
											if (valueOf.contains("2")) {
												valueOf = "0";
											} else {
												valueOf = "1";
											}
											facilityScore.setScore(Double.parseDouble(valueOf));
//										System.out.println("select_one yes_no -->"+Double.parseDouble(valueOf));
										}

										else {
//										if(subXpath.equals("bg_m")) {
//											continue;
//										}
											facilityScore.setScore(Double.parseDouble(xPath.compile("/submission/data/"

													+ "/" + subXpath).evaluate(submission_doc)));
										}

									}
								}
								System.out.println("formID-" + lvd.getxForm().getxFormId() + ", lvdId-"
										+ lvd.getLastVisitDataId());
								if (formXpathScoreMapping.getMaxScore() != null
										|| (formXpathScoreMapping.getMaxCalXpath() != null
												&& formXpathScoreMapping.getMaxCalXpath().trim() != ""
												&& formXpathScoreMapping.getType().equals("numeric")))
									facilityScore
											.setMaxScore(
													formXpathScoreMapping
															.getMaxScore() != null
																	? formXpathScoreMapping.getMaxScore()
																	: Double.parseDouble(xPath
																			.compile("/submission/data/"

																					+ "/"
																					+ formXpathScoreMapping
																							.getMaxCalXpath())
																			.evaluate(submission_doc)));

								facilityScore.setLastVisitData(lvd);
								facilityScoreRepository.save(facilityScore);
							}
							lastVisitDataMap.put(instance_id, lvd);
						}
					}

				}
			}
			System.out.println("complited");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
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

	@Transactional
	@Override
	public boolean updateDataTreeData() throws Exception {

		List<Area> areaDetails = areaDetailsRepository.findAll();

		Map<String, Area> areaMap = new HashMap<String, Area>();

		for (Area area : areaDetails) {
			areaMap.put(area.getAreaCode(), area);
		}

		for (Area area : areaDetails) {
			if (area.getAreaLevel().getAreaLevelId() == 4) {
				areaMap.put(area.getParentAreaId() + "_" + area.getAreaName(), area);
			}
		}

		List<LastVisitData> lastVisitDatas = lastVisitDataRepository.findByTimPeriodTimePeriodId(1);
		Map<String, LastVisitData> lastVisitDataMap = new HashMap<String, LastVisitData>();
		for (LastVisitData lastVisitData : lastVisitDatas) {
			lastVisitDataMap.put(lastVisitData.getInstanceId(), lastVisitData);

		}
		List<XForm> xfroms = xFormRepository.findAllByIsLiveTrue();

		for (XForm xform : xfroms) {
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
					String instance_id = nodeIdList.item(node_no).getFirstChild().getNodeValue().trim();
					String instance_id1 = instance_id;// .replace(":", "");
					if (lastVisitDataMap.containsKey(instance_id1)) {
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
							lvd.setInstanceId(instance_id);
							String areaCode = null;
							for (String areaPath : xform.getAreaXPath().split(",")) {
								if (!xPath.compile("/submission/data/" + rootElement + areaPath)
										.evaluate(submission_doc).trim().equalsIgnoreCase("")) {
									areaCode = xPath.compile("/submission/data/" + rootElement + areaPath)
											.evaluate(submission_doc);
									if (areaMap.containsKey(xPath.compile("/submission/data/" + rootElement + areaPath)
											.evaluate(submission_doc))) {

										lvd.setArea(
												areaMap.get(xPath.compile("/submission/data/" + rootElement + areaPath)
														.evaluate(submission_doc)));
									} else {
										Area area = new Area();

										String blockName = xPath.compile(
												"/submission/data/" + rootElement + xform.getSecondaryAreaXPath())
												.evaluate(submission_doc);
										Area block = areaDetailsRepository.findByAreaCode(blockName);
										List<ChoicesDetails> choiceDetails = choiceDetailsRepository
												.findByFormFormIdAndLabel(xform.getFormId(), areaCode);
										area.setAreaCode(areaCode);
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

										area = areaDetailsRepository.save(area);
										areaMap.put(area.getAreaCode(), area);

										lvd.setArea(area);
									}
									break;
								}
							}

							lvd.setLive(true);
							// to be uncommented and images should be set into
							// the
							// lvds

							if (!xPath.compile("/submission/data/" + xform.getLocationXPath()).evaluate(submission_doc)
									.trim().equalsIgnoreCase("")) {
								lvd.setLatitude(xPath.compile("/submission/data/"

										+ xform.getLocationXPath()).evaluate(submission_doc).split(" ")[0]);
								lvd.setLongitude(xPath.compile("/submission/data/"

										+ xform.getLocationXPath()).evaluate(submission_doc).split(" ")[1]);
							}
							lvd.setxForm(xform);
							SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
							lvd.setDateOfVisit(new Date(dateFormat.parse(xPath.compile("/submission/data/"

									+ xform.getDateOfVisitXPath()).evaluate(submission_doc)).getTime()));
							lvd.setSubmissionFileName("");
							lvd.setSubmissionFileURL("");
							lvd.setImageFileNames("");
							lvd.setImageFileNames("");
							CollectUser collectUser = new CollectUser();
							collectUser.setUserId(1);
							lvd.setUser(collectUser);
							lvd.setTimPeriod(xform.getTimePeriod());
							LastVisitData lvd1 = lastVisitDataRepository
									.findByxFormFormIdAndAreaAreaIdAndTimPeriodTimePeriodId(xform.getFormId(),
											lvd.getArea().getAreaId(), 1);

							if (lvd1 != null) {
								if (lvd1.getMarkedAsCompleteDate().before(lvd.getMarkedAsCompleteDate())) {
									lvd1.setLive(false);
									lastVisitDataRepository.save(lvd);
								} else if (lvd1.getMarkedAsCompleteDate().after(lvd.getMarkedAsCompleteDate())) {
									lvd.setLive(false);
								}

							}
							lvd = lastVisitDataRepository.save(lvd);

							lastVisitDataMap.put(lvd.getInstanceId(), lvd);

						} else {
							lvd = lastVisitDataMap.get(instance_id1);
						}
//						String s = xPath.compile(
//								"/submission/data/" + rootElement
//										+ "/bg_a/facility").evaluate(
//								submission_doc);
						for (FormXpathScoreMapping formXpathScoreMapping : xform.getFormXpathScoreMappings()) {
							if (formXpathScoreMapping.getFormXpathScoreId() > 312) {
								FacilityScore facilityScore = new FacilityScore();
								facilityScore.setFormXpathScoreMapping(formXpathScoreMapping);
								if (formXpathScoreMapping.getType().contains("split")) {
									for (String path : formXpathScoreMapping.getxPath().split(",")) {
										if (xPath.compile("/submission/data/" + rootElement + "/" + path)
												.evaluate(submission_doc).trim().equalsIgnoreCase("")) {

										} else {
											facilityScore.setScore(Double.parseDouble(
													xPath.compile("/submission/data/" + rootElement + "/" + path)
															.evaluate(submission_doc)));

										}
									}

								}

								else {
									if (xPath
											.compile("/submission/data/" + rootElement + "/"
													+ formXpathScoreMapping.getxPath())
											.evaluate(submission_doc).trim().equalsIgnoreCase("")) {

									} else {
										String valueOf = xPath.compile("/submission/data/" + rootElement + "/"
												+ formXpathScoreMapping.getxPath()).evaluate(submission_doc);
										if (valueOf.contains("yes") || valueOf.contains("no")) {
											facilityScore.setScore(
													valueOf.equalsIgnoreCase("yes") ? (double) 1 : (double) 0);

										} else if (formXpathScoreMapping.getType()
												.equalsIgnoreCase("select_one yes_no")) {
											if (valueOf.contains("2")) {
												valueOf = "0";
											} else {
												valueOf = "1";
											}
											facilityScore.setScore(Double.parseDouble(valueOf));
										}

										else {

											facilityScore.setScore(Double.parseDouble(xPath
													.compile("/submission/data/" + rootElement + "/"
															+ formXpathScoreMapping.getxPath())
													.evaluate(submission_doc)));
										}

									}
								}
								facilityScore.setLastVisitData(lvd);
								facilityScoreRepository.save(facilityScore);
							}
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
//	@Scheduled(cron="0 0 0 * * ?")
	public void updateFacilityData() throws Exception {

		System.out.println("Starts");
		List<Area> areaDetails = areaDetailsRepository.findAll();

		Map<String, Area> areaMap = new HashMap<String, Area>();

		for (Area area : areaDetails) {
			areaMap.put(area.getAreaCode(), area);
		}

		for (Area area : areaDetails) {
			if (area.getAreaLevel().getAreaLevelId() == 4) {
				areaMap.put(area.getParentAreaId() + "_" + area.getAreaName(), area);
			}
		}

		List<LastVisitData> lastVisitDatas = lastVisitDataRepository.findAll();
		Map<String, LastVisitData> lastVisitDataMap = new HashMap<String, LastVisitData>();
		for (LastVisitData lastVisitData : lastVisitDatas) {
			lastVisitDataMap.put(lastVisitData.getInstanceId(), lastVisitData);

		}
		List<XForm> xfroms = xFormRepository.findAllByIsLiveTrue();

		for (XForm xform : xfroms) {
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
					String instance_id = nodeIdList.item(node_no).getFirstChild().getNodeValue().trim();
					if (!lastVisitDataMap.containsKey(instance_id)
							|| lastVisitDataMap.get(instance_id).getFacilityScores() == null
							|| lastVisitDataMap.get(instance_id).getFacilityScores().size() < 1
							|| lastVisitDataMap.get(instance_id).getFacilityScores().isEmpty()) {
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
						if (!lastVisitDataMap.containsKey(instance_id)) {
							lvd.setMarkedAsCompleteDate(new Timestamp((sdf.parse(markedAsCompleteDate)).getTime()));
							lvd.setInstanceId(instance_id);
							String areaCode = null;
							for (String areaPath : xform.getAreaXPath().split(",")) {
								if (!xPath.compile("/submission/data/" + rootElement + areaPath)
										.evaluate(submission_doc).trim().equalsIgnoreCase("")) {
									areaCode = xPath.compile("/submission/data/" + rootElement + areaPath)
											.evaluate(submission_doc);
									if (areaMap.containsKey(xPath.compile("/submission/data/" + rootElement + areaPath)
											.evaluate(submission_doc))) {

										lvd.setArea(
												areaMap.get(xPath.compile("/submission/data/" + rootElement + areaPath)
														.evaluate(submission_doc)));
									} else {
										Area area = new Area();

										String blockName = xPath.compile(
												"/submission/data/" + rootElement + xform.getSecondaryAreaXPath())
												.evaluate(submission_doc);
										Area block = areaDetailsRepository.findByAreaCode(blockName);
										List<ChoicesDetails> choiceDetails = choiceDetailsRepository
												.findByFormFormIdAndLabel(xform.getFormId(), areaCode);
										area.setAreaCode(areaCode);
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

										area = areaDetailsRepository.save(area);
										areaMap.put(area.getAreaCode(), area);

										lvd.setArea(area);
									}
									break;
								}
							}

							lvd.setLive(true);
							// to be uncommented and images should be set into
							// the
							// lvds

							if (!xPath.compile("/submission/data/" + xform.getLocationXPath()).evaluate(submission_doc)
									.trim().equalsIgnoreCase("")) {
								lvd.setLatitude(xPath.compile("/submission/data/"

										+ xform.getLocationXPath()).evaluate(submission_doc).split(" ")[0]);
								lvd.setLongitude(xPath.compile("/submission/data/"

										+ xform.getLocationXPath()).evaluate(submission_doc).split(" ")[1]);
							}
							lvd.setxForm(xform);
							SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
							lvd.setDateOfVisit(new Date(dateFormat.parse(xPath.compile("/submission/data/"

									+ xform.getDateOfVisitXPath()).evaluate(submission_doc)).getTime()));
							lvd.setSubmissionFileName("");
							lvd.setSubmissionFileURL("");
							lvd.setImageFileNames("");
							lvd.setImageFileNames("");
							CollectUser collectUser = new CollectUser();
							collectUser.setUserId(1);
							lvd.setUser(collectUser);
							lvd.setTimPeriod(xform.getTimePeriod());
							LastVisitData lvd1 = lastVisitDataRepository
									.findByxFormFormIdAndAreaAreaIdAndTimPeriodTimePeriodId(xform.getFormId(),
											lvd.getArea().getAreaId(), 1);

							if (lvd1 != null) {
								if (lvd1.getMarkedAsCompleteDate().before(lvd.getMarkedAsCompleteDate())) {
									lvd1.setLive(false);
									lastVisitDataRepository.save(lvd);
								} else if (lvd1.getMarkedAsCompleteDate().after(lvd.getMarkedAsCompleteDate())) {
									lvd.setLive(false);
								}

							}
							lvd = lastVisitDataRepository.save(lvd);

							lastVisitDataMap.put(lvd.getInstanceId(), lvd);

						} else {
							lvd = lastVisitDataMap.get(instance_id);
						}
//						String s = xPath.compile(
//								"/submission/data/" + rootElement
//										+ "/bg_a/facility").evaluate(
//								submission_doc);
						for (FormXpathScoreMapping formXpathScoreMapping : xform.getFormXpathScoreMappings()) {
							FacilityScore facilityScore = new FacilityScore();
							facilityScore.setFormXpathScoreMapping(formXpathScoreMapping);
							if (formXpathScoreMapping.getType().contains("split")) {
								for (String path : formXpathScoreMapping.getxPath().split(",")) {
									if (xPath.compile("/submission/data/" + rootElement + "/" + path)
											.evaluate(submission_doc).trim().equalsIgnoreCase("")) {

									} else {
										facilityScore.setScore(Double.parseDouble(
												xPath.compile("/submission/data/" + rootElement + "/" + path)
														.evaluate(submission_doc)));

									}
								}

							}

							else {
								if (xPath
										.compile("/submission/data/" + rootElement + "/"
												+ formXpathScoreMapping.getxPath())
										.evaluate(submission_doc).trim().equalsIgnoreCase("")) {

								} else {
									String valueOf = xPath.compile(
											"/submission/data/" + rootElement + "/" + formXpathScoreMapping.getxPath())
											.evaluate(submission_doc);
									if (valueOf.contains("yes") || valueOf.contains("no")) {
										facilityScore
												.setScore(valueOf.equalsIgnoreCase("yes") ? (double) 1 : (double) 0);

									} else if (formXpathScoreMapping.getType().equalsIgnoreCase("select_one yes_no")) {
										if (valueOf.contains("2")) {
											valueOf = "0";
										} else {
											valueOf = "1";
										}
										facilityScore.setScore(Double.parseDouble(valueOf));
									}

									else {

										facilityScore
												.setScore(Double.parseDouble(xPath
														.compile("/submission/data/" + rootElement + "/"
																+ formXpathScoreMapping.getxPath())
														.evaluate(submission_doc)));
									}

								}
							}
							facilityScore.setLastVisitData(lvd);
							facilityScoreRepository.save(facilityScore);
						}
						lastVisitDataMap.put(instance_id, lvd);
					}
				}

			}
		}
	}

	@Override
	public boolean updateXformMapping() throws Exception {
		List<XForm> xforms = xFormRepository.findAllByIsLiveTrue();

		for (XForm xform : xforms) {
			Map<String, FormXpathScoreMapping> formMap = new LinkedHashMap<String, FormXpathScoreMapping>();
			List<FormXpathScoreMapping> formXpathScoreMappings = formXpathScoreMappingRepository
					.findByFormIdAndWithNoChildren(xform.getFormId());
			for (FormXpathScoreMapping formXpathScoreMapping : formXpathScoreMappings) {
				formMap.put(formXpathScoreMapping.getxPath().split(",")[0], formXpathScoreMapping);
			}

			List<String> strings = new ArrayList<String>();
			for (String key : formMap.keySet()) {
				if (!strings.contains(key)) {
					for (String key1 : formMap.keySet()) {
						if (formMap.get(key1).getMaxScore() == null && ((key1.startsWith(key + "_"))
								|| (key1.startsWith(key + ".")) || key1.startsWith(key))
								&& !key1.equalsIgnoreCase(key)) {
							FormXpathScoreMapping formXpathScoreMapping = formMap.get(key1);
							formXpathScoreMapping.setParentXpathId(formMap.get(key).getFormXpathScoreId());
							formXpathScoreMappingRepository.save(formXpathScoreMapping);
							strings.add(key1);
						}
					}
				}
			}

			/*
			 * for (String key:formMap.keySet()) { if(!strings.contains(key)) { for (String
			 * key1:formMap.keySet()) { if(key1.startsWith(key)&&
			 * !key1.equalsIgnoreCase(key)&&(key1.contains("bg_e_1")||key1.contains(
			 * "gr_5a_s"))) { FormXpathScoreMapping formXpathScoreMapping =
			 * formMap.get(key1);
			 * formXpathScoreMapping.setParentXpathId(formMap.get(key).getFormXpathScoreId()
			 * ); formXpathScoreMappingRepository.save(formXpathScoreMapping);
			 * strings.add(key1); } } } }
			 */
		}
		return false;
	}

	@Override
//	@Scheduled(cron="0 0 0 * * ?")
	public boolean sendSubmitionFormReport() throws Exception {
		// List<TimePeriod> timePeriods =
		// timePeriodRepository.findByOrderByTimePeriodIdDesc();

		// int lTimePeriodId =timePeriods.get(0).getTimePeriodId();
		java.util.Date cdate = new java.util.Date();

		List<LastVisitData> lvds = lastVisitDataRepository.findByDateOfVisit(cdate);
		List<Integer> areaIds = new ArrayList<Integer>();
		lvds.forEach(d -> {
			areaIds.add(d.getArea().getAreaId());
		});
		List<Area> areas = areaDetailsRepository.findByAreaIdIn(areaIds);
		System.out.println("length->" + lvds.size() + "areas->" + areas.size());

		return true;
	}

	@Override
	public boolean finalizeLastVisitData(int lastvistitDataId) {

		try {
			System.out.println("Starts");
			List<Area> areaDetails = areaDetailsRepository.findAll();

			Map<String, Area> areaMap = new HashMap<String, Area>();

			for (Area area : areaDetails) {
				areaMap.put(area.getAreaCode(), area);
			}

			for (Area area : areaDetails) {
				if (area.getAreaLevel().getAreaLevelId() == 4) {
					areaMap.put(area.getParentAreaId() + "_" + area.getAreaName(), area);
				}
			}

			Map<String, Area> areaMap2 = areaDetailsRepository.findAll().stream()
					.collect(Collectors.toMap(Area::getAreaName, a -> a));

			LastVisitData lastVisitDatas = lastVisitDataRepository.findByLastVisitDataIdAndIsLiveTrue(lastvistitDataId);
			Map<String, LastVisitData> lastVisitDataMap = new HashMap<String, LastVisitData>();

			lastVisitDataMap.put(lastVisitDatas.getInstanceId(), lastVisitDatas);

			List<LastVisitData> availableData = lastVisitDataRepository.findByAreaAndTimPeriodAndXForm(
					lastVisitDatas.getArea(), lastVisitDatas.getTimPeriod(), lastVisitDatas.getxForm());
			XForm xform = xFormRepository.findByFormId(lastVisitDatas.getxForm().getFormId());

			if (availableData.isEmpty()) {
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
					for (int node_no = nodeIdList.getLength() - 1; node_no < nodeIdList.getLength(); node_no++) {
						String instance_id = nodeIdList.item(node_no).getFirstChild().getNodeValue().trim();

						if (!lastVisitDataMap.containsKey(instance_id)
								|| lastVisitDataMap.get(instance_id).getFacilityScores() == null
								|| lastVisitDataMap.get(instance_id).getFacilityScores().size() < 1
								|| lastVisitDataMap.get(instance_id).getFacilityScores().isEmpty()) {
							String link_formID = generateFormID(xform.getxFormId(), formRooTitle, instance_id);
							Map<String, String> submiteParams = new HashMap<String, String>();
							submiteParams.put("formId", link_formID);
							String full_url = WebUtils.createLinkWithProperties(submission_xml_url, submiteParams);

							serializer = new KXmlSerializer();
							StringWriter data_writer = new StringWriter();
							try {
								result = AggregateUtils.getXmlDocument(full_url, serverInfo, false,
										submissionDescription, null);
								serializer.setOutput(data_writer);
								result.doc.write(serializer);
							} catch (Exception e) {

								System.out.println(xform.getFormId() + ":-" + instance_id + ":-" + e.getMessage());
								continue;
							}

							Document submission_doc = dBuilder.parse(new InputSource(
									new ByteArrayInputStream(data_writer.toString().getBytes("utf-8"))));
							XPath xPath = XPathFactory.newInstance().newXPath();
							submission_doc.getDocumentElement().normalize();

							String markedAsCompleteDate = xPath.compile("/submission/data/" + "/@markedAsCompleteDate")
									.evaluate(submission_doc);

							LastVisitData lvd = new LastVisitData();

							lvd = lastVisitDataMap.get(instance_id);
//System.out.println("1st Lvd-> "+lvd.getLastVisitDataId());
//						String s = xPath.compile(
//								"/submission/data/" + rootElement
//										+ "/bg_a/facility").evaluate(
//								submission_doc);
							for (FormXpathScoreMapping formXpathScoreMapping : xform.getFormXpathScoreMappings()) {
								System.out.println("/submission/data/"

										+ "/" + formXpathScoreMapping.getxPath() + "  ,type:-"
										+ formXpathScoreMapping.getType());
								FacilityScore facilityScore = new FacilityScore();
								facilityScore.setFormXpathScoreMapping(formXpathScoreMapping);
								if (formXpathScoreMapping.getType().contains("split")) {
									for (String path : formXpathScoreMapping.getxPath().split(",")) {
										if (xPath.compile("/submission/data/" + "/" + path).evaluate(submission_doc)
												.trim().equalsIgnoreCase("")) {

										} else {
											facilityScore.setScore(Double.parseDouble(xPath.compile("/submission/data/"

													+ "/" + path).evaluate(submission_doc)));

										}
									}

								}

								else {
									String subXpath = "";
									if (formXpathScoreMapping.getxPath().substring(0, 1).equals("/")) {
										subXpath = formXpathScoreMapping.getxPath().substring(1);
									} else {
										subXpath = formXpathScoreMapping.getxPath();
									}
									if (xPath.compile("/submission/data"

											+ "//" + subXpath).evaluate(submission_doc).trim().equalsIgnoreCase("")) {

									} else {
										String valueOf = xPath.compile("/submission/data/"

												+ "/" + subXpath).evaluate(submission_doc);
										if (valueOf.toLowerCase().contains("yes")
												|| valueOf.toLowerCase().contains("no")) {
											facilityScore.setScore(
													valueOf.equalsIgnoreCase("yes") ? (double) 1 : (double) 0);
//										System.out.println("yes_no score-->"+(valueOf.equalsIgnoreCase("yes")?(double)1:(double)0));
										} else if (formXpathScoreMapping.getType()
												.equalsIgnoreCase("select_one yes_no")) {
											if (valueOf.contains("2")) {
												valueOf = "0";
											} else {
												valueOf = "1";
											}
											facilityScore.setScore(Double.parseDouble(valueOf));
//										System.out.println("select_one yes_no -->"+Double.parseDouble(valueOf));
										}

										else {
//										if(subXpath.equals("bg_m")) {
//											continue;
//										}
											facilityScore.setScore(Double.parseDouble(xPath.compile("/submission/data/"

													+ "/" + subXpath).evaluate(submission_doc)));
										}

									}
								}
								System.out.println("formID-" + lvd.getxForm().getxFormId() + ", lvdId-"
										+ lvd.getLastVisitDataId());
								if (formXpathScoreMapping.getMaxScore() != null
										|| (formXpathScoreMapping.getMaxCalXpath() != null
												&& formXpathScoreMapping.getMaxCalXpath().trim() != ""
												&& formXpathScoreMapping.getType().equals("numeric")))
									facilityScore
											.setMaxScore(
													formXpathScoreMapping
															.getMaxScore() != null
																	? formXpathScoreMapping.getMaxScore()
																	: Double.parseDouble(xPath
																			.compile("/submission/data/"

																					+ "/"
																					+ formXpathScoreMapping
																							.getMaxCalXpath())
																			.evaluate(submission_doc)));

								facilityScore.setLastVisitData(lvd);
								facilityScoreRepository.save(facilityScore);
							}
							lastVisitDataMap.put(instance_id, lvd);
//						make lastvisitData as finalized
							lvd.setFinalized(true);
							lastVisitDataRepository.save(lvd);
						}
					}

				}
			}
			System.out.println("complited");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public boolean aproveAndFinalizeLastVisitData(int lastvistitDataId) {

		try {
			System.out.println("Starts");
			List<Area> areaDetails = areaDetailsRepository.findAll();

			Map<String, Area> areaMap = new HashMap<String, Area>();

			for (Area area : areaDetails) {
				areaMap.put(area.getAreaCode(), area);
			}

			for (Area area : areaDetails) {
				if (area.getAreaLevel().getAreaLevelId() == 4) {
					areaMap.put(area.getParentAreaId() + "_" + area.getAreaName(), area);
				}
			}

			Map<String, Area> areaMap2 = areaDetailsRepository.findAll().stream()
					.collect(Collectors.toMap(Area::getAreaName, a -> a));

			LastVisitData lastVisitDatas = lastVisitDataRepository.findByLastVisitDataIdAndIsLiveTrue(lastvistitDataId);
			Map<String, LastVisitData> lastVisitDataMap = new HashMap<String, LastVisitData>();

			lastVisitDataMap.put(lastVisitDatas.getInstanceId(), lastVisitDatas);

			List<LastVisitData> availableData = lastVisitDataRepository.findByAreaAndTimPeriodAndXForm(
					lastVisitDatas.getArea(), lastVisitDatas.getTimPeriod(), lastVisitDatas.getxForm());
			XForm xform = xFormRepository.findByFormId(lastVisitDatas.getxForm().getFormId());

			if (true) {
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
					for (int node_no = nodeIdList.getLength() - 1; node_no < nodeIdList.getLength(); node_no++) {
//						String instance_id = nodeIdList.item(node_no).getFirstChild().getNodeValue().trim();
						String instance_id = lastVisitDatas.getInstanceId();

						if (!lastVisitDataMap.containsKey(instance_id)
								|| lastVisitDataMap.get(instance_id).getFacilityScores() == null
								|| lastVisitDataMap.get(instance_id).getFacilityScores().size() < 1
								|| lastVisitDataMap.get(instance_id).getFacilityScores().isEmpty()) {
							String link_formID = generateFormID(xform.getxFormId(), formRooTitle, instance_id);
							Map<String, String> submiteParams = new HashMap<String, String>();
							submiteParams.put("formId", link_formID);
							String full_url = WebUtils.createLinkWithProperties(submission_xml_url, submiteParams);

							serializer = new KXmlSerializer();
							StringWriter data_writer = new StringWriter();
							try {
								result = AggregateUtils.getXmlDocument(full_url, serverInfo, false,
										submissionDescription, null);
								serializer.setOutput(data_writer);
								result.doc.write(serializer);
							} catch (Exception e) {

								System.out.println(xform.getFormId() + ":-" + instance_id + ":-" + e.getMessage());
								continue;
							}

							Document submission_doc = dBuilder.parse(new InputSource(
									new ByteArrayInputStream(data_writer.toString().getBytes("utf-8"))));
							XPath xPath = XPathFactory.newInstance().newXPath();
							submission_doc.getDocumentElement().normalize();

							String markedAsCompleteDate = xPath.compile("/submission/data/" + "/@markedAsCompleteDate")
									.evaluate(submission_doc);

							LastVisitData lvd = new LastVisitData();

							lvd = lastVisitDataMap.get(instance_id);
//System.out.println("1st Lvd-> "+lvd.getLastVisitDataId());
//						String s = xPath.compile(
//								"/submission/data/" + rootElement
//										+ "/bg_a/facility").evaluate(
//								submission_doc);
							for (FormXpathScoreMapping formXpathScoreMapping : xform.getFormXpathScoreMappings()) {
								System.out.println("/submission/data/"

										+ "/" + formXpathScoreMapping.getxPath() + "  ,type:-"
										+ formXpathScoreMapping.getType());
								FacilityScore facilityScore = new FacilityScore();
								facilityScore.setFormXpathScoreMapping(formXpathScoreMapping);
								if (formXpathScoreMapping.getType().contains("split")) {
									for (String path : formXpathScoreMapping.getxPath().split(",")) {
										if (xPath.compile("/submission/data/" + "/" + path).evaluate(submission_doc)
												.trim().equalsIgnoreCase("")) {

										} else {
											facilityScore.setScore(Double.parseDouble(xPath.compile("/submission/data/"

													+ "/" + path).evaluate(submission_doc)));

										}
									}

								}

								else {
									String subXpath = "";
									if (formXpathScoreMapping.getxPath().substring(0, 1).equals("/")) {
										subXpath = formXpathScoreMapping.getxPath().substring(1);
									} else {
										subXpath = formXpathScoreMapping.getxPath();
									}
									if (xPath.compile("/submission/data"

											+ "//" + subXpath).evaluate(submission_doc).trim().equalsIgnoreCase("")) {

									} else {
										String valueOf = xPath.compile("/submission/data/"

												+ "/" + subXpath).evaluate(submission_doc);
										if (valueOf.toLowerCase().contains("yes")
												|| valueOf.toLowerCase().contains("no")) {
											facilityScore.setScore(
													valueOf.equalsIgnoreCase("yes") ? (double) 1 : (double) 0);
//										System.out.println("yes_no score-->"+(valueOf.equalsIgnoreCase("yes")?(double)1:(double)0));
										} else if (formXpathScoreMapping.getType()
												.equalsIgnoreCase("select_one yes_no")) {
											if (valueOf.contains("2")) {
												valueOf = "0";
											} else {
												valueOf = "1";
											}
											facilityScore.setScore(Double.parseDouble(valueOf));
//										System.out.println("select_one yes_no -->"+Double.parseDouble(valueOf));
										}

										else {
//										if(subXpath.equals("bg_m")) {
//											continue;
//										}
											facilityScore.setScore(Double.parseDouble(xPath.compile("/submission/data/"

													+ "/" + subXpath).evaluate(submission_doc)));
										}

									}
								}
								System.out.println("formID-" + lvd.getxForm().getxFormId() + ", lvdId-"
										+ lvd.getLastVisitDataId());
								if (formXpathScoreMapping.getMaxScore() != null
										|| (formXpathScoreMapping.getMaxCalXpath() != null
												&& formXpathScoreMapping.getMaxCalXpath().trim() != ""
												&& formXpathScoreMapping.getType().equals("numeric")))
									facilityScore
											.setMaxScore(
													formXpathScoreMapping
															.getMaxScore() != null
																	? formXpathScoreMapping.getMaxScore()
																	: Double.parseDouble(xPath
																			.compile("/submission/data/"

																					+ "/"
																					+ formXpathScoreMapping
																							.getMaxCalXpath())
																			.evaluate(submission_doc)));

								facilityScore.setLastVisitData(lvd);
								facilityScoreRepository.save(facilityScore);
							}
							lastVisitDataMap.put(instance_id, lvd);
//						make lastvisitData as finalized
							lvd.setFinalized(true);
							lastVisitDataRepository.save(lvd);
						}
					}

				}
			}
			System.out.println("complited");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}
}
