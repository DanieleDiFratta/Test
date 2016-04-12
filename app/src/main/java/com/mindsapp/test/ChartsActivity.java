package com.mindsapp.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.mindsapp.test.model.Channel;
import com.mindsapp.test.model.ChannelManager;
import com.mindsapp.test.model.WifiNetwork;
import com.mindsapp.test.utility.ChartColor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class ChartsActivity extends AppCompatActivity {

    public static final int NUM_PAGES = 2;

    static List<String> fragments;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);
        fragments = new ArrayList<>();
        fragments.add(PowerFragment.class.getName());
        fragments.add(ChannelFragment.class.getName());
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_charts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public static class PowerFragment extends Fragment {

        private WifiManager manager;
        private LineChart powerChart;
        private BroadcastReceiver br;

        public PowerFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View powerView = inflater.inflate(R.layout.fragment_power_chart, container, false);
            powerChart = (LineChart) powerView.findViewById(R.id.powerChart);
            powerChart.setTouchEnabled(false);
            powerChart.getAxisRight().setEnabled(false);
            powerChart.getLegend().setWordWrapEnabled(true);
            YAxis yLeft = powerChart.getAxisLeft();
            yLeft.setStartAtZero(false);
            yLeft.setAxisMaxValue(-20);
            yLeft.setAxisMinValue(-100);
            LineData data = new LineData();
            powerChart.setData(data);
            manager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
            br = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    addEntry();
                    manager.startScan();
                }
            };
            manager.startScan();
            return powerView;
        }

        @Override
        public void onResume() {
            super.onResume();
            getActivity().registerReceiver(br, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        }

        @Override
        public void onPause() {
            super.onPause();
            getActivity().unregisterReceiver(br);
        }

        private void addEntry() {
            LineData data = powerChart.getData();

            for (ScanResult result :
                    manager.getScanResults()) {
                ILineDataSet set = data.getDataSetByLabel(result.SSID, true);
                if (set == null) {
                    set = createSet(result.SSID);
                    data.addDataSet(set);
                }
                data.addEntry(new Entry(result.level, set.getEntryCount()), data.getIndexOfDataSet(set));
            }

            data.addXValue("");

            // let the chart know it's data has changed
            powerChart.notifyDataSetChanged();

            // limit the number of visible entries
            powerChart.setVisibleXRangeMaximum(10);

            // move to the latest entry
            powerChart.moveViewToX(data.getXValCount() - 11);
        }

        private ILineDataSet createSet(String SSID) {
            LineDataSet set = new LineDataSet(null, SSID);
            set.setAxisDependency(YAxis.AxisDependency.LEFT);
            set.setColor(ChartColor.LINE_CHART_COLORS[powerChart.getLineData().getDataSetCount()]);
            set.setCircleColor(Color.BLACK);
            set.setLineWidth(2f);
            set.setCircleRadius(4f);
            set.setFillAlpha(65);
            set.setFillColor(ColorTemplate.getHoloBlue());
            set.setHighLightColor(Color.rgb(244, 117, 117));
            set.setValueTextColor(Color.BLACK);
            set.setValueTextSize(9f);
            set.setDrawValues(false);
            return set;
        }
    }

    public static class ChannelFragment extends Fragment {

        private BarChart channelChart;
        private WifiManager wifiManager;
        private BroadcastReceiver br;
        private ChannelManager channelManager;

        public ChannelFragment ()
        {}

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View channelView = inflater.inflate(R.layout.fragment_channel_chart, container, false);
            wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
            channelChart = (BarChart) channelView.findViewById(R.id.channelChart);
            BarData data = new BarData();
            channelChart.setData(data);
            channelManager = new ChannelManager();
            br = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    updateResults(channelManager.selectChannel(wifiManager.getScanResults()));
                }
            };
            wifiManager.startScan();
            return channelView;
        }

        private void updateResults(Collection<Channel> channels) {
            BarData data = channelChart.getData();
            data.clearValues();
            for (Channel ch :
                    channels) {
                for (WifiNetwork network :
                        ch.getNetworks()) {
                    if(network!=null) {
                        IBarDataSet set = data.getDataSetByLabel(network.getSSID(), true);
                        if (set == null) {
                            List<BarEntry> barEntries = new ArrayList<>();
                            barEntries.add(new BarEntry(network.getRSSI(), ch.getId()));
                            set = new BarDataSet(barEntries, network.getSSID());
                            data.addDataSet(set);
                        }
                    }
                }
            }
            channelChart.notifyDataSetChanged();
            channelChart.invalidate();
        }

        @Override
        public void onResume() {
            super.onResume();
            getActivity().registerReceiver(br, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        }

        @Override
        public void onPause() {
            super.onPause();
            getActivity().unregisterReceiver(br);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public List<String> fragmentsA;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            fragmentsA = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return Fragment.instantiate(ChartsActivity.this, fragmentsA.get(position));
        }

        @Override
        public int getCount() {
            return fragmentsA.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentsA.get(position).toUpperCase();
        }
    }
}
