package peterfajdiga.fastdraw.launcher;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import peterfajdiga.fastdraw.Preferences;
import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.launcher.item.LauncherItem;

class CategoryArrayAdapter extends ArrayAdapter<LauncherItem> {

    public CategoryArrayAdapter(Context context) {
        super(context, Preferences.appItemResource);
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity)getContext()).getLayoutInflater().inflate(Preferences.appItemResource, null);
        }
        final LauncherItem item = getItem(position);

        ImageView appIcon = convertView.findViewById(R.id.app_item_icon);
        appIcon.setImageDrawable(item.getIcon());

        TextView appLabel = convertView.findViewById(R.id.app_item_name);
        appLabel.setText(item.getLabel());

        return convertView;
    }

    void sort() {
        sort(LauncherItem::compareTo);
    }
}
