package de.florian_adelt.fred.helper;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.florian_adelt.fred.R;
import de.florian_adelt.fred.wifi.Wifi;

public class WifiAdapter extends RecyclerView.Adapter<WifiAdapter.MyViewHolder> {


    protected List<Wifi> list;

    public WifiAdapter(List<Wifi> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.wifi, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int i) {
        Wifi wifi = list.get(i);
        holder.ssid.setText(wifi.getSsid());
        holder.level.setText("Level: " + wifi.getLevel());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView ssid, level;

        public MyViewHolder(View view) {
            super(view);

            ssid = (TextView) view.findViewById(R.id.ssid);
            level = (TextView) view.findViewById(R.id.level);
        }
    }


}
