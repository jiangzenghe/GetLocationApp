package com.myapp.getlocation.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 */
@DatabaseTable(tableName="SCENIC_AREA")
public class ScenicModel {
	@DatabaseField(generatedId=true)
    private Integer id;
	@DatabaseField(width=20)
    private String createdTime;
	@DatabaseField(width=20)
	private String scenicId;//场景id
    @DatabaseField(width=20)
    private String scenicName;//场景名称
    @DatabaseField(width=30)
    private String scenicLocation;//场景所在地
    @DatabaseField()
    private Double absoluteLongitude;
    @DatabaseField()
    private Double absoluteLatitude;
    @DatabaseField(width=20)
    private String scenicMapurl;

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

    public String getScenicId() {
        return scenicId;
    }

    public void setScenicId(String scenicId) {
        this.scenicId = scenicId;
    }

    public String getScenicName() {
        return scenicName;
    }

    public void setScenicName(String scenicName) {
        this.scenicName = scenicName;
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

    public String getScenicMapurl() {
        return scenicMapurl;
    }

    public void setScenicMapurl(String scenicMapurl) {
        this.scenicMapurl = scenicMapurl;
    }

	public String getScenicLocation() {
		return scenicLocation;
	}

	public void setScenicLocation(String scenicLocation) {
		this.scenicLocation = scenicLocation;
	}

}
