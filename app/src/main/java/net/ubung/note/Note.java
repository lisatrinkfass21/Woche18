package net.ubung.note;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Note {
    private Date date = new Date();
    private String name;
    private String detail;
    static DateFormat dtf;

    public Note(Date date, String name, String detail) {
        this.date = date;
        this.name = name;
        this.detail = detail;
        dtf = new SimpleDateFormat("dd.MM.yyyy hh:mm");
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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

    @Override
    public String toString() {
        return dtf.format(date) + "," + this.name + "," + this.detail;
    }

    public static Note deser(String no) {
        try {
            String[] splittedNote = no.split(",");
            String name = splittedNote[1];
            String detail = splittedNote[2];
            Date date = dtf.parse(splittedNote[0]);
            return new Note(date,name, detail);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}