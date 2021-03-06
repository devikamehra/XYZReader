package com.example.xyzreader.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Matteo on 01/07/2015.
 */
public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    public static final int LEFT = 0;

    public static final int RIGHT = 1;

    private Cursor mCursor;
    private Context mContext;

    static class ViewHolder extends RecyclerView.ViewHolder {

        @Bind (R.id.article_image) DynamicHeightNetworkImageView imageView;
        @Bind (R.id.article_title) TextView titleView;
        @Bind (R.id.article_author) TextView authorNameView;
        @Bind (R.id.article_date) TextView dateView;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public MyRecyclerViewAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(ArticleLoader.Query._ID);
    }

    @Override
    public int getItemViewType(int position) {
        if(position % 2 == 0){
            return LEFT;
        }else{
            return RIGHT;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
        View view = mInflater.inflate(R.layout.list_item_article_left, parent, false);

        if(viewType == RIGHT){
            view = mInflater.inflate(R.layout.list_item_article_right, parent, false);
        }

        final ViewHolder vh = new ViewHolder(view);
        view.setOnClickListener((v) -> mContext.startActivity(new Intent(Intent.ACTION_VIEW,
                ItemsContract.Items.buildItemUri(getItemId(vh.getAdapterPosition())))));

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
        holder.authorNameView.setText(mCursor.getString(ArticleLoader.Query.AUTHOR));
        holder.dateView.setText(DateUtils.getRelativeTimeSpanString(
                mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_ALL).toString());
        String url = mCursor.getString(ArticleLoader.Query.PHOTO_URL);
        if (url != null) {
            holder.imageView.setImageUrl(
                    mCursor.getString(ArticleLoader.Query.THUMB_URL),
                    ImageLoaderHelper.getInstance(mContext).getImageLoader());
            holder.imageView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));
        }
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

}
