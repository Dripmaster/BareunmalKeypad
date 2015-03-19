package exam.bitbyte.fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import exam.bitbyte.MySQLiteOpenHelper;
import exam.bitbyte.R;

/**
 * Created by neokree on 16/12/14.
 */
public class GraphFragment extends Fragment {

    //변수 선언
    int data1, data2, data3, data4, data5, data6, data7;
    String text1, text2, text3, text4, text5, text6, text7;

    //날짜 구하는 메소드
    public static String date(int input){
        Calendar calendar = new GregorianCalendar(Locale.KOREA);
        java.util.Date trialTime = new Date();
        calendar.setTime(trialTime);

        calendar.add(Calendar.DATE, input);
        String year = String.valueOf(calendar.get(Calendar.YEAR));
        String month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        String date = String.valueOf(calendar.get(Calendar.DATE));
        if(month.length()<2)month = "0" + month;
        if(date.length()<2)date = "0" + date;

        return year + "-" + month + "-" + date;
    }

    @Override
    public void onResume() {
        super.onResume();

        /* db 준비 */
        MySQLiteOpenHelper testHelper = new MySQLiteOpenHelper(getActivity());
        SQLiteDatabase db = testHelper.getWritableDatabase();
        String query="UPDATE settings SET user_selected=1 WHERE user_selected = 0;";
        db.execSQL(query);

        ImageView graph1 = (ImageView) getActivity().findViewById(R.id.graph_1);
        ImageView graph2 = (ImageView) getActivity().findViewById(R.id.graph_2);
        ImageView graph3 = (ImageView) getActivity().findViewById(R.id.graph_3);
        ImageView graph4 = (ImageView) getActivity().findViewById(R.id.graph_4);
        ImageView graph5 = (ImageView) getActivity().findViewById(R.id.graph_5);
        ImageView graph6 = (ImageView) getActivity().findViewById(R.id.graph_6);
        ImageView graph7 = (ImageView) getActivity().findViewById(R.id.graph_7);

        TextView gText1 = (TextView) getActivity().findViewById(R.id.gText1);
        TextView gText2 = (TextView) getActivity().findViewById(R.id.gText2);
        TextView gText3 = (TextView) getActivity().findViewById(R.id.gText3);
        TextView gText4 = (TextView) getActivity().findViewById(R.id.gText4);
        TextView gText5 = (TextView) getActivity().findViewById(R.id.gText5);
        TextView gText6 = (TextView) getActivity().findViewById(R.id.gText6);
        TextView gText7 = (TextView) getActivity().findViewById(R.id.gText7);

        //DB에서 각 날짜별로 욕설 횟수를 받아 각 변수에 저장
        Cursor cursor = db.rawQuery("SELECT count(*) FROM log WHERE date_ like '" + date(0) + "';", null);
        cursor.moveToFirst(); // 이거 빼먹으면 애러남!! 꼭 넣어야함!
        data7 = Integer.parseInt(cursor.getString(cursor.getColumnIndex("count(*)")));
        text7 = cursor.getString(cursor.getColumnIndex("count(*)"));
        if (data7 == 0) {
            graph7.getLayoutParams().height = 1;
        } else if (data7 == 66) {
            graph7.getLayoutParams().height = 1000;
        } else {
            graph7.getLayoutParams().height = data7 * 15;
        }
        gText7.setText(text7);

        cursor = db.rawQuery("SELECT count(*) FROM log WHERE date_ like '" + date(-1) + "';", null);
        cursor.moveToFirst();
        data6 =  Integer.parseInt(cursor.getString(cursor.getColumnIndex("count(*)")));
        text6 = cursor.getString(cursor.getColumnIndex("count(*)"));
        if (data6 == 0){
            graph6.getLayoutParams().height = 1;
        }
        else if (data6 == 66){
            graph6.getLayoutParams().height = 1000;
        }
        else{
            graph6.getLayoutParams().height = data6*15;
        }
        gText6.setText(text6);

        cursor = db.rawQuery("SELECT count(*) FROM log WHERE date_ like '" + date(-2) + "';", null);
        cursor.moveToFirst();
        data5 =  Integer.parseInt(cursor.getString(cursor.getColumnIndex("count(*)")));
        text5 = cursor.getString(cursor.getColumnIndex("count(*)"));
        if (data5 == 0){
            graph5.getLayoutParams().height = 1;
        }
        else if (data5 == 66){
            graph5.getLayoutParams().height = 1000;
        }
        else{
            graph5.getLayoutParams().height = data5*15;
        }
        gText5.setText(text5);

        cursor = db.rawQuery("SELECT count(*) FROM log WHERE date_ like '" + date(-3) + "';", null);
        cursor.moveToFirst();
        data4 =  Integer.parseInt(cursor.getString(cursor.getColumnIndex("count(*)")));
        text4 = cursor.getString(cursor.getColumnIndex("count(*)"));
        if (data4 == 0){
            graph4.getLayoutParams().height = 1;
        }
        else if (data4 == 66){
            graph4.getLayoutParams().height = 1000;
        }
        else{
            graph4.getLayoutParams().height = data4*15;
        }
        gText4.setText(text4);

        cursor = db.rawQuery("SELECT count(*) FROM log WHERE date_ like '" + date(-4) + "';", null);
        cursor.moveToFirst();
        data3 =  Integer.parseInt(cursor.getString(cursor.getColumnIndex("count(*)")));
        text3 = cursor.getString(cursor.getColumnIndex("count(*)"));
        if (data3 == 0){
            graph3.getLayoutParams().height = 1;
        }
        else if (data3 == 66){
            graph3.getLayoutParams().height = 1000;
        }
        else{
            graph3.getLayoutParams().height = data3*15;
        }
        gText3.setText(text3);

        cursor = db.rawQuery("SELECT count(*) FROM log WHERE date_ like '" + date(-5) + "';", null);
        cursor.moveToFirst();
        data2 =  Integer.parseInt(cursor.getString(cursor.getColumnIndex("count(*)")));
        text2 = cursor.getString(cursor.getColumnIndex("count(*)"));
        if (data2 == 0){
            graph2.getLayoutParams().height = 1;
        }
        else if (data2 == 66){
            graph2.getLayoutParams().height = 1000;
        }
        else{
            graph2.getLayoutParams().height = data2*15;
        }
        gText2.setText(text2);

        cursor = db.rawQuery("SELECT count(*) FROM log WHERE date_ like '" + date(-6) + "';", null);
        cursor.moveToFirst();
        data1 =  Integer.parseInt(cursor.getString(cursor.getColumnIndex("count(*)")));
        text1 = cursor.getString(cursor.getColumnIndex("count(*)"));
        if (data1 == 0){
            graph1.getLayoutParams().height = 1;
        }
        else if (data1 == 66){
            graph1.getLayoutParams().height = 1000;
        }
        else{
            graph1.getLayoutParams().height = data1*15;
        }
        gText1.setText(text1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fregment_main2, container, false);
        return rootView;
    }
}
