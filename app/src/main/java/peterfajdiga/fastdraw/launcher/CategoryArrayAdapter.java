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

import peterfajdiga.fastdraw.Preferences;
import peterfajdiga.fastdraw.R;
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
        appIcon.setImageDrawable(item.getIcon());

        TextView appLabel = convertView.findViewById(R.id.app_item_name);
        appLabel.setText(item.getLabel());

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
        sort(LauncherItem::compareTo);
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
                Log.e("CategoryArrayAdapter", "Could not load launcher item: " + item.getID());
                // TODO (BUG): retry?
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
