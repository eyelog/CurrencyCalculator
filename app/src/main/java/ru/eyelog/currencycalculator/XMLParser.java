package ru.eyelog.currencycalculator;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class XMLParser {

    /**
     Так как в техзадании сторонний парсинг запрещен, напишем свой.

     План действий.
     Получаем на вход String
     substring-ом разбираем на элементы
     и собираем Map-овый массив.
     Этот массив отправляем на выход.
     */

    // Выходной массив.
    ArrayList<HashMap<String, String>> dataList;
    HashMap<String, String> hm;

    // Искомые таги.
    final String TAG_VALUTE = "Valute";
    final String TAG_NUMCODE = "NumCode";
    final String TAG_CHARCODE = "CharCode";
    final String TAG_NOMINAL = "Nominal";
    final String TAG_NAME = "Name";
    final String TAG_VALUE = "Value";

    private static String gotString; // Общая переменная полученной строки
    private static int xmlStep = 0; // Общий счётчик шагов


    XMLParser(){

    }

    public ArrayList getArrayedData(String gotString){

        dataList = new ArrayList<>();

        this.gotString = gotString;

        String subChar = ""; // Переменная для ловли символов
        String subName = ""; // Переменная для ловли имён
        String subValue = ""; // Переменная ловли зачений

        // Главный цикл ищет блоки с именем "Valute"
        do{
            // Получаем символ
            subChar = gotString.substring(xmlStep, xmlStep+1);

            // И проверяем если это знак "<"
            if(subChar.equals("<")){
                // Если нашли открывающий знак - собираем имя следующее за знаком "<"
                subName = collectName();

                //Log.e("Main tag", subName);

                // И если это имя блока, идем по этому блоку сибирая его элементы
                if(subName.equals(TAG_VALUTE)){

                    // Мы нашли блок.
                    hm = new HashMap<>();

                    // Собираем его элементы.
                    hm = collectBlockData();

                    // И кладём в общий список.
                    dataList.add(hm);

                    // И идём дальше.
                    xmlStep++;

                }else{
                    // Если это не блок, просто идём дальше.
                    xmlStep++;
                }

            }else{
                // Если это не открывающий знак, то идем дальше.
                xmlStep++;
            }

        }while (xmlStep<gotString.length());

        return dataList;
    }

    // метод обрабатывающий блок данных по одной валюте
    public HashMap<String, String> collectBlockData(){
        HashMap<String, String> hm = new HashMap<>();

        String subChar = ""; // Переменная для ловли символов
        String subName = ""; // Переменная для ловли имён
        String subValue = ""; // Переменная ловли зачений

        boolean gotBlockEnd = false;

        do{
            // Получаем символ
            subChar = gotString.substring(xmlStep, xmlStep+1);

            //Log.e("collectBlockData", subChar);

            // Если это символ "<", это может быть как начало начала так и начало конца
            if(subChar.equals("<")){
                // проверим на закрытие
                xmlStep++;
                subChar = gotString.substring(xmlStep, xmlStep+1);

                if(subChar.equals("/")){
                    // Если тег закрывается
                    // Сначала поёмем, что это за тег
                    subName = collectName();

                    // И если закрылся блок
                    if(subName.equals(TAG_VALUTE)){

                        // возвращаемся в основной цикл с добычей.
                        gotBlockEnd = true;
                    }else{

                        // Если закрылся тег элемента
                        // Просто идем дальше
                        xmlStep++;
                    }

                }else {
                    // У нас начало имени элемента
                    // Получим имя элемента
                    xmlStep--;
                    subName = collectName();

                    xmlStep++;

                    // Сразу за именем элемента следует его значение.
                    subValue = collectValue();

                    // Получив имя и значение, кладём его в HashMap
                    hm.put(subName, subValue);

                    // И топаем дальше.

                    xmlStep++;
                }
            }else{
                // просто идем дальше
                xmlStep++;
            }

        }while (!gotBlockEnd);

        return hm;
    }

    // Метод собирающий слова
    public String collectName(){
        String stCollectedName = "";
        String subChar = "";
        boolean stopLoop = false;

        do{
            xmlStep++;
            subChar = gotString.substring(xmlStep, xmlStep+1);
            stCollectedName += subChar;

            if(gotString.substring(xmlStep+1, xmlStep+2).equals(" ")||
                    gotString.substring(xmlStep+1, xmlStep+2).equals(">")){
                stopLoop = true;
            }
        }while (!stopLoop);

        return stCollectedName;
    }

    // Метод собирающий Значения
    public String collectValue(){
        String stCollectedValue = "";
        String subChar = "";
        boolean stopLoop = false;

        do{
            xmlStep++;
            subChar = gotString.substring(xmlStep, xmlStep+1);
            stCollectedValue += subChar;

            if(gotString.substring(xmlStep+1, xmlStep+2).equals("<")){
                stopLoop = true;
            }
        }while (!stopLoop);

        return stCollectedValue;
    }

    public void oldStuff(String gotString){

        String subChar = ""; // Переменная для ловли символов
        String subName = ""; // Переменная для ловли имён
        String subValue = ""; // Переменная ловли зачений

        // Во внешнем круге цикла ищем блок с тагом "Valute"
        do{
            // Ищем знак "<"
            subChar = gotString.substring(xmlStep, xmlStep+1);
            //Log.e("subChar", subChar);
            if(subChar.equals("<")){
                xmlStep++;
                // Сперва проверим открывается или закрыватся таг
                subChar = gotString.substring(xmlStep, xmlStep+1);
                if(subChar.equals("/")){
                    // Таг закрывается.
                    // Если это закрывается таг блока - обновляем HashMap
                    xmlStep++;

                    // Собираем выходное имя
                    subName = "";
                    do{
                        subChar = gotString.substring(xmlStep, xmlStep+1);
                        subName += subChar;
                        xmlStep++;
                    }while (subChar.equals(" ")||subChar.equals(">"));

                    // И если это закрылся выходной таг блока
                    // Добавляем HashMap в ArrayList
                    dataList.add(hm);

                }else {
                    // Мы нашли какое-то имя
                    // Собираем его в целое слово
                    subName = "";
                    do{
                        subChar = gotString.substring(xmlStep, xmlStep+1);
                        subName += subChar;
                        xmlStep++;
                    }while (subChar.equals(" ")||subChar.equals(">"));

                    // Мы получили некоторое имя.
                    if(subName.equals(TAG_VALUTE)){
                        // Если это начало блока, инициируем HashMap
                        hm = new HashMap<>();
                        // и идём дальше
                        xmlStep++;
                    }else if(subName.equals(TAG_NUMCODE)||
                            subName.equals(TAG_CHARCODE)||
                            subName.equals(TAG_NOMINAL)||
                            subName.equals(TAG_NAME)||
                            subName.equals(TAG_VALUE)){
                        // Мы получил некоторое имя за которым идет значение
                        // Значение за именем обёрнуто >xxx<
                        // Собираем значение
                        subValue = "";
                        do{
                            subChar = gotString.substring(xmlStep, xmlStep+1);
                            subValue += subChar;
                            xmlStep++;
                        }while (subChar.equals("<"));

                        hm.put(subName, subValue);
                    }else{
                        // Проходим мимо.
                        xmlStep++;
                    }
                }

            }else {
                xmlStep++;
            }

        }while (xmlStep<gotString.length());
    }
}