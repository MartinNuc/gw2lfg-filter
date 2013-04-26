package cz.nuc.gw2lfg;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: mist
 * Date: 13.4.13
 * Time: 7:49
 * To change this template use File | Settings | File Templates.
 */
public class LookingForGroupAdapter  extends ArrayAdapter<LookingForGroupItem> {

        private LayoutInflater inflater;
        private List<LookingForGroupItem> items;

        public LookingForGroupAdapter(Context context, List<LookingForGroupItem> objects) {
            super(context, 0, objects);
            items = objects;
            inflater = LayoutInflater.from(getContext());
        }

        @Override
        public final View getView(int position, View convertView, ViewGroup parent) {
            View vi=convertView;
            if(convertView==null)  {
                vi = inflater.inflate(R.layout.lfgitem, null);
            }

            TextView name = (TextView)vi.findViewById(R.id.name); // name
            TextView event = (TextView)vi.findViewById(R.id.event); // event
            TextView text = (TextView)vi.findViewById(R.id.text); // text
            TextView old = (TextView)vi.findViewById(R.id.old); // old
            TextView level = (TextView)vi.findViewById(R.id.level); // level

            LookingForGroupItem item = items.get(position);

            // Setting all values in listview
            name.setText(item.getName());
            event.setText(item.getEvent());
            text.setText(item.getText());
            old.setText("(old: " + item.getUpdated() + ")");
            if (item.getLevel() != null)
            {
                level.setText("lvl " + item.getLevel());
            }
            else
            {
                level.setText("");
            }
            return vi;
        }
    }