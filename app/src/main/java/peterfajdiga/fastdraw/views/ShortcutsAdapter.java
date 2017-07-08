package peterfajdiga.fastdraw.views;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collection;

import peterfajdiga.fastdraw.R;

public class ShortcutsAdapter extends RecyclerView.Adapter<ShortcutsAdapter.ShortcutViewHolder> {

    private ResolveInfo[] shortcuts;
    private OnShortcutClickedListener onShortcutClickedListener = null;

    public ShortcutsAdapter(@NonNull ResolveInfo[] shortcuts) {
        this.shortcuts = shortcuts;
    }

    public ShortcutsAdapter(@NonNull Collection<ResolveInfo> shortcuts) {
        this(shortcuts.toArray(new ResolveInfo[shortcuts.size()]));
    }

    public void setOnShortcutClickedListener(@Nullable OnShortcutClickedListener listener) {
        onShortcutClickedListener = listener;
    }

    @Override
    public int getItemCount() {
        return shortcuts.length;
    }

    @Override
    public ShortcutViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final Context context = parent.getContext();
        final View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_grid_item, parent, false);
        view.setOnClickListener(new ItemClickListener());
        view.setOnLongClickListener(new ItemLongClickListener());
        return new ShortcutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ShortcutViewHolder holder, int position) {
        final PackageManager packageManager = holder.view.getContext().getPackageManager();
        final ResolveInfo shortcutItem = shortcuts[position];
        holder.view.setTag(shortcutItem);

        final TextView shortcutLabel = (TextView)holder.view.findViewById(R.id.text);
        shortcutLabel.setText(shortcutItem.loadLabel(packageManager));

        final ImageView shortcutIcon = (ImageView)holder.view.findViewById(R.id.icon);
        shortcutIcon.setImageDrawable(shortcutItem.loadIcon(packageManager));
    }


    class ShortcutViewHolder extends RecyclerView.ViewHolder {
        View view;
        ShortcutViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }
    }


    private class ItemClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (onShortcutClickedListener == null) {
                return;
            }
            final ResolveInfo shortcut = (ResolveInfo)view.getTag();
            onShortcutClickedListener.OnShortcutClicked(shortcut);
        }
    }
    private class ItemLongClickListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View view) {
            if (onShortcutClickedListener == null) {
                return false;
            }
            final ResolveInfo shortcut = (ResolveInfo)view.getTag();
            onShortcutClickedListener.OnShortcutLongClicked(shortcut);
            return true;
        }
    }


    public interface OnShortcutClickedListener {
        void OnShortcutClicked(ResolveInfo shortcut);
        void OnShortcutLongClicked(ResolveInfo shortcut);
    }
}
