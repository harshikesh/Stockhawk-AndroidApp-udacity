package com.sam_chordas.android.stockhawk.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.sam_chordas.android.stockhawk.R;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by harshikesh.kumar on 11/10/16.
 */
public class StockDetailActivity extends AppCompatActivity {

  private static final String TAG = StockDetailActivity.class.getSimpleName();

  public static final String STOCK_SYMBOL = "STOCK_SYMBOL";
  public static final String STOCK_PRICE = "STOCK_PRICE";
  public static final String STOCK_CHANGE = "STOCK_CHANGE";

  private LineChartView mChart;
  private String mSymbol;
  private String mStockPrice;
  private String mStockChange;
  private TextView mIncDec;
  private TextView mCurrentValue;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.detail_activity);
    mChart = (LineChartView) findViewById(R.id.stock_chart);
    mCurrentValue = (TextView) findViewById(R.id.currentValue);
    mIncDec = (TextView) findViewById(R.id.incdec);

    mSymbol = getIntent().getStringExtra(STOCK_SYMBOL);
    mStockPrice = getIntent().getStringExtra(STOCK_PRICE);
    mStockChange = getIntent().getStringExtra(STOCK_CHANGE);
    mCurrentValue.setText(mStockPrice + "$");
    mIncDec.setText(mStockChange);

    setTitle(mSymbol);
    getStockDetailsFromAPI();
  }

  // Get stock historical data from yahoo api
  private void getStockDetailsFromAPI() {
    OkHttpClient client = new OkHttpClient();

    StringBuilder urlStringBuilder = new StringBuilder();
    try {
      // Base URL for the Yahoo query
      urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
      urlStringBuilder.append(URLEncoder.encode(
          "select * from yahoo.finance.historicaldata where symbol = "
              + "\""
              + mSymbol
              + "\""
              + " and startDate = "
              + "\"2016-06-10\""
              + " and endDate = "
              + "\"2016-10-10\"", "UTF-8"));
      // finalize the URL for the API query.
      urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
          + "org%2Falltableswithkeys&callback=");
    } catch (UnsupportedEncodingException e) {
      Log.e(TAG, e.getMessage());
    }
    String urlString = null;
    if (urlStringBuilder != null) {
      urlString = urlStringBuilder.toString();
    }

    Request request = new Request.Builder().url(urlString).build();
    Log.d("Request url : ", request.url().toString());

    client.newCall(request).enqueue(new Callback() {
      @Override public void onResponse(Response response) throws IOException {
        if (response.code() == 200) {
          try {
            JSONObject jsonResponse = new JSONObject(response.body().string());
            JSONObject queryObj = jsonResponse.getJSONObject("query");
            JSONObject resultObj = queryObj.getJSONObject("results");
            JSONArray quoteArray = resultObj.getJSONArray("quote");
            updateChart(quoteArray);
          } catch (Exception ex) {
            failureMessage();
            Log.e(TAG, ex.getMessage());
          }
        } else {
          failureMessage();
        }
      }

      @Override public void onFailure(Request request, IOException e) {
        failureMessage();
      }
    });
  }

  private void failureMessage() {
    Toast.makeText(getApplicationContext(), R.string.sorry_tech_issue, Toast.LENGTH_LONG).show();
    finish();
  }

  private void updateChart(final JSONArray data) {
    StockDetailActivity.this.runOnUiThread(new Runnable() {
      @Override public void run() {

        List<AxisValue> axisValuesX = new ArrayList<>();
        List<PointValue> pointValues = new ArrayList<>();

        int counter = -1;
        for (int i = 0; i < data.length(); i++) {
          try {
            JSONObject item = data.getJSONObject(i);
            String date = item.getString("Date");
            String bidPrice = item.getString("Close");
            counter++;
            // We have to show chart in right order.
            int x = data.length() - 1 - counter;

            // Point for line chart (date, price).
            PointValue pointValue = new PointValue(x, Float.valueOf(bidPrice));
            pointValue.setLabel(date);
            pointValues.add(pointValue);

            // Set labels for x-axis (we have to reduce its number to avoid overlapping text).
            if (counter != 0 && counter % (data.length() / 3) == 0) {
              AxisValue axisValueX = new AxisValue(x);
              axisValueX.setLabel(date);
              axisValuesX.add(axisValueX);
            }
          } catch (Exception e) {
            Log.d(TAG, e.getMessage());
          }
        }

        // Prepare data for chart
        Line line = new Line(pointValues).setColor(Color.WHITE).setCubic(false);
        line.setPointColor(Color.GREEN);
        List<Line> lines = new ArrayList<>();
        lines.add(line);
        LineChartData lineChartData = new LineChartData();
        lineChartData.setLines(lines);

        // Init x-axis
        Axis axisX = new Axis(axisValuesX);
        axisX.setHasLines(true);
        axisX.setMaxLabelChars(4);
        lineChartData.setAxisXBottom(axisX);

        // Init y-axis
        Axis axisY = new Axis();
        axisY.setAutoGenerated(true);
        axisY.setHasLines(true);
        axisY.setMaxLabelChars(4);
        lineChartData.setAxisYLeft(axisY);

        // Update chart with new data.
        mChart.setInteractive(false);
        mChart.setLineChartData(lineChartData);

        // Show chart
        mChart.setVisibility(View.VISIBLE);
      }
    });
  }
}
