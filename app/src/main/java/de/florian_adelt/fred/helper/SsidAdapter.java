package de.florian_adelt.fred.helper;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.florian_adelt.fred.R;

public class SsidAdapter extends WifiAdapter {


    public SsidAdapter(List<SimpleListable> list) {
        super(list);
    }

    @Override
    public View getViewFor(ViewGroup parent, int i) {
        View view = super.getViewFor(parent, i);
        view.setOnClickListener(itemClickListener);
        return view;
    }
}
