/**
 * 
 */
package org.sdrc.dga.repository.springdatajpa;

import java.util.List;

import org.sdrc.dga.domain.FacilityPlanning;
import org.sdrc.dga.repository.FacilityPlanningRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author Harsh(harsh@sdrc.co.in)
 *
 */
public interface SpringDataFacilityPlanningRepository
		extends FacilityPlanningRepository, JpaRepository<FacilityPlanning, Integer> {

	@Override
	@Query("SELECT DISTINCT(fp.timeperiodNId) FROM FacilityPlanning fp")
	public List<Integer> findAllTimePeriodNids();

	@Override
	@Query(value = "select sum(facilityPlanned)  FROM FacilityPlanning  where FormId=:formId and timePeriodId=:timePeriodId "
			+ "and AreaId in (select AreaId from Area where Aspirational = 1 and AreaLevelId=4 and ParentAreaId=:areaId)", nativeQuery = true)
	public Object[] findAreaAspirationalFacilityPlanned(@Param("formId") Integer formId,
			@Param("timePeriodId") Integer timePeriodId, @Param("areaId") Integer areaId);

	@Override
	@Query(value = "select sum(fp.facilityPlanned)  FROM FacilityPlanning fp join XForm xf on "
			+ "fp.FormId=xf.FormId where xf.xform_meta_id=:metaId and fp.timePeriodId in :timePeriodIds and "
			+ "fp.AreaId in (select AreaId from Area where Aspirational = 1 and AreaLevelId=4)", nativeQuery = true)
	public Object[] findAreaAspirationalFacilityPlannedForNational(@Param("metaId") Integer metaId,
			@Param("timePeriodIds") List<Integer> timePeriodIds);

}
