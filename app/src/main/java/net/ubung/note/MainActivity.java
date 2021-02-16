package net.ubung.note;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.text.ParseException;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

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
        registerForContextMenu(lv);
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        int viewId = v.getId();
        if (viewId == R.id.listView) {
            getMenuInflater().inflate(R.menu.kontext_menu, menu);
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            deleteNote(info.position);

        } else if (item.getItemId() == R.id.edit) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            editNote(info.position);
        } else if (item.getItemId() == R.id.detail) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            showDetail(info.position);
        }
        return super.onContextItemSelected(item);
    }

    private void showDetail(int position) {
        Note temp = notes.get(position);
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        TextView date = new TextView(this);
        date.setText("Date: "+temp.getFullDateString());
        TextView titel = new TextView(this);
        titel.setText("Title: "+temp.getName());
        TextView detail = new TextView(this);
        detail.setText("Detail: "+temp.getDetail());

        ll.addView(date);
        ll.addView(titel);
        ll.addView(detail);

        new AlertDialog.Builder(this)
                .setTitle("Details")
                .setView(ll)
                .show();

    }

    private void editNote(int position) {
        Note temp = notes.get(position);


        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        final EditText date = new EditText(this);
        date.setText(temp.getDateString());
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
                                    date.setText(dayOfMonth + "." + (month + 1) + "." + year);
                                }

                                ;
                            }, y, m, d);
                    datePickerDialog.show();
                }
            }
        });


        final EditText Zeit = new EditText(this);
        Zeit.setText(temp.getTimeString());
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
                                    Zeit.setText(hourOfDay + ":" + minute);
                                }
                            }, h, m, false);
                    timePickerDialog.show();
                }
            }
        });


        final EditText titel = new EditText(this);
        titel.setText(temp.getName());
        final EditText detail = new EditText(this);
        detail.setText(temp.getDetail());
        linearLayout.addView(date);
        linearLayout.addView(but);
        linearLayout.addView(Zeit);
        linearLayout.addView(but2);
        linearLayout.addView(titel);
        linearLayout.addView(detail);


        new AlertDialog.Builder(this)
                .setTitle("edit Note")
                .setView(linearLayout)
                .setPositiveButton("Edit", (dialog, which) -> {
                    try {

                        String date_String = date.getText().toString() + " " + Zeit.getText().toString();
                        Date datu = Note.dtf.parse(date_String);
                        Note note = new Note(datu, titel.getText().toString(), detail.getText().toString());
                        notes.add(note);
                        notes.remove(temp);
                        lv = findViewById(R.id.listView);
                        bindAdapterToListView(lv);

                    } catch (ParseException e) {
                    }
                })

                .setNegativeButton("Cancel", null)
                .show();



    }

    private void deleteNote(int position) {
        notes.remove(position);
        bindAdapterToListView(lv);

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
                                    date.setText(dayOfMonth + "." + (month + 1) + "." + year);
                                }

                                ;
                            }, y, m, d);
                    datePickerDialog.show();
                }
            }
        });


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
                                    Zeit.setText(hourOfDay + ":" + minute);
                                }
                            }, h, m, false);
                    timePickerDialog.show();
                }
            }
        });


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

                        String date_String = date.getText().toString() + " " + Zeit.getText().toString();
                        Date datu = Note.dtf.parse(date_String);
                        Note note = new Note(datu, titel.getText().toString(), detail.getText().toString());
                        notes.add(note);
                        lv = findViewById(R.id.listView);
                        bindAdapterToListView(lv);


                    } catch (ParseException e) {
                    }
                })

                .setNegativeButton("Cancel", null)
                .show();


    }

    private void saveNotes() {

        try {
            FileOutputStream fos = openFileOutput(FILE, MODE_PRIVATE);
            PrintWriter out = new PrintWriter(new OutputStreamWriter(fos));
            for(Note n : notes){
                out.println(n.toString());
            }
            out.flush();
            out.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        }
    }


    private void loadNotes() {
        String line;
        int counter = 0;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput(FILE)))) {

                while ((line = br.readLine()) != null) {
                    Note n = Note.deser(line);
                    if (n != null) {
                        notes.add(n);
                        counter++;
                    } else {
                        Toast.makeText(getApplicationContext(), "parsen hat nicht funktioniert", Toast.LENGTH_LONG).show();
                    }

                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(counter==0)
         {
            Toast.makeText(getApplicationContext(), "CSV noch nicht vorhanden", Toast.LENGTH_LONG).show();
        }

    }
}