package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;

    @Bind(R.id.backdrop) ImageView mPhoto;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.article_author) TextView mAuthorName;
    @Bind(R.id.article_date) TextView mArticleDate;
    @Bind(R.id.desc) TextView mDesc;
    @Bind(R.id.collapsing_toolbar) CollapsingToolbarLayout layout;
    @Bind(R.id.share_fab) FloatingActionButton mShareFab;
    @Bind(R.id.cardview) CardView cardView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        ButterKnife.bind(this, mRootView);

        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.setSupportActionBar(mToolbar);
        if (appCompatActivity.getSupportActionBar() != null) {
            appCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mShareFab.setOnClickListener((v) ->
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                .setType("text/plain")
                .setText("Some sample text")
                .getIntent(), getString(R.string.action_share))));

        bindViews();
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        bindViews();
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        if (mCursor != null) {

            cardView.setVisibility(View.VISIBLE);
            String title = mCursor.getString(ArticleLoader.Query.TITLE);
            layout.setTitle(title);

            String author = mCursor.getString(ArticleLoader.Query.AUTHOR);
            mAuthorName.setText(author);
            mArticleDate.setText(DateUtils.getRelativeTimeSpanString(
                    mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                    System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_ALL).toString());

            String body = mCursor.getString(ArticleLoader.Query.BODY);
            mDesc.setText(Html.fromHtml(body));

            String url = mCursor.getString(ArticleLoader.Query.PHOTO_URL);
            if (url != null) {
                ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                        .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                            @Override
                            public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                                Bitmap bitmap = imageContainer.getBitmap();
                                if (bitmap != null) {
                                    Palette palette = Palette.generate(bitmap, 12);
                                    Palette.Swatch swatch = palette.getVibrantSwatch();
                                    if (swatch != null) {
                                        layout.setContentScrimColor(swatch.getRgb());
                                        layout.setCollapsedTitleTextColor(swatch.getTitleTextColor());
                                        mPhoto.setImageBitmap(imageContainer.getBitmap());
                                    }
                                }
                            }

                            @Override
                            public void onErrorResponse(VolleyError volleyError) {

                            }
                        });
            }
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, getActivity().getString(R.string.error_text));
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }
}
