package com.example.niklasm.iliasbuddy.ilias_rss_handler;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IliasRssItemListAdapter extends RecyclerView.Adapter<IliasRssItemListAdapter.ViewHolder> implements Filterable {

    private final List<IliasRssItem> items;
    private final SimpleDateFormat viewDateFormat;
    private final SimpleDateFormat viewTimeFormat;
    private final Context CONTEXT;
    private final IliasRssItemListAdapterInterface ADAPTER_INTERFACE;
    // Allows to remember the last item shown on screen
    private int lastPosition = -1;
    private List<IliasRssItem> itemsFiltered;

    public IliasRssItemListAdapter(final List<IliasRssItem> dataSet, @NonNull final Context CONTEXT,
                                   @NonNull final IliasRssItemListAdapterInterface ADAPTER_INTERFACE) {
        items = dataSet;
        itemsFiltered = dataSet;
        viewDateFormat = new SimpleDateFormat("dd.MM", CONTEXT.getResources().getConfiguration().locale);
        viewTimeFormat = new SimpleDateFormat("HH:mm", CONTEXT.getResources().getConfiguration().locale);
        this.CONTEXT = CONTEXT;
        this.ADAPTER_INTERFACE = ADAPTER_INTERFACE;
    }

    public static void alertDialogRssFeedEntry(@NonNull final IliasRssItem ILIAS_RSS_ITEM,
                                               @NonNull final IliasRssItemAlertDialogInterface ILIAS_RSS_ITEM_ALERT_DIALOG_INTERFACE,
                                               @NonNull final Context CONTEXT) {
        if (ILIAS_RSS_ITEM.getDescription() == null || ILIAS_RSS_ITEM.getDescription().equals("")) {
            // if there is no description this means it was an upload
            // therefore instantly link to the Ilias page
            ILIAS_RSS_ITEM_ALERT_DIALOG_INTERFACE.alertDialogOpenUrl(ILIAS_RSS_ITEM.getLink());
        } else {
            // if not this must be a legit message for which a popup dialog will be opened
            final String message = ">> " + ILIAS_RSS_ITEM.getTitle() + "\n\n" + Html.fromHtml(ILIAS_RSS_ITEM.getDescription());
            final AlertDialog dialog = new AlertDialog.Builder(CONTEXT)
                    .setTitle(ILIAS_RSS_ITEM.getCourse() + " (" + new SimpleDateFormat("dd.MM", CONTEXT.getResources().getConfiguration().locale).format(ILIAS_RSS_ITEM.getDate()) + ")")
                    .setMessage(message)
                    .setCancelable(true)
                    .setPositiveButton(CONTEXT.getString(R.string.open_in_ilias),
                            (dialog1, id) -> ILIAS_RSS_ITEM_ALERT_DIALOG_INTERFACE.alertDialogOpenUrl(ILIAS_RSS_ITEM.getLink()))
                    .setNegativeButton(CONTEXT.getString(R.string.go_back),
                            (dialog12, id) -> dialog12.cancel())
                    .show();
            final TextView textView = Objects.requireNonNull(dialog.getWindow()).getDecorView().findViewById(android.R.id.message);
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
        final IliasRssItem CURRENT_ENTRY = itemsFiltered.get(position);
        final String description = CURRENT_ENTRY.getDescription();

        // reset holder
        holder.background.setBackgroundResource(android.R.color.transparent);
        holder.course.setVisibility(View.VISIBLE);
        holder.course.setText("");
        holder.date.setVisibility(View.VISIBLE);
        holder.date.setText("");
        holder.time.setVisibility(View.VISIBLE);
        holder.time.setText("");
        holder.title.setVisibility(View.VISIBLE);
        holder.title.setText("");
        holder.extra.setVisibility(View.VISIBLE);
        holder.extra.setText("");
        holder.description.setVisibility(View.VISIBLE);
        holder.description.setText("");
        holder.extraCard.setVisibility(View.VISIBLE);
        holder.titleExtraCard.setVisibility(View.VISIBLE);
        holder.titleExtraCard.setCardBackgroundColor(ContextCompat.getColor(CONTEXT, R.color.colorPrimary));
        holder.titleExtra.setVisibility(View.VISIBLE);
        holder.titleExtra.setText("");

        // CHECK THIS LATER
        if (ADAPTER_INTERFACE.listAdapterGetLatestEntry() == null ||
                ADAPTER_INTERFACE.listAdapterGetLatestEntry().getDate().getTime()
                        < CURRENT_ENTRY.getDate().getTime()) {
            holder.background.setBackgroundResource(R.color.colorNewEntry);
        }
        // CHECK THIS LATER

        // These views have always these values
        holder.course.setText(CURRENT_ENTRY.getCourse());
        holder.date.setText(viewDateFormat.format(CURRENT_ENTRY.getDate()));
        holder.time.setText(viewTimeFormat.format(CURRENT_ENTRY.getDate()));
        holder.title.setText(CURRENT_ENTRY.getTitle());

        // if extra is null hide extra card or else set the text
        if (CURRENT_ENTRY.getExtra() == null) {
            holder.extraCard.setVisibility(View.GONE);
        } else {
            holder.extra.setText(CURRENT_ENTRY.getExtra());
        }

        if (CURRENT_ENTRY.getTitleExtra() == null) {
            holder.titleExtraCard.setVisibility(View.GONE);
        } else {
            holder.titleExtra.setText(CURRENT_ENTRY.getTitleExtra());
        }

        if (description == null || description.equals("")) {
            holder.description.setVisibility(View.GONE);
        } else {
            holder.description.setText(Html.fromHtml(description)
                    .toString()
                    .replaceAll("\\s+", " ")
                    .trim());
        }

        if ((description == null || description.equals("")) && CURRENT_ENTRY.getTitleExtra() != null) {
            holder.titleExtra.setText(CONTEXT.getResources().getString(R.string.new_file));
            holder.title.setText(CURRENT_ENTRY.getTitleExtra());
            holder.titleExtraCard.setCardBackgroundColor(
                    ContextCompat.getColor(CONTEXT, android.R.color.holo_red_dark));
        }

        setAnimation(holder.itemView, position);
    }

    /**
     * Here is the key method to apply the animation
     * https://stackoverflow.com/a/26748274/7827128
     */
    private void setAnimation(final View viewToAnimate, final int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            // R.anim.slide_up, slide_in_left
            final Animation animation = AnimationUtils.loadAnimation(CONTEXT, R.anim.fall_down);
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
                if (charString.isEmpty()) {
                    itemsFiltered = items;
                } else {
                    final List<IliasRssItem> filteredList = new ArrayList<>();
                    // check which entries should be added to the filtered list
                    for (final IliasRssItem ENTRY : items) {
                        if (ENTRY.containsIgnoreCase(charString, viewDateFormat, viewTimeFormat)) {
                            Log.i("Adapterdebug", "entry added: " + ENTRY.toString());
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
            protected void publishResults(final CharSequence charSequence, final FilterResults filterResults) {
                if (filterResults != null && filterResults.values != null) {
                    Log.i("publishResults", filterResults.values.toString());
                    final List<?> result = (List<?>) filterResults.values;
                    itemsFiltered = new ArrayList<>();
                    for (final Object object : result) {
                        if (object instanceof IliasRssItem) {
                            itemsFiltered.add((IliasRssItem) object);
                        }
                    }
                }

                // refresh the list with filtered data
                notifyDataSetChanged();
            }
        };
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
            final int itemPosition = ADAPTER_INTERFACE.listAdapterGetRecyclerViewChildLayoutPosition(view);
            IliasRssItemListAdapter.alertDialogRssFeedEntry(itemsFiltered.get(itemPosition), ADAPTER_INTERFACE, CONTEXT);
        }
    }
}

