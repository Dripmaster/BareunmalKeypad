package exam.bitbyte.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import exam.bitbyte.R;
import exam.bitbyte.activitys.MainActivity;

/**
 * Created by neokree on 16/12/14.
 */
public class Tutorial6Fragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tutorial_layout_6, container, false);

        Button button = (Button) rootView.findViewById(R.id.tuto_btn1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), MainActivity.class));
                getActivity().finish();
            }
        });
        return rootView;
    }
}
