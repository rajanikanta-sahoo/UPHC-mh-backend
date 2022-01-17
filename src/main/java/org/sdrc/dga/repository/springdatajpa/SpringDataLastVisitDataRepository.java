package org.sdrc.dga.repository.springdatajpa;

import java.util.List;
import java.util.Set;

import org.sdrc.dga.domain.Area;
import org.sdrc.dga.domain.LastVisitData;
import org.sdrc.dga.repository.LastVisitDataRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface SpringDataLastVisitDataRepository extends
		LastVisitDataRepository, Repository<LastVisitData, Integer> {
	
	@Override
	@Query("SELECT lastVisitData, score, xForm, formScore,(score.score / score.maxScore)*100 FROM LastVisitData lastVisitData,"
			+" FacilityScore score, XForm xForm, FormXpathScoreMapping formScore "
			+" WHERE lastVisitData.isLive IS TRUE " 
			+" AND lastVisitData.lastVisitDataId = score.lastVisitData.lastVisitDataId "
			+" AND lastVisitData.xForm.formId = xForm.formId "
			+ "AND formScore.formXpathScoreId =:sectorId"
			+" AND score.formXpathScoreMapping.formXpathScoreId = formScore.formXpathScoreId "
			+" AND xForm.formId = :formId"
			+ " AND lastVisitData.timPeriod.timePeriodId = :timeperiodId")
	List<Object[]> getDataByFormId(@Param("formId") Integer formId,@Param("sectorId")Integer sectorId ,@Param("timeperiodId") int timePeriod) throws DataAccessException;

	@Override
	@Query("SELECT lastVisitData, score, formScore, (score.score/score.maxScore)*100 FROM LastVisitData lastVisitData, FacilityScore score, FormXpathScoreMapping formScore "
	+" WHERE lastVisitData.isLive IS TRUE " 
	+" AND lastVisitData.lastVisitDataId = score.lastVisitData.lastVisitDataId "
	+" AND score.formXpathScoreMapping.formXpathScoreId = formScore.formXpathScoreId "
	+" AND lastVisitData.lastVisitDataId = :lastVisitDataId")
	List<Object[]> getByLastVisitData(@Param("lastVisitDataId") Integer lastVisitDataId) throws DataAccessException;
	
	
	@Override
	@Query("SELECT lastVisitData, score, xForm, formScore,(score.score / score.maxScore)*100 FROM LastVisitData lastVisitData,"
			+" FacilityScore score, XForm xForm, FormXpathScoreMapping formScore "
			+" WHERE lastVisitData.isLive IS TRUE "
			+ "AND lastVisitData.area.parentAreaId=:areaId " 
			+" AND score.lastVisitData.lastVisitDataId = lastVisitData.lastVisitDataId "
			+" AND lastVisitData.xForm.formId = xForm.formId "
			+ "AND formScore.formXpathScoreId =:sectorId"
			+" AND score.formXpathScoreMapping.formXpathScoreId = formScore.formXpathScoreId "
			+" AND xForm.formId = :formId"
			+ " AND lastVisitData.timPeriod.timePeriodId = :timeperiodId"
			
			)
	public List<Object[]> getDataByFormIdAndAreaId(@Param("formId") Integer formId,
			@Param("sectorId")Integer sectorId,@Param("areaId") Integer areaId ,@Param("timeperiodId") int timePeriod) throws DataAccessException;
	
	
	@Override
	@Query("SELECT lastVisitData, score, xForm, formScore,(score.score / score.maxScore)*100 FROM LastVisitData lastVisitData,"
			+" FacilityScore score, XForm xForm, FormXpathScoreMapping formScore,Area area "
			+" WHERE lastVisitData.isLive IS TRUE "
			+ " AND area.parentAreaId=:areaId " 
			+ "AND lastVisitData.area.parentAreaId =area.areaId "
			+" AND score.lastVisitData.lastVisitDataId = lastVisitData.lastVisitDataId "
			+" AND lastVisitData.xForm.formId = xForm.formId "
			+ "AND formScore.formXpathScoreId =:sectorId"
			+" AND score.formXpathScoreMapping.formXpathScoreId = formScore.formXpathScoreId "
			+" AND xForm.formId = :formId"
			+ " AND lastVisitData.timPeriod.timePeriodId = :timeperiodId"
			)
	public List<Object[]> getDataByFormIdAndDistrictAreaId(@Param("formId") Integer formId,
			@Param("sectorId")Integer sectorId,@Param("areaId") Integer areaId,@Param("timeperiodId") int timePeriod);
	
	@Override
	@Query("SELECT formScore.label,(score.score / score.maxScore)*100 FROM LastVisitData lastVisitData,"
			+" FacilityScore score, XForm xForm, FormXpathScoreMapping formScore "
			+" WHERE lastVisitData.isLive IS TRUE " 
			+ "AND lastVisitData.area.areaId =:areaId "
			+" AND score.lastVisitData.lastVisitDataId = lastVisitData.lastVisitDataId "
			+" AND lastVisitData.xForm.formId = xForm.formId "
			+ "AND formScore.formXpathScoreId =:sectorId"
			+" AND score.formXpathScoreMapping.formXpathScoreId = formScore.formXpathScoreId "
			+ "AND lastVisitData.timPeriod.timePeriodId= :timeperiodId")
	public List<Object[]> getDataBySectorIdIdAndDistrictAreaId(
			@Param("sectorId")Integer sectorId,@Param("areaId") Integer areaId,@Param("timeperiodId")int timeperiodId);
	
	@Override
	@Query("SELECT formScore.label,score.score,score.maxScore,formScore.formXpathScoreId ,formScore.type "
			+ "FROM LastVisitData lastVisitData,"
			+" FacilityScore score, XForm xForm, FormXpathScoreMapping formScore"
			+" WHERE lastVisitData.isLive IS TRUE " 
			+ "AND lastVisitData.area.areaId =:areaId "
			+" AND score.lastVisitData.lastVisitDataId = lastVisitData.lastVisitDataId "
			+" AND lastVisitData.xForm.formId = xForm.formId "
			+ "AND formScore.parentXpathId =:sectorId"
			+" AND score.formXpathScoreMapping.formXpathScoreId = formScore.formXpathScoreId "
			+ "AND lastVisitData.timPeriod.timePeriodId= :timeperiodId")
	public List<Object[]> getDataByparentSectorIdIdAndDistrictAreaId(
			@Param("sectorId")Integer sectorId,@Param("areaId") Integer areaId,@Param("timeperiodId")int timeperiodId);
	
	
	@Override
	@Query("SELECT MAX(lvd.timPeriod.timePeriodId),MIN(lvd.timPeriod.timePeriodId)"
			+ " FROM LastVisitData lvd "
			+ " WHERE lvd.isLive IS TRUE"
			+ " AND lvd.area.parentAreaId = :areaId"
			+"  AND lvd.xForm.xform_meta_id = :formMetaId "
			+ " GROUP BY lvd.area.parentAreaId")
	public List<Object[]> findMaxMinTimePeriodIdForADistrict(@Param("areaId")Integer areaId,@Param("formMetaId") Integer formMetaId);
	
	
	@Override
	@Query("SELECT MAX(lvd.timPeriod.timePeriodId),MIN(lvd.timPeriod.timePeriodId)"
			+ " FROM LastVisitData lvd ,Area ar"
			+ " WHERE lvd.isLive IS TRUE"
			+ " AND ar.parentAreaId=:areaId"
			+ " AND lvd.area.parentAreaId = ar.areaId"
			+"  AND lvd.xForm.xform_meta_id = :formMetaId "
			+ " GROUP BY ar.parentAreaId")
	public List<Object[]> findMaxMinTimePeriodIdForADistrictPHCCHC(@Param("areaId")Integer areaId,@Param("formMetaId") Integer formMetaId);
	
	
	@Override
	@Query("SELECT MAX(lvd.timPeriod.timePeriodId),MIN(lvd.timPeriod.timePeriodId)"
			+ " FROM LastVisitData lvd "
			+ " WHERE lvd.isLive IS TRUE"
			+ " AND lvd.area.areaId =(SELECT lvd1.area.areaId from LastVisitData lvd1 where lvd1.lastVisitDataId = :lastVisitId)"
			+ " GROUP BY lvd.area.areaId")
	public List<Object[]> findMaxMinTimePeriodIdForAFacility(
			@Param("lastVisitId")Integer lastVisitDataId);
	
	@Override
	@Query("SELECT MAX(lvd.timPeriod.timePeriodId),MIN(lvd.timPeriod.timePeriodId)"
			+ " FROM LastVisitData lvd "
			+ " WHERE lvd.isLive IS TRUE "
			+	" AND lvd.xForm.xform_meta_id = :formMetaId "
			+ " AND lvd.xForm.state.areaId = :stateId"
			)
	public List<Object[]> findMaxMinTimePeriodIdForState(@Param("formMetaId") Integer formMetaId , @Param("stateId") Integer stateId);
	
	
	@Override
	@Query("SELECT MAX(lvd.timPeriod.timePeriodId),MIN(lvd.timPeriod.timePeriodId)"
			+ " FROM LastVisitData lvd "
			+ " WHERE lvd.isLive IS TRUE "
			+	" AND lvd.xForm.xform_meta_id = :formMetaId "
			+ " AND lvd.xForm.state.areaId = :stateId"
			)
	public List<Object[]> findMaxMinTimePeriodIdForHwcState(@Param("formMetaId") Integer formMetaId , @Param("stateId") Integer stateId);
	
	
	@Override
	@Query("SELECT distinct lvd.timPeriod.timePeriodId"
			+ " FROM LastVisitData lvd "
			+ " WHERE lvd.isLive IS TRUE "
			+	" AND lvd.xForm.xform_meta_id = :formMetaId "
			+ " AND lvd.xForm.state.areaId = :stateId"
			)
	public List<Object[]> findDistinctTimePeriodIdForHwcState(@Param("formMetaId") Integer formMetaId , @Param("stateId") Integer stateId);
	

		@Override
		@Query("SELECT lvd FROM LastVisitData lvd "
			+ " WHERE lvd.isLive IS TRUE AND lvd.xForm.formId IN :formId")
		public List<LastVisitData> findByIsLiveTrueAndxFormFormIdIn(@Param("formId")Set<Integer> ids);

		@Override
		@Query("SELECT lvd "
				+ " FROM LastVisitData lvd "
				+ " WHERE lvd.isLive IS TRUE "
				+	" AND lvd.xForm.xform_meta_id = :formMetaId "
				+ " AND lvd.area.areaCode = :areaCode"
				+ " ORDER BY lvd.timPeriod.timePeriodId Asc"
				)
		List<LastVisitData> findByAreaAreaCodeAndIsLiveTrueAndXFormMetaIdOrderByTimPeriodTimePeriodIdAsc(@Param("areaCode") String areaCode,
				@Param("formMetaId")	int formMetaId);
		
		@Override
		@Query("SELECT MAX(lvd.timPeriod.timePeriodId),MAX(lvd.timPeriod.timePeriodId)-1,MIN(lvd.timPeriod.timePeriodId)"
				+ " FROM LastVisitData lvd "
				+ " WHERE lvd.isLive IS TRUE"
				+ " AND lvd.area.parentAreaId = :areaId"
				+"  AND lvd.xForm.xform_meta_id = :formMetaId "
				+ " GROUP BY lvd.area.parentAreaId")
		public List<Object[]> findAllTimePeriodIdForADistrict(@Param("areaId")Integer areaId,@Param("formMetaId") Integer formMetaId);
		
		@Override
		@Query("SELECT MAX(lvd.timPeriod.timePeriodId),MIN(lvd.timPeriod.timePeriodId)"
				+ " FROM LastVisitData lvd "
				+ " WHERE lvd.isLive IS TRUE"
				+ " AND lvd.area.parentAreaId = :areaId"
				+"  AND lvd.xForm.xform_meta_id = :formMetaId "
				+ " GROUP BY lvd.area.parentAreaId")
		public List<Object[]> findAllTimePeriodIdForADistrictv3(@Param("areaId")Integer areaId,@Param("formMetaId") Integer formMetaId);
		
		
		@Override
		@Query("SELECT MAX(lvd.timPeriod.timePeriodId),MAX(lvd.timPeriod.timePeriodId)-1,MIN(lvd.timPeriod.timePeriodId)"
				+ " FROM LastVisitData lvd ,Area ar"
				+ " WHERE lvd.isLive IS TRUE"
				+ " AND ar.parentAreaId=:areaId"
				+ " AND lvd.area.parentAreaId = ar.areaId"
				+"  AND lvd.xForm.xform_meta_id = :formMetaId "
				+ " GROUP BY ar.parentAreaId")
		public List<Object[]> findAllTimePeriodIdForADistrictPHCCHC(@Param("areaId")Integer areaId,@Param("formMetaId") Integer formMetaId);
		
		
		@Override
		@Query("SELECT MAX(lvd.timPeriod.timePeriodId),MAX(lvd.timPeriod.timePeriodId)-1,MIN(lvd.timPeriod.timePeriodId)"
				+ " FROM LastVisitData lvd "
				+ " WHERE lvd.isLive IS TRUE"
				+ " AND lvd.area.areaId =(SELECT lvd1.area.areaId from LastVisitData lvd1 where lvd1.lastVisitDataId = :lastVisitId)"
				+ " GROUP BY lvd.area.areaId")
		public List<Object[]> findAllTimePeriodIdForAFacility(
				@Param("lastVisitId")Integer lastVisitDataId);
		
		@Override
		@Query("SELECT distinct lvd.timPeriod.timePeriodId"
				+ " FROM LastVisitData lvd "
				+ " WHERE lvd.isLive IS TRUE"
				+ " AND lvd.area.areaId =(SELECT lvd1.area.areaId from LastVisitData lvd1 where lvd1.lastVisitDataId = :lastVisitId)"
				+ " GROUP BY lvd.area.areaId")
		public List<Object[]> findAllDistictTimePeriodIdForAFacility(
				@Param("lastVisitId")Integer lastVisitDataId);
		
		@Override
		@Query("SELECT MAX(lvd.timPeriod.timePeriodId),MAX(lvd.timPeriod.timePeriodId)-1,MIN(lvd.timPeriod.timePeriodId)"
				+ " FROM LastVisitData lvd "
				+ " WHERE lvd.isLive IS TRUE "
				+	" AND lvd.xForm.xform_meta_id = :formMetaId "
				+ " AND lvd.xForm.state.areaId = :stateId"
				)
		public List<Object[]> findAllTimePeriodIdForState(@Param("formMetaId") Integer formMetaId , @Param("stateId") Integer stateId);
		
		@Override
		@Query("SELECT MAX(lvd.timPeriod.timePeriodId),MIN(lvd.timPeriod.timePeriodId)"
				+ " FROM LastVisitData lvd ,Area ar"
				+ " WHERE lvd.isLive IS TRUE"
				+ " AND ar.parentAreaId=:areaId"
				+ " AND lvd.area.parentAreaId = ar.areaId"
				+"  AND lvd.xForm.xform_meta_id = :formMetaId "
				+ " GROUP BY ar.parentAreaId")
		public List<Object[]> findAllTimePeriodIdForADistrictPHCCHCForUhcAndHwc(@Param("areaId")Integer areaId,@Param("formMetaId") Integer formMetaId);
		
		@Override
		@Query("SELECT MAX(lvd.timPeriod.timePeriodId),MIN(lvd.timPeriod.timePeriodId)"
				+ " FROM LastVisitData lvd "
				+ " WHERE lvd.isLive IS TRUE"
				+ " AND lvd.area.areaId =(SELECT lvd1.area.areaId from LastVisitData lvd1 where lvd1.lastVisitDataId = :lastVisitId)"
				+ " GROUP BY lvd.area.areaId")
		public List<Object[]> findAllTimePeriodIdForAFacilityOfUhc(
				@Param("lastVisitId")Integer lastVisitDataId);
		
		@Override
		List<LastVisitData> findByIsLiveTrueAndTimPeriodTimePeriodIdAndXFormFormIdIn(int tp,List<Integer> formids);
	
		
		@Override
		@Query("SELECT lastVisitData, score, xForm, formScore,(score.score / score.maxScore)*100 FROM LastVisitData lastVisitData,"
				+" FacilityScore score, XForm xForm, FormXpathScoreMapping formScore "
				+" WHERE lastVisitData.isLive IS TRUE " 
				+" AND lastVisitData.lastVisitDataId = score.lastVisitData.lastVisitDataId "
				+" AND lastVisitData.xForm.formId = xForm.formId "
				+ "AND formScore.formXpathScoreId =:sectorId"
				+" AND score.formXpathScoreMapping.formXpathScoreId = formScore.formXpathScoreId "
				+" AND xForm.formId = :formId"
				+" AND lastVisitData.area.aspirational = true"
				+ " AND lastVisitData.timPeriod.timePeriodId = :timeperiodId")
		List<Object[]> getDataByFormIdOfAspirational(@Param("formId") Integer formId,@Param("sectorId")Integer sectorId ,@Param("timeperiodId") int timePeriod) throws DataAccessException;

		@Override
		@Query("SELECT lastVisitData, score, xForm, formScore,(score.score / score.maxScore)*100 FROM LastVisitData lastVisitData,"
				+" FacilityScore score, XForm xForm, FormXpathScoreMapping formScore,Area area "
				+" WHERE lastVisitData.isLive IS TRUE "
				+ " AND area.parentAreaId=:areaId " 
				+ "AND lastVisitData.area.parentAreaId =area.areaId "
				+" AND score.lastVisitData.lastVisitDataId = lastVisitData.lastVisitDataId "
				+" AND lastVisitData.xForm.formId = xForm.formId "
				+ "AND formScore.formXpathScoreId =:sectorId"
				+" AND score.formXpathScoreMapping.formXpathScoreId = formScore.formXpathScoreId "
				+" AND xForm.formId = :formId"
				+" AND lastVisitData.area.aspirational = true"
				+ " AND lastVisitData.timPeriod.timePeriodId = :timeperiodId"
				)
		public List<Object[]> getDataByFormIdAndDistrictAreaIdOfAspirational(@Param("formId") Integer formId,
				@Param("sectorId")Integer sectorId,@Param("areaId") Integer areaId,@Param("timeperiodId") int timePeriod);

		
		@Override
		@Query("SELECT lastVisitData, score, xForm, formScore,(score.score / score.maxScore)*100 FROM LastVisitData lastVisitData,"
				+" FacilityScore score, XForm xForm, FormXpathScoreMapping formScore "
				+" WHERE lastVisitData.isLive IS TRUE "
				+ "AND lastVisitData.area.parentAreaId=:areaId " 
				+" AND score.lastVisitData.lastVisitDataId = lastVisitData.lastVisitDataId "
				+" AND lastVisitData.xForm.formId = xForm.formId "
				+ "AND formScore.formXpathScoreId =:sectorId"
				+" AND score.formXpathScoreMapping.formXpathScoreId = formScore.formXpathScoreId "
				+" AND xForm.formId = :formId"
				+ " AND lastVisitData.timPeriod.timePeriodId = :timeperiodId"
				
				)
		public List<Object[]> getDataByFormIdAndAreaIdOfAspirational(@Param("formId") Integer formId,
				@Param("sectorId")Integer sectorId,@Param("areaId") Integer areaId ,@Param("timeperiodId") int timePeriod) throws DataAccessException;
		
		@Override
		@Query("SELECT MAX(lvd.timPeriod.timePeriodId),MAX(lvd.timPeriod.timePeriodId)-1,MIN(lvd.timPeriod.timePeriodId)"
				+ " FROM LastVisitData lvd "
				+ " WHERE lvd.isLive IS TRUE"
				+ " AND lvd.area.parentAreaId = :areaId"
				+ " AND lvd.area.aspirational = true"
				+"  AND lvd.xForm.xform_meta_id = :formMetaId "
				+ " GROUP BY lvd.area.parentAreaId")
		public List<Object[]> findAllTimePeriodIdForADistrictOfAspirational(@Param("areaId")Integer areaId,@Param("formMetaId") Integer formMetaId);
		
		@Override
		@Query("SELECT MAX(lvd.timPeriod.timePeriodId),MIN(lvd.timPeriod.timePeriodId)"
				+ " FROM LastVisitData lvd ,Area ar"
				+ " WHERE lvd.isLive IS TRUE"
				+ " AND ar.parentAreaId=:areaId"
				+ " AND lvd.area.parentAreaId = ar.areaId"
				+"  AND lvd.xForm.xform_meta_id = :formMetaId "
				+ " GROUP BY ar.parentAreaId")
		public List<Object[]> findAllTimePeriodIdForADistrictPHCCHCForUhcAndHwcOfAspirational(@Param("areaId")Integer areaId,@Param("formMetaId") Integer formMetaId);
		
		
		@Override
		@Query("SELECT MAX(lvd.timPeriod.timePeriodId),MAX(lvd.timPeriod.timePeriodId)-1,MIN(lvd.timPeriod.timePeriodId)"
				+ " FROM LastVisitData lvd ,Area ar"
				+ " WHERE lvd.isLive IS TRUE"
				+ " AND lvd.area.aspirational = true"
				+ " AND ar.parentAreaId=:areaId"
				+ " AND lvd.area.parentAreaId = ar.areaId"
				+"  AND lvd.xForm.xform_meta_id = :formMetaId "
				+ " GROUP BY ar.parentAreaId")
		public List<Object[]> findAllTimePeriodIdForADistrictPHCCHCOfAspirational(@Param("areaId")Integer areaId,@Param("formMetaId") Integer formMetaId);
		
		
		@Override
		@Query("SELECT MAX(lvd.timPeriod.timePeriodId),MIN(lvd.timPeriod.timePeriodId)"
				+ " FROM LastVisitData lvd "
				+ " WHERE lvd.isLive IS TRUE "
				+ " AND lvd.area.aspirational = true"
				+	" AND lvd.xForm.xform_meta_id = :formMetaId "
				+ " AND lvd.xForm.state.areaId = :stateId"
				)
		public List<Object[]> findMaxMinTimePeriodIdForHwcStateOfAspirational(@Param("formMetaId") Integer formMetaId , @Param("stateId") Integer stateId);
		
		
		@Override
		@Query("SELECT MAX(lvd.timPeriod.timePeriodId),MAX(lvd.timPeriod.timePeriodId)-1,MIN(lvd.timPeriod.timePeriodId)"
				+ " FROM LastVisitData lvd "
				+ " WHERE lvd.isLive IS TRUE "
				+" AND lvd.area.aspirational = true"
				+	" AND lvd.xForm.xform_meta_id = :formMetaId "
				+ " AND lvd.xForm.state.areaId = :stateId"
				)
		public List<Object[]> findAllTimePeriodIdForStateOfAspirational(@Param("formMetaId") Integer formMetaId , @Param("stateId") Integer stateId);
	
		
		@Override
		@Query("SELECT MAX(lvd.timPeriod.timePeriodId),MIN(lvd.timPeriod.timePeriodId)"
				+ " FROM LastVisitData lvd "
				+ " WHERE lvd.isLive IS TRUE"
				+" AND lvd.area.aspirational = true"
				+ " AND lvd.area.areaId =(SELECT lvd1.area.areaId from LastVisitData lvd1 where lvd1.lastVisitDataId = :lastVisitId)"
				+ " GROUP BY lvd.area.areaId")
		public List<Object[]> findAllTimePeriodIdForAFacilityOfUhcOfAspirational(@Param("lastVisitId")Integer lastVisitDataId);
		
		@Override
		@Query("SELECT MAX(lvd.timPeriod.timePeriodId),MAX(lvd.timPeriod.timePeriodId)-1,MIN(lvd.timPeriod.timePeriodId)"
				+ " FROM LastVisitData lvd "
				+ " WHERE lvd.isLive IS TRUE"
				+" AND lvd.area.aspirational = true"
				+ " AND lvd.area.areaId =(SELECT lvd1.area.areaId from LastVisitData lvd1 where lvd1.lastVisitDataId = :lastVisitId)"
				+ " GROUP BY lvd.area.areaId")
		public List<Object[]> findAllTimePeriodIdForAFacilityOfAspirational(@Param("lastVisitId")Integer lastVisitDataId);
		
		
		@Override
		@Query("SELECT lastVisitData, score, xForm, formScore,(score.score / score.maxScore)*100 FROM LastVisitData lastVisitData,"
				+" FacilityScore score, XForm xForm, FormXpathScoreMapping formScore "
				+" WHERE lastVisitData.isLive IS TRUE " 
				+" AND lastVisitData.lastVisitDataId = score.lastVisitData.lastVisitDataId "
				+" AND lastVisitData.xForm.formId = xForm.formId "
				+ "AND formScore.formXpathScoreId =:sectorId"
				+" AND score.formXpathScoreMapping.formXpathScoreId = formScore.formXpathScoreId "
				+" AND xForm.xform_meta_id = :formMetaId"
				+" AND lastVisitData.area.aspirational = true"
				+" And xForm.state in :stateIds AND lastVisitData.timPeriod.timePeriodId = :timeperiodId")
		List<Object[]> getDataByFormIdOfAspirationalNational(@Param("formMetaId") Integer formMetaId,@Param("sectorId")Integer sectorId ,@Param("timeperiodId") int timePeriod,@Param("stateIds")List<Area> stateIds) throws DataAccessException;

		
		@Override
		@Query("select lastVisitData from LastVisitData lastVisitData where area.aspirational is true "
				+ "and isLive is true and timPeriod.timePeriodId in :timePeriodId and xForm.xform_meta_id=:formMetaId")
		List<LastVisitData> findfacilitesCovered(@Param("timePeriodId") List<Integer> timePeriodId,@Param("formMetaId")	Integer formMetaId);
		
		@Override
		@Query(value ="select * from LastVisitData where IsLive =1 and timePeriodId=:timePeriodId and formId=:formId and AreaId in " + 
				"(select a.AreaId from Area a left join Area a2 on a2.AreaId=a.ParentAreaId where a.AreaLevelId=8 and a2.ParentAreaId=:areaId) order by IsFinalized", nativeQuery=true)
		List<LastVisitData> findByDistrictId(@Param("areaId") int areaId,@Param("timePeriodId") int timePeriodId,@Param("formId") int formId);
		
		@Override
		@Modifying(clearAutomatically = true)
		@Query("update LastVisitData set IsLive=0 where IsLive=1 and FormId =:formId and timePeriodId=:timePeriodId and AreaId=:areaId and LastVisitDataId !=:lastVisitDataId")
		void updateLastVisitDataForFinalize(@Param("formId") int formId,@Param("timePeriodId") int timePeriodId,@Param("areaId") int areaId,@Param("lastVisitDataId") int lastVisitDataId);
		
		
		@Override
		@Query(value ="select * from LastVisitData where FormId=:formId and timePeriodId=:timePeriodId and AreaId=:areaid and IsFinalized =1", nativeQuery=true)
		LastVisitData getByXFormTimPeriodAreaIsFinalized(@Param("formId") int formId,@Param("timePeriodId") int timePeriodId,@Param("areaid") int areaid);
		
}
