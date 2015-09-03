package doubtsapp;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.ac.iitb.doubtsapp.R;

/**
 * Created by Ramprakash on 19-08-2015.
 * Adapter handling the list of doubts
 */
public class DoubtsListAdapter extends BaseAdapter {

    private class EarliestComparator implements Comparator<Doubt> {
        @Override
        public int compare(Doubt lhs, Doubt rhs) {
            if (lhs.DoubtId == rhs.DoubtId) return 0;
            return lhs.DoubtId > rhs.DoubtId ? 1 : -1;
        }
    }

    private class LatestComparator implements Comparator<Doubt> {
        @Override
        public int compare(Doubt lhs, Doubt rhs) {
            if (lhs.DoubtId == rhs.DoubtId) return 0;
            return lhs.DoubtId < rhs.DoubtId ? 1 : -1;
        }
    }

    private class MostUpvotesComparator implements Comparator<Doubt> {
        @Override
        public int compare(Doubt lhs, Doubt rhs) {
            if (lhs.upVotesCount == rhs.upVotesCount) {
                if (lhs.DoubtId == rhs.DoubtId) return 0;
                return lhs.DoubtId > rhs.DoubtId ? 1 : -1;
            } else {
                return lhs.upVotesCount > rhs.upVotesCount ? -1 : 1;
            }
        }
    }

    private class LeastUpvotesComparator implements Comparator<Doubt> {
        @Override
        public int compare(Doubt lhs, Doubt rhs) {
            if (lhs.upVotesCount == rhs.upVotesCount) {
                if (lhs.DoubtId == rhs.DoubtId) return 0;
                return lhs.DoubtId > rhs.DoubtId ? 1 : -1;
            } else {
                return lhs.upVotesCount > rhs.upVotesCount ? 1 : -1;
            }
        }
    }

    private List<Doubt> doubts;
    private Map<Integer, Integer> idToPositionMap;
    private DoubtItemViewBinder.DoubtHandler doubtHandler;
    private Comparator<Doubt> currentComparator;

    public DoubtsListAdapter(DoubtItemViewBinder.DoubtHandler doubtHandler) {
        doubts = new ArrayList<>();
        idToPositionMap = new HashMap<>();
        currentComparator = new EarliestComparator();
        this.doubtHandler = doubtHandler;
    }

    public void setFilterType(FiltersManager.FilterType filter) {
        switch (filter) {
            case TIME_EARLIEST_FIRST:
                currentComparator = new EarliestComparator();
                break;
            case TIME_LATEST_FIRST:
                currentComparator = new LatestComparator();
                break;
            case MOST_UPVOTES_FIRST:
                currentComparator = new MostUpvotesComparator();
                break;
            case LEAST_UPVOTES_FIRST:
                currentComparator = new LeastUpvotesComparator();
                break;
        }
        Collections.sort(doubts, currentComparator);
        resetPositionMap(0);
        notifyDataSetChanged();
    }

    public void addDoubt(Doubt doubt) {
        int position = Collections.binarySearch(doubts, doubt, currentComparator);
        if (position > 0) return;
        doubts.add(-position - 1, doubt);
        resetPositionMap(-position - 1);
        notifyDataSetChanged();
    }

    public void deleteDoubt(int doubtId) {
        int position = idToPositionMap.remove(doubtId);
        doubts.remove(position);
        resetPositionMap(position);
        notifyDataSetChanged();
    }

    public void updateDoubt(int doubtId, int newCnt, boolean userChange, boolean isUpvote) {
        Doubt doubt = doubts.get(idToPositionMap.get(doubtId));
        doubt.upVotesCount = newCnt;
        if (userChange) doubt.hasUserUpVoted = isUpvote;
        Collections.sort(doubts, currentComparator);
        resetPositionMap(0);
        notifyDataSetChanged();
    }

    public String getDoubt(int doubtId) {
        return doubts.get(idToPositionMap.get(doubtId)).getDoubt();
    }

    public void clearAll() {
        doubts.clear();
        idToPositionMap.clear();
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

    private void resetPositionMap(int fromPosition) {
        for (int i = fromPosition; i < doubts.size(); i++) {
            idToPositionMap.put(getItem(i).DoubtId, i);
        }
    }
}
