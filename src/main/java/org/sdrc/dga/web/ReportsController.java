package org.sdrc.dga.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;

import org.sdrc.dga.model.AreaModel;
import org.sdrc.dga.model.CrossTabDataModel;
import org.sdrc.dga.model.CrossTabDropDownData;
import org.sdrc.dga.model.DevInfoAreaModel;
import org.sdrc.dga.model.FipDistrict;
import org.sdrc.dga.model.SectorModel;
import org.sdrc.dga.model.TimePeriodModel;
import org.sdrc.dga.model.XFormModel;
import org.sdrc.dga.service.FIPService;
import org.sdrc.dga.service.RawDataReportService;
import org.sdrc.dga.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Harsh
 * @since version 1.0.0.0
 *
 */

@Controller
public class ReportsController {

	@Autowired
	private ReportService reportService;

	@Autowired
	private FIPService fIPService;

	@Autowired
	private RawDataReportService rawDataReportService;

	@PreAuthorize("hasAuthority('report,View')")
	@GetMapping("/getAllSectors")
	@ResponseBody
	List<SectorModel> getAllSectors() {
		return reportService.getAllSectors();
	}

	@PreAuthorize("hasAuthority('report,View')")
	@GetMapping("/getAllSectorPrograms")
	@ResponseBody
	List<SectorModel> getAllSectorPrograms(@RequestParam("stateCode") String stateCode) {
		return reportService.getAllSectorPrograms(stateCode);
	}

	@PreAuthorize("hasAuthority('report,View')")
	@GetMapping("/getAllStates")
	@ResponseBody
	List<DevInfoAreaModel> getAllStates() {
		return reportService.getAllStates();
	}

	@PreAuthorize("hasAuthority('report,View')")
	@GetMapping("/getAllTimePeriods")
	@ResponseBody
	List<TimePeriodModel> getTimePeriods(@RequestParam("stateId") int stateId) {
		return reportService.getAllTimePeriod(stateId);
	}

	@PreAuthorize("hasAuthority('report,View')")
	@GetMapping("/getSummary")
	@ResponseBody
	Object getSummaryData(@RequestParam("checklistId") Integer checklistId,
			@RequestParam("sectionId") Integer sectionId, @RequestParam("timperiodNid") Integer timperiodNid,
			@RequestParam("programNid") Integer programId, @RequestParam("sectionNid") Integer sectionNid) {
		return reportService.fetchSummaryforSectorAndTimePeriod(checklistId, sectionId, timperiodNid, programId,
				sectionNid);
	}

	@PreAuthorize("hasAuthority('report,View')")
	@GetMapping("/getRawData")
	@ResponseBody
	Object getRawData(@RequestParam("division") Integer division, @RequestParam("checklistId") Integer checklistId,
			@RequestParam("sectionId") Integer sectionId, @RequestParam("timperiodNid") Integer timperiodNid,
			@RequestParam("programNid") Integer programId, @RequestParam("sectionNid") Integer sectionNid) {
		return reportService.fetchRawDataforSectorAndTimePeriodAndDivsion(division, checklistId, sectionId,
				timperiodNid, programId, sectionNid);
	}

	@PreAuthorize("hasAuthority('report,View')")
	@GetMapping("report")
	String reportPage() {
		return "report";
	}

	@PreAuthorize("hasAuthority('report,View')")
	@GetMapping("/getAllDistricts")
	@ResponseBody
	List<AreaModel> getAllDistricts() {
		return reportService.getAllDistricts();
	}

	// crossTabReport page

	@PreAuthorize("hasAuthority('CrossTab,View')")
	@GetMapping("crossTabReport")
	String crossTabReportPage() {
		return "dyanamicCrossTabReport";
	}

	@PreAuthorize("hasAuthority('FIP,View')")
	@GetMapping("getFIPReport")
	@ResponseBody
	Map<String, String> getFIPReport(@RequestParam("areaCode") String areaCode,
			@RequestParam("formMetaId") int formMetaId, @RequestParam("stateId") int stateId) throws IOException {
		String fileName = fIPService.generateFIP(areaCode, formMetaId, stateId);
		Map<String, String> map = new HashMap<String, String>();
		map.put("File", fileName);
		return map;
	}

	@PreAuthorize("hasAuthority('FIP,View')")
	@GetMapping("facilityImprovementPlan")
	String facilityImprovementPlanPage() {
		return "facilityImprovementPlan";
	}

	@PreAuthorize("hasAuthority('CrossTab,View')")
	@GetMapping("crossTabDropDownData")
	@ResponseBody
	CrossTabDropDownData crossTabDropDownData(@RequestParam("stateId") Integer stateId, int timePeriodId) {
		return reportService.getCrossTabDropDown(stateId, timePeriodId);
	}

	@PreAuthorize("hasAuthority('CrossTab,View')")
	@PostMapping("/getCrossTabTableData")
	@ResponseBody
	Object getCrossTabTableData(@RequestBody CrossTabDataModel crossTabDataModel) {
		return reportService.getCrossTabTableData(crossTabDataModel);
	}

	@PreAuthorize("hasAuthority('FIP,View')")
	@RequestMapping("/getFacilityFormADistrict")
	@ResponseBody
	public Map<String, List<XFormModel>> getFacilityFormADistrict(@RequestParam("stateId") Integer stateId) {
		return fIPService.getFacilityFormADistrict(stateId);
	}

	@PreAuthorize("hasAuthority('FIP,View')")
	@RequestMapping("/getFipDistrict")
	@ResponseBody
	public List<FipDistrict> getFipDistrict(@RequestParam("stateId") Integer stateId) {
		return fIPService.getFipDistrict(stateId);
	}

	@PreAuthorize("hasAuthority('report,View')")
	@GetMapping("/getRawDataList")
	@ResponseBody
	List<String> getRawDataList() {
		return reportService.getAllRawDataReportsList();
	}

	@PreAuthorize("hasAuthority('report,View')")
	@RequestMapping("/getFacilityFormADistrictForRawData")
	@ResponseBody
	public Map<String, List<XFormModel>> getFacilityFormADistrictForRawData(@RequestParam("stateId") Integer stateId) {
		return rawDataReportService.getFacilityFormADistrictForRawData(stateId);
	}

	@PreAuthorize("hasAuthority('report,View')")
	@GetMapping("/getAllTimePeriodForRawData")
	@ResponseBody
	List<TimePeriodModel> getAllPlanningTimePeriodForRawData(@RequestParam("stateId") int stateId,
			@RequestParam("programId") String programId) {
		return rawDataReportService.getAllPlanningTimePeriodForRawData(stateId, programId);
	}

	@PreAuthorize("hasAuthority('report,View')")
	@PostMapping("/getRawDataReport")
	@ResponseBody
//	ResponseEntity<InputStreamResource> getRawDataReport(@RequestParam("programId")  String programId,@RequestParam("facilityType")String facilityName,@RequestParam("timePeriod")int timePeriodId) {
	ResponseEntity<InputStreamResource> getRawDataReport(@RequestBody String[] datas) {
		String programId = datas[0];
		String facilityName = datas[1];
		int timePeriodId = Integer.parseInt(datas[2]);
		String stateId = datas[3];
		String fileName = "";
		InputStream inputStream;
		try {
			fileName = "";
			// fileName = "DGA_2_CHC_Raw Data_r1.xlsx";
//			fileName = rawDataReportService.getRawDataReportName(programId, facilityName, timePeriodId, stateId);
			File file;
			fileName = rawDataReportService.getRawDataReport(programId, facilityName, timePeriodId, stateId);
			if (fileName.length() > 1) {
				file = ResourceUtils.getFile( fileName);

				HttpHeaders respHeaders = new HttpHeaders();
				respHeaders.add("Content-Disposition", "attachment; filename=" + file.getName());
				FileInputStream fis = new FileInputStream(file);
				InputStreamResource isr = new InputStreamResource(fis);
				return new ResponseEntity<InputStreamResource>(isr, respHeaders, HttpStatus.OK);
				
				/*
				 * inputStream = new FileInputStream(fileName); String headerKey =
				 * "Content-Disposition"; String headerValue =
				 * String.format("attachment; filename=\"%s\"", new
				 * java.io.File(fileName).getName()); response.setHeader(headerKey,
				 * headerValue); response.setContentType("application/octet-stream"); // for all
				 * file type ServletOutputStream outputStream = response.getOutputStream();
				 * FileCopyUtils.copy(inputStream, outputStream); outputStream.close();
				 */
			} else {
				return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
	
	
	@PreAuthorize("hasAuthority('report,View')")
	@GetMapping("getRawDataReportFile")
	@ResponseBody
	Map<String, String> getRawDataReportFile(@RequestParam("facilityName") String facilityName,@RequestParam("programId") String programId,
			@RequestParam("timePeriodId") int timePeriodId, @RequestParam("stateId") String stateId) throws Exception {
		String fileName = rawDataReportService.getRawDataReport(programId, facilityName, timePeriodId, stateId);
		Map<String, String> map = new HashMap<String, String>();
		map.put("File", fileName);
		return map;
	}
	
}
