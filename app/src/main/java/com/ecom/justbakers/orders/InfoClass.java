package com.ecom.justbakers.orders;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InfoClass {

    private String name;
    private String gmail;
    private String phoneNumber;
    private String pincode;
    private String society;
    private String flatNumber;

    public InfoClass(){
    }


    public InfoClass( String gmail, String name, String phoneNumber, String pinCode, String society, String flatNumber){
        this.name = name;
        this.gmail = gmail;
        this.phoneNumber = phoneNumber;
        this.pincode = pinCode;
        this.society = society;
        this.flatNumber = flatNumber;
    }

    public void setName(String name){
        this.name = name;
    }

    /* IMPORTANT FUNCTIONS TO BE IMPLEMENTED ELSE FIREBASE WILL CRASH
       WHILE SETTING THE VALUE OF THE OBJECT */
    public String getName(){
        return name;
    }
    public String getGmail(){
        return gmail;
    }
    public String getPhoneNumber(){
        return phoneNumber;
    }
    public String getPincode(){
        return pincode;
    }
    public String getSociety(){
        return society;
    }
    public String getFlatNumber(){
        return flatNumber;
    }

}
