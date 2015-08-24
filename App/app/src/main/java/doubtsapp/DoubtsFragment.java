package doubtsapp;

import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import in.ac.iitb.doubtsapp.R;

/**
 * Created by Ramprakash on 19-08-2015.
 * Fragment that displays all the doubts
 */
public class DoubtsFragment extends Fragment {
    private DoubtsListAdapter doubtsListAdapter;
    private View.OnClickListener addDoubtListener;

    public void setAddDoubtListener(View.OnClickListener listener) {
        addDoubtListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        View view = inflater.inflate(R.layout.doubts_fragment, container, false);
        doubtsListAdapter = new DoubtsListAdapter();
        ListView listView = (ListView) view.findViewById(R.id.doubts_list);
        listView.setAdapter(doubtsListAdapter);
        view.findViewById(R.id.compose_button).setOnClickListener(addDoubtListener);
//        for (int i = 1; i <= 5; i++) {
//            addDoubt("Doubt" + i);
//        }
        return view;
    }

    public void addDoubt(String doubt) {
        doubtsListAdapter.addDoubt(doubt);
    }

    public void clearAll() {
        doubtsListAdapter.clearAll();
    }
}
