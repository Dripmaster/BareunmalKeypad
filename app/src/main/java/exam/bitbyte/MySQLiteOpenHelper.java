package exam.bitbyte;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    static String name = "york.db";
    static SQLiteDatabase.CursorFactory factory = null;
    static int version = 1;
    public MySQLiteOpenHelper(Context context) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE york (plain VARCHAR(20) NULL, emoticon VARCHAR(20) NULL);";
        db.execSQL(query);
        AddYork(db);
        query="CREATE TABLE log (date_ VARCHAR(20) NULL, plain VARCHAR(20) NULL);";
        db.execSQL(query);
        query="CREATE TABLE settings (name VARCHAR(20) NULL, user_selected int(4) NULL);";
        db.execSQL(query);
        SetSettings(db);
    }
    void AddYork(SQLiteDatabase db){ //기본적으로 제공하는 욕설DB
        String york[] = { "시발", "씨발", "개새끼", "지랄", "병신아", "좆", "존나", "새끼", "ㅅㅂ",
                "병신", "ㅂㅅ", "꺼져", "미친", "나대지마", "졸라", "머저리", "등신", "뻑킹", "젠장", "ㅄ" };

        String emoticon[] = { "(뽀뽀)", "(뽀뽀)", "(하하)새(하트)", "이상한말", "친구야", "뭐", "진짜",
                "친구", "(뽀뽀)", "(방긋)", "(방긋)", "저리가", "(크크)", "앞장서지마", "진짜",
                "(깜찍)", "친구", "방긋", "이런", "(방긋)" };

        String query;
        for(int i =0;i<york.length;i++){
            query = "INSERT INTO york VALUES('"+york[i]+"','"+emoticon[i]+"');";
            db.execSQL(query);
        }
    }
    void SetSettings(SQLiteDatabase db)
    {
        String setname[] = { "tutorial","jindong","emoticon" };
        String query;

        int defaultValue[] = { 0,1,1 };

        for(int i =0;i<setname.length;i++){
            query = "INSERT INTO settings VALUES('"+setname[i]+"', '"+defaultValue[i]+"');";
            db.execSQL(query);
        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query = "DROP TABLE IF EXISTS myinfo";
        db.execSQL(query);
        onCreate(db);
    }
}
/*

SELECT로 모든 데이터값 조회하기
Cursor cursor = db.rawQuery("SELECT * FROM york;", null);

        String plain;
        String emoticon;

        if (cursor.moveToFirst()) {
            do {
                plain = cursor.getString(cursor.getColumnIndex("plain"));
                emoticon = cursor.getString(cursor.getColumnIndex("emoticon"));
                System.out.println("DB : " + plain + " " + emoticon);
            } while (cursor.moveToNext());
        }

 */
