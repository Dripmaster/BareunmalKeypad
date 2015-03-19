package exam.bitbyte.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import exam.bitbyte.R;

/**
 * Created by neokree on 16/12/14.
 */
public class Tutorial2Fragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tutorial_layout_2, container, false);

        return rootView;
    }
}
