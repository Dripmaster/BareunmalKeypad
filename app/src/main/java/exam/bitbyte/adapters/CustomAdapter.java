package exam.bitbyte.adapters;


import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import exam.bitbyte.Data;
import exam.bitbyte.R;

public class CustomAdapter extends ArrayAdapter<Data>
{
	private Context mContext;
	private int mLayoutResource;
	private ArrayList<Data> mList;
	
	private LayoutInflater mInflater;
	
	public CustomAdapter(Context context, int rowLayoutResource, ArrayList<Data> objects)
	{
		super(context, rowLayoutResource, objects);
		this.mContext = context;
		this.mLayoutResource = rowLayoutResource;
		this.mList = objects;
		this.mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount()
	{
		return mList.size();
	}

	@Override
	public Data getItem(int position)
	{
		return mList.get(position);
	}

	@Override
	public int getPosition(Data item)
	{
		return mList.indexOf(item);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{

		if(convertView == null)
		{
			convertView = mInflater.inflate(mLayoutResource, null);
		}
		
		Data data = getItem(position);
		
		if(data != null)
		{
			ImageView ivImage = (ImageView)convertView.findViewById(R.id.item_click_example_row_iv_image);
			TextView tvTitle = (TextView)convertView.findViewById(R.id.item_click_example_row_tv_title);
			TextView tvDescription = (TextView)convertView.findViewById(R.id.item_click_example_row_tv_description);

			if(data.getchecked()&&!(data.getTitle()==R.string.title_3))
			{
				ivImage.setImageResource(R.drawable.on);
			}
			else if(!(data.getTitle()==R.string.title_3))
			{
				ivImage.setImageResource(R.drawable.off);
			}
			tvTitle.setText(data.getTitle());
			tvDescription.setText(data.getDescription());
		}

		return convertView;
	}

}
