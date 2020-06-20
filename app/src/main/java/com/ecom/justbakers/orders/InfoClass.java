package com.ecom.justbakers.orders;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InfoClass {

    private String name;
    private String gmail;
    private String phoneNumber;
    private String area;
    private String society;
    private String flatNumber;

    public InfoClass(){
    }


    public InfoClass( String gmail, String name, String phoneNumber, String area, String society, String flatNumber){
        this.name = name;
        this.gmail = gmail;
        this.phoneNumber = phoneNumber;
        this.area = area;
        this.society = society;
        this.flatNumber = flatNumber;


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
    public String getArea(){
        return area;
    }
    public String getSociety(){
        return society;
    }
    public String getFlatNumber(){
        return flatNumber;
    }

}
