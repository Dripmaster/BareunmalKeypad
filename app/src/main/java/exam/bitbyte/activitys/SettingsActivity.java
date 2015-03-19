package exam.bitbyte.activitys;

import java.util.ArrayList;

import android.app.Dialog;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import exam.bitbyte.adapters.CustomAdapter;
import exam.bitbyte.Data;
import exam.bitbyte.MySQLiteOpenHelper;
import exam.bitbyte.R;

public class SettingsActivity extends ActionBarActivity {

	Integer[] images = { R.drawable.on, R.drawable.off, R.drawable.on,
			R.drawable.off, R.drawable.on, R.drawable.off };

	Integer[] history = { R.string.history_1, R.string.history_2,
			R.string.history_3 };

	Integer[] title = { R.string.title_1, R.string.title_2, R.string.title_3 };

	private ListView mLvData; //
	private CustomAdapter mCustomAdapter;
	private ArrayList<Data> mList;
    private Toolbar toolbar;

	static final int DIALOG_CUSTOM_ID = 0;
	int selectedImageId = 0;
	Dialog dialog = null;
	ImageView image;
	TextView title_history;
	TextView description_history;
	MySQLiteOpenHelper testHelper;
	SQLiteDatabase db;
	String query;
	Cursor cursor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("설정");

		/* db 준비 */
		testHelper = new MySQLiteOpenHelper(this);
		db = testHelper.getWritableDatabase();
		/* db끝 */

		mLvData = (ListView) findViewById(R.id.ListView1);
		mList = new ArrayList<Data>();

		mCustomAdapter = new CustomAdapter(this, R.layout.setting_list_view, mList);

		mLvData.setAdapter(mCustomAdapter);

		mLvData.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				selectedImageId = (int) id;
				mList.get(position).setchceck();
				mCustomAdapter.getView(position, v, parent);
        /*셋팅 */
                cursor = db.rawQuery("SELECT * FROM settings;", null);
                String name[] = { "", "", "", "", "" },
                        user_selected[] = { "", "", "","", "" };
                int index = 0;
                if (cursor.moveToFirst()) {
                    do {
                        name[index] = cursor.getString(cursor.getColumnIndex("name"));
                        user_selected[index++] = cursor.getString(cursor
                                .getColumnIndex("user_selected"));
                    } while (cursor.moveToNext());
                }


                switch ((int) id) {
				case 0:/* 욕설 사용시 진동 버튼이 눌렷습니다. */
                    System.out.println("욕설 사용 버튼 : " + user_selected[1]);
                    if(user_selected[1].equals("0"))
                        query="UPDATE settings SET user_selected=1 WHERE name = 'jindong';";
                    else
                        query="UPDATE settings SET user_selected=0 WHERE name = 'jindong';";
                    db.execSQL(query);
					break;
				case 1:/* 욕설 자동 변환 버튼이 눌렷습니다. */
                    if(user_selected[2].equals("0"))
                        query="UPDATE settings SET user_selected=1 WHERE name = 'emoticon';";
                    else
                        query="UPDATE settings SET user_selected=0 WHERE name = 'emoticon';";
                    db.execSQL(query);
                    break;
				case 2:/* db 추가 버튼이 눌렷습니다. */
					// insert your code
					Intent intentSubActivity = new Intent(SettingsActivity.this,
							DbListActivity.class);
					startActivity(intentSubActivity);
					break;
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

		mList.clear();
		cursor = db.rawQuery("SELECT * FROM settings;", null);
		String name[] = { "", "", "", "", "" }, 
		user_selected[] = { "", "", "","", "" };
		int index = 0;
		if (cursor.moveToFirst()) {
			do {
				name[index] = cursor.getString(cursor.getColumnIndex("name"));
				user_selected[index++] = cursor.getString(cursor
						.getColumnIndex("user_selected"));
			} while (cursor.moveToNext());
		}
		
		if (user_selected[1].equals("1")) {
			mList.add(new Data(R.string.title_1, "욕설을 입력하면 진동이 울립니다.",
					R.drawable.on));
		} else {
			mList.add(new Data(R.string.title_1, "욕설을 입력하면 진동이 울립니다.",
					R.drawable.off));
		}
		
		if (user_selected[2].equals("1")) {
			mList.add(new Data(R.string.title_2,
					"욕설 입력시 이모티콘으로 자동 변환됩니다.", R.drawable.on));
		} else {
			mList.add(new Data(R.string.title_2,
					"욕설 입력시 이모티콘으로 자동 변환됩니다.", R.drawable.off));
		}
		
		mList.add(new Data(R.string.title_3, "기본으로 설정되지 않은 욕설을 추가합니다.",
				R.drawable.on));

		mCustomAdapter.notifyDataSetChanged();
	}

}