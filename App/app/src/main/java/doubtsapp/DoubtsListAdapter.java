package doubtsapp;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.ac.iitb.doubtsapp.R;

/**
 * Created by Ramprakash on 19-08-2015.
 * Adapter handling the list of doubts
 */
public class DoubtsListAdapter extends BaseAdapter {

    private List<Doubt> doubts;
    private Map<Integer,Integer> idToPositionMap;
    private DoubtItemViewBinder.DoubtHandler doubtHandler;

    public DoubtsListAdapter(DoubtItemViewBinder.DoubtHandler doubtHandler) {
        doubts = new ArrayList<>();
        idToPositionMap = new HashMap<>();
        this.doubtHandler = doubtHandler;
    }

    public void addDoubt(Doubt doubt) {
        idToPositionMap.put(doubt.DoubtId, doubts.size());
        doubts.add(doubt);
        notifyDataSetChanged();
    }

    public void deleteDoubt(int doubtId) {
        int position = idToPositionMap.remove(doubtId);
        doubts.remove(position);
        for (int i = position ; i < doubts.size(); i++) {
            idToPositionMap.put(doubts.get(i).DoubtId, i);
        }
        notifyDataSetChanged();
    }

    public void updateDoubt(int doubtId, int newCnt, boolean userChange, boolean isUpvote) {
        Doubt doubt = doubts.get(idToPositionMap.get(doubtId));
        doubt.upVotesCount = newCnt;
        if (userChange) doubt.hasUserUpVoted = isUpvote;
        notifyDataSetChanged();
    }

    public String getDoubt(int doubtId) {
        return doubts.get(idToPositionMap.get(doubtId)).getDoubt();
    }

    public void clearAll() {
        doubts.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return doubts.size();
    }

    @Override
    public Doubt getItem(int position) {
        return doubts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.doubts_list_item,parent, false);
        }
        DoubtItemViewBinder.bind(convertView, getItem(position), doubtHandler);
        return convertView;
    }
}
