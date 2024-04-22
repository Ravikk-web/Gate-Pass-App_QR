package com.example.gate_pass_app_qr;

public class ReadWriteUserDetails {

    public String enrollno, dob, gender, mobile;
    public String parentName, parentEmail, parentMobile;

    public ReadWriteUserDetails(){}


    public ReadWriteUserDetails(String enrollno, String textDoB, String textGander, String textMoblie){
        this.enrollno = enrollno;
        this.dob = textDoB;
        this.gender = textGander;
        this.mobile = textMoblie;
    }

    public ReadWriteUserDetails(String enrollno, String textDoB, String textGender, String textMoblie, String textParentFullName, String textParentEmail, String textParentMobile){
        this.enrollno = enrollno;
        this.dob = textDoB;
        this.gender = textGender;
        this.mobile = textMoblie;

        this.parentName = textParentFullName;
        this.parentEmail = textParentEmail;
        this.parentMobile = textParentMobile;
    }
}
