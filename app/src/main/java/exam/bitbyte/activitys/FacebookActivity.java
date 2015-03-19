package exam.bitbyte.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.android.Facebook;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.entities.Profile.Properties;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnLogoutListener;
import com.sromku.simple.fb.listeners.OnProfileListener;

import java.util.List;

import android.view.Menu;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import exam.bitbyte.adapters.Adapter;
import exam.bitbyte.FacebookFriend;
import exam.bitbyte.R;

public class FacebookActivity extends ActionBarActivity {

    private static final List<String> PERMISSIONS = Arrays.asList("user_friends", "manage_pages");
    private ArrayList<FacebookFriend> friends = new ArrayList<FacebookFriend>();

    protected static final String TAG ="";
    private Toolbar toolbar;
    private Button mButtonLogin;
    private Button mButtonLogout;
    private SimpleFacebook simplef;

    Permission[] permissions = new Permission[] {
            Permission.USER_PHOTOS,
            Permission.EMAIL,
            Permission.PUBLISH_ACTION,
            Permission.READ_FRIENDLISTS,
            Permission.USER_FRIENDS,
            Permission.PUBLIC_PROFILE,
            Permission.USER_ABOUT_ME,
            Permission.USER_ACTIVITIES,
            Permission.MANAGE_PAGES
    };

    SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
            .setAppId("823873040998862")
            .setNamespace("바른말키패드")
            .setPermissions(permissions)
            .build();

    Profile.Properties properties = new Profile.Properties.Builder()
            .add(Properties.ID)
            .add(Properties.FIRST_NAME)
            .add(Properties.COVER)
            .add(Properties.WORK)
            .add(Properties.EDUCATION)
            .add(Properties.PICTURE)
            .build();

    @Override
    protected void onResume() {
        super.onResume();
        simplef = SimpleFacebook.getInstance(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        simplef.onActivityResult(this, requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("친구들");

        SimpleFacebook.setConfiguration(configuration);
        simplef = SimpleFacebook.getInstance(this);

        mButtonLogin = (Button) findViewById(exam.bitbyte.R.id.entrar);
        mButtonLogout = (Button) findViewById(exam.bitbyte.R.id.salir);
        //mTextStatus = (TextView) findViewById(exam.bitbyte.R.id.mostrar);

        setLogin();
        setLogout();
        setUIState();
    }

    private void setLogin() {
        // Login listener
        final OnLoginListener onLoginListener = new OnLoginListener() {

            @Override
            public void onFail(String reason) {
                //mTextStatus.setText(reason);
                Log.w(TAG, "Failed to login");
            }

            @Override
            public void onException(Throwable throwable) {
                //mTextStatus.setText("Exception: " + throwable.getMessage());
                Log.e(TAG, "Bad thing happened", throwable);
            }

            @Override
            public void onThinking() {
                // show progress bar or something to the user while login is
                // happening
                //mTextStatus.setText("Thinking...");
            }

            @Override
            public void onLogin() {
                // change the state of the button or do whatever you want
                //mTextStatus.setText("Logged in");
                loggedInUIState();
            }

            @Override
            public void onNotAcceptingPermissions(Permission.Type type) {
                //				toast(String.format("You didn't accept %s permissions", type.name()));
            }
        };


        mButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                simplef.login(onLoginListener);
            }
        });
    }

    /**
     * Logout example
     */
    private void setLogout() {
        final OnLogoutListener onLogoutListener = new OnLogoutListener() {




            @Override
            public void onFail(String reason) {
                //mTextStatus.setText(reason);
                Log.w(TAG, "Failed to login");
            }

            @Override
            public void onException(Throwable throwable) {
                //mTextStatus.setText("Exception: " + throwable.getMessage());
                Log.e(TAG, "Bad thing happened", throwable);
            }



            @Override
            public void onThinking() {
                // show progress bar or something to the user while login is
                // happening
                //mTextStatus.setText("Thinking...");
            }

            @Override
            public void onLogout() {
                // change the state of the button or do whatever you want
                //mTextStatus.setText("Logged out");
                loggedOutUIState();
            }

        };

        mButtonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                simplef.logout(onLogoutListener);
            }
        });
    }

    private void setUIState() {
        if (simplef.isLogin()) {
            loggedInUIState();
        }
        else {
            loggedOutUIState();
        }
    }

    private void loggedInUIState() {
        mButtonLogin.setEnabled(false);
        mButtonLogout.setEnabled(true);

        //mTextStatus.setText("Logged in");
        oListener();


    }

    private void loggedOutUIState() {
        mButtonLogin.setEnabled(true);
        mButtonLogout.setEnabled(false);

        //mTextStatus.setText("Logged out");

    }


    public OnProfileListener oListener(){



        OnProfileListener onProfileListener = new OnProfileListener() {
            @Override

            public void onComplete(Profile profile) {
                Log.i(TAG, "Mi nombre = " + profile.getFirstName() + "Birthday" + profile.getBirthday());
                Toast.makeText(FacebookActivity.this, profile.getName()+ "님 안녕하세요 !", Toast.LENGTH_LONG).show();

                Session session = Session.getActiveSession();

                System.out.println("SESSION : " + session.toString());

                Facebook aa ;
                new Request(
                        session,
                        "/me/friends",
                        null,
                        HttpMethod.GET,
                        new Request.Callback() {
                            public void onCompleted(Response response) throws JSONException {
                                JSONArray array = null;
                                try {
                                    System.out.println(response);
                                    array = response.getGraphObject().getInnerJSONObject().getJSONArray("data");
                                    System.out.println(array);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                for(int i=0; i < array.length(); i++){

                                    JSONObject jsonObject = array.getJSONObject(i);

                                    //System.out.println(jsonObject.getJSONObject("picture").getJSONObject("data").getString("url") + " " + jsonObject.getString("id"));

                                    FacebookFriend fb = new FacebookFriend( jsonObject.getString("id"), jsonObject.getString("name"), "");
                                    friends.add(fb);


                                }
                                final ListView listview = (ListView) findViewById(R.id.listview);
                                Adapter adapter = new Adapter(FacebookActivity.this, friends);
                                listview.setAdapter(adapter);


                                //Log.d( "FacebookConnect", "FacebookConnect > retrieveUserData graphObjectList: " + response.getGraphObject().getInnerJSONObject());
                            }
                        }
                ).executeAsync();


            }

		    /*
		     * You can override other methods here:
		     * onThinking(), onFail(String reason), onException(Throwable throwable)
		     */
        };

        simplef.getProfile(onProfileListener);

        return onProfileListener;

    }

}