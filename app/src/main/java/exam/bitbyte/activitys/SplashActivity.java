package exam.bitbyte.activitys;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;

import exam.bitbyte.MySQLiteOpenHelper;
import exam.bitbyte.R;

/**
 * Created by AhnSeohyung on 14. 12. 21..
 */
public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        MySQLiteOpenHelper testHelper = new MySQLiteOpenHelper(this);
        SQLiteDatabase db = testHelper.getWritableDatabase();
        String name="";
        String user_selected="";
        Cursor cursor = db.rawQuery("SELECT * FROM settings;", null);
        if (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndex("name"));
            user_selected = cursor.getString(cursor.getColumnIndex("user_selected"));
        }
        if(user_selected.equals("0")){
            Handler hd = new Handler();
            hd.postDelayed(new Runnable() {

                @Override
                public void run() {
                    Intent intentActivity = new Intent(SplashActivity.this,
                            TutorialActivity.class);
                    startActivity(intentActivity);
                    finish();
                }
            }, 1500);
        }
        else
        {
            Handler hd = new Handler();
            hd.postDelayed(new Runnable() {

                @Override
                public void run() {
                    Intent intentActivity = new Intent(SplashActivity.this,
                            MainActivity.class);
                    startActivity(intentActivity);
                    finish();
                }
            }, 1000);
        }
    }
}
