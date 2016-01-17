package doubtsapp;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import in.ac.iitb.doubtsapp.R;

/**
 * Manager to handle changing filters
 */
public class FiltersManager {

    public enum FilterType {
        TIME_EARLIEST_FIRST ("Earliest First"),
        TIME_LATEST_FIRST ("Latest First"),
        MOST_UPVOTES_FIRST ("Most Upvotes First"),
        LEAST_UPVOTES_FIRST ("Least Upvotes First"),
        RANDOM_ORDER("Random Order");

        private final String name;

        FilterType(String s) {
            name = s;
        }

        public String toString() {
            return name;
        }
    }

    public interface FilterChangedListener {
        void onFilterChanged(FilterType newFilter);
    }

    FilterType currentFilterType;
    FilterChangedListener listener;

    public FiltersManager(FilterChangedListener listener) {
        this.listener = listener;
        currentFilterType = FilterType.TIME_LATEST_FIRST;
    }

    public void onFilterButtonClick(View v) {
        PopupMenu menu = new PopupMenu(v.getContext(), v);
        for (FilterType type : FilterType.values()) {
            addMenuItem(menu, type, v.getResources());
        }
        menu.setOnMenuItemClickListener(
            new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    FilterType newType = null;
                    for (FilterType type : FilterType.values()) {
                        if (type.toString().equals(item.getTitle().toString())) {
                            newType = type;
                            break;
                        }
                    }
                    if (newType == null) return false;
                    if (newType == currentFilterType &&
                        newType != FilterType.RANDOM_ORDER) return true;
                    currentFilterType = newType;
                    listener.onFilterChanged(newType);
                    return true;
                }
            });
        menu.show();
    }

    public void addMenuItem(PopupMenu menu, FilterType filterType, Resources resources) {
        SpannableString menuTitle = new SpannableString(filterType.toString());
        if (filterType == currentFilterType) {
            menuTitle.setSpan(
                new ForegroundColorSpan(resources.getColor(R.color.menu_item_selected)),
                0, menuTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            menuTitle.setSpan(
                new StyleSpan(Typeface.BOLD),
                0, menuTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        menu.getMenu().add(menuTitle);
    }
}
