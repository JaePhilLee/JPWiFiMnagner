package com.hstelnet.wififorandroidq;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class HSWiFiRecyclerViewAdapter extends RecyclerView.Adapter<HSWiFiRecyclerViewHolder> {
    private MainActivity activity;
    private List<HSWiFiModel> models;

    public HSWiFiRecyclerViewAdapter(MainActivity activity, List<HSWiFiModel> models) {
        this.activity = activity;
        this.models = models;
    }

    // 필수 오버라이드 : View 생성, ViewHolder 호출
    @NonNull
    @Override
    public HSWiFiRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_wifi_item, parent, false);
        HSWiFiRecyclerViewHolder holder = new HSWiFiRecyclerViewHolder(v);

        return holder;
    }

    // 필수 오버라이드 : 재활용되는 View 가 호출, Adapter 가 해당 position 에 해당하는 데이터를 결합
    @Override
    public void onBindViewHolder(@NonNull final HSWiFiRecyclerViewHolder holder, final int position) {
        holder.textViewSSID.setText(models.get(position).getSSID());
        holder.textViewRSSI.setText(models.get(position).getRSSI());
        holder.textViewType.setText(models.get(position).getPasswordType());

        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showWiFiPopup(position);
            }
        });
    }

    // 필수 오버라이드 : 데이터 갯수 반환
    @Override
    public int getItemCount() {
        return models == null ? 0 : models.size();
    }


    private void showWiFiPopup(final int position) {
        //다이얼로그 셋팅
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setTitle("Wi-Fi 연결")
                .setMessage("Wi-Fi 연결을 위해 비밀번호를 입력해주세요.")
                .setCancelable(false)
                .setView(R.layout.dialog_wifi_input_password)
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("연결", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        ((TextView)alertDialog.findViewById(R.id.TEXT_VIEW_SSID)).setText(models.get(position).getSSID());
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = alertDialog.findViewById(R.id.EDIT_TEXT_PASSWORD);
                if (editText == null) {
                    return;
                }

                models.get(position).setPassword(editText.getText().toString().trim());
                activity.connectWiFi(position);

                alertDialog.dismiss();
            }
        });
    }
}
