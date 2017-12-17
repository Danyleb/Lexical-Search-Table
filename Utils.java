package com.cleveroad.sample.utils;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by Dany on 17/11/2017.
 */

class Utils {



    public static List<Word> getApiUserList(String value){



        try

    {
        Document document = Jsoup.connect("http://www.rimessolides.com/motscles.aspx?m=" +value ).get();
        ArrayList<Word> wordList = new ArrayList<>();

        Word word;


        Elements elements = document.getElementsByClass("MotCle");

        for (Element e : elements) {

            word = new Word();

            // On récupère l'image

            word.setWord(e.getElementsByTag("a").first().text());
            wordList.add(word);

    }

        return wordList;

    } catch (IOException e) {
        e.printStackTrace();
        Log.e("Error", e.getMessage());
    }

            return null;
    }
}

