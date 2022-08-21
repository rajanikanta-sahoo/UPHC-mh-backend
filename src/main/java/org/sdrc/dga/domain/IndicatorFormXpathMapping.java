/**
 * 
 */
package org.sdrc.dga.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author Harsh Pratyush (harsh@sdrc.co.in)
 *
 */
@Entity
public class IndicatorFormXpathMapping {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int indicatorFormXpathMappingId;

	private String dispansary;

	private String healthPost;

	private String maternityHome;
	
	private String uphc;

	

	

	@Column(nullable = false)
	private String label;
	
	@Column(nullable = false)
	private String sector;
	
	private String subSector;
	
	@Column(nullable = false)
	private String subGroup;
	
	@Column(nullable = false)
	private String type;

	public int getIndicatorFormXpathMappingId() {
		return indicatorFormXpathMappingId;
	}

	public void setIndicatorFormXpathMappingId(int indicatorFormXpathMappingId) {
		this.indicatorFormXpathMappingId = indicatorFormXpathMappingId;
	}

	

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getSector() {
		return sector;
	}

	public void setSector(String sector) {
		this.sector = sector;
	}

	public String getSubSector() {
		return subSector;
	}

	public void setSubSector(String subSector) {
		this.subSector = subSector;
	}

	public String getSubGroup() {
		return subGroup;
	}

	public void setSubGroup(String subGroup) {
		this.subGroup = subGroup;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDispansary() {
		return dispansary;
	}

	public void setDispansary(String dispansary) {
		this.dispansary = dispansary;
	}

	public String getHealthPost() {
		return healthPost;
	}

	public void setHealthPost(String healthPost) {
		this.healthPost = healthPost;
	}

	public String getMaternityHome() {
		return maternityHome;
	}

	public void setMaternityHome(String maternityHome) {
		this.maternityHome = maternityHome;
	}

	public String getUphc() {
		return uphc;
	}

	public void setUphc(String uphc) {
		this.uphc = uphc;
	}

	
}
