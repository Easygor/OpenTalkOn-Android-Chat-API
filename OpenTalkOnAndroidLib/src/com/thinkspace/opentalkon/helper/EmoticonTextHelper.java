package com.thinkspace.opentalkon.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.Spannable;
import android.text.Spannable.Factory;
import android.text.style.ImageSpan;
import android.widget.TextView;

import com.thinkspace.opentalkon.R;

public class EmoticonTextHelper {
	private static final Factory spannableFactory = Spannable.Factory.getInstance();
	private static final Map<Pattern, Integer> emoticons = new HashMap<Pattern, Integer>();

	static {
	    addPattern(emoticons, "(like)", R.drawable.oto_like_icon);
	}

	private static void addPattern(Map<Pattern, Integer> map, String smile,
	        int resource) {
	    map.put(Pattern.compile(Pattern.quote(smile)), resource);
	}
	
	public static void applyTextEmoticon(Context context, TextView view){
		view.setText(getEmoticonText(context, view.getText()));
	}

	public static boolean addEmoticons(Context context, Spannable spannable) {
	    boolean hasChanges = false;
	    for (Entry<Pattern, Integer> entry : emoticons.entrySet()) {
	        Matcher matcher = entry.getKey().matcher(spannable);
	        while (matcher.find()) {
	            boolean set = true;
	            for (ImageSpan span : spannable.getSpans(matcher.start(),matcher.end(), ImageSpan.class))
	                if (spannable.getSpanStart(span) >= matcher.start() && spannable.getSpanEnd(span) <= matcher.end())
	                    spannable.removeSpan(span);
	                else {
	                    set = false;
	                    break;
	                }
	            if (set) {
	                hasChanges = true;
	                spannable.setSpan(new ImageSpan(context, entry.getValue()), matcher.start(), matcher.end(),
	                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	            }
	        }
	    }
	    return hasChanges;
	}

	public static Spannable getEmoticonText(Context context, CharSequence text) {
	    Spannable spannable = spannableFactory.newSpannable(text);
	    addEmoticons(context, spannable);
	    return spannable;
	}
	
}
