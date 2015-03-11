package com.myapp.getlocation.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 */
@DatabaseTable(tableName="SCENIC_LINE")
public class ScenicLineModel {
	@DatabaseField(generatedId=true)
    private Integer id;
	@DatabaseField(width=20)
    private String createdTime;
	@DatabaseField(width=20)
    private String scenicId;
	@DatabaseField(width=20)
    private String scenicLineId;
	@DatabaseField(width=20)
    private String scenicLinename;

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

	public String getScenicLineId() {
		return scenicLineId;
	}

	public void setScenicLineId(String scenicLineId) {
		this.scenicLineId = scenicLineId;
	}

	public String getScenicLinename() {
		return scenicLinename;
	}

	public void setScenicLinename(String scenicLinename) {
		this.scenicLinename = scenicLinename;
	}

}
