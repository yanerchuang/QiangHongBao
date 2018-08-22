package com.ywj.qianghongbao;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by weijing on 2017-01-06 14:31
 */
public class ToastUtils {
    private static Toast toast;
    public static void  show(Context context,String msg){
        if (toast==null)
            toast=Toast.makeText(context,msg,Toast.LENGTH_SHORT);

        toast.setText(msg);
        toast.show();
    }
}
