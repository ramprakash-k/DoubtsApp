package doubtsapp;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.HashMap;
import java.util.Map;

import doubtsapp.DoubtItemViewBinder.DoubtHandler;
import in.ac.iitb.doubtsapp.R;

/**
 * Created by Ramprakash on 19-08-2015.
 * Fragment that displays all the doubts
 */
public class DoubtsFragment
    extends Fragment
    implements FiltersManager.FilterChangedListener {

    private DoubtsListAdapter doubtsListAdapter;

    Map<Integer, Doubt> doubts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        View view = inflater.inflate(R.layout.doubts_fragment, container, false);
        doubtsListAdapter = new DoubtsListAdapter((DoubtHandler) getActivity());
        ListView listView = (ListView) view.findViewById(R.id.doubts_list);
        listView.addFooterView(inflater.inflate(R.layout.doubt_list_footer, listView, false));
        listView.setAdapter(doubtsListAdapter);
        view.findViewById(R.id.compose_button).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AddDoubtsPrompt(getActivity()).create().show();
                }
            }
        );
        doubts = new HashMap<>();
        return view;
    }

    public void addDoubt(Doubt tDoubt) {
        if (!doubts.containsKey(tDoubt.DoubtId)) {
            if (tDoubt.linesReceived == tDoubt.lines) {
                doubtsListAdapter.addDoubt(tDoubt);
            } else {
                doubts.put(tDoubt.DoubtId, tDoubt);
            }
        }
        else {
            Doubt doubt = doubts.get(tDoubt.DoubtId);
            doubt.lines = tDoubt.lines;
            doubt.linesReceived++;
            doubt.name = tDoubt.name;
            doubt.rollNo = tDoubt.rollNo;
            doubt.setDoubtLine(1, tDoubt.getFirstLine());
            if (doubt.linesReceived == doubt.lines) {
                doubtsListAdapter.addDoubt(doubt);
                doubts.remove(tDoubt.DoubtId);
            }
        }
    }

    public void appendDoubt(int doubtId ,int line, String dLine) {
        if (doubts.containsKey(doubtId)) {
            Doubt doubt = doubts.get(doubtId);
            doubt.setDoubtLine(line, dLine);
            doubt.linesReceived++;
            if (doubt.linesReceived == doubt.lines) {
                doubtsListAdapter.addDoubt(doubt);
                doubts.remove(doubtId);
            }
        } else {
            Doubt doubt = new Doubt();
            doubt.DoubtId = doubtId;
            doubt.linesReceived++;
            doubt.setDoubtLine(line, dLine);
            doubts.put(doubtId, doubt);
        }
    }

    public String getDoubt(int doubtId) {
        return doubtsListAdapter.getDoubt(doubtId);
    }

    public void clearAll() {
        doubtsListAdapter.clearAll();
    }

    public void updateUpvoteCount(int doubtId, int newCnt, boolean userChange, boolean isUpvote) {
        doubtsListAdapter.updateDoubt(doubtId, newCnt, userChange, isUpvote);
    }

    public void deleteDoubt(int doubtId) {
        doubts.remove(doubtId);
        doubtsListAdapter.deleteDoubt(doubtId);
    }

    @Override
    public void onFilterChanged(FiltersManager.FilterType newFilter) {
        doubtsListAdapter.setFilterType(newFilter);
    }
}
