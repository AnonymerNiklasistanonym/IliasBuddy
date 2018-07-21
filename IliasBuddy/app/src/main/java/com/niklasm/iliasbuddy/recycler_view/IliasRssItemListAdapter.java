package com.niklasm.iliasbuddy.recycler_view;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.niklasm.iliasbuddy.R;
import com.niklasm.iliasbuddy.handler.IliasBuddyMiscellaneousHandler;
import com.niklasm.iliasbuddy.handler.IliasBuddyPreferenceHandler;
import com.niklasm.iliasbuddy.objects.IliasRssFeedItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IliasRssItemListAdapter extends RecyclerView.Adapter<IliasRssItemListAdapter.ViewHolder>
        implements Filterable {

    private final List<IliasRssFeedItem> items;
    private final SimpleDateFormat viewDateFormat;
    private final SimpleDateFormat viewTimeFormat;
    private final Activity ACTIVITY;
    private final IliasRssItemListAdapterInterface ADAPTER_INTERFACE;

    // Allows to remember the last item shown on screen
    private int lastPosition = -1;
    private List<IliasRssFeedItem> itemsFiltered;
    private String currentSearch = "";

    private boolean showFileChanges;
    private boolean showPosts;

    public IliasRssItemListAdapter(final List<IliasRssFeedItem> dataSet, @NonNull final Activity ACTIVITY,
                                   @NonNull final IliasRssItemListAdapterInterface ADAPTER_INTERFACE) {
        items = dataSet;
        itemsFiltered = dataSet;
        viewDateFormat = new SimpleDateFormat("dd.MM", ACTIVITY.getResources()
                .getConfiguration().locale);
        viewTimeFormat = new SimpleDateFormat("HH:mm", ACTIVITY.getResources()
                .getConfiguration().locale);
        this.ACTIVITY = ACTIVITY;
        this.ADAPTER_INTERFACE = ADAPTER_INTERFACE;

        showFileChanges = IliasBuddyPreferenceHandler.getFilterFileChanges(ACTIVITY, true);
        showPosts = IliasBuddyPreferenceHandler.getFilterPosts(ACTIVITY, true);
    }

    public static void alertDialogRssFeedEntry(@NonNull final IliasRssFeedItem ILIAS_RSS_ITEM,
                                               @NonNull final Activity ACTIVITY) {
        if (ILIAS_RSS_ITEM.getDescription().equals("")) {
            // if there is no description this means it was an upload
            // therefore instantly link to the Ilias page
            IliasBuddyMiscellaneousHandler.openUrl(ACTIVITY, ILIAS_RSS_ITEM.getLink());
        } else {
            // if not this must be a legit message for which a popup dialog will be opened
            final String message = ">> " + ILIAS_RSS_ITEM.getTitle() + "\n\n" +
                    Html.fromHtml(ILIAS_RSS_ITEM.getDescription());
            final AlertDialog dialog = new AlertDialog.Builder(ACTIVITY)
                    .setTitle(ILIAS_RSS_ITEM.getCourse() + " (" +
                            new SimpleDateFormat("dd.MM HH:mm",
                                    ACTIVITY.getResources().getConfiguration().locale)
                                    .format(ILIAS_RSS_ITEM.getDate()) + ")")
                    .setMessage(message)
                    .setCancelable(true)
                    .setPositiveButton(ACTIVITY.getString(R.string.dialog_open),
                            (dialog1, id) -> IliasBuddyMiscellaneousHandler
                                    .openUrl(ACTIVITY, ILIAS_RSS_ITEM.getLink()))
                    .setNegativeButton(ACTIVITY.getString(R.string.dialog_share),
                            (dialog1, id) -> IliasBuddyMiscellaneousHandler
                                    .shareEntry(ACTIVITY, ILIAS_RSS_ITEM))
                    .setNeutralButton(ACTIVITY.getString(R.string.dialog_back),
                            (dialog12, id) -> dialog12.cancel())
                    .show();
            final TextView textView = Objects.requireNonNull(dialog.getWindow()).getDecorView()
                    .findViewById(android.R.id.message);
            textView.setTextIsSelectable(true);
        }
    }

    @Override
    @NonNull
    public IliasRssItemListAdapter.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent,
                                                                 final int viewType) {
        // Create new views (invoked by the layout manager)
        final View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_element, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        // Replace the contents of a view (invoked by the layout manager)
        // - get element from the data set at this position
        // - replace the contents of the view with that element
        final IliasRssFeedItem CURRENT_ELEMENT = itemsFiltered.get(position);

        // These views have always these values and are always visible
        holder.course.setText(CURRENT_ELEMENT.getCourse());
        holder.date.setText(viewDateFormat.format(CURRENT_ELEMENT.getDate()));
        holder.time.setText(viewTimeFormat.format(CURRENT_ELEMENT.getDate()));

        if (CURRENT_ELEMENT.getTitleExtra() != null) {
            holder.titleExtra.setText(CURRENT_ELEMENT.getTitleExtra());
            holder.titleExtraCard.setVisibility(View.VISIBLE);
        } else {
            holder.titleExtraCard.setVisibility(View.GONE);
        }

        // When there is an file update
        if (CURRENT_ELEMENT.isFileUpdate()) {
            // set file name as title
            holder.title.setText(CURRENT_ELEMENT.getExtra());
            // and display if file was updated/added in a red label
            holder.extra.setText(CURRENT_ELEMENT.getTitle());
            holder.extraCard.setVisibility(View.VISIBLE);
            // and hide description
            holder.description.setVisibility(View.GONE);
        } else {
            // else set normal title as title
            holder.title.setText(CURRENT_ELEMENT.getTitle());
            // and hide the red label
            holder.extraCard.setVisibility(View.GONE);
            // and display a description
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(Html.fromHtml(CURRENT_ELEMENT.getDescription())
                    .toString().replaceAll("\\s+", " ").trim());
        }

        // Add animation
        setAnimation(holder.itemView, position);

        /*
        Highlight background if the current entry is new in respect to the latest item of the
         adapter interface - else reset background color to transparent
        */
        final long ADAPTER_INTERFACE_LATEST_ITEM_TIME =
                ADAPTER_INTERFACE.listAdapterGetLatestEntryTime();
        final long CURRENT_ITEM_TIME =
                CURRENT_ELEMENT.getDate().getTime();

        // if interface time == -1 or bigger/same than the current time highlight nothing
        if (ADAPTER_INTERFACE_LATEST_ITEM_TIME == -1 ||
                ADAPTER_INTERFACE_LATEST_ITEM_TIME >= CURRENT_ITEM_TIME) {
            holder.background.setBackgroundResource(R.drawable.ripple);
        } else {
            // otherwise highlight background of item
            holder.background.setBackgroundResource(R.drawable.ripple_highlight);
        }
    }

    /**
     * Here is the key method to apply the animation
     * https://stackoverflow.com/a/26748274/7827128
     */
    private void setAnimation(final View viewToAnimate, final int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            // R.anim.slide_up, slide_in_left
            final Animation animation = AnimationUtils.loadAnimation(ACTIVITY, R.anim.fall_down);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return itemsFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(final CharSequence charSequence) {
                final String charString = charSequence.toString();
                currentSearch = charString;

                if (charString.isEmpty()) {
                    // if query contains nothing display everything
                    itemsFiltered = new ArrayList<>(items);
                } else {
                    // else display only elements that contain the query in some way
                    final List<IliasRssFeedItem> filteredList = new ArrayList<>();
                    for (final IliasRssFeedItem ENTRY : items) {
                        if (ENTRY.containsIgnoreCase(charString, viewDateFormat, viewTimeFormat)) {
                            filteredList.add(ENTRY);
                        }
                    }
                    itemsFiltered = filteredList;
                }

                // additionally filter if file changes or posts should be shown
                if (!showFileChanges || !showPosts) {
                    final List<IliasRssFeedItem> filteredList = new ArrayList<>();
                    // check which entries should be added to the filtered list
                    for (final IliasRssFeedItem ENTRY : itemsFiltered) {
                        if ((!showFileChanges && !ENTRY.isFileUpdate()) && showPosts) {
                            filteredList.add(ENTRY);
                        } else if ((!showPosts && ENTRY.isFileUpdate()) && showFileChanges) {
                            filteredList.add(ENTRY);
                        }
                    }
                    itemsFiltered = filteredList;
                }

                final FilterResults filterResults = new FilterResults();
                filterResults.values = itemsFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(final CharSequence charSequence,
                                          final FilterResults filterResults) {
                if (filterResults != null && filterResults.values != null) {
                    Log.i("publishResults", filterResults.values.toString());
                    final List<?> result = (List<?>) filterResults.values;
                    itemsFiltered = new ArrayList<>();
                    for (final Object object : result) {
                        if (object instanceof IliasRssFeedItem) {
                            itemsFiltered.add((IliasRssFeedItem) object);
                        }
                    }
                }

                // refresh the list with filtered data
                notifyDataSetChanged();
            }
        };
    }

    public void showFileChanges(final boolean b) {
        if (b == showFileChanges) {
            return;
        }
        showFileChanges = b;
        // instantly filter
        getFilter().filter(currentSearch);
    }

    public void showPosts(final boolean b) {
        if (b == showPosts) {
            return;
        }
        showPosts = b;
        // instantly filter
        getFilter().filter(currentSearch);
    }

    public void filter() {
        getFilter().filter(currentSearch);
    }

    /**
     * Provide a reference to the views for each data item - Complex data items may need more
     * than one view per item, and you provide access to all the views for a data item in a
     * view holder
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final public TextView course, title, date, time, description, extra, titleExtra;
        final public LinearLayout background;
        final public CardView extraCard, titleExtraCard;

        private ViewHolder(final View itemView) {
            super(itemView);
            // add on click listener to each view holder
            itemView.setOnClickListener(this);
            // set the views of the view holder
            background = itemView.findViewById(R.id.background);
            course = itemView.findViewById(R.id.course);
            date = itemView.findViewById(R.id.date);
            description = itemView.findViewById(R.id.description);
            extra = itemView.findViewById(R.id.extra);
            extraCard = itemView.findViewById(R.id.extraCard);
            time = itemView.findViewById(R.id.time);
            title = itemView.findViewById(R.id.title);
            titleExtra = itemView.findViewById(R.id.titleExtra);
            titleExtraCard = itemView.findViewById(R.id.titleExtraCard);
        }

        @Override
        public void onClick(final View view) {
            IliasRssItemListAdapter.alertDialogRssFeedEntry(itemsFiltered.get(
                    ADAPTER_INTERFACE.listAdapterGetRecyclerViewChildLayoutPosition(view)),
                    ACTIVITY);
        }
    }
}

