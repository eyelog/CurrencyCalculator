package ru.eyelog.currencycalculator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static android.R.layout.simple_spinner_item;

public class MainActivity extends Activity {

    TextView tvCurrencyCostFrom, tvCurrencyCostTo, tvCurrencyValue;
    EditText editText;
    String stGotValue;
    static AsyncTask<Void, Void, Void> mTask;
    String jsonString;
    String url = "http://www.cbr.ru/scripts/XML_daily.asp";


    double gotValueFrom, gotNominalFrom, gotValueTo, gotNominalTo, gotValue, countedValue;
    String stOutLine;
    static int positionFrom = 0, positionTo = 0;

    NumberFormat format;
    Number number;

    Spinner spinnerFrom, spinnerTo;

    XMLParser xmlParser;
    ArrayList<HashMap<String, String>> currencyList;
    HashMap<String, String> hm;

    DBSharedData dbSharedData;

    boolean gotAnswer;

    //final String TAG_NUMCODE = "NumCode";
    //final String TAG_CHARCODE = "CharCode";
    final String TAG_NOMINAL = "Nominal";
    final String TAG_NAME = "Name";
    final String TAG_VALUE = "Value";

    String stRUR;

    ArrayAdapter<String> adapter_from, adapter_to;

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvCurrencyCostFrom = findViewById(R.id.textView2);
        tvCurrencyCostTo = findViewById(R.id.textView4);
        tvCurrencyValue = findViewById(R.id.textView5);
        editText = findViewById(R.id.editText);
        editText.setText("1");
        spinnerFrom = findViewById(R.id.spinner_00);
        spinnerTo = findViewById(R.id.spinner_01);

        stRUR = getString(R.string.RUR);

        dbSharedData = new DBSharedData(this);

        xmlParser = new XMLParser();

        mTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                gotAnswer = true;
                jsonString = "";
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    jsonString = getJsonFromServer(url);
                } catch (IOException e) {
                    e.printStackTrace();
                    gotAnswer = false;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                if(jsonString.equals("")||jsonString==null||!gotAnswer){

                    currencyList = dbSharedData.readData();

                }else {

                    currencyList = xmlParser.getArrayedData(jsonString);

                    if(currencyList.size()==0){
                        dbSharedData.createData(currencyList);
                    }else{
                        dbSharedData.updateData(currencyList);
                    }
                }
                createMode();
            }
        };

        mTask.execute();

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                outLineUpdater();
            }
        });

    }

    // Метод локализациии данных
    public void createMode(){

        // Добавляем российский рубль.
        hm = new HashMap();
        hm.put(TAG_NAME, stRUR);
        hm.put(TAG_NOMINAL, "1");
        hm.put(TAG_VALUE, "1");
        currencyList.add(hm);

        // Готовим данные для спинеров
        String [] data = new String[currencyList.size()];
        for(int i=0; i<currencyList.size(); i++){
            data[i] = currencyList.get(i).get(TAG_NAME);
        }

        // адаптер
        adapter_from = new ArrayAdapter<>(this, simple_spinner_item, data);
        adapter_from.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerFrom.setAdapter(adapter_from);
        // выделяем элемент
        // устанавливаем обработчик нажатия
        spinnerFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                format = NumberFormat.getInstance(Locale.FRANCE);
                try {
                    number = format.parse(currencyList.get(position).get(TAG_VALUE));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                gotValueFrom = number.doubleValue();
                gotNominalFrom = Double.parseDouble(currencyList.get(position).get(TAG_NOMINAL));

                positionFrom = position;

                outLineUpdater();

                tvCurrencyCostFrom.setText(String.valueOf(gotValueFrom/gotNominalFrom));

            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        // адаптер
        adapter_to = new ArrayAdapter<>(this, simple_spinner_item, data);
        adapter_to.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerTo.setAdapter(adapter_to);
        // выделяем элемент
        // устанавливаем обработчик нажатия
        spinnerTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                format = NumberFormat.getInstance(Locale.FRANCE);
                try {
                    number = format.parse(currencyList.get(position).get(TAG_VALUE));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                gotValueTo = number.doubleValue();
                gotNominalTo = Double.parseDouble(currencyList.get(position).get(TAG_NOMINAL));

                positionTo = position;

                outLineUpdater();

                tvCurrencyCostTo.setText(String.valueOf(gotValueTo/gotNominalTo));

            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    // Вывод расчёта на экран.
    public void outLineUpdater(){
        stGotValue = editText.getText().toString();

        if(stGotValue.equals("")){
            gotValue = 0;
        }else {
            gotValue = Double.parseDouble(stGotValue);
        }

        countedValue = (gotValueFrom/gotNominalFrom) * gotValue / (gotValueTo/gotNominalTo);

        stOutLine = gotValue + " " + currencyList.get(positionFrom).get(TAG_NAME) + " = " + "\n" +
                countedValue + " " + currencyList.get(positionTo).get(TAG_NAME);

        tvCurrencyValue.setText(stOutLine);
    }

    // Метод запроса данныйх с сервера
    public static String getJsonFromServer(String url) throws IOException {

        BufferedReader inputStream = null;

        URL jsonUrl = new URL(url);
        URLConnection dc = jsonUrl.openConnection();

        dc.setConnectTimeout(5000);
        dc.setReadTimeout(5000);

        inputStream = new BufferedReader(new InputStreamReader(
                dc.getInputStream(), "Windows-1251"));

        // read the JSON results into a string
        return inputStream.readLine();
    }
}
