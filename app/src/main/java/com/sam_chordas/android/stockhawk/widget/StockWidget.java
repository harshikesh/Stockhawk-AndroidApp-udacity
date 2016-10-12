package com.sam_chordas.android.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.ui.StockDetailActivity;

/**
 * Created by harshikesh.kumar on 12/10/16.
 */
public class StockWidget extends AppWidgetProvider {

  private static final String CLICK_ACTION = "CLICK_ACTION";

  private static final String TAG = StockWidget.class.getSimpleName();

  @Override public void onReceive(Context context, Intent intent) {
    Log.d(TAG, "widget broadcast receive" + intent.getAction());
    final String action = intent.getAction();
    if (action.equals(CLICK_ACTION)) {
      final String symbol = intent.getStringExtra(StockDetailActivity.STOCK_SYMBOL);
      final String price = intent.getStringExtra(StockDetailActivity.STOCK_PRICE);
      final String change = intent.getStringExtra(StockDetailActivity.STOCK_CHANGE);
      Intent detailIntent = new Intent(context, StockDetailActivity.class);
      detailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      detailIntent.putExtra(StockDetailActivity.STOCK_SYMBOL, symbol);
      detailIntent.putExtra(StockDetailActivity.STOCK_PRICE, price);
      detailIntent.putExtra(StockDetailActivity.STOCK_CHANGE, change);
      context.startActivity(detailIntent);
    }
    super.onReceive(context, intent);
  }

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    final int count = appWidgetIds.length;

    for (int i = 0; i < count; i++) {
      Log.d(TAG, "widget update");
      int widgetId = appWidgetIds[i];
      Intent intent = new Intent(context, StockWidgetService.class);
      intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
      intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

      RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_stock);
      remoteViews.setRemoteAdapter(R.id.widget_list, intent);
      remoteViews.setEmptyView(R.id.widget_list, R.id.empty_view);

      final Intent clickIntent = new Intent(context, StockWidget.class);
      clickIntent.setAction(StockWidget.CLICK_ACTION);
      clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
      clickIntent.setData(Uri.parse(clickIntent.toUri(Intent.URI_INTENT_SCHEME)));
      final PendingIntent clickPendingIntent =
          PendingIntent.getBroadcast(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
      //remoteViews.setOnClickPendingIntent(R.id.widget_list,clickPendingIntent);
      remoteViews.setPendingIntentTemplate(R.id.widget_list, clickPendingIntent);

      appWidgetManager.updateAppWidget(widgetId, remoteViews);
      appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_list);
    }
    super.onUpdate(context, appWidgetManager, appWidgetIds);
  }
}
