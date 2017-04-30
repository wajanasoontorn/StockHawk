package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.ui.DetailActivity;
import com.udacity.stockhawk.utils.NumberFormatUtils;

/**
 * Created by wajanasoontorn on 4/29/17.
 */

public class StockRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor mData = null;

            @Override
            public void onCreate() {
            }

            @Override
            public void onDataSetChanged() {
                if (mData != null) {
                    mData.close();
                }

                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission

                final long identityToken = Binder.clearCallingIdentity();

                mData = getContentResolver().query(
                        Contract.Quote.URI, Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                        null, null, Contract.Quote.COLUMN_SYMBOL);

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (mData != null) {
                    mData.close();
                    mData = null;
                }
            }

            @Override
            public int getCount() {
                return mData == null ? 0 : mData.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        mData == null || !mData.moveToPosition(position)) {
                    return null;
                }

                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.list_item_widget);

                String symbol = mData.getString(Contract.Quote.POSITION_SYMBOL);
                float price = mData.getFloat(Contract.Quote.POSITION_PRICE);
                float rawAbsoluteChange = mData.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
                float percentageChange = mData.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

                String change = NumberFormatUtils.getInstance().getDollarWithPlusString(rawAbsoluteChange);
                String percentage = NumberFormatUtils.getInstance().getPercentageString(percentageChange / 100);

                if (PrefUtils.getDisplayMode(StockRemoteViewsService.this)
                        .equals(getString(R.string.pref_display_mode_absolute_key))) {
                    views.setTextViewText(R.id.change, change);
                    views.setContentDescription(R.id.change, String.format(getString(R.string.a11y_price_change), change));
                } else {
                    views.setTextViewText(R.id.change, percentage);
                    views.setContentDescription(R.id.change, String.format(getString(R.string.a11y_price_change), percentage));
                }

                final String setBackgroundResource = "setBackgroundResource";
                if (rawAbsoluteChange > 0) {
                    views.setInt(R.id.change, setBackgroundResource, R.drawable.percent_change_pill_green);
                } else {
                    views.setInt(R.id.change, setBackgroundResource, R.drawable.percent_change_pill_red);
                }

                String strPrice = NumberFormatUtils.getInstance().getDollarString(price);
                views.setTextViewText(R.id.price, strPrice);
                views.setContentDescription(R.id.price, String.format(getString(R.string.a11y_price), strPrice));
                views.setTextViewText(R.id.symbol, symbol);

                Intent fillInIntent = new Intent();
                fillInIntent.putExtra(DetailActivity.SYMBOL, symbol);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.list_item_widget);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (mData.moveToPosition(position))
                    return mData.getString(Contract.Quote.POSITION_SYMBOL).hashCode();
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
