package com.hstelnet.wififorandroidq;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class HSWiFiRecyclerViewHolder extends RecyclerView.ViewHolder {
    public ConstraintLayout parent;
    public TextView textViewSSID, textViewRSSI, textViewType;

    public HSWiFiRecyclerViewHolder(@NonNull View view) {
        super(view);

        parent = view.findViewById(R.id.ITEM_PARENT);
        textViewSSID = view.findViewById(R.id.TEXT_VIEW_SSID);
        textViewRSSI = view.findViewById(R.id.TEXT_VIEW_RSSI);
        textViewType = view.findViewById(R.id.TEXT_VIEW_TYPE);
    }
}
