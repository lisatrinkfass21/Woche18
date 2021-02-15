package net.ubung.note;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import java.time.Year;
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
        bindAdapterToListView(lv);
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
      lva = new ListViewAdapter(this, R.layout.listview_item, notes);
        listView.setAdapter(lva);
    }

    private void addNote() {//fertig
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);


        final EditText date = new EditText(this);
        date.setHint("Datum");
        Button but = new Button(this);
        but.setText("wähle Datum");
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int y = c.get(Calendar.YEAR);
                int m = c.get(Calendar.MONTH);
                int d = c.get(Calendar.DAY_OF_MONTH);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                            new DatePickerDialog.OnDateSetListener() {

                                @Override
                                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                    date.setText(dayOfMonth + "."+(dayOfMonth+1)+"."+year);
                                };
                            },y,m,d); datePickerDialog.show();
                }
            }});


        final EditText Zeit = new EditText(this);
        Zeit.setHint("Zeit");
        Button but2 = new Button(this);
        but2.setText("wähle Zeit");
        but2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int m = c.get(Calendar.MINUTE);
                int h = c.get(Calendar.HOUR);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                            new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                    Zeit.setText(hourOfDay+":"+minute);
                                }
                            },h,m,false);timePickerDialog.show();
            }}});



        final EditText titel = new EditText(this);
        titel.setHint("Titel");
        final EditText detail = new EditText(this);
        detail.setHint("Detail");
        linearLayout.addView(date);
        linearLayout.addView(but);
        linearLayout.addView(Zeit);
        linearLayout.addView(but2);
        linearLayout.addView(titel);
        linearLayout.addView(detail);


        new AlertDialog.Builder(this)
                .setTitle("new Note")
                .setView(linearLayout)
        .setPositiveButton("ADD", (dialog, which) -> {
            try {

                String date_String =  date.getText().toString()+" "+Zeit.getText().toString();
                Date datu = Note.dtf.parse(date_String);
                Note note = new Note(datu, titel.getText().toString(),detail.getText().toString());
                notes.add(note);

            } catch (ParseException e) {
            }
        })

                .setNegativeButton("Cancel", null)
                .show();


        bindAdapterToListView(lv);


    }

    private void saveNotes() {//noch nicht fertig
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