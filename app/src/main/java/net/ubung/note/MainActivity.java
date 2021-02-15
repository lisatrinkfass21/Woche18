package net.ubung.note;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.text.ParseException;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final static String FILE = "notes.csv";
    ListViewAdapter lva;
    List<Note> notes = new ArrayList<>();
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = findViewById(R.id.listView);
        loadNotes();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.save:
                saveNotes();
                break;
            case R.id.add:
                addNote();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void bindAdapterToListView(ListView listView) {
        lva = new ListView(this, R.layout.listview_item, notes);
        listView.setAdapter(lva);
    }

    private void addNote() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(LinearLayout.HORIZONTAL);
        DatePicker datePicker = new DatePicker(MainActivity.this);
        TimePicker timePicker = new TimePicker(MainActivity.this);
        linearLayout2.addView(datePicker);
        linearLayout2.addView(timePicker);
        final EditText titel = new EditText(this);
        final EditText detail = new EditText(this);
        linearLayout.addView(linearLayout2);
        linearLayout.addView(titel);
        linearLayout.addView(detail);


        new AlertDialog.Builder(this)
                .setTitle("new Note")
                .setView(linearLayout)
        .setPositiveButton("ADD", (dialog, which) -> {
            try {
                int month = datePicker.getMonth();
                int year = datePicker.getYear();
                int day = datePicker.getDayOfMonth();
                int hour;
                int minute;
                if (Build.VERSION.SDK_INT >= 23 ){
                    hour = timePicker.getHour();
                   minute = timePicker.getMinute();
                }
                else{
                    hour = timePicker.getCurrentHour();
                    minute = timePicker.getCurrentMinute();
                }
                String date_String = day+"."+month+"."+year+" "+hour+":"+minute;
                Date date = Note.dtf.parse(date_String);
                Note note = new Note(date, titel.getText().toString(),detail.getText().toString());
                notes.add(note);

            } catch (ParseException e) {
            }
        })

                .setNegativeButton("Cancel", null)
                .show();


    }

    private void saveNotes() {
        AssetManager assets = getAssets();
        //assets.
        try {
            FileWriter writer = new FileWriter("notes.csv");
            for (Note no : notes) {
                writer.append(no.toString());
                writer.append("\n");
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private InputStream getAssetsInput() {
        AssetManager assets = getAssets();
        try {
            return assets.open(FILE);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadNotes() {
        String line;
        InputStream is = getAssetsInput();
        if (is != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                while ((line = br.readLine()) != null) {
                    Note n = Note.deser(line);
                    if (n != null) {
                        notes.add(n);
                    } else {
                        Toast.makeText(getApplicationContext(), "parsen hat nicht funktioniert", Toast.LENGTH_LONG).show();
                    }

                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ;
        }
        Toast.makeText(getApplicationContext(), "CSV noch nicht vorhanden", Toast.LENGTH_LONG).show();
    }
}