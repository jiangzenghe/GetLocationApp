package com.myapp.getlocation.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 */
@DatabaseTable(tableName="SCENIC_SECTION")
public class ScenicLineSectionModel {
	@DatabaseField(generatedId=true)
    private Integer id;
	@DatabaseField(width=20)
    private String createdTime;
	@DatabaseField(width=20)
	private String scenicId;
	@DatabaseField(width=20)
	private String scenicLineId;
	@DatabaseField(width=20)
    private String linesectionId;
	@DatabaseField(width=20)
    private Integer routeOrder;
    @DatabaseField(width=20)
    private String aspotId;
    @DatabaseField(width=20)
    private String bspotId;

    public String getScenicId() {
        return scenicId;
    }

    public void setScenicId(String scenicId) {
        this.scenicId = scenicId;
    }

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

    public Integer getRouteOrder() {
        return routeOrder;
    }

    public void setRouteOrder(Integer routeOrder) {
        this.routeOrder = routeOrder;
    }

    public String getAspotId() {
        return aspotId;
    }

    public void setAspotId(String aspotId) {
        this.aspotId = aspotId;
    }

    public String getBspotId() {
        return bspotId;
    }

    public void setBspotId(String bspotId) {
        this.bspotId = bspotId;
    }

}