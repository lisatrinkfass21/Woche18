package net.ubung.note;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public static boolean darkOn = false;

    public final static String FILE = "notes.json";
    ListViewAdapter lva;
    List<Note> notes = new ArrayList<>();

    ListView lv;

    private List<Note> temp = new ArrayList<>();
    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener prefsChangeListener;

    private static final int RQ_WRITE_STORAGE = 12345;
    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = findViewById(R.id.listView);
        registerForContextMenu(lv);
        loadFromSD();
        Collections.sort(notes);
        bindAdapterToListView(lv);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            Window window = getWindow();
            window.setStatusBarColor(Color.parseColor("#87CEFA"));
            System.out.println("set");
        }



        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefsChangeListener = (sharedPrefs, key) -> preferenceChanged(sharedPrefs, key);
        prefs.registerOnSharedPreferenceChangeListener(prefsChangeListener);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object itemObject = parent.getAdapter().getItem(position);
                Note itemDto = (Note) itemObject;

                CheckBox itemCheckbox = (CheckBox) view.findViewById(R.id.checkbox);
                if (itemDto.getChecked()) {
                    itemCheckbox.setChecked(false);
                    itemDto.setChecked(false);
                } else {
                    itemCheckbox.setChecked(true);
                    itemDto.setChecked(true);
                }

            }
        });



        Button selectButton = (Button) findViewById(R.id.delete_selected_rows);
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setMessage("Are you sure to remove selected listview items?");
                alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int size = notes.size();
                        for (int i = 0; i < size; i++) {
                            Note note = notes.get(i);
                            if (note.getChecked()) {
                                notes.remove(i);
                                i--;
                                size = notes.size();
                            }
                        }
                        Collections.sort(notes);
                        bindAdapterToListView(lv);
                        printInput();
                    }
                });
                alertDialog.show();
            }
        });


    }


    public void printInput(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, RQ_WRITE_STORAGE);
            }else{
                saveInSD();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0&&grantResults[0]!=PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "SD Dard Permission verweigert", Toast.LENGTH_LONG).show();
        }else{
            saveInSD();
        }
    }

    public void checkNotify(Note note){
        if (DateUtils.isToday(note.getDatebis().getTime())) {
            if(!note.getChecked()) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("Task zu erledigen")
                        .setContentText(note.getName());
                Intent notifint = new Intent(this, MainActivity.class);
                PendingIntent contint = PendingIntent.getActivity(this,0,notifint, PendingIntent.FLAG_CANCEL_CURRENT);
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(0, builder.build());
                System.out.println("notify");
                Toast.makeText(getApplicationContext(), note.getName()+" muss erledigt werden", Toast.LENGTH_LONG).show();


            }
        }
    }



    private void preferenceChanged(SharedPreferences sharedPrefs, String key) {

        boolean value = sharedPrefs.getBoolean(key, false);
        if (key.equals("Darkmode")) {
            setDarkMode(value);
        } else if (key.equals("anzeigeDateBefore")) {
            temp.addAll(anzeigeDatebefore(value, temp));

        }
    }

    private List<Note> anzeigeDatebefore(boolean value, List<Note> temp) {
        List<Note> alternativeNotes = new ArrayList<>();
        if (value == false) {
            alternativeNotes.clear();
            alternativeNotes.addAll(notes);
            notes.clear();
            for (Note note : alternativeNotes) {
                int diff = note.getDatebis().compareTo(new Date());
                if (diff > -1) {
                    notes.add(note);
                }
            }
            Collections.sort(notes);
            lv = findViewById(R.id.listView);
            bindAdapterToListView(lv);
            return alternativeNotes;
        } else {
            alternativeNotes.addAll(temp);
            for (Note note : alternativeNotes)
                if (!notes.contains(note)) {
                    notes.add(note);
                }
            lv = findViewById(R.id.listView);
            Collections.sort(notes);
            bindAdapterToListView(lv);
            return temp;
        }
    }



    private void setDarkMode(boolean value) {
        View mainView = findViewById(R.id.screen1);
        View root1 = mainView.getRootView();
        lv = findViewById(R.id.listView);
        System.out.println("1");
        if(value) {
            darkOn = true;
            root1.setBackgroundColor(Color.parseColor("#000000"));
            bindAdapterToListView(lv);
            System.out.println("2");

        }else{
            darkOn = false;
            root1.setBackgroundColor(Color.parseColor("#FFFFFF"));
            bindAdapterToListView(lv);
            System.out.println("3");

        }

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
                printInput();
                break;
            case R.id.add:
                addNote();
                break;
            case R.id.preffs:
                Intent intent = new Intent(this, MySettingsActivity.class);
                startActivityForResult(intent, 1);
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
        date.setPadding(60,40,40,0);
        date.setText("Date: " + temp.getFullDateString());
        TextView titel = new TextView(this);
        titel.setPadding(60,40,40,0);
        titel.setText("Title: " + temp.getName());
        TextView detail = new TextView(this);
        detail.setText("Detail: " + temp.getDetail());
        detail.setPadding(60,40,40,0);
        TextView checked = new TextView(this);
        checked.setText("Done: " + String.valueOf(temp.getChecked()));
        checked.setPadding(60,40,40,40);


        ll.addView(date);
        ll.addView(titel);
        ll.addView(detail);
        ll.addView(checked);

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
                        Note note = new Note(titel.getText().toString(), detail.getText().toString(), datu, false);
                        notes.add(note);
                        notes.remove(temp);
                        lv = findViewById(R.id.listView);
                        Collections.sort(notes);
                        bindAdapterToListView(lv);
                        printInput();

                    } catch (ParseException e) {
                    }
                })

                .setNegativeButton("Cancel", null)
                .show();


    }

    private void deleteNote(int position) {
        notes.remove(position);
        bindAdapterToListView(lv);
        printInput();

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
                        if (titel.getText().toString().equals("") || detail.getText().toString().equals("")) {
                            Toast.makeText(this, "Eingabe ist nicht vollständig", Toast.LENGTH_LONG).show();
                        } else {
                            Note note = new Note(titel.getText().toString(), detail.getText().toString(), datu, false);
                            notes.add(note);
                            lv = findViewById(R.id.listView);
                            Collections.sort(notes);
                            bindAdapterToListView(lv);
                            printInput();
                        }


                    } catch (ParseException e) {
                    }
                })

                .setNegativeButton("Cancel", null)
                .show();


    }

    private void saveInSD(){
        String state = Environment.getExternalStorageState();
        if(!state.equals(Environment.MEDIA_MOUNTED))return;
        File output = getExternalFilesDir(null);
        String path = output.getAbsolutePath();
        String fullPath = path+File.separator+FILE;
        try {
            Gson gson = new Gson();
            PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fullPath)));
            out.print(gson.toJson(notes));
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }

    private void loadFromSD(){
        String state = Environment.getExternalStorageState();
        if (!(state.equals(Environment.MEDIA_MOUNTED))) {
            Toast.makeText(this, "There is no any sd card", Toast.LENGTH_LONG).show();}else{
            BufferedReader reader = null;
            try{
                File file = getExternalFilesDir(null);
                File textFile = new File(file.getAbsolutePath()+File.separator+FILE);
                reader = new BufferedReader(new FileReader(textFile));
                String line = reader.readLine();
                Gson gson = new Gson();
                notes.clear();
                TypeToken<List<Note>> token = new TypeToken<List<Note>>(){};
                notes.addAll(gson.fromJson(line, token.getType()));
                Toast.makeText(this, "loaded from SD", Toast.LENGTH_LONG).show();


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    /*

    private void saveNotes() {//old version ... eigenes layout
        try {
            FileOutputStream fos = openFileOutput(FILE, MODE_PRIVATE);
            PrintWriter out = new PrintWriter(new OutputStreamWriter(fos));

            for (Note n : notes) {

                out.println("name=" + n.getName());
                out.println("detail=" + n.getDetail());
                out.println("date=" + n.getFullDateString());
                out.println("checked=" + n.getChecked());
                out.println("new=null");

            }
            out.flush();
            out.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        }
    }


    private void loadNotes() { //old version ... eigenes layout
        String name = "";
        Date date = new Date();
        boolean checked = false;
        String detail = "";
        String line;
        int counter = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput(FILE)))) {
            while ((line = br.readLine()) != null) {

                String[] splittedLine = line.split("=");
                switch (splittedLine[0]) {
                    case "name":
                        name = splittedLine[1];
                        break;
                    case "date":
                        date = Note.dtf.parse(splittedLine[1]);
                        break;
                    case "detail":
                        detail = splittedLine[1];
                        break;
                    case "checked":
                        checked = Boolean.parseBoolean(splittedLine[1]);
                        break;
                    case "new":
                        if (!name.equals("")) {
                            Note note = new Note(name, detail, date, checked);
                            notes.add(note);
                            checkNotify(note);
                            counter++;
                        }
                        break;
                    default:
                        System.out.println("default" + splittedLine[0]);
                }

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (counter == 0) {
            Toast.makeText(getApplicationContext(), "noch keine Tasks vorhanden", Toast.LENGTH_LONG).show();
        }

    }*/
}

//bonus:
// - zeitliche sortierung(vorher zu erledigende tasks oben)
// - automatische speicherungen nach änderungen der listview
// - abgelaufene Tasks in roter Schrift
// - preferences (Anzeige mit abgelaufenen / ohne abgelaufenen Tasks)

//- pushnotification funktioniert nicht ganz / Toast stattdessen
//- darktheme funktioniert