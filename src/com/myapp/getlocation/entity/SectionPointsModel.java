package com.myapp.getlocation.entity;

import java.util.ArrayList;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 
 */
@DatabaseTable(tableName="SCENIC_SECTION_POINTS")
public class SectionPointsModel {
	@DatabaseField(generatedId=true)
	private Integer id;
	@DatabaseField(width=20)
    private String createdTime;
	@DatabaseField(width=20)
	private String scenicId;//场景id
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
    @DatabaseField(width=20)
    private String aspotName;
    @DatabaseField(width=20)
    private String bspotName;
	@DatabaseField(dataType= DataType.SERIALIZABLE)
	private ArrayList<Points> sectionPoints;
	@DatabaseField(width=3)
	private int pointsNum;
	@DatabaseField(width=20)
	private boolean isSubmited;
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
	public boolean isSubmited() {
		return isSubmited;
	}
	public void setSubmited(boolean isSubmited) {
		this.isSubmited = isSubmited;
	}
	public int getPointsNum() {
		return pointsNum;
	}
	public void setPointsNum(int pointsNum) {
		this.pointsNum = pointsNum;
	}
	public String getScenicLineId() {
		return scenicLineId;
	}
	public void setScenicLineId(String scenicLineId) {
		this.scenicLineId = scenicLineId;
	}
	public String getLinesectionId() {
		return linesectionId;
	}
	public void setLinesectionId(String linesectionId) {
		this.linesectionId = linesectionId;
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
	public ArrayList<Points> getSectionPoints() {
		return sectionPoints;
	}
	public void setSectionPoints(ArrayList<Points> sectionPoints) {
		this.sectionPoints = sectionPoints;
	}
	public String getAspotName() {
		return aspotName;
	}

	public void setAspotName(String aspotName) {
		this.aspotName = aspotName;
	}

	public String getBspotName() {
		return bspotName;
	}

	public void setBspotName(String bspotName) {
		this.bspotName = bspotName;
	}
}
