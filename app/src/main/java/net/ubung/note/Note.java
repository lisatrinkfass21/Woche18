package net.ubung.note;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Note {//Task

    private String name;
    private String detail;
    static DateFormat dtf = new SimpleDateFormat("dd.MM.yyyy hh:mm");
    private Date datebis = new Date();
    private boolean done;


    public Note(String name, String detail, Date datebis, boolean done) {
        this.name = name;
        this.detail = detail;
        this.datebis = datebis;
        this.done = done;

    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Date getDatebis() {
        return datebis;
    }

    public void setChecked(boolean done){
        this.done = done;
    }

    public boolean getChecked(){
        return done;
    }

    public void setDatebis(Date datebis) {
        this.datebis = datebis;
    }



    public String getDateString(){
        String date = dtf.format(this.datebis);
        String[] splittedDate = date.split(" ");
        return splittedDate[0];
    }

    public String getTimeString(){
        String date = dtf.format(this.datebis);
        String[] splittedDate = date.split(" ");
        return splittedDate[1];
    }

    public String getFullDateString(){
        return dtf.format(this.datebis);
    }
}