package org.sdrc.dga.repository.springdatajpa;

import java.util.List;

import org.sdrc.dga.domain.Area;
import org.sdrc.dga.repository.AreaRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataAreaRepository extends AreaRepository,
		JpaRepository<Area, Integer> {

	@Override
	@Query("SELECT area.areaId FROM Area area WHERE area.parentAreaId=:paremtAreaID")
	public List<Integer> findAreaIdByParentAreaId(@Param("paremtAreaID")Integer paremtAreaID);
	
	
	@Override
	@Query(value = "select a2.AreaName from Area a1 join Area a2 on a1.ParentAreaId = a2.AreaId where a1.AreaId=:paremtAreaID",nativeQuery = true)
	public String findByDistrictPareantAreaId(@Param("paremtAreaID")Integer paremtAreaID);
	
	@Override
	@Query(value ="SELECT distinct a2.* FROM Area a1 join Area a2 on a2.ParentAreaId=a1.AreaId where a1.ParentAreaId in :areaIds",nativeQuery = true)
	public List<Area> findFacilityAndDistrict(@Param("areaIds")List<Integer> areaIds);
	
	@Override
	@Query("SELECT Distinct l.xForm.state FROM LastVisitData l"
	+" WHERE l.isLive IS TRUE " 
	+" AND l.area.aspirational = true ")
	List<Area> getAspirationalSates() throws DataAccessException;
	
	@Override
	@Query(" select distinct lvd.xForm.state.areaId,max(lvd.xForm.timePeriod.timePeriodId) as maxTimePeriod from LastVisitData lvd where "
			+ "lvd.area.aspirational=true  group by lvd.xForm.state.areaId")
	List<Object[]> getAspirationalSatesAndTimePeriod() throws DataAccessException;
	
	@Override
	@Query(value ="select count(a.AreaId) as counts ,a2.AreaId from Area a join Area a2 on a.ParentAreaId=a2.AreaId where a.Aspirational=1 and a.AreaLevelId=4 group by a2.AreaId",nativeQuery = true)
	List<Object[]> getAspirationalDistCount() throws DataAccessException;
	
	@Override
	@Query(value ="select a1.AreaId as id, a1.AreaName as fasility,a2.AreaName as muncipality,"
			+ "a3.AreaName as district from Area a1 join Area a2 on a2.AreaId=a1.ParentAreaId "
			+ "join Area a3 on a3.AreaId=a2.ParentAreaId where a1.AreaLevelId in (7,8,9,10)",nativeQuery = true)
	List<Object[]> getFacilityDistrics() throws DataAccessException;
}
