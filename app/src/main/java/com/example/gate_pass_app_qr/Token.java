package com.example.gate_pass_app_qr;
import com.google.firebase.Timestamp;
public class Token {

    String studentName, studentId, studentReason, tokenStatus;
    Timestamp timestamp;

    public Token() {

    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    public String getStudentReason() {
        return studentReason;
    }

    public void setStudentReason(String studentReason) {
        this.studentReason = studentReason;
    }

    public String getTokenStatus(){ return tokenStatus;}
    public void setTokenStatus(String tokenStatus){ this.tokenStatus = tokenStatus;}

    public Timestamp getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
