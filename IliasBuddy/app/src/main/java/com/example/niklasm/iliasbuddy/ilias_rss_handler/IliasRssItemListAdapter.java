package com.example.niklasm.iliasbuddy.ilias_rss_handler;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.niklasm.iliasbuddy.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;

public class IliasRssItemListAdapter extends RecyclerView.Adapter<IliasRssItemListAdapter.ViewHolder> {

    private final List<IliasRssItem> items;
    private final SimpleDateFormat viewDateFormat;
    private final SimpleDateFormat viewTimeFormat;
    private final Context CONTEXT;
    private final IliasRssItemListAdapterInterface ADAPTER_INTERFACE;
    // Allows to remember the last item shown on screen
    private int lastPosition = -1;

    public IliasRssItemListAdapter(final List<IliasRssItem> dataSet, @NonNull final Context CONTEXT,
                                   @NonNull final IliasRssItemListAdapterInterface ADAPTER_INTERFACE) {
        items = dataSet;
        viewDateFormat = new SimpleDateFormat("dd.MM", CONTEXT.getResources().getConfiguration().locale);
        viewTimeFormat = new SimpleDateFormat("HH:mm", CONTEXT.getResources().getConfiguration().locale);
        this.CONTEXT = CONTEXT;
        this.ADAPTER_INTERFACE = ADAPTER_INTERFACE;
    }


    @Override
    @NonNull
    public IliasRssItemListAdapter.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent,
                                                                 final int viewType) {
        // Create new views (invoked by the layout manager)
        final View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_new, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        // Replace the contents of a view (invoked by the layout manager)
        // - get element from the data set at this position
        // - replace the contents of the view with that element
        final IliasRssItem entry = items.get(position);
        final String description = entry.getDescription();

        // CHECK THIS LATER
        if (ADAPTER_INTERFACE.listAdapterGetLatestEntry() == null ||
                ADAPTER_INTERFACE.listAdapterGetLatestEntry().getDate().getTime()
                        < entry.getDate().getTime()) {
            holder.background.setBackgroundResource(R.color.colorNewEntry);
        }
        // CHECK THIS LATER

        // These views have always these values
        holder.course.setText(entry.getCourse());
        holder.date.setText(viewDateFormat.format(entry.getDate()));
        holder.time.setText(viewTimeFormat.format(entry.getDate()));
        holder.star.setVisibility(View.GONE);
        holder.title.setText(entry.getTitle());

        // if extra is null hide extra card or else set the text
        if (entry.getExtra() == null) {
            holder.extraCard.setVisibility(View.GONE);
        } else {
            holder.extra.setText(entry.getExtra());
        }

        if (entry.getTitleExtra() == null) {
            holder.titleExtraCard.setVisibility(View.GONE);
        } else {
            holder.titleExtra.setText(entry.getTitleExtra());
        }

        if (description == null || description.equals("")) {
            holder.description.setVisibility(View.GONE);
        } else {
            holder.description.setText(Html.fromHtml(description)
                    .toString()
                    .replaceAll("\\s+", " ")
                    .trim());
        }

        if ((description == null || description.equals("")) && entry.getTitleExtra() != null) {
            holder.titleExtra.setText(CONTEXT.getResources().getString(R.string.new_file));
            holder.title.setText(entry.getTitleExtra());
            holder.titleExtraCard.setCardBackgroundColor(
                    ContextCompat.getColor(CONTEXT, android.R.color.holo_red_dark));
        }

            /* if there is no description make title longer and hide it
            if (description == null || description.equals("")) {
                holder.description.setVisibility(View.GONE);
                holder.title.setLines(2);
                holder.title.setText(titleExtra);
                holder.titleExtra.setText(context.getResources().getString(R.string.new_file));
                holder.titleExtraCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.holo_red_dark));
            } else {
            }

            // if there is no title extra and description hide label
            if (titleExtra == null && !(description == null || description.equals(""))) {
                holder.titleExtraCard.setVisibility(View.GONE);
            } else {
                holder.titleExtra.setText(titleExtra);
            }*/


            /*final ImageView starView = holder.star;
            holder.star.setOnClickListener(new View.OnClickListener() {
                private boolean clicked = false;
                @Override
                public void onClick(final View view) {
                    final IliasRssItem entry = dataSet[position];
                    Log.i("MainActivity", "Star clicked " + entry.toString());
                    if (clicked) {
                        starView.setImageResource(R.drawable.ic_star);
                    } else {
                        starView.setImageResource(R.drawable.ic_star_filled);
                    }
                    clicked = !clicked;
                }
            });*/

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
        return items.size();
    }

    /**
     * Provide a reference to the views for each data item - Complex data items may need more
     * than one view per item, and you provide access to all the views for a data item in a
     * view holder
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final public TextView course, title, date, time, description, extra, titleExtra;
        final public LinearLayout background;
        final public ImageView star;
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
            star = itemView.findViewById(R.id.star);
            time = itemView.findViewById(R.id.time);
            title = itemView.findViewById(R.id.title);
            titleExtra = itemView.findViewById(R.id.titleExtra);
            titleExtraCard = itemView.findViewById(R.id.titleExtraCard);
        }

        @Override
        public void onClick(final View view) {
            final int itemPosition = ADAPTER_INTERFACE.listAdapterGetRecyclerViewChildLayoutPosition(view);
            //mRecyclerView.getChildLayoutPosition(view);
            final IliasRssItem entry = items.get(itemPosition);

            if (entry.getDescription() == null || entry.getDescription().equals("")) {
                // if there is no description this means it was an upload
                // therefore instantly link to the Ilias page
                ADAPTER_INTERFACE.listAdapterOpenUrl(entry.getLink());
            } else {
                // if not this must be a legit message for which a popup dialog will be opened
                final String message = ">> " + entry.getTitle() + "\n\n" + Html.fromHtml(entry.getDescription());
                final AlertDialog dialog = new AlertDialog.Builder(CONTEXT)
                        .setTitle(entry.getCourse() + " (" + viewDateFormat.format(entry.getDate()) + ")")
                        .setMessage(message)
                        .setCancelable(true)
                        .setPositiveButton(CONTEXT.getString(R.string.open_in_ilias),
                                (dialog1, id) -> ADAPTER_INTERFACE.listAdapterOpenUrl(entry.getLink()))
                        .setNegativeButton(CONTEXT.getString(R.string.go_back),
                                (dialog12, id) -> dialog12.cancel())
                        .show();
                final TextView textView = Objects.requireNonNull(dialog.getWindow()).getDecorView().findViewById(android.R.id.message);
                textView.setTextIsSelectable(true);
            }
        }
    }
}

