package org.opencv.samples.facedetect;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyAdapter extends BaseAdapter {
	private LayoutInflater adapterLayoutInflater;
	public MyAdapter(Context c){
		adapterLayoutInflater = LayoutInflater.from(c);

	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 4;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		// TODO Auto-generated method stub
		TagView tag;
		if(view == null){
			view = adapterLayoutInflater.inflate(R.layout.adapter, null);
			tag = new TagView(
					(ImageView)view.findViewById(R.id.AdapterImage),
					(TextView)view.findViewById(R.id.AdapterText),
                    (TextView)view.findViewById(R.id.AdapterText2));
			view.setTag(tag);
		}
		else{
			tag = (TagView)view.getTag();
		}
        int index=4;
        String [] records=DBfunction.best_record(4);
        if (position==0) {
            tag.image.setBackgroundResource(R.drawable.one);
            tag.text.setText("1st");
            tag.text2.setText(records[0]+" : "+records[0+index]);
        }
        else if (position==1) {
            tag.image.setBackgroundResource(R.drawable.two);
            tag.text.setText("2nd");
            tag.text2.setText(records[1]+" : "+records[1+index]);
        }
        else if (position==2) {
            tag.image.setBackgroundResource(R.drawable.third);
            tag.text.setText("3rd");
            tag.text2.setText(records[2]+" : "+records[2+index]);
        }
        else {
            tag.image.setBackgroundResource(R.drawable.point);
            tag.text.setText("4th");
            tag.text2.setText(records[3]+" : "+records[3+index]);
        }


        return view;
	}
	public class TagView{
		ImageView image;
		TextView text,text2;
		
		public TagView(ImageView image, TextView text, TextView text2){
			this.image = image;
			this.text = text;
            this.text2=text2;
			
		}
	}
}
