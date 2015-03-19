package exam.bitbyte.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.facebook.widget.ProfilePictureView;

import java.util.List;

import exam.bitbyte.FacebookFriend;
import exam.bitbyte.R;

public class Adapter extends ArrayAdapter<FacebookFriend> {
	  private final Context context;
	  private final List<FacebookFriend> values;

	  public Adapter(Context context, List<FacebookFriend> friends) {
	    super(context, R.layout.list_item,friends);
	    this.context = context;
	    this.values = friends;
	  }

	  @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
		FacebookFriend friend = values.get(position);
	    LayoutInflater inflater = (LayoutInflater) context
	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.list_item, parent, false);
	    TextView textView = (TextView) rowView.findViewById(R.id.name);
	    ProfilePictureView imageView = (ProfilePictureView) rowView.findViewById(R.id.icon);
	    textView.setText(friend.getFirstName()+" "+friend.getLastName());



	    imageView.setProfileId(friend.getId());
	    return rowView;
	  }
	} 