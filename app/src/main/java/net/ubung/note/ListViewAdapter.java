package net.ubung.note;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ListViewAdapter extends BaseAdapter {

    private int listViewItemLayoutId;
    private List<Note> notes = new ArrayList<>();
    private LayoutInflater inflater;

    public ListViewAdapter(Context con, int listLayoutId, List<Note> no){
        this.listViewItemLayoutId = listLayoutId;
        this.notes.addAll(no);
        this.inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return this.notes.size();
    }

    @Override
    public Object getItem(int position) {
        return notes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Date date = new Date();
        Note note = notes.get(position);
        View listItem = (convertView == null)?
                inflater.inflate(this.listViewItemLayoutId,null) : convertView;
        CheckBox cb = (CheckBox) listItem.findViewById(R.id.checkbox);
        ((TextView) listItem.findViewById(R.id.time)).setText(Note.dtf.format(note.getDatebis()));
        ((TextView) listItem.findViewById(R.id.note)).setText(note.getName());
        cb.setChecked(note.getChecked());
        if(note.getDatebis().compareTo(date)<0){
            TextView time = listItem.findViewById(R.id.time);
            time.setTextColor(Color.RED);
            TextView name = listItem.findViewById(R.id.note);
            name.setTextColor(Color.RED);

        }

        return listItem;
    }
}
