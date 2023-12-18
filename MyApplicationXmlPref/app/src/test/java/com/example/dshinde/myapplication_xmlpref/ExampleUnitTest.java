package com.example.dshinde.myapplication_xmlpref;

import android.content.ComponentName;

import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.helper.Utils;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void extractUrlsFromText() throws Exception {
        String urls[] = {"http://www.google.com", "http://devendra-shinde.com"};
        String text = "a " + urls[0] + " is goolge site where has " + urls[1] + " is Devendra Shinde's webpage";
        List<String> extractedUrls = Utils.extractLinks(text);
        assertArrayEquals(urls, extractedUrls.toArray(new String[extractedUrls.size()]));
    }


}