package com.lightmsg.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lightmsg.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;


public class EmojiUtil {
    private static final String TAG = EmojiUtil.class.getSimpleName();

    private int pageSize = 20;
    
    private int IMAGE_DIP_SMALL_SIZE = 20; //dip
    private int IMAGE_DIP_BIG_SIZE = 30; //dip
    private int IMAGE_DIP_SIZE = IMAGE_DIP_SMALL_SIZE; //dip
    
    private float IMAGE_PX_SIZE;

    private static EmojiUtil mEmojiUtil;

    private HashMap<String, String> emojiMap = new HashMap<String, String>();

    private List<ChatEmoji> emojis = new ArrayList<ChatEmoji>();

    public List<List<ChatEmoji>> emojiLists = new ArrayList<List<ChatEmoji>>();

    private EmojiUtil() {

    }

    public static EmojiUtil getInstance() {
        if (mEmojiUtil == null) {
            mEmojiUtil = new EmojiUtil();
        }
        return mEmojiUtil;
    }

    public SpannableString addEmoji(Context context, int imgId, String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), imgId);
        //IMAGE_PX_SIZE = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, IMAGE_DIP_SIZE,
        //        context.getResources().getDisplayMetrics());
        float textSize = (float)context.getResources().getDimensionPixelSize(R.dimen.chat_btm_send_edittext_emoj_size);
        IMAGE_PX_SIZE = textSize;//*(4/3)+37-20;
        Log.v(TAG, "IMAGE_PX_SIZE="+IMAGE_PX_SIZE+"px");
        bitmap = Bitmap.createScaledBitmap(bitmap, (int)IMAGE_PX_SIZE, (int)IMAGE_PX_SIZE, true);
        ImageSpan imageSpan = new ImageSpan(context, bitmap);
        SpannableString spannable = new SpannableString(str);
        spannable.setSpan(imageSpan, 0, str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    public void setImageSize(int size) {
        IMAGE_DIP_SIZE = size;
    }
    
    public void setImageSizeSmall() {
        setImageSize(IMAGE_DIP_SMALL_SIZE);
    }
    
    public void setImageSizeBig() {
        setImageSize(IMAGE_DIP_BIG_SIZE);
    }

    public SpannableString getExpressionString(Context context, String str) {
        if (str == null) {
            str = "";
        }
        
        SpannableString spannableString = new SpannableString(str);

        String re = "\\[[^\\]]+\\]";

        Pattern rePattern = Pattern.compile(re, Pattern.CASE_INSENSITIVE);
        try {
            dealExpression(context, spannableString, rePattern, 0);
        } catch (Exception e) {
            Log.e("dealExpression", e.getMessage());
        }
        return spannableString;
    }

    private void dealExpression(Context context,
            SpannableString spannableString, Pattern patten, int start)
                    throws Exception {
        Matcher matcher = patten.matcher(spannableString);
        while (matcher.find()) {
            String key = matcher.group();
            if (matcher.start() < start) {
                continue;
            }
            String value = emojiMap.get(key);
            if (TextUtils.isEmpty(value)) {
                continue;
            }
            int resId = context.getResources().getIdentifier(value, "drawable",
                    context.getPackageName());

            if (resId != 0) {
                Bitmap bitmap = BitmapFactory.decodeResource(
                        context.getResources(), resId);
                
                IMAGE_PX_SIZE = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, IMAGE_DIP_SIZE,
                        context.getResources().getDisplayMetrics());
                //Log.d(TAG, "IMAGE_PX_SIZE="+IMAGE_PX_SIZE);
                bitmap = Bitmap.createScaledBitmap(bitmap, (int)IMAGE_PX_SIZE, (int)IMAGE_PX_SIZE, true);

                //ImageSpan imageSpan = new ImageSpan(bitmap);
                ImageSpan imageSpan = new ImageSpan(context, bitmap);
                int end = matcher.start() + key.length();
                spannableString.setSpan(imageSpan, matcher.start(), end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (end < spannableString.length()) {
                    dealExpression(context, spannableString, patten, end);
                }
                break;
            }
        }
    }

    public void getFileText(Context context) {
        ParseAssetsData(EmojiFileUtils.getEmojiFile(context), context);
    }

    private void ParseAssetsData(List<String> data, Context context) {
        Log.v(TAG, "ParseAssetsData()...");
        if (data == null) {
            Log.e(TAG, "ParseAssetsData()..data="+data);
            return;
        }
        ChatEmoji emojEentry;
        try {
            Log.v(TAG, "ParseAssetsData()..data.size()="+data.size());
            for (String str : data) {
                Log.v(TAG, "ParseAssetsData()..str="+str);
                String[] text = str.split(",");
                String fileName = text[0]
                        .substring(0, text[0].lastIndexOf("."));
                emojiMap.put(text[1], fileName);
                int resID = context.getResources().getIdentifier(fileName,
                        "drawable", context.getPackageName());

                if (resID != 0) {
                    emojEentry = new ChatEmoji();
                    emojEentry.setId(resID);
                    emojEentry.setCharacter(text[1]);
                    emojEentry.setEmojiName(fileName);
                    emojis.add(emojEentry);
                }
            }
            int pageCount = (int) Math.ceil(emojis.size() / 20 + 0.1);

            Log.v(TAG, "ParseAssetsData()..pageCount="+pageCount);
            for (int i = 0; i < pageCount; i++) {
                emojiLists.add(getData(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getResText(Context context) {
        String[] emoji_files;
        String[] emoji_names;

        Resources res = context.getResources();
        Locale loc = res.getConfiguration().locale;
        Log.v(TAG, "Locale="+loc);
        if (loc.equals(Locale.CHINA)) {
            emoji_files = res.getStringArray(R.array.emoji_file);
            emoji_names = res.getStringArray(R.array.emoji_name_ch);
        } /*else if (loc.equals(Locale.ENGLISH)) {
            emoji_files = res.getStringArray(R.array.emoji_file);
            emoji_names = res.getStringArray(R.array.emoji_name_en);
        } */else {
            emoji_files = res.getStringArray(R.array.emoji_file);
            emoji_names = res.getStringArray(R.array.emoji_name_en);
        }

        ParseResData(emoji_files, emoji_names, context);
    }

    private void ParseResData(String[] files, String[] names, Context context) {
        Log.v(TAG, "ParseResData()...");

        ChatEmoji emojEentry;
        try {
            Log.v(TAG, "ParseResData(), files.length="+files.length+", names.length="+names.length);
            int i = 0;
            for (i = 0; i < files.length; i++) {
                String file = files[i];
                String name = names[i];
                //Log.v(TAG, "ParseResData(), file="+file+", name="+name);

                emojiMap.put(name, file);
                int resID = context.getResources().getIdentifier(file,
                        "drawable", context.getPackageName());

                if (resID != 0) {
                    emojEentry = new ChatEmoji();
                    emojEentry.setId(resID);
                    emojEentry.setCharacter(name);
                    emojEentry.setEmojiName(file);
                    emojis.add(emojEentry);
                }
            }
            int pageCount = (int) Math.ceil(emojis.size() / 20 + 0.1);

            Log.v(TAG, "ParseData()..pageCount="+pageCount);
            for (i = 0; i < pageCount; i++) {
                emojiLists.add(getData(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<ChatEmoji> getData(int page) {
        int startIndex = page * pageSize;
        int endIndex = startIndex + pageSize;

        if (endIndex > emojis.size()) {
            endIndex = emojis.size();
        }

        List<ChatEmoji> list = new ArrayList<ChatEmoji>();
        list.addAll(emojis.subList(startIndex, endIndex));
        if (list.size() < pageSize) {
            for (int i = list.size(); i < pageSize; i++) {
                ChatEmoji object = new ChatEmoji();
                list.add(object);
            }
        }
        if (list.size() == pageSize) {
            ChatEmoji object = new ChatEmoji();
            object.setId(R.drawable.face_del_icon);
            list.add(object);
        }
        return list;
    }

    public static class ChatEmoji {
        private int mID;
        private String mChar;
        private String mFileName;

        public void setId(int resID) {
            mID = resID;
        }
        public void setCharacter(String text) {
            mChar = text;
        }
        public void setEmojiName(String fileName) {
            mFileName = fileName;
        }

        public int getId() {
            return mID;
        }
        public String getCharacter() {
            return mChar;
        }
        public String getEmojiName() {
            return mFileName;
        }
    }
}
