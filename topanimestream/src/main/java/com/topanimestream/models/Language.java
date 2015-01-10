package com.topanimestream.models;

public class Language {
	private int LanguageId;
	private String Name;
	private String ISO639;
	
	public Language() {
		super();
	}
	
	public Language(int languageId, String name, String iSO639) {
		super();
		LanguageId = languageId;
		Name = name;
		ISO639 = iSO639;
	}
	
	public int getLanguageId() {
		return LanguageId;
	}
	public void setLanguageId(int languageId) {
		LanguageId = languageId;
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public String getISO639() {
		return ISO639;
	}
	public void setISO639(String iSO639) {
		ISO639 = iSO639;
	}
	
}
