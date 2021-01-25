package com.jjkaps.epantry.models.TVData;

import android.os.Parcel;
import android.os.Parcelable;

import javax.annotation.Nullable;

public class TextViewData implements Parcelable {
    private String name;
    private String unitText;
    private String response;
    private int maxLength;

    public TextViewData(String name){
        this.name = name;
        this.unitText = null;
        this.response = null;
        this.maxLength = 100;
    }

    public TextViewData(String name, int maxLength){
        this.name = name;
        this.unitText = null;
        this.response = null;
        this.maxLength = maxLength;
    }

    public TextViewData(String name, String unitText, String response, int maxLength){
        this.name = name;
        this.unitText = unitText;
        this.response = response;
        this.maxLength = maxLength;
    }

    protected TextViewData(Parcel in) {
        name = in.readString();
        unitText = in.readString();
        response = in.readString();
        maxLength = in.readInt();
    }

    public static final Creator<TextViewData> CREATOR = new Creator<TextViewData>() {
        @Override
        public TextViewData createFromParcel(Parcel in) {
            return new TextViewData(in);
        }

        @Override
        public TextViewData[] newArray(int size) {
            return new TextViewData[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    public String getUnitText() {
        return unitText;
    }

    public void setUnitText(String unitText) {
        this.unitText = unitText;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(unitText);
        dest.writeString(response);
        dest.writeInt(maxLength);
    }
}
