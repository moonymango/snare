package com.moonymango.snareDemo;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class SnareDemoActivity extends ListActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setListAdapter(new DemoListAdapter(this));
        
        ListView v = getListView();
        v.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Log.d("samples", "select");
                //Toast.makeText(getApplicationContext(), Sample.getSample(position).getName(), Toast.LENGTH_LONG).show();
                Demo sample = Demo.get(position);
                startActivity(new Intent(getApplicationContext(), sample.mCLZ));
                
            }
        });
        
    }
}