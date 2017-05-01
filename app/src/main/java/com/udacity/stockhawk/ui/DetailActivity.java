package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.chart)
    LineChart mChart;

    private static final int STOCK_LOADER = 0;
    private static final float CHART_BOTTOM_OFFSET = 4;
    private String mSymbol;

    public static final String SYMBOL = "Symbol";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        if (getIntent().hasExtra(SYMBOL)) {
            mSymbol = getIntent().getStringExtra(SYMBOL);
            setTitle(mSymbol);
        }

        getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mSymbol == null) {
            return null;
        }

        return new CursorLoader(this,
                Contract.Quote.makeUriForStock(mSymbol),
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            String raw = data.getString(Contract.Quote.POSITION_HISTORY);
            if (raw != null && !raw.isEmpty()) {
                setChart(getEntries(raw));
            }
        }
    }

    private List<Entry> getEntries(String raw) {
        final int dateColumn = 0;
        final int priceColumn = 1;
        List<Entry> entries = new ArrayList<>();

        try {
            String rows[] = raw.split("\\r?\\n");

            for (int i = 0; i < rows.length; i++) {
                String columns[] = rows[i].split("\\s*,\\s*");
                Date date = new Date(Long.parseLong(columns[dateColumn]));
                Entry pointValue = new Entry(date.getTime(), Float.parseFloat(columns[priceColumn]));
                entries.add(pointValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Collections.sort(entries, new EntryXComparator());

        return entries;
    }

    private void setChart(List<Entry> entries) {
        mChart.setContentDescription(String.format(getString(R.string.a11y_price_over_time), mSymbol));
        mChart.getDescription().setEnabled(false);
        LineDataSet dataSet = new LineDataSet(entries, "");
        mChart.getLegend().setEnabled(false);
        dataSet.setColor(ContextCompat.getColor(this, R.color.chart_line));
        dataSet.setDrawCircles(false);

        LineData lineData = new LineData(dataSet);
        mChart.setData(lineData);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                SimpleDateFormat format = new SimpleDateFormat(DateUtils.getYearAndMonthFormat(), Locale.getDefault());
                return format.format(new Date((long)value));
            }
        });

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(ContextCompat.getColor(this, R.color.chart_label));
        xAxis.setTextSize(getResources().getDimension(R.dimen.chart_label_font_size_dp));

        YAxis axisLeft = mChart.getAxisLeft();
        axisLeft.setTextColor(ContextCompat.getColor(this, R.color.chart_label));
        axisLeft.setTextSize(getResources().getDimension(R.dimen.chart_label_font_size_dp));

        YAxis axisRight = mChart.getAxisRight();
        axisRight.setTextColor(ContextCompat.getColor(this, R.color.chart_label));
        axisRight.setTextSize(getResources().getDimension(R.dimen.chart_label_font_size_dp));
        mChart.setExtraBottomOffset(CHART_BOTTOM_OFFSET);

        mChart.invalidate();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //do nothing
    }
}
