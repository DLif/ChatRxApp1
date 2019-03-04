package com.example.chatrxtest1.MessagesMVC;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.chatrxtest1.Audio.AudioPlayer;
import com.example.chatrxtest1.Audio.AudioStateConveyor;

public class ChatMessage implements Parcelable {
    private String _text;
    private MemberData _memberData;

    private boolean _isAudioAvailable;

    private boolean _isAudioId;
    private int _audioId;
    private String _audioPath;
    private int _audioPosition;

    private AudioPlayer _audioPlayer;
    private boolean _isPlaying;

    public ChatMessage(String text, MemberData data) {
        _text = text;
        _memberData = data;

        _isAudioAvailable = false;
        _audioPath = "";
        _isPlaying = false;
    }

    public ChatMessage(String text, MemberData data, int audioId) {
        _text = text;
        _memberData = data;
        _audioId = audioId;
        _isAudioId = true;
        _isAudioAvailable = true;
        _audioPosition= -1;
        _isPlaying = false;
    }

    public ChatMessage(String text, MemberData data, String audioPath) {
        _text = text;
        _memberData = data;
        _audioPath = audioPath;
        _isAudioId = false;
        _isAudioAvailable = true;
        _audioPosition = -1;
        _isPlaying = false;
    }

    protected ChatMessage(Parcel in) {
        _text = in.readString();
        _isAudioAvailable = in.readByte() != 0;
        _isAudioId = in.readByte() != 0;
        _audioId = in.readInt();
        _audioPath = in.readString();
        _audioPosition = in.readInt();
        _isPlaying= false;
    }

    public String getText() {
        return _text;
    }

    public MemberData get_memberData() {
        return _memberData;
    }

    public boolean getIsAudioAvailable() { return _isAudioAvailable; }

    public AudioPlayer getAudioPlayer() {
        return _audioPlayer;
    }

    public boolean audio_play() {
        boolean retVal = false;
        if (_isAudioAvailable && !_isPlaying) {
            if (_isAudioId) {
                retVal = _audioPlayer.load_setPosition_play(_audioId);
            } else {
                retVal = _audioPlayer.load_setPosition_play(_audioPath);
            }
            _isPlaying = true;
        }
        return retVal;
    }

    public void setAudioStateConveyor(AudioStateConveyor audioStateConveyor, Context context) {
        if (_isAudioAvailable) {
            _audioPlayer = new AudioPlayer(context);
            _audioPlayer.set_audioStateConveyor(audioStateConveyor);
            if (_audioPosition != -1) _audioPlayer.seekTo(_audioPosition);
        }
    }

    public static String getRandomText() {
        String[] adjs = {"autumn", "hidden", "bitter", "misty", "silent", "empty", "dry", "dark", "summer", "icy", "delicate", "quiet", "white", "cool", "spring", "winter", "patient", "twilight", "dawn", "crimson", "wispy", "weathered", "blue", "billowing", "broken", "cold", "damp", "falling", "frosty", "green", "long", "late", "lingering", "bold", "little", "morning", "muddy", "old", "red", "rough", "still", "small", "sparkling", "throbbing", "shy", "wandering", "withered", "wild", "black", "young", "holy", "solitary", "fragrant", "aged", "snowy", "proud", "floral", "restless", "divine", "polished", "ancient", "purple", "lively", "nameless"};
        String[] nouns = {"waterfall", "river", "breeze", "moon", "rain", "wind", "sea", "morning", "snow", "lake", "sunset", "pine", "shadow", "leaf", "dawn", "glitter", "forest", "hill", "cloud", "meadow", "sun", "glade", "bird", "brook", "butterfly", "bush", "dew", "dust", "field", "fire", "flower", "firefly", "feather", "grass", "haze", "mountain", "night", "pond", "darkness", "snowflake", "silence", "sound", "sky", "shape", "surf", "thunder", "violet", "water", "wildflower", "wave", "water", "resonance", "sun", "wood", "dream", "cherry", "tree", "fog", "frost", "voice", "paper", "frog", "smoke", "star"};
        return (
                adjs[(int) Math.floor(Math.random() * adjs.length)] + "_" +
                nouns[(int) Math.floor(Math.random() * nouns.length)]  + "_" +
                adjs[(int) Math.floor(Math.random() * adjs.length)] + "_" +
                nouns[(int) Math.floor(Math.random() * nouns.length)]  + "_" +
                adjs[(int) Math.floor(Math.random() * adjs.length)] + "_" +
                nouns[(int) Math.floor(Math.random() * nouns.length)]
        );
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        byte isAudioAvailable_byte = 0;
        byte isAudioId_byte = 0;

        if (_isAudioAvailable) {
            isAudioAvailable_byte = 1;
            _audioPosition = _audioPlayer.get_positionMemory();
        }
        if (_isAudioId) isAudioId_byte = 1;


        dest.writeString(_text);
        dest.writeString(_memberData.getName());
        dest.writeByte(isAudioAvailable_byte);
        dest.writeByte(isAudioId_byte);
        dest.writeInt(_audioId);
        dest.writeString(_audioPath);
        dest.writeInt(_audioPosition);
    }

    public void audioReset() {
        if (_isAudioAvailable) {
            _audioPlayer.reset();
            _audioPosition = _audioPlayer.get_positionMemory();
            _isPlaying = false;
        }
    }

    public void audioPause() {
        if (_isAudioAvailable) {
            _audioPlayer.pause();
            _audioPosition = _audioPlayer.get_positionMemory();
            _isPlaying = false;
        }
    }

    public static final Creator<ChatMessage> CREATOR = new Creator<ChatMessage>() {
        @Override
        public ChatMessage createFromParcel(Parcel in) {
            return new ChatMessage(in);
        }

        @Override
        public ChatMessage[] newArray(int size) {
            return new ChatMessage[size];
        }
    };
}
