package org.topanimestream.models;

public class Status {
	private int StatusId;
	private String Name;
	
	public Status() {
		super();
	}
	public Status(int statusId, String name) {
		super();
		StatusId = statusId;
		Name = name;
	}
	public int getStatusId() {
		return StatusId;
	}
	public void setStatusId(int statusId) {
		StatusId = statusId;
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	
}
