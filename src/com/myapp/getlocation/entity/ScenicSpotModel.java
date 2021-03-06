package com.myapp.getlocation.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 
 */
@DatabaseTable(tableName="SCENIC_SPOT")
public class ScenicSpotModel {
	@DatabaseField(generatedId=true)
	private Integer id;
	@DatabaseField(width=20)
    private String createdTime;
	@DatabaseField(width=20)
    private String scenicId;//场景id
	@DatabaseField(width=20)
	private String spotId;//景点id
	@DatabaseField(width=20)
    private String scenicspotName;//场景分景点
	@DatabaseField(width=2)
    private String spotType;//
	@DatabaseField()
    private Double absoluteLongitude;
	@DatabaseField()
    private Double absoluteLatitude;
	@DatabaseField()
    private Double absoluteAltitude;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getCreatedTime() {
		return createdTime;
	}
	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}
	public String getSpotId() {
		return spotId;
	}
	public void setSpotId(String spotId) {
		this.spotId = spotId;
	}
	public String getScenicId() {
		return scenicId;
	}
	public void setScenicId(String scenicId) {
		this.scenicId = scenicId;
	}
	public String getScenicspotName() {
		return scenicspotName;
	}
	public void setScenicspotName(String scenicspotName) {
		this.scenicspotName = scenicspotName;
	}
	public String getSpotType() {
		return spotType;
	}
	public void setSpotType(String spotType) {
		this.spotType = spotType;
	}
	public Double getAbsoluteLongitude() {
		return absoluteLongitude;
	}
	public void setAbsoluteLongitude(Double absoluteLongitude) {
		this.absoluteLongitude = absoluteLongitude;
	}
	public Double getAbsoluteLatitude() {
		return absoluteLatitude;
	}
	public void setAbsoluteLatitude(Double absoluteLatitude) {
		this.absoluteLatitude = absoluteLatitude;
	}
	public Double getAbsoluteAltitude() {
		return absoluteAltitude;
	}
	public void setAbsoluteAltitude(Double absoluteAltitude) {
		this.absoluteAltitude = absoluteAltitude;
	}
	
}
