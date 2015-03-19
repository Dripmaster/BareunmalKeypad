package exam.bitbyte.adapters;


import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import exam.bitbyte.R;
import exam.bitbyte.YorkData;

public class YorkCustomAdapter extends ArrayAdapter<YorkData>
{
	private Context mContext;
	private int mLayoutResource;
	private ArrayList<YorkData> mList;
	
	private LayoutInflater mInflater;
	
	public YorkCustomAdapter(Context context, int rowLayoutResource, ArrayList<YorkData> objects)
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
	public YorkData getItem(int position)
	{
		return mList.get(position);
	}

	@Override
	public int getPosition(YorkData item)
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
		
		YorkData data = getItem(position);
		
		if(data != null)
		{
			ImageView ivImage = (ImageView)convertView.findViewById(R.id.item_click_example_york_iv_image);
			TextView tvTitle = (TextView)convertView.findViewById(R.id.item_click_example_york_tv_title);
			
			ivImage.setImageResource(data.getImage());
			tvTitle.setText(data.getTitle());
		}

		return convertView;
	}

}
