package com.cleveroad.sample.adapter;

import android.graphics.Color;

/**
 * Created by Dany on 24/11/2017.
 */

public class IntColor {
int r;
int g;
int b;
    int rand;

    public IntColor(int r, int g, int b){
    this.b = b;
    this.g = g;
    this.r = r;
    rand = Color.rgb(b,g,r);
    }




    public int getcolor(){
return rand;
    }


}
