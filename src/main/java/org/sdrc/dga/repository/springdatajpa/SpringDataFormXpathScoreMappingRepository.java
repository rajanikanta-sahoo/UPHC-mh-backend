package org.sdrc.dga.repository.springdatajpa;

/**
 * @author Harsh(harsh@sdrc.co.in)
 */
import java.util.List;

import org.sdrc.dga.domain.FormXpathScoreMapping;
import org.sdrc.dga.repository.FormXpathScoreMappingRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface SpringDataFormXpathScoreMappingRepository extends
		FormXpathScoreMappingRepository,
		Repository<FormXpathScoreMapping, Integer> {

	@Override
	@Query(" SELECT fxm from FormXpathScoreMapping fxm,"
			+ " FacilityScore fs,LastVisitData lvd,Area area "
			+ " WHERE fxm.formXpathScoreId=:formXpathScoreId "
			+ " AND fs.formXpathScoreMapping.formXpathScoreId=fxm.formXpathScoreId"
			+ " AND lvd.isLive=true "
			+ " AND area.parentAreaId=:districtId "
			+ " AND lvd.area.parentAreaId=area.areaId "
			+ " AND fs.lastVisitData.lastVisitDataId=lvd.lastVisitDataId ")
	public FormXpathScoreMapping findByFormXpathScoreIdAndDistrictId(
			@Param("formXpathScoreId")Integer formXpathScoreId, 
			@Param("districtId")Integer districtId);
	
	@Override
	@Query(" SELECT fs.formXpathScoreMapping from "
			+ " FacilityScore fs,LastVisitData lvd "
			+ " WHERE fs.formXpathScoreMapping.formXpathScoreId=:formXpathScoreId "
			+ " AND lvd.isLive=true "
			+ " AND lvd.area.parentAreaId=:districtId "
			+ " AND fs.lastVisitData.lastVisitDataId=lvd.lastVisitDataId "	)
	public FormXpathScoreMapping findByFormXpathScoreIdAndDistrictIdForDga(
			@Param("formXpathScoreId")Integer formXpathScoreId, 
			@Param("districtId")Integer districtId);
	
	
	
	@Override
	@Query("SELECT fxm from FormXpathScoreMapping fxm WHERE "
			+ "fxm.form.formId = :formId AND "
			+ "fxm.formXpathScoreId NOT IN (SELECT DISTINCT(fxm1.parentXpathId) FROM FormXpathScoreMapping fxm1)")
	public List<FormXpathScoreMapping> findByFormIdAndWithNoChildren(
			@Param("formId")Integer formId);
	
	@Override
	@Query("SELECT MAX(fxm.formXpathScoreId) from FormXpathScoreMapping fxm")
	public int findLastId();
	
	
	@Override
	@Query(value = "select fxp.formXpathScoreId from FormXpathScoreMapping fxp join XForm xf on xf.FormId = fxp.formId"
			+ " where fxp.parentXpathId=-1 and xf.xform_meta_id=:metaId  and xf.timePeriodId in :timeperiods",nativeQuery = true)
	public List<Integer> getFormXpathScoreId(@Param("metaId")Integer metaId,@Param("timeperiods")List<Integer> timeperiods);

	
	@Override
	@Query(value = "SELECT distinct label,xf.AreaLevelId,xf.xform_meta_id,xf.markerClass FROM "
			+ "FormXpathScoreMapping fs join Program_XForm_Mapping pm on fs.formId=pm.formId join "
			+ "XForm xf on xf.FormId=fs.formId where fs.parentXpathId=-1 and pm.ProgramId=1 group by "
			+ "label,xf.AreaLevelId,xf.xform_meta_id,xf.markerClass order by xf.AreaLevelId",nativeQuery = true)
	public List<Object[]> getFormTypes();
	
	

	@Override
	@Query(value = "SELECT top 1 fm.*  FROM FormXpathScoreMapping fm join XForm xf on xf.FormId=fm.formId where xf.xform_meta_id=:metaId",nativeQuery = true)
	public FormXpathScoreMapping findByFormXformMetaId(@Param("metaId")Integer metaId);
}
