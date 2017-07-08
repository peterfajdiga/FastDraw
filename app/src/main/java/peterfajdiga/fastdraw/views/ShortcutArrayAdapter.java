package peterfajdiga.fastdraw.views;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import peterfajdiga.fastdraw.R;

public class ShortcutArrayAdapter extends ArrayAdapter<ResolveInfo> {

    public ShortcutArrayAdapter(Context context, List<ResolveInfo> shortcuts) {
        super(context, R.layout.bottom_sheet_grid_item, shortcuts);
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.bottom_sheet_grid_item, null);
        }
        final ResolveInfo item = getItem(position);

        final TextView shortcutLabel = (TextView)convertView.findViewById(R.id.text);
        shortcutLabel.setText(item.loadLabel(getContext().getPackageManager()));

        final ImageView shortcutIcon = (ImageView)convertView.findViewById(R.id.icon);
        shortcutIcon.setImageDrawable(item.loadIcon(getContext().getPackageManager()));

        return convertView;
    }
}
