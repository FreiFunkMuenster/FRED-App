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


    protected List<SimpleListable> list;

    public WifiAdapter(List<SimpleListable> list) {
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
        SimpleListable item = list.get(i);
        holder.title.setText(item.getTitle());
        if (item.getTitleColor() != 0) {
            holder.title.setTextColor(item.getTitleColor());
        }
        else {
            holder.title.setTextColor(holder.subtitle.getCurrentTextColor());  // use default text color
        }
        holder.subtitle.setText(item.getSubtitle());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, subtitle;

        public MyViewHolder(View view) {
            super(view);

            title = (TextView) view.findViewById(R.id.network_view_title);
            subtitle = (TextView) view.findViewById(R.id.network_view_level);
        }
    }


}
