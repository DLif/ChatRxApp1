package com.example.chatrxtest1.MessagesMVC;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.chatrxtest1.Audio.AudioStateConveyor;
import com.example.chatrxtest1.R;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends BaseAdapter {

    private ArrayList<ChatMessage> _messages;
    private Context _context;
    private Activity _activity;
    private boolean _autoStartPreference;

    public MessageAdapter(Context context, Activity activity, boolean autoStartPreference) {
        _context = context;
        _activity = activity;
        _messages = new ArrayList<>();
        _autoStartPreference = autoStartPreference;
    }

    public void set_autoStartPreference(boolean autoStartPreference) {
        _autoStartPreference = autoStartPreference;
    }

    public ArrayList<ChatMessage> get_messages() {
        return _messages;
    }

    public void add(ChatMessage message) {
        //Stop the previous message running - if it is
        for (ChatMessage oldMsg : _messages) {
            oldMsg.audioReset();
        }

        this._messages.add(message);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return _messages.size();
    }

    @Override
    public Object getItem(int i) {
        return _messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint({"ClickableViewAccessibility", "ViewHolder"})
    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup) {
        ChatMessage message = _messages.get(i);
        MessageViewHolder holder;

        //This is done according to the view holder pattern
        holder = new MessageViewHolder();
        holder.originModel = message;

        LayoutInflater messageInflater = (LayoutInflater) _context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        convertView = messageInflater.inflate(R.layout.incoming_message, null);

        holder.name = convertView.findViewById(R.id.name);
        holder.messageBody =  convertView.findViewById(R.id.message_body);
        holder.playPauseButton =  convertView.findViewById(R.id.play_pause_button);
        holder.seekBar =  convertView.findViewById(R.id.audio_seekBar);

        if (message.getIsAudioAvailable()) {
            holder.playPauseButton.setSelected(false); //Start with play
            holder.playPauseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //v.setSelected(!v.isSelected());
                    if (!v.isSelected()) {
                        _messages.get(i).audio_play();
                    } else {
                        _messages.get(i).audioPause();
                    }
                }
            });

            //This disables the drag option on the seek bar
            holder.seekBar.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return true;
                }
            });


            AudioStateConveyor audioStateConveyor = new AudioStateConveyor(holder, _activity);
            message.setAudioStateConveyor(audioStateConveyor, _context);
        }

        convertView.setTag(holder);

        holder.name.setText(message.get_memberData().getName());
        holder.messageBody.setText(message.getText());

        if (message.getIsAudioAvailable()) {
            //set autoPlay on last
            if (_autoStartPreference && i == (_messages.size() - 1)) {
                holder.playPauseButton.callOnClick();
            }
        } else {
            holder.playPauseButton.setVisibility(View.GONE);
            holder.seekBar.setVisibility(View.GONE);
        }

        return convertView;
    }

    public void pauseAll() {
        for (ChatMessage message : _messages) {
            message.audioPause();
        }
    }

    public void release() {
        for (ChatMessage message : _messages) {
            if (message.getIsAudioAvailable()) {
                message.getAudioPlayer().release();
            }
        }
    }
}

