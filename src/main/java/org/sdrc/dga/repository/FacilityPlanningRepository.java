package org.sdrc.dga.repository;

import java.util.List;

import org.sdrc.dga.domain.FacilityPlanning;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 
 * @author Harsh(harsh@sdrc.co.in)
 *
 */
public interface FacilityPlanningRepository {
	
	public FacilityPlanning findByAreaAreaIdAndXFormFormIdAndTimPeriodTimePeriodId(Integer areaId,Integer formId, int timePeriodId);
 
	public List<Integer> findAllTimePeriodNids();
	
	public FacilityPlanning findByAreaAreaIdAndXFormFormIdAndTimPeriodTimePeriodIdAndAreaAspirationalIsTrue(Integer areaId,Integer formId, int timePeriodId);
	
	public Object[] findAreaAspirationalFacilityPlanned(Integer formId,Integer timePeriodId,Integer areaId);
	
	public Object[] findAreaAspirationalFacilityPlannedForNational(Integer formId,List<Integer> timePeriodId);

}
