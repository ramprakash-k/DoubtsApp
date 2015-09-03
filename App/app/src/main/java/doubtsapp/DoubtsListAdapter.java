package doubtsapp;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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

    private Set<Doubt> doubts;
    private Map<Integer, Integer> positionToIdMap;
    private Map<Integer, Doubt> idToDoubtMap;
    private DoubtItemViewBinder.DoubtHandler doubtHandler;

    public DoubtsListAdapter(DoubtItemViewBinder.DoubtHandler doubtHandler) {
        doubts = new TreeSet<>(new EarliestComparator());
        positionToIdMap = new HashMap<>();
        idToDoubtMap = new HashMap<>();
        this.doubtHandler = doubtHandler;
    }

    public void setFilterType(FiltersManager.FilterType filter) {
        Set<Doubt> temp = new HashSet<>();
        temp.addAll(doubts);
        doubts.clear();
        switch (filter) {
            case TIME_EARLIEST_FIRST:
                doubts = new TreeSet<>(new EarliestComparator());
                break;
            case TIME_LATEST_FIRST:
                doubts = new TreeSet<>(new LatestComparator());
                break;
            case MOST_UPVOTES_FIRST:
                doubts = new TreeSet<>(new MostUpvotesComparator());
                break;
            case LEAST_UPVOTES_FIRST:
                doubts = new TreeSet<>(new LeastUpvotesComparator());
                break;
        }
        doubts.addAll(temp);
        temp.clear();
        resetPositionMap();
        notifyDataSetChanged();
    }

    public void addDoubt(Doubt doubt) {
        doubts.add(doubt);
        idToDoubtMap.put(doubt.DoubtId, doubt);
        resetPositionMap();
        notifyDataSetChanged();
    }

    public void deleteDoubt(int doubtId) {
        Doubt doubt = idToDoubtMap.remove(doubtId);
        doubts.remove(doubt);
        resetPositionMap();
        notifyDataSetChanged();
    }

    public void updateDoubt(int doubtId, int newCnt, boolean userChange, boolean isUpvote) {
        Doubt doubt = idToDoubtMap.get(doubtId);
        doubts.remove(doubt);
        doubt.upVotesCount = newCnt;
        if (userChange) doubt.hasUserUpVoted = isUpvote;
        doubts.add(doubt);
        resetPositionMap();
        notifyDataSetChanged();
    }

    public String getDoubt(int doubtId) {
        return idToDoubtMap.get(doubtId).getDoubt();
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
        return idToDoubtMap.get(positionToIdMap.get(position));
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

    private void resetPositionMap() {
        int i = 0;
        positionToIdMap.clear();
        for (Doubt d : doubts) {
            positionToIdMap.put(i, d.DoubtId);
            i++;
        }
    }
}
