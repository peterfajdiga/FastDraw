package peterfajdiga.fastdraw.dragdrop;

import android.content.Context;
import android.view.View;

import peterfajdiga.fastdraw.launcher.item.AppItem;

public class DropZoneAppInfo extends DropZone {

    @Override
    protected void onDrop(final View view) {
        final Context context = view.getContext();
        final Owner owner = castToOwner(context);
        //assert owner.getDraggedItem() instanceof AppItem;
        final AppItem appItem = (AppItem)owner.getDraggedItem();
        appItem.openAppDetails(context);
    }
}
