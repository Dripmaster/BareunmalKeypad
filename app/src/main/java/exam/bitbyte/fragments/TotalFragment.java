package exam.bitbyte.fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.BitmapDrawable;
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
public class TotalFragment extends Fragment {

    //변수 선언
    private int data;

    //날짜 구하는 메소드
    public static String[] date(int input){
        Calendar calendar = new GregorianCalendar(Locale.KOREA);
        java.util.Date trialTime = new Date();
        calendar.setTime(trialTime);

        calendar.add(Calendar.DATE, input);
        String year = String.valueOf(calendar.get(Calendar.YEAR));
        String month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        String date = String.valueOf(calendar.get(Calendar.DATE));
        if(month.length()<2)month = "0" + month;
        if(date.length()<2)date = "0" + date;

        String[] result = {year, month, date};
        return result;
    }

    @Override
    public void onResume() {
        super.onResume();

        /* db 준비 */
        MySQLiteOpenHelper testHelper = new MySQLiteOpenHelper(getActivity());
        SQLiteDatabase db = testHelper.getWritableDatabase();
        String query="UPDATE settings SET user_selected=1 WHERE user_selected = 0;";
        db.execSQL(query);
        String plain;
        String date;

        TextView today = (TextView) getActivity().findViewById(R.id.todayCount);
        TextView month = (TextView) getActivity().findViewById(R.id.monthCount);
        TextView monthNum = (TextView) getActivity().findViewById(R.id.month);
        TextView total = (TextView) getActivity().findViewById(R.id.totalCount);
        TextView comment1 = (TextView) getActivity().findViewById(R.id.comment1);
        TextView comment2 = (TextView) getActivity().findViewById(R.id.comment2);
        ImageView emoticon = (ImageView) getActivity().findViewById(R.id.emoticon);

        BitmapDrawable img0 = (BitmapDrawable)getResources().getDrawable(R.drawable.emoticon0);
        BitmapDrawable img1 = (BitmapDrawable)getResources().getDrawable(R.drawable.emoticon1);
        BitmapDrawable img2 = (BitmapDrawable)getResources().getDrawable(R.drawable.emoticon2);
        BitmapDrawable img3 = (BitmapDrawable)getResources().getDrawable(R.drawable.emoticon3);

        Cursor cursor = db.rawQuery("SELECT * FROM log;", null);
        if (cursor.moveToFirst()) {
            do {
                date = cursor.getString(cursor.getColumnIndex("date_"));
                plain = cursor.getString(cursor.getColumnIndex("plain"));
                System.out.println("Log: " + date + " " + plain);
            } while (cursor.moveToNext());
        }
        //오늘의 욕설 횟수
        cursor = db.rawQuery("SELECT count(*) FROM log WHERE date_ like '" + date(0)[0] + "-" + date(0)[1] + "-" + date(0)[2] + "';", null);
        cursor.moveToFirst();
        today.setText(cursor.getString(cursor.getColumnIndex("count(*)")) + "회");

        //이번달의 욕설 횟수
        cursor = db.rawQuery("SELECT count(*) FROM log WHERE date_ like '" + date(0)[0] + "-" + date(0)[1] + "-%%';", null);
        cursor.moveToFirst();
        monthNum.setText(date(0)[1] + "월");
        month.setText(cursor.getString(cursor.getColumnIndex("count(*)")) + "회");

        //총 욕설 횟수
        cursor = db.rawQuery("SELECT count(*) FROM log WHERE date_ like '%%-%%-%%';", null);
        cursor.moveToFirst();
        total.setText(cursor.getString(cursor.getColumnIndex("count(*)")) + "회");

        cursor = db.rawQuery("SELECT count(*) FROM log WHERE date_ like '" + date(0)[0] + "-" + date(0)[1] + "-" + date(0)[2] + "';", null);
        cursor.moveToFirst();

        data = Integer.parseInt(cursor.getString(cursor.getColumnIndex("count(*)")));
        if (data == 0){
            comment1.setText("클-린-");
            comment2.setText("오늘 한번도 욕설을 사용하지 않았습니다!\n멋져요~");
            emoticon.setImageDrawable(img0);
        }
        else if (data > 0 && data < 20) {
            comment1.setText("좋아요!");
            comment2.setText("지금 이 상태를 계속 유지하세요!");
            emoticon.setImageDrawable(img1);
        }
        else if (data >= 20 && data < 50) {
            comment1.setText("주의하세요");
            comment2.setText("오늘 욕설을 " + cursor.getString(cursor.getColumnIndex("count(*)")) + "번 사용했어요...\n주의하세요!");
            emoticon.setImageDrawable(img2);
        }
        else {
            comment1.setText("심각해요!");
            comment2.setText("오늘 욕설을 " + cursor.getString(cursor.getColumnIndex("count(*)")) + "번이나 사용했어요...!!\n내일은 더 많이 줄이도록 노력하세요!");
            emoticon.setImageDrawable(img3);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fregment_main1, container, false);
        return rootView;
    }
}
