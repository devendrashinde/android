package com.example.dshinde.myapplication_xmlpref;

import android.content.ComponentName;

import com.example.dshinde.myapplication_xmlpref.common.Constants;
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
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    boolean autoKey=true;
    String collectionName = "CafeSettings";
    List<KeyValue> data = new ArrayList<>();

    @Test
    public void addition_isCorrect() throws Exception {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance()
                .getReference((autoKey ? "" : Constants.DATABASE_PATH_NOTE_DETAILS + "/") + collectionName + "/" + mAuth.getUid());
        readDataOnce();
        assertFalse(data.isEmpty());
    }

    private void readDataOnce(){
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                loadData(snapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadData(DataSnapshot snapshot) {
        data.clear();
        //iterating through all the values in database
        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
            String key = autoKey ? (String) postSnapshot.getValue() : postSnapshot.getKey();
            String value = autoKey ? postSnapshot.getKey() : (String) postSnapshot.getValue();

            KeyValue upload = new KeyValue(key, value);
            data.add(upload);
        }
    }

}