package doubtsapp;

import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import in.ac.iitb.doubtsapp.R;

/**
 * Binder that binds a doubt to a view with layout doubts_list_item
 */
public class DoubtItemViewBinder {

    public interface DoubtHandler {
        void onUpVoteClick(int doubtId);
        void onNupVoteClick(int doubtId);
        boolean onEditDoubt(int doubtId);
        boolean onDeleteDoubt(int doubtId);
    }

    private static void setBackground(View view, int resId) {
        final int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(ContextCompat.getDrawable(view.getContext(), resId));
        } else {
            view.setBackground(ContextCompat.getDrawable(view.getContext(), resId));
        }
    }

    public static void bind(
        View view,
        final Doubt doubt,
        final DoubtHandler handler,
        final boolean merged) {
        setBackground(
            view,
            merged ? R.drawable.doubt_merge_background : R.drawable.doubt_list_item_background);
        ((TextView) view.findViewById(R.id.name_text))
            .setText(doubt.name +
                (doubt.childCount > 0
                    ? " + " + Integer.toString(doubt.childCount)
                    : ""));
        final TextView timeText = ((TextView) view.findViewById(R.id.time_text));
        timeText.setText(doubt.time);
        ((TextView) view.findViewById(R.id.doubt_text))
            .setText(doubt.getDoubt());
        TextView upvoteText = ((TextView) view.findViewById(R.id.upvote_text));
        SpannableString upvoteString = new SpannableString(Integer.toString(doubt.upVotesCount));
        if (doubt.hasUserUpVoted) {
            StyleSpan span = new StyleSpan(Typeface.BOLD);
            upvoteString.setSpan(span, 0, upvoteString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        upvoteText.setText(upvoteString);
        View.OnClickListener listener = merged ? null : new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (doubt.canUserUpVote) {
                    if (!doubt.hasUserUpVoted) {
                        handler.onUpVoteClick(doubt.DoubtId);
                    } else {
                        handler.onNupVoteClick(doubt.DoubtId);
                    }
                } else {
                    Toast.makeText(v.getContext(), "Can't Upvote Own Doubt", Toast.LENGTH_SHORT)
                        .show();
                }
            }
        };
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (doubt.isOwnDoubt && !merged) {
                    PopupMenu menu = new PopupMenu(v.getContext(), timeText);
                    menu.getMenu().add("Edit");
                    menu.getMenu().add("Delete");
                    menu.setOnMenuItemClickListener(
                        new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                if (item.getTitle().equals("Edit")) {
                                    return handler.onEditDoubt(doubt.DoubtId);
                                } else if (item.getTitle().equals("Delete")) {
                                    return handler.onDeleteDoubt(doubt.DoubtId);
                                }
                                return false;
                            }
                        });
                    menu.show();
                    return true;
                }
                return false;
            }
        });
        view.findViewById(R.id.upvote_icon).setOnClickListener(listener);
        view.findViewById(R.id.upvote_text).setOnClickListener(listener);
    }
}
