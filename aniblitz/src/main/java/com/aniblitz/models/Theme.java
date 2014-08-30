package com.aniblitz.models;

public class Theme {
	private int ThemeId;
	private String Name;
	public Theme(int themeId, String name) {
		super();
		ThemeId = themeId;
		Name = name;
	}
	public int getThemeId() {
		return ThemeId;
	}
	public void setThemeId(int themeId) {
		ThemeId = themeId;
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	
	
}
