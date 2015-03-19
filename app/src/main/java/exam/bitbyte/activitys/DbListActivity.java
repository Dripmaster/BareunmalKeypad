package exam.bitbyte.activitys;

import java.util.ArrayList;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import exam.bitbyte.MySQLiteOpenHelper;
import exam.bitbyte.R;
import exam.bitbyte.adapters.YorkCustomAdapter;
import exam.bitbyte.YorkData;

public class DbListActivity extends ActionBarActivity {

    private ListView mLvData;
    private YorkCustomAdapter mCustomAdapter;
    private ArrayList<YorkData> mList;
    private Toolbar toolbar;

    int selectedImageId = 0;
    MySQLiteOpenHelper testHelper;
    SQLiteDatabase db;
    String query;
    String york[],emoticon[];
    Cursor cursor;
    EditText edit;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db_list);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("욕설 데이터 추가");

		/* db 준비 */
        System.out.println("DB LIST-------------------");
        testHelper = new MySQLiteOpenHelper(this);
        db = testHelper.getWritableDatabase();
		/* db끝 */
        Button btn=(Button)findViewById(R.id.york_insert);
        edit=(EditText)findViewById(R.id.york);
        btn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (edit.getText().toString().isEmpty())
                {
                    Toast.makeText(getBaseContext(), "추가할 욕설을 입력하세요", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    String customYork=edit.getText().toString();
                    query = "INSERT INTO york VALUES('"+customYork+"','"+emoticon[2]+"');";
                    db.execSQL(query);
                    Intent intentSubActivity = new Intent(DbListActivity.this,
                            DbListActivity.class);
                    startActivity(intentSubActivity);
                    finish();
                }
            }
        });
        mLvData = (ListView) findViewById(R.id.ListView1);
        mList = new ArrayList<YorkData>();

        mCustomAdapter = new YorkCustomAdapter(this, R.layout.york_db_list_item, mList);

        mLvData.setAdapter(mCustomAdapter);

        mLvData.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                selectedImageId = (int) id;
                query="DELETE FROM york WHERE plain ='"+york[position]+"';";
                db.execSQL(query);

                if(position>=mList.size()-1){
                    Intent intentSubActivity = new Intent(DbListActivity.this,
                            DbListActivity.class);
                    startActivity(intentSubActivity);
                    finish();
                }
                else{
                    mList.remove(position);
                    mCustomAdapter.notifyDataSetChanged();
                    mCustomAdapter.getView(position, v, parent);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            default:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        cursor = db.rawQuery("SELECT count(*) FROM york;", null); //욕테이블 갯수
        cursor.moveToFirst();
        int count = Integer.parseInt(cursor.getString(cursor.getColumnIndex("count(*)")));
        york = new String[count]; //갯수만큼 할당
        emoticon = new String[count];
        cursor = db.rawQuery("SELECT * FROM york;", null);

        int index=0;
        if (cursor.moveToFirst()) {
            do {
                york[index] = cursor.getString(cursor.getColumnIndex("plain"));
                emoticon[index] = cursor.getString(cursor.getColumnIndex("emoticon"));
                mList.add(new YorkData(york[index++],R.drawable.delete));
            } while (cursor.moveToNext());
        }

        mCustomAdapter.notifyDataSetChanged();
    }

}