package peterfajdiga.fastdraw.launcher;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Comparator;

import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.launcher.item.AppItem;
import peterfajdiga.fastdraw.launcher.item.LauncherItem;

class CategoryArrayAdapter extends ArrayAdapter<LauncherItem>{

    public static final int APP_ITEM_STYLE = R.layout.app_item;

    public CategoryArrayAdapter(Context context) {
        super(context, APP_ITEM_STYLE);
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity)getContext()).getLayoutInflater().inflate(CategoryArrayAdapter.APP_ITEM_STYLE, null);
        }
        final LauncherItem item = getItem(position);

//        if (item instanceof ShortcutItem) {
//            convertView.setBackgroundColor(0x40000000);
//        }

        ImageView appIcon = (ImageView)convertView.findViewById(R.id.app_item_icon);
        appIcon.setImageDrawable(item.icon);

        TextView appLabel = (TextView)convertView.findViewById(R.id.app_item_name);
        appLabel.setText(item.name);

        if (CategoryArrayAdapter.APP_ITEM_STYLE == R.layout.app_item_package && item instanceof AppItem) {
            TextView appName = (TextView)convertView.findViewById(R.id.app_item_package_name);
            appName.setText(((AppItem)item).packageName);
        }

        convertView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
            ((CategoryView)parent).interceptTouchTime = Long.MAX_VALUE;
            return false;
            }
        });

        return convertView;
    }

    void sort() {
        sort(new Comparator<LauncherItem>() {
            @Override
            public int compare(LauncherItem launcherItem, LauncherItem t1) {
                return launcherItem.compareTo(t1);
            }
        });
    }
}
