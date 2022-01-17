package org.sdrc.dga.repository.springdatajpa;

import java.util.List;

import org.sdrc.dga.domain.Area;
import org.sdrc.dga.domain.FacilityScore;
import org.sdrc.dga.repository.FacilityScoreRepository;
import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.Repository;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.data.repository.query.Param;

@RepositoryDefinition(domainClass =FacilityScore.class, idClass = Integer.class)
public interface SpringDataFacilityScoreRepository extends
		FacilityScoreRepository {

	@Override
	@Query(value = "SELECT fc.formXpathScoreId, fxm.label, fxm.parentXpathId, AVG((fc.score/fxm.maxScore)*100), fxm.maxScore FROM "
			+ "FacilityScore fc JOIN FormXpathScoreMapping fxm ON "
			+ "fc.formXpathScoreId = fxm.formXpathScoreId, "
			+ "LastVisitData ld WHERE ld.LastVisitDataId = fc.lastVisitDataId "
			+ "AND ld.IsLive = 1"
			+ "GROUP BY fc.formXpathScoreId, fxm.label, fxm.parentXpathId, fxm.maxScore, fxm.formId HAVING fxm.formId = :formId", nativeQuery = true)
	List<Object[]> findAvgByFormId(@Param("formId") Integer formId);

	@Override
	@Query(value = "SELECT fxm.label, fxm.parentXpathId, fxm.maxScore, fc.score, "
			+ "fc.lastVisitDataId,(fc.score / fc.maxScore)*100 FROM "
			+ "FacilityScore fc JOIN FormXpathScoreMapping fxm "
			+ "ON fc.formXpathScoreId = fxm.formXpathScoreId WHERE, "
			+ "LastVisitData ld where ld.LastVisitDataId = fc.lastVisitDataId "
			+ " AND ld.IsLive = 1 AND fxm.formId = :formId", nativeQuery = true)
	List<Object[]> findAllPercentValueByFormId(@Param("formId") Integer formId);

	@Override
	@Query("SELECT fxm.label,(fc.score / fc.maxScore)*100,fc.lastVisitData.timPeriod.timeperiod,fxm.formXpathScoreId FROM "
			+ "FacilityScore fc JOIN fc.formXpathScoreMapping fxm "
			+ "WHERE fxm.parentXpathId=:parenXpathId " 
			+ "AND fc.lastVisitData.lastVisitDataId=:lastVisitDataId "
			+ " AND fc.lastVisitData.isLive = true  AND fxm.form.xform_meta_id = :formMetaId "
			+ "ORDER BY fxm.label ASC ")
	public List<Object[]> findSpiderDataChartByLasatVisitDatAndFormId(
			@Param("formMetaId") Integer formMetaId,
			@Param("lastVisitDataId") Integer lastVisitDataId,@Param("parenXpathId")Integer parenXpathId);

	@Override
	@Query("SELECT fxm.label,(AVG(fc.score / fc.maxScore)*100),fc.lastVisitData.timPeriod.timeperiod,fxm.formXpathScoreId FROM "
			+ "FacilityScore fc JOIN fc.formXpathScoreMapping fxm "
			+ "WHERE  fxm.parentXpathId=:parenXpathId "
			+ "AND fc.formXpathScoreMapping.formXpathScoreId = fxm.formXpathScoreId "
			+ "AND fc.lastVisitData.isLive = true  AND fxm.form.xform_meta_id = :formMetaId "
			+ " AND fc.lastVisitData.timPeriod.timePeriodId = :timeperiodId"
			+ " GROUP BY fxm.label ,fxm.maxScore,fc.lastVisitData.timPeriod.timeperiod,fxm.formXpathScoreId "
			+ "ORDER BY fxm.label ASC ")
	public List<Object[]> findSpiderDataChartByFormId(
			@Param("formMetaId") Integer formMetaId,@Param("parenXpathId")Integer parenXpathId,@Param("timeperiodId")int timeperiodId);
	
	@Override
	@Query("SELECT fxm.label,(AVG(fc.score / fc.maxScore)*100),fc.lastVisitData.timPeriod.timeperiod,fxm.formXpathScoreId FROM "
			+ "FacilityScore fc JOIN fc.formXpathScoreMapping fxm,Area area "
			+ "WHERE  fxm.parentXpathId=:parenXpathId "
			+ "AND fc.formXpathScoreMapping.formXpathScoreId = fxm.formXpathScoreId "
			+ "AND area.parentAreaId=:areaId "
			+ "AND fc.lastVisitData.area.parentAreaId=area.areaId "
			+ "AND fc.lastVisitData.isLive = true  AND fxm.form.xform_meta_id = :formMetaId "
			+ " AND fc.lastVisitData.timPeriod.timePeriodId = :timeperiodId"
			+ " GROUP BY fxm.label ,fxm.maxScore,fc.lastVisitData.timPeriod.timeperiod,fxm.formXpathScoreId "
			+ "ORDER BY fxm.label ASC ")
	public List<Object[]> findSpiderDataChartByFormIdForDistrict(
			@Param("formMetaId") Integer formMetaId,@Param("parenXpathId")Integer parenXpathId,@Param("areaId")Integer areaId,@Param("timeperiodId")int timeperiodId);
	
	@Override
	@Query("SELECT fxm.label,(AVG(fc.score / fc.maxScore)*100),fc.lastVisitData.timPeriod.timeperiod,fxm.formXpathScoreId FROM "
			+ "FacilityScore fc JOIN fc.formXpathScoreMapping fxm "
			+ "WHERE  fxm.parentXpathId=:parenXpathId "
			+ "AND fc.formXpathScoreMapping.formXpathScoreId = fxm.formXpathScoreId "
			+ "AND fc.lastVisitData.area.parentAreaId=:areaId "
			+ "AND fc.lastVisitData.isLive = true  AND fxm.form.xform_meta_id = :formMetaId "
			+ " AND fc.lastVisitData.timPeriod.timePeriodId = :timeperiodId"
			+ " GROUP BY fxm.label ,fxm.maxScore,fc.lastVisitData.timPeriod.timeperiod,fxm.formXpathScoreId "
			+ "ORDER BY fxm.label ASC ")
	public List<Object[]> findSpiderDataChartByFormIdForDistrictForDGA(
			@Param("formMetaId") Integer formMetaId,@Param("parenXpathId")Integer parenXpathId,@Param("areaId")Integer areaId,@Param("timeperiodId")int timeperiodId);


	
	@Override
	@Query("SELECT fxm.label,(fc.score / fc.maxScore)*100,fc.lastVisitData.timPeriod.timeperiod,fxm.formXpathScoreId FROM "
			+ " FacilityScore fc JOIN fc.formXpathScoreMapping fxm , LastVisitData lvd "
			+ " WHERE fxm.parentXpathId=:parenXpathId "
			+ " AND lvd.lastVisitDataId = :lastVisitDataId " 
			+ " AND fc.lastVisitData.area.areaId = lvd.area.areaId "
			+ " AND fc.lastVisitData.isLive = true  AND fxm.form.xform_meta_id = :formMetaId "
			+ " AND fc.lastVisitData.timPeriod.timePeriodId = :timeperiodId"
			+ " ORDER BY fxm.label ASC ")
	public List<Object[]> findSpiderDataChartByLasatVisitDatAndFormIdAndTimePeriodId(
			@Param("formMetaId") Integer formMetaId,@Param("lastVisitDataId")Integer areaId,@Param("parenXpathId")Integer parenXpathId,@Param("timeperiodId")int timeperiodId);
	
	@Override
	@Query("SELECT fxm.label,(AVG(fc.score / fc.maxScore)*100),fc.lastVisitData.timPeriod.timeperiod,fxm.formXpathScoreId FROM "
			+ "FacilityScore fc JOIN fc.formXpathScoreMapping fxm "
			+ "WHERE  fxm.parentXpathId=:parenXpathId "
			+ "AND fc.formXpathScoreMapping.formXpathScoreId = fxm.formXpathScoreId "
			+ "AND fc.lastVisitData.area.parentAreaId=:areaId "
			+ "AND fc.lastVisitData.isLive = true  AND fxm.form.xform_meta_id = :formMetaId "
			+ " AND fc.lastVisitData.timPeriod.timePeriodId = :timeperiodId"
			+ " AND fc.lastVisitData.area.aspirational=true "
			+ " GROUP BY fxm.label ,fxm.maxScore,fc.lastVisitData.timPeriod.timeperiod,fxm.formXpathScoreId "
			+ "ORDER BY fxm.label ASC ")
	public List<Object[]> findSpiderDataChartByFormIdForDistrictForDGAOfAspirational(
			@Param("formMetaId") Integer formMetaId,@Param("parenXpathId")Integer parenXpathId,@Param("areaId")Integer areaId,@Param("timeperiodId")int timeperiodId);

	@Override
	@Query("SELECT fxm.label,(AVG(fc.score / fc.maxScore)*100),fc.lastVisitData.timPeriod.timeperiod,fxm.formXpathScoreId FROM "
			+ "FacilityScore fc JOIN fc.formXpathScoreMapping fxm,Area area "
			+ "WHERE  fxm.parentXpathId=:parenXpathId "
			+ "AND fc.formXpathScoreMapping.formXpathScoreId = fxm.formXpathScoreId "
			+ "AND area.parentAreaId=:areaId "
			+ "AND fc.lastVisitData.area.parentAreaId=area.areaId "
			+ "AND fc.lastVisitData.isLive = true  AND fxm.form.xform_meta_id = :formMetaId "
			+ " AND fc.lastVisitData.timPeriod.timePeriodId = :timeperiodId"
			+ " AND fc.lastVisitData.area.aspirational=true "
			+ " GROUP BY fxm.label ,fxm.maxScore,fc.lastVisitData.timPeriod.timeperiod,fxm.formXpathScoreId "
			+ "ORDER BY fxm.label ASC ")
	public List<Object[]> findSpiderDataChartByFormIdForDistrictOfAspirational(
			@Param("formMetaId") Integer formMetaId,@Param("parenXpathId")Integer parenXpathId,@Param("areaId")Integer areaId,@Param("timeperiodId")int timeperiodId);
	
	
	@Override
	@Query("SELECT fxm.label,(AVG(fc.score / fc.maxScore)*100),fc.lastVisitData.timPeriod.timeperiod,fxm.formXpathScoreId FROM "
			+ "FacilityScore fc JOIN fc.formXpathScoreMapping fxm "
			+ "WHERE  fxm.parentXpathId=:parenXpathId "
			+ "AND fc.formXpathScoreMapping.formXpathScoreId = fxm.formXpathScoreId "
			+ "AND fc.lastVisitData.isLive = true  AND fxm.form.xform_meta_id = :formMetaId "
			+ " AND fc.lastVisitData.timPeriod.timePeriodId = :timeperiodId"
			+ " AND fc.lastVisitData.area.aspirational=true "
			+ " GROUP BY fxm.label ,fxm.maxScore,fc.lastVisitData.timPeriod.timeperiod,fxm.formXpathScoreId "
			+ "ORDER BY fxm.label ASC ")
	public List<Object[]> findSpiderDataChartByFormIdOfAspirational(
			@Param("formMetaId") Integer formMetaId,@Param("parenXpathId")Integer parenXpathId,@Param("timeperiodId")int timeperiodId);
	
	
	@Override
	@Query("SELECT fxm.label,(fc.score / fc.maxScore)*100,fc.lastVisitData.timPeriod.timeperiod,fxm.formXpathScoreId FROM "
			+ " FacilityScore fc JOIN fc.formXpathScoreMapping fxm , LastVisitData lvd "
			+ " WHERE fxm.parentXpathId=:parenXpathId "
			+ " AND lvd.lastVisitDataId = :lastVisitDataId " 
			+ " AND fc.lastVisitData.area.areaId = lvd.area.areaId "
			+ " AND fc.lastVisitData.isLive = true  AND fxm.form.xform_meta_id = :formMetaId "
			+ " AND fc.lastVisitData.timPeriod.timePeriodId = :timeperiodId"
			+ " AND fc.lastVisitData.area.aspirational=true "
			+ " ORDER BY fxm.label ASC ")
	public List<Object[]> findSpiderDataChartByLasatVisitDatAndFormIdAndTimePeriodIdOfAspirational(
			@Param("formMetaId") Integer formMetaId,@Param("lastVisitDataId")Integer areaId,@Param("parenXpathId")Integer parenXpathId,@Param("timeperiodId")int timeperiodId);
	
	
	@Override
	@Query("SELECT fxm.label,(AVG(fc.score / fc.maxScore)*100),fc.lastVisitData.timPeriod.timeperiod,fxm.formXpathScoreId FROM "
			+ "FacilityScore fc JOIN fc.formXpathScoreMapping fxm "
			+ "WHERE  fxm.parentXpathId=:parenXpathId "
			+ "AND fc.formXpathScoreMapping.formXpathScoreId = fxm.formXpathScoreId "
			+ "AND fc.lastVisitData.isLive = true  AND fxm.form.xform_meta_id = :formMetaId "
			+ " AND fc.lastVisitData.timPeriod.timePeriodId = :timeperiodId"
			+ " AND fc.lastVisitData.area.aspirational=true "
			+ " AND fc.lastVisitData.xForm.state in :states "
			+ 	" GROUP BY fxm.label ,fxm.maxScore,fc.lastVisitData.timPeriod.timeperiod,fxm.formXpathScoreId "
			+ "ORDER BY fxm.label ASC ")
	public List<Object[]> findSpiderDataChartByFormIdOfAspirationalNational(
			@Param("formMetaId") Integer formMetaId,@Param("parenXpathId")Integer parenXpathId,@Param("timeperiodId")int timeperiodId,@Param("states")List<Area> states);
	
	
	
	@Override
	@Query(value = "select ROUND((AVG(fc.score / fc.maxScore)*100),2"
			+ "),fxp.label,fxp.formXpathScoreId,xf.stateId,xf.timePeriodId,xf.FormId from LastVisitData lvd join " + 
			"FacilityScore fc on lvd.LastVisitDataId=fc.lastVisitDataId " + 
			" join XForm xf on lvd.FormId=xf.FormId join FormXpathScoreMapping fxp on " + 
			" fxp.formXpathScoreId = fc.formXpathScoreId join Area a on a.AreaId=lvd.AreaId" + 
			" where  xf.stateId in :states and xf.timePeriodId in :timeperiodIds and " + 
			"xf.xform_meta_id=:formMetaId and fxp.parentXpathId=-1 and lvd.IsLive= 1 and a.Aspirational= 1  "
			+ "group by fxp.label, fxp.formXpathScoreId, xf.stateId, xf.timePeriodId,xf.FormId ORDER BY fxp.label ASC ",nativeQuery = true)
	public List<Object[]> getDataForAspirationalNational(
			@Param("formMetaId") Integer formMetaId,@Param("timeperiodIds")List<Integer> timeperiodIds,@Param("states")List<Integer> states);
	
	@Override
	@Query(value = "select ROUND((AVG(fc.score / fc.maxScore)*100),2) as score,fxp.label from LastVisitData lvd join " + 
			"FacilityScore fc on lvd.LastVisitDataId=fc.lastVisitDataId " + 
			" join XForm xf on lvd.FormId=xf.FormId join FormXpathScoreMapping fxp on " + 
			" fxp.formXpathScoreId = fc.formXpathScoreId join Area a on a.AreaId=lvd.AreaId " + 
			"where  xf.stateId in :states and xf.timePeriodId in :timeperiodIds and " + 
			"xf.xform_meta_id=:formMetaId and fxp.parentXpathId in :parenXpathId and lvd.IsLive=1 and a.Aspirational= 1 group by " + 
			"fxp.label ORDER BY fxp.label ASC",nativeQuery = true)
	public List<Object[]> getDataForAspirationalNationalSpiderData(
			@Param("formMetaId") Integer formMetaId,@Param("timeperiodIds")List<Integer> timeperiodIds,@Param("states")List<Integer> states,@Param("parenXpathId")List<Integer>parenXpathId);
	
	@Override
	@Query(value = "select ROUND((AVG(fc.score / fc.maxScore)*100),2) as score,fxp.label,xf.stateId from LastVisitData lvd join " + 
			"FacilityScore fc on lvd.LastVisitDataId=fc.lastVisitDataId " + 
			" join XForm xf on lvd.FormId=xf.FormId join FormXpathScoreMapping fxp on " + 
			" fxp.formXpathScoreId = fc.formXpathScoreId join Area a on a.AreaId=lvd.AreaId " + 
			"where  xf.stateId in :states and xf.timePeriodId in :timeperiodIds and " + 
			"xf.xform_meta_id=:formMetaId and fxp.parentXpathId in :parenXpathId and lvd.IsLive=1 and a.Aspirational= 1 group by " + 
			"fxp.label,xf.stateId ORDER BY fxp.label,xf.stateId ASC",nativeQuery = true)
	public List<Object[]> getDataForAspirationalNationalSpiderDataWithStates(
			@Param("formMetaId") Integer formMetaId,@Param("timeperiodIds")List<Integer> timeperiodIds,@Param("states")List<Integer> states,@Param("parenXpathId")List<Integer>parenXpathId);
	
	
}
