package com.yobotics.control.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yobotics.control.bean.ChatMessage;
import com.yobotics.control.bean.ChatMessage.Type;
import com.yobotics.control.demo.R;

import java.util.List;


public class ChatMessageAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<ChatMessage> mDatas;
 
	public ChatMessageAdapter(Context context, List<ChatMessage> datas) {
		mInflater = LayoutInflater.from(context);
		mDatas = datas;
	
	}

	@Override
	public int getCount() {
		return mDatas.size();
	}

	@Override
	public Object getItem(int position) {
		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * 接受到消息为1，发送消息为0
	 */
	@Override
	public int getItemViewType(int position) {
		ChatMessage msg = mDatas.get(position);
		return msg.getType() == Type.INPUT ? 1 : 0;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ChatMessage chatMessage = mDatas.get(position);

		ViewHolder viewHolder = null;

		if (convertView == null) {
			viewHolder = new ViewHolder();
			if (chatMessage.getType() == Type.INPUT) {
				convertView = mInflater.inflate(R.layout.main_chat_from_msg,
						parent, false);
				viewHolder.createDate = (TextView) convertView.findViewById(R.id.chat_from_createDate);
				viewHolder.content = (TextView) convertView.findViewById(R.id.chat_from_content);
				viewHolder.imageView = (ImageView) convertView.findViewById(R.id.chat_from_ImageView);
				convertView.setTag(viewHolder);
			} else {
				convertView = mInflater.inflate(R.layout.main_chat_send_msg,
						null);

				viewHolder.createDate = (TextView) convertView.findViewById(R.id.chat_send_createDate);
				viewHolder.content = (TextView) convertView.findViewById(R.id.chat_send_content);
				viewHolder.imageView = (ImageView) convertView.findViewById(R.id.chat_send_ImageView);
				convertView.setTag(viewHolder);
			}

		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
        if(chatMessage.getMsg()!=null&&"img".equals(chatMessage.getMsg())){
        	viewHolder.content.setVisibility(View.GONE);
        	viewHolder.imageView.setVisibility(View.VISIBLE);
        	viewHolder.imageView.setImageURI(Uri.parse(chatMessage.getVideoUrl()));
        	 
        }else{
        	viewHolder.content.setVisibility(View.VISIBLE);
        	viewHolder.imageView.setVisibility(View.GONE);
        	viewHolder.content.setText(chatMessage.getMsg());
		}
		viewHolder.createDate.setText(chatMessage.getDateStr());

		return convertView;
	}

	private class ViewHolder {
		public TextView createDate;
		public TextView name;
		public TextView content;
		
		public ImageView imageView;
	}

}
