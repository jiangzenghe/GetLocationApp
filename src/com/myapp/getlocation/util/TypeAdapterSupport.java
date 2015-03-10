package com.myapp.getlocation.util;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

public abstract class TypeAdapterSupport<T> extends TypeAdapter<T> {
	private Gson gson;

	public Gson getGson() {
		return gson;
	}

	public void setGson(Gson gson) {
		this.gson = gson;
	}
}
