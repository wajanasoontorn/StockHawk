package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.utils.DateUtils;
import com.udacity.stockhawk.data.Contract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.chart)
    LineChartView mChart;

    private static final int STOCK_LOADER = 0;
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
                setChart(getPointValues(raw));
            }
        }
    }

    private List<PointValue> getPointValues(String raw) {
        final int dateColumn = 0;
        final int priceColumn = 1;
        List<PointValue> values = new ArrayList<>();

        try {
            String rows[] = raw.split("\\r?\\n");

            for (int i = 0; i < rows.length; i++) {
                String columns[] = rows[i].split("\\s*,\\s*");
                Date date = new Date(Long.parseLong(columns[dateColumn]));
                PointValue pointValue = new PointValue(date.getTime(), Float.parseFloat(columns[priceColumn]));
                values.add(pointValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Collections.sort(values, new Comparator<PointValue>() {
            @Override
            public int compare(PointValue o1, PointValue o2) {
                if (o1.getX() > o2.getX()) {
                    return 1;
                } else if (o1.getX() < o2.getX()){
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        return values;
    }

    private void setChart(List<PointValue> values) {
        PointValue min = values.get(0);
        PointValue max = values.get(values.size() - 1);

        Calendar minDate = Calendar.getInstance();
        Calendar maxDate = Calendar.getInstance();
        minDate.setTimeInMillis((long) min.getX());
        maxDate.setTimeInMillis((long) max.getX());


        Line line = new Line(values).setColor(ContextCompat.getColor(this, R.color.chart_line)).setCubic(true);
        line.setHasLines(true);
        line.setHasPoints(false);

        List<Line> lines = new ArrayList<>();
        lines.add(line);

        LineChartData lineChartData = new LineChartData();
        lineChartData.setLines(lines);

        Axis axisX = new Axis();
        Axis axisY = new Axis();

        int length = DateUtils.getMonthsDifference(minDate, maxDate);

        Calendar calendar = Calendar.getInstance();
        calendar.set(minDate.get(Calendar.YEAR), minDate.get(Calendar.MONTH), minDate.get(Calendar.DATE));

        SimpleDateFormat format = new SimpleDateFormat(DateUtils.getYearAndMonthFormat(), Locale.getDefault());
        calendar.set(minDate.get(Calendar.YEAR), minDate.get(Calendar.MONTH), 1);
        List<AxisValue> axisValues = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            calendar.add(Calendar.MONTH, i == 0 ? 0 : 1);
            Date date = calendar.getTime();
            axisValues.add(new AxisValue(date.getTime()).setLabel(format.format(date)));
        }

        axisX.setValues(axisValues);

        lineChartData.setAxisXBottom(axisX);
        lineChartData.setAxisYLeft(axisY);


        mChart.setLineChartData(lineChartData);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
