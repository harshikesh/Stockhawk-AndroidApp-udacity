package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.StockDetailActivity;

/**
 * Created by harshikesh.kumar on 12/10/16.
 */
public class StockWidgetService extends RemoteViewsService {

  @Override public RemoteViewsFactory onGetViewFactory(Intent intent) {
    return new StockRemoteViewsFactory(this.getApplicationContext(), intent);
  }
}

class StockRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
  private static final String TAG = StockRemoteViewsFactory.class.getSimpleName();
  private final int mAppWidgetId;

  private Context mContext;
  private Cursor mCursor;

  public StockRemoteViewsFactory(Context context, Intent intent) {
    mContext = context;
    mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID);
  }

  public void onCreate() {
  }

  public void onDestroy() {
    if (mCursor != null) {
      mCursor.close();
    }
  }

  public int getCount() {
    return mCursor.getCount();
  }

  @Override public RemoteViews getViewAt(int position) {
    Log.d(TAG, "widgetservice : " + position);
    String symbol = "";
    String bidPrice = "";
    String change = "";
    int isUp = 1;
    if (mCursor.moveToPosition(position)) {
      final int symbolColIndex = mCursor.getColumnIndex(QuoteColumns.SYMBOL);
      final int bidPriceColIndex = mCursor.getColumnIndex(QuoteColumns.BIDPRICE);
      final int changeColIndex = mCursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE);
      final int isUpIndex = mCursor.getColumnIndex(QuoteColumns.ISUP);
      symbol = mCursor.getString(symbolColIndex);
      bidPrice = mCursor.getString(bidPriceColIndex);
      change = mCursor.getString(changeColIndex);
      isUp = mCursor.getInt(isUpIndex);
    }
    RemoteViews rvItem = new RemoteViews(mContext.getPackageName(), R.layout.list_item_quote);
    rvItem.setTextViewText(R.id.stock_symbol, symbol);
    rvItem.setTextViewText(R.id.bid_price, bidPrice);
    rvItem.setTextViewText(R.id.change, change);
    if (isUp == 1) {
      rvItem.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
    } else {
      rvItem.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
    }

    // Setting a fill-intent, which will be used to fill in the pending intent template
    // that is set on the collection view in StockWidgetProvider.
    final Intent fillInIntent = new Intent();
    final Bundle extras = new Bundle();
    extras.putString(StockDetailActivity.STOCK_SYMBOL, symbol);
    extras.putString(StockDetailActivity.STOCK_PRICE, bidPrice);
    extras.putString(StockDetailActivity.STOCK_CHANGE, change);
    Log.d(TAG, "widget price : " + bidPrice);
    fillInIntent.putExtras(extras);
    rvItem.setOnClickFillInIntent(R.id.list_item_layout, fillInIntent);
    return rvItem;
  }

  @Override public RemoteViews getLoadingView() {
    return null;
  }

  @Override public int getViewTypeCount() {
    return 1;
  }

  @Override public long getItemId(int position) {
    return position;
  }

  @Override public boolean hasStableIds() {
    return true;
  }

  @Override public void onDataSetChanged() {
    // Refresh the cursor
    Log.d(TAG, "widget :  on data set change" + mCursor);
    if (mCursor != null) {
      mCursor.close();
    }
    mCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI, new String[] {
        QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE, QuoteColumns.PERCENT_CHANGE,
        QuoteColumns.CHANGE, QuoteColumns.ISUP
    }, QuoteColumns.ISCURRENT + " = ?", new String[] { "1" }, null);
  }
}
