/**
 * 
 */
package org.sdrc.dga.web;

import java.util.List;
import java.util.Map;

import org.sdrc.dga.model.CollectUserModel;
import org.sdrc.dga.model.FipDistrict;
import org.sdrc.dga.model.FormModel;
import org.sdrc.dga.model.InitalDataModel;
import org.sdrc.dga.model.SubmissionDataModel;
import org.sdrc.dga.service.FIPService;
import org.sdrc.dga.service.FinalizeService;
import org.sdrc.dga.service.MasterRawDataService;
import org.sdrc.dga.service.ODKService;
import org.sdrc.dga.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Harsh Pratyush (harsh@sdrc.co.in), Pratyush(pratyush@sdrc.co.in) This
 *         controller is not meant for the use in production enviroment . All
 *         database update will be donw using this controller
 */

@Controller
@RequestMapping("database")
public class DatabaseManagementController {

	@Autowired
	private MasterRawDataService masterRawDataService;

	@Autowired
	private ODKService odkService;

	@Autowired
	private MessageSource messages;

	@Autowired
	private UserService userService;

//	@Autowired
//	private FIPService fipService;
	
	@Autowired
	private FinalizeService finalizeService;
	
	@Autowired
	private org.sdrc.dga.service.LegacyDataImportService legacyDataImportService;

//	
//	@GetMapping("insertXpathsUptoQuestionLevel")
//	@ResponseBody
//	String insertXpaths() throws Exception
//	{
//		return masterRawDataService.getAllFormXpathScoreMappingUptoQuestionLevel();
//	}
//	
//	
//	
//	http://localhost:8080/uphc-mh/database/generateXpath
	@GetMapping("generateXpath")
	@ResponseBody
	boolean generateXpath() throws Exception {
		return masterRawDataService.generateXpath();
	}

	@GetMapping("updateRawData")
	@ResponseBody
	boolean updateRawData() throws Exception {
		return masterRawDataService.persistRawData();

	}

	@GetMapping("updateXpathIdOnRawDataScore")
	@ResponseBody
	boolean updateXpathIdOnRawDataScore() throws Exception {
		return masterRawDataService.updateXpathIdOnRawDataScore();

	}

	@GetMapping("/updateFacilityScore")
	@ResponseBody
	public boolean updateFacilityScore() throws Exception {
		return odkService.updateFacilityScore();
	}

//	@GetMapping("updateRawDataOfDataTree")
//	@ResponseBody
//	boolean updateRawDataOfDataTree() throws Exception
//	{
//		
//		return odkService.updateDataTreeData();
//	}
//	
//	@GetMapping("updateXformMapping")
//	@ResponseBody
//	boolean updateXformMapping() throws Exception
//	{
//		
//		return odkService.updateXformMapping();
//	}
//	
//	
//	@GetMapping("/insertCrossTabIndicator")
//	@ResponseBody
//	public boolean insertCrossTabIndicator() throws Exception
//	{
//		return masterRawDataService.insertCrossTabIndicatorXpath();
//	}
//	
//	
//	@GetMapping("/folderStructure")
//	@ResponseBody
//	public boolean folderStructure() throws Exception
//	{
//		return masterRawDataService.createFoldersOfImages();
//	}
//	
//	@GetMapping("/updateLatLong")
//	@ResponseBody
//	public boolean updateLatLong() throws Exception
//	{
//		return masterRawDataService.updateLatitudeLogitudeOfSubmission();
//	}

	@PostMapping(value = "updatePassword")
	@ResponseBody
	boolean updatePassword(@RequestBody CollectUserModel collectUserModel, @RequestHeader("secret") String secret)
			throws Exception {
		if (secret != null && secret.equalsIgnoreCase(messages.getMessage("secret.code", null, null)))
			return userService.updatePassword(collectUserModel);
		else
			return false;
	}

	//http://localhost:8080/uphc-mh/database/updateArea
	@GetMapping(value = "updateArea")
	@ResponseBody
	boolean updateArea() {
		try {
			return masterRawDataService.updateArea();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@GetMapping(value = "updateAreaLevel")
	@ResponseBody
	boolean updateAreaLevel() {
		try {
			return masterRawDataService.updateAreaLevel();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	//http://localhost:8080/uphc-mh/database/updateAspirationalArea?areaIds=8,10
//	areaIds of aspiretional District areaId in requestParam
	@GetMapping(value = "updateAspirationalArea")
	@ResponseBody
	boolean updateAspirationalArea(@RequestParam("areaIds") List<Integer> areaIds) {
		try {
			return masterRawDataService.updateAspirationalArea( areaIds);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

//	
	@GetMapping("insertUserTable")
	@ResponseBody
	boolean insertUserTable() {
		return userService.insertUserTable();
	}
//	

//	http://localhost:8080/uphc-mh/database/configureUserDatabase
	@GetMapping("configureUserDatabase")
	@ResponseBody
	boolean configureUserDatabase() {
		return userService.configureUserDatabase();
	}

//	
//	@GetMapping("configureRawDataXpath")
//	@ResponseBody
//	boolean configureRawDataXpath()
//	{
//		try {
//			return masterRawDataService.updateRawXpaths();
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
//	}

//	@GetMapping("generateIndicatorMap")
//	@ResponseBody
//	boolean generateIndicatorMap() throws Exception
//	{
//		return masterRawDataService.generateIndicatorXpathMapping();
//		
//	}

//	@Autowired
//	private DistrictDataManageService districtDataManageService;
//
//	@GetMapping("/configureQuestionTemplate")
//	private boolean configureQuestionTemplate() {
//		return districtDataManageService.configureIrfQuestionTemplate();
//	}

//	@GetMapping("/downloadFIP")
//	@ResponseBody
//	private boolean fipDownloads() throws Exception {
//		
//		fipService.fipDownload();
//		
//		return true;
//	}

//	@GetMapping("/updateFormXpathScoreMapping")
//	@ResponseBody
//	private boolean updateFormXpathScoreMapping() throws Exception
//	{
//		return masterRawDataService.updateFormXpathScoreMapping();
//	}
	
	@GetMapping("/getActiveSubmissions")
	@ResponseBody
	private List<SubmissionDataModel> getActiveSubmissions(@RequestParam("areaId") Integer areaId,@RequestParam("timePeriodId") Integer timePeriodId,@RequestParam("formId") Integer formId) throws Exception
	{
		
		return finalizeService.getActiveSubmissions(areaId, timePeriodId, formId);
	}
	
	@GetMapping("/checkFinalize")
	@ResponseBody
	private Boolean checkFinalize(@RequestParam("lastVistDataId") int lastVistDataId) throws Exception
	{
		return finalizeService.checkFinalize(lastVistDataId);
	}

	@GetMapping("/generateFIP")
	@ResponseBody
	private Map<String, String> generateFIP(@RequestParam("lastVistDataId") int lastVistDataId) throws Exception
	{
		return finalizeService.generateFIP(lastVistDataId);
	}

	
	@GetMapping("/makeSubmissionFinalize")
	@ResponseBody
	private Boolean makeSubmissionFinalize(@RequestParam("lastVistDataId") int lastVistDataId) throws Exception
	{
		return finalizeService.makeSubmissionFinalize(lastVistDataId);
	}
	
	@GetMapping("/getInitialData")
	@ResponseBody
	private InitalDataModel getInitialData() throws Exception
	{
		return finalizeService.getInitialData();
	}
	
	@GetMapping("/getXForms")
	@ResponseBody
	private List<FormModel> getXForms(@RequestParam("timePeriodId") int timePeriodId) throws Exception
	{
		return finalizeService.getXForms(timePeriodId);
	}
	
	@GetMapping("/rejectSubmission")
	@ResponseBody
	private Boolean rejectSubmission(@RequestParam("lastVistDataId") int lastVistDataId) throws Exception
	{
		return finalizeService.rejectSubmission(lastVistDataId);
	}
	
	@PostMapping("/importLegacyData")
	@ResponseBody
	private Boolean importLegacyData(@RequestParam("timePeriodId") int timePeriodId,@RequestParam("formId") int formId,@RequestParam("file") MultipartFile file) throws Exception
	{
		return legacyDataImportService.importLegacyData(timePeriodId, formId, file);
	}
	
	@GetMapping("/")
	@ResponseBody
	String wellcome() {
		return "Welcome to UPHC MH";
	}
	
	@GetMapping("/acceptSubmission")
	@ResponseBody
	private Boolean acceptSubmission(@RequestParam("lastVistDataId") int lastVistDataId) throws Exception
	{
		return finalizeService.acceptSubmission(lastVistDataId);
	}
	
	
	@GetMapping("/getFinalizeDistrict")
	@ResponseBody
	public List<FipDistrict> getFinalizeDistrict(@RequestParam("stateId") Integer stateId) {
		return finalizeService.getFinalizeDistrict(stateId);
	}
}
