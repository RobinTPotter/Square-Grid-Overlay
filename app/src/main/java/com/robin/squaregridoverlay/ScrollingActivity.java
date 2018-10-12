package com.robin.squaregridoverlay;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;

public class ScrollingActivity extends Activity {

    GridView gridview;
    File[] files;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        gridview = (GridView) findViewById(R.id.grid_view);


        ImageAdapter imad = new ImageAdapter(this);
        File cam = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM)
                , "Camera");

        files = cam.listFiles();

        imad.setFiles(Arrays.copyOfRange(files,0,10));

        Toast.makeText(ScrollingActivity.this, imad.toString(), Toast.LENGTH_SHORT).show();

        gridview.setAdapter(imad);
        //resizeGridView(files.length, 3);

        Toast.makeText(ScrollingActivity.this, "" + String.valueOf(gridview.getWidth())+" "+ String.valueOf(gridview.getHeight()), Toast.LENGTH_SHORT).show();

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(ScrollingActivity.this, "" + files[position].getPath(),
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ScrollingActivity.this, MainActivity.class);
                intent.putExtra("Picture", files[position].getPath() );
                startActivity(intent);
            }
        });
    }

    private void resizeGridView(int items, int columns) {
        ViewGroup.LayoutParams params = gridview.getLayoutParams();
        int oneRowHeight = gridview.getHeight();
        int rows = (int) (items / columns);
        params.height = oneRowHeight * rows;
        gridview.setLayoutParams(params);
        gridview.invalidate();
    }

}
