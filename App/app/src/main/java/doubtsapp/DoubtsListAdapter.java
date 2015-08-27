package doubtsapp;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.ac.iitb.doubtsapp.R;

/**
 * Created by Ramprakash on 19-08-2015.
 * Adapter handling the list of doubts
 */
public class DoubtsListAdapter extends BaseAdapter {

    List<Doubt> doubts;
    Boolean isFooterEnabled = false;

    public DoubtsListAdapter() {
        doubts = new ArrayList<>();
    }

//    public void addDoubts(ArrayList<String> doubts) {
//        this.doubts.addAll(doubts);
//        notifyDataSetChanged();
//    }

    public void addDoubt(Doubt doubt) {
        doubts.add(doubt);
        if (getCount() > 9) isFooterEnabled = true;
        notifyDataSetChanged();
    }

    public void clearAll() {
        doubts.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return doubts.size() + (isFooterEnabled ? 1 : 0);
    }

    @Override
    public Doubt getItem(int position) {
        if (position < doubts.size())
            return doubts.get(position);
        else
            return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == getCount() - 1 && isFooterEnabled) {
            return LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.doubt_list_footer, parent, false);
        }
        View view = convertView;
        if (convertView == null) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.doubts_list_item,parent, false);
        }
        Doubt doubt = getItem(position);
        ((TextView) view.findViewById(R.id.doubt_text))
            .setText((doubt.userId != null)
                ? doubt.userId + " : " + doubt.doubt
                : doubt.doubt);
        return view;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == getCount() - 1 && isFooterEnabled) ? 1 : 0;
    }

    @Override
    public int getViewTypeCount() {
        return (isFooterEnabled ? 2 : 1);
    }
}
