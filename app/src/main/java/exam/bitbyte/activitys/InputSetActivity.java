package exam.bitbyte.activitys;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import exam.bitbyte.R;

public class InputSetActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inputset);

        InputSetActivity.this.startActivity(new Intent("android.settings.INPUT_METHOD_SETTINGS"));
    }
}