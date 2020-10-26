package peterfajdiga.fastdraw.launcher;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Comparator;

import peterfajdiga.fastdraw.Preferences;
import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.launcher.item.AppItem;
import peterfajdiga.fastdraw.launcher.item.LauncherItem;
import peterfajdiga.fastdraw.launcher.item.Loadable;

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

        /*if (item instanceof ShortcutItem) {
            convertView.setBackgroundColor(0x40000000);
        }*/

        ImageView appIcon = convertView.findViewById(R.id.app_item_icon);
        appIcon.setImageDrawable(item.icon);

        TextView appLabel = convertView.findViewById(R.id.app_item_name);
        appLabel.setText(item.getLabel());

        if (Preferences.appItemResource == R.layout.app_item_package && item instanceof AppItem) {
            TextView appName = convertView.findViewById(R.id.app_item_package_name);
            appName.setText(((AppItem)item).getPackageName());
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


    /* loading */

    public void loadItems() {
        final Context context = getContext();
        final int n = getCount();
        for (int i = 0; i < n; i++) {
            final LauncherItem item = getItem(i);
            if (item instanceof Loadable) {
                ((Loadable)item).load(context);
            }
        }
        sort();
        notifyDataSetChanged();
    }

    public void loadItemsAsync() {
        final ItemLoader itemLoader = new ItemLoader();
        itemLoader.execute();
    }

    private void reportIfLoadFailure() {
        final int n = getCount();
        for (int i = 0; i < n; i++) {
            final LauncherItem item = getItem(i);
            if (item instanceof Loadable && !((Loadable)item).isLoaded()) {
                final Toast toast = Toast.makeText(getContext(), "Could not load: " + item.getIntent(), Toast.LENGTH_LONG);
                toast.show();
                return;
            }
        }
    }

    private final class ItemLoader extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                final Context context = getContext();
                final int n = getCount();
                for (int i = 0; i < n; i++) {
                    final LauncherItem item = getItem(i);
                    if (item instanceof Loadable) {
                        ((Loadable)item).load(context);
                    }
                }
            } catch (Exception e) {
                Log.e("FastDraw_itemLoad", e.toString());
            }
            return null;
        }

        /**
         * this must be called on main thread
         */
        @Override
        protected void onPostExecute(Void result) {
            reportIfLoadFailure();
            sort();
            notifyDataSetChanged();
        }
    }
}
