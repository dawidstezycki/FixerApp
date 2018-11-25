package com.example.dawid.fixerapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements CurrAdapter.ListItemClickListener {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    ArrayList<CurrencyItem> currencyList = new ArrayList<CurrencyItem>();
    private CurrAdapter mAdapter;
    private RecyclerView mCurrList;

    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 5;
    private int firstVisibleItem, visibleItemCount, totalItemCount;

    private Date currentDate;
    private Calendar cal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Get the date for the first currency rates to show
        cal = Calendar.getInstance();
        currentDate = cal.getTime();

//        Convert the date to string in the format 2018-10-28
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String strCurrentDate = df.format(currentDate);

//        Make Http request and add currencies from given date to the arraylist
        new DownloadCurrencies().execute(strCurrentDate);

//        Set up Recycler View with Linear LayOutManager and custom adapter
        mCurrList = (RecyclerView) findViewById(R.id.recycler_view);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mCurrList.setLayoutManager(layoutManager);
        mAdapter = new CurrAdapter(currencyList, this);
        mCurrList.setAdapter(mAdapter);

//        Add Listener to update the arraylist with currencies of the day before when the end is reached
        mCurrList.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = mCurrList.getChildCount();
                totalItemCount = layoutManager.getItemCount();
                firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount)
                        <= (firstVisibleItem + visibleThreshold)) {

//                    Get the date of the day before and format it to fit the Http request
                    cal.add(Calendar.DATE, -1);
                    currentDate = cal.getTime();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    String strCurrentDate = df.format(currentDate);

//                    Update the arraylist with new data
                    new DownloadCurrencies().execute(strCurrentDate);

                    loading = true;
                }
            }
        });


    }

//    When clicked on a list item open activity with its data displayed
    @Override
    public void onListItemClick(CurrencyItem curr_item) {
        Intent intent = new Intent(this, CurrencyActivity.class);

        intent.putExtra("currency_name", curr_item.getCurrency());
        intent.putExtra("currency_value", curr_item.getValue());
        intent.putExtra("currency_date", curr_item.getDate());
        startActivity(intent);

    }

//    Extract currency data from json from API
    public void extractCurrencies(String JSON_RESPONSE) {
        try {
            JSONObject baseJsonResponse = new JSONObject(JSON_RESPONSE);

            String strCurrencyDate = baseJsonResponse.getString("date");

//            Change date format to show the day first instead of the year
            try {
                DateFormat oldFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date currencyDate = oldFormat.parse(strCurrencyDate);
                DateFormat newFormat = new SimpleDateFormat("dd-MM-yyyy");
                strCurrencyDate = newFormat.format(currencyDate);

            }
            catch(ParseException pe ) {
                Log.e(LOG_TAG, "Problem changing date format", pe);
            }

//            Adding date separator item to arraylist before adding currency rates from given date
            currencyList.add(new CurrencyItem("DATE SEPARATOR", "-1", strCurrencyDate));

            JSONObject currencyRates = baseJsonResponse.getJSONObject("rates");
            JSONArray currencyNames = currencyRates.names();

            for (int i = 0; i < currencyNames.length(); i++) {
                String currencyName = currencyNames.getString(i);
                double currencyValue = currencyRates.getDouble(currencyName);

                currencyList.add(new CurrencyItem(currencyName, String.format("%.4f", currencyValue), strCurrencyDate));
            }

        } catch (JSONException e) {
            Log.e("CurrencyExtraction", "Problem parsing the JSON results", e);
        }
    }

    private class DownloadCurrencies extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... dates) {

            //            Generate request URL based on the date input
            String BASE_URL = "http://data.fixer.io/api/";
            String ACCESS_KEY = "?access_key=c0074774b4c7156f28c3c53864478849&format=1";
            String requestURL = BASE_URL + dates[0] + ACCESS_KEY;

            URL url = createUrl(requestURL);

            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem making the HTTP request.", e);
            }
            return jsonResponse;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            extractCurrencies(s);

//            Update the set of data used by RecyclerView
            mAdapter.setCurrencyData(currencyList);
            mAdapter.notifyDataSetChanged();
        }

        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";

            // If the URL is null, then return early.
            if (url == null) {
                return jsonResponse;
            }

            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();

                // If the request was successful (response code 200),
                // then read the input stream and parse the response.
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                } else {
                    Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            return jsonResponse;
        }

        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Error with creating URL", exception);
                return null;
            }
            return url;
        }
    }
}
