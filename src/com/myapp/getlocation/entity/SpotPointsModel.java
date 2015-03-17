package com.myapp.getlocation.entity;

import java.util.ArrayList;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 
 */
@DatabaseTable(tableName="SCENIC_SPOT_POINTS")
public class SpotPointsModel {
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
	@DatabaseField(dataType= DataType.SERIALIZABLE)
	private ArrayList<Points> spotPoints;
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
	public boolean isSubmited() {
		return isSubmited;
	}
	public void setSubmited(boolean isSubmited) {
		this.isSubmited = isSubmited;
	}
	public String getSpotType() {
		return spotType;
	}
	public void setSpotType(String spotType) {
		this.spotType = spotType;
	}
	public ArrayList<Points> getSpotPoints() {
		return spotPoints;
	}
	public void setSpotPoints(ArrayList<Points> spotPoints) {
		this.spotPoints = spotPoints;
	}
	public int getPointsNum() {
		return pointsNum;
	}
	public void setPointsNum(int pointsNum) {
		this.pointsNum = pointsNum;
	}
	
}
