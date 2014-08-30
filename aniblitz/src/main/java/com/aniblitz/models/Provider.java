package com.aniblitz.models;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class Provider implements Parcelable {
	private int providerId;
	private String name;
	
	public Provider() {
		super();
	}
	public Provider(JSONObject jsonProvider)
	{
		try{
		this.providerId = !jsonProvider.isNull("ProviderId") ? jsonProvider.getInt("ProviderId") : 0;
		this.name = !jsonProvider.isNull("Name") ? jsonProvider.getString("Name") : null;
		}catch(Exception e){}
	}
	public Provider(int providerId, String name) {
		super();
		this.providerId = providerId;
		this.name = name;
	}
    public Provider(Parcel in) {
    	providerId = in.readInt();
    	name = in.readString();
    }
	public int getProviderId() {
		return providerId;
	}
	public void setProviderId(int providerId) {
		this.providerId = providerId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public int describeContents() {

		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(providerId);
        dest.writeString(name);
	}
    public static final Parcelable.Creator<Provider> CREATOR = new Parcelable.Creator<Provider>()
    {
        public Provider createFromParcel(Parcel in)
        {
            return new Provider(in);
        }
        public Provider[] newArray(int size)
        {
            return new Provider[size];
        }
    };
}
