package org.sdrc.dga.repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.sdrc.dga.domain.Area;
import org.sdrc.dga.domain.LastVisitData;
import org.sdrc.dga.domain.TimePeriod;
import org.sdrc.dga.domain.XForm;
import org.springframework.dao.DataAccessException;
import org.springframework.data.repository.query.Param;

public interface LastVisitDataRepository {

	List<Object[]> getDataByFormId(Integer formId, Integer sectorId, int timeperiodId) throws DataAccessException;
	
	List<Object[]> getDataByFormIdAndAreaId(Integer formId, Integer sectorId,Integer areaId, int timeperiodId) throws DataAccessException;

	List<Object[]> getByLastVisitData(Integer lastVisitDataId)
			throws DataAccessException;

	List<Object[]> getDataByFormIdAndDistrictAreaId(Integer formId,
			Integer sectorId, Integer areaId, int timeperiodId);
	
	List<Object[]> getDataByFormIdAndMunicipalAreaId(Integer formId,
			Integer sectorId, Integer areaId, int timeperiodId);
	
	List<Object[]> getDataByFormIdAndWordAreaId(Integer formId,
			Integer sectorId, Integer areaId, int timeperiodId);

	List <Object[]> getDataBySectorIdIdAndDistrictAreaId(Integer sectorId,
			Integer areaId, int timeperiodId);

	List<Object[]> getDataByparentSectorIdIdAndDistrictAreaId(Integer sectorId,
			Integer areaId, int timeperiodId);
	
	LastVisitData findByLastVisitDataIdAndIsLiveTrue(Integer lastVisitDataId);

	LastVisitData save(LastVisitData lastVisitDataLocal);

	LastVisitData findByxFormFormIdAndInstanceId(Integer formId,
			String instanceId);

	List<LastVisitData> findAll();

	List<Object[]> findMaxMinTimePeriodIdForADistrict(Integer areaId, Integer formMetaId);

	List<Object[]> findMaxMinTimePeriodIdForState(Integer formMetaId, Integer stateId);
	
	List<Object[]> findMaxMinTimePeriodIdForHwcState(Integer formMetaId, Integer stateId);
	
	List<Object[]> findDistinctTimePeriodIdForHwcState(Integer formMetaId, Integer stateId);

	List<Object[]> findMaxMinTimePeriodIdForAFacility(Integer lastVisitDataId);

	List<Object[]> findMaxMinTimePeriodIdForADistrictPHCCHC(Integer areaId,
			Integer formMetaId);
	
	
	List<Object[]> findAllTimePeriodIdForADistrict(Integer areaId, Integer formMetaId);
	
	List<Object[]> findAllTimePeriodIdForADistrictv3(Integer areaId, Integer formMetaId);

	List<Object[]> findAllTimePeriodIdForState(Integer formMetaId, Integer stateId);
	
	List<Object[]> findAllTimePeriodIdForAFacility(Integer lastVisitDataId);
	
	List<Object[]> findAllDistictTimePeriodIdForAFacility(Integer lastVisitDataId);
	
	

	List<Object[]> findAllTimePeriodIdForADistrictPHCCHC(Integer areaId,
			Integer formMetaId);
	List<Object[]> findAllTimePeriodIdForADistrictPHCCHCForUhcAndHwc(Integer areaId,
			Integer formMetaId);
	List<Object[]> findAllTimePeriodIdForAFacilityOfUhc(Integer lastVisitDataId);
	
	

	LastVisitData findByxFormFormIdAndAreaAreaIdAndTimPeriodTimePeriodId(
			Integer formId, Integer areaId, int i);

	List<LastVisitData> findByTimPeriodTimePeriodId(int i);


//	List<LastVisitData> findByAreaAreaCodeAndIsLiveTrueAndXFormFormIdNotEqualsOrderByTimPeriodTimePeriodIdAsc(
//			String areaCode, Integer i);

	List<LastVisitData> findByAreaAreaCodeAndIsLiveTrueAndXFormFormIdLessThanOrderByTimPeriodTimePeriodIdAsc(
			String areaCode, int i);

//	List<LastVisitData> findByIsLiveTrue();

	List<LastVisitData> findByIsLiveTrueAndxFormFormIdIn(Set<Integer> ids);

	List<LastVisitData> findByLatitudeIsNullAndIsLiveTrue();

	LastVisitData findByAreaAreaIdAndTimPeriodTimePeriodIdAndIsLiveTrueAndLatitudeIsNotNull(
			Integer areaId, int timePeriodId);

	List<LastVisitData> findByAreaAreaCodeAndIsLiveTrueAndXFormMetaIdOrderByTimPeriodTimePeriodIdAsc(String areaCode,
			int formMetaId);
	
	List<LastVisitData> findByAreaAreaCodeAndIsLiveTrueAndXFormMetaIdIsisFinalizedTrueOrderByTimPeriodTimePeriodIdAsc(String areaCode,
			int formMetaId);

	LastVisitData findByxFormFormIdAndInstanceIdAndIsLiveTrue(Integer formId, String instanceId);
	
	List<LastVisitData> findByDateOfVisit(Date date);
	
	List<LastVisitData> findByIsLiveTrueAndTimPeriodTimePeriodIdAndXFormFormIdIn(int tp,List<Integer> formids);
	
	List<Object[]> getDataByFormIdOfAspirational(Integer formId,Integer sectorId , int timePeriod) throws DataAccessException;

    List<Object[]> getDataByFormIdAndDistrictAreaIdOfAspirational( Integer formId,
			Integer sectorId, Integer areaId, int timePeriod);
    
    public List<Object[]> getDataByFormIdAndAreaIdOfAspirational( Integer formId,
			Integer sectorId, Integer areaId ,int timePeriod) throws DataAccessException;
    
    public List<Object[]> findAllTimePeriodIdForADistrictOfAspirational(Integer areaId, Integer formMetaId);
    
    public List<Object[]> findAllTimePeriodIdForADistrictPHCCHCForUhcAndHwcOfAspirational(Integer areaId, Integer formMetaId);
	
    public List<Object[]> findAllTimePeriodIdForADistrictPHCCHCOfAspirational(Integer areaId,Integer formMetaId);
    
    public List<Object[]> findMaxMinTimePeriodIdForHwcStateOfAspirational( Integer formMetaId , Integer stateId);
 
    public List<Object[]> findAllTimePeriodIdForStateOfAspirational( Integer formMetaId ,  Integer stateId);
    
    public List<Object[]> findAllTimePeriodIdForAFacilityOfUhcOfAspirational(Integer lastVisitDataId);
    
    public List<Object[]> findAllTimePeriodIdForAFacilityOfAspirational(Integer lastVisitDataId);
    
    List<Object[]> getDataByFormIdOfAspirationalNational(Integer formMetaId,Integer sectorId,int timePeriod,List<Area> stateIds) throws DataAccessException;
    
    List<LastVisitData> findfacilitesCovered(List<Integer> timePeriodId,Integer formMetaId);
    
    List<LastVisitData> findByAreaAndTimPeriodAndXForm(Area area,TimePeriod timePeriod,XForm xForm);
    
    List<LastVisitData> findByDistrictId(int areaId, int timePeriodId,int formId);
    
    void updateLastVisitDataForFinalize(int formId, int timePeriodId,int areaId,int lastVisitDataId);
    
    LastVisitData getByXFormTimPeriodAreaIsFinalized(int formId,int timePeriodId,int areaid);
    
    List<LastVisitData> findByIsLiveTrueAndTimPeriodTimePeriodIdAndXFormFormId(Integer timePeriodId, Integer xFormId);
    
    List<LastVisitData> getByXFormTimPeriodAreaIsNotFinalized(int formId,int timePeriodId,int areaid);
    
    List<LastVisitData> findByIsLiveTrueAndXFormMetaIdOrderByTimPeriodTimePeriodIdAsc(int formMetaId);
}
