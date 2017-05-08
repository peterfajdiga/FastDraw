package peterfajdiga.fastdraw.views;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ShortcutArrayAdapter extends ArrayAdapter<ResolveInfo> {

    public ShortcutArrayAdapter(Context context, List<ResolveInfo> shortcuts) {
        super(context, android.R.layout.select_dialog_item, shortcuts);
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView shortcutLabel = (TextView)view.findViewById(android.R.id.text1);
        ResolveInfo item = getItem(position);
        shortcutLabel.setText(item.loadLabel(getContext().getPackageManager()));
        shortcutLabel.setCompoundDrawablesWithIntrinsicBounds(item.loadIcon(getContext().getPackageManager()), null, null, null);
        shortcutLabel.setCompoundDrawablePadding((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getContext().getResources().getDisplayMetrics()));

        return view;
    }
}
