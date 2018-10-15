package com.robin.squaregridoverlay;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int MY_PERMISSIONS_REQUEST_READ_PICS = 0;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_PICS = 0;
    private final int SELECT_PHOTO = 1;
    Process process;
    private boolean locked = false;
    PictureView pictureView;
    Uri imageUri;
    int orientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionCheck();
        try {

            process = launchLogcat();
            setContentView(R.layout.activity_main);
            final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

            final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    locked = !locked;
                    if (locked) {
                        Snackbar.make(view, "Locked", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                        orientation= getRequestedOrientation();
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
                    } else {
                        Snackbar.make(view, "Unocked", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        setRequestedOrientation(orientation);
                    }
                    pictureView.setStateLocked(locked);
                }
            });

            pictureView = (PictureView) findViewById(R.id.pictureView);

        } catch (Exception ex) {
            Toast.makeText(this, "error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("colour", pictureView.getColour());
        savedInstanceState.putInt("columns", pictureView.getColumns());
        savedInstanceState.putInt("rows", pictureView.getRows());
        savedInstanceState.putFloat("mPosX", pictureView.getPosX());
        savedInstanceState.putFloat("mPosY", pictureView.getPosY());
        savedInstanceState.putFloat("mRotate", pictureView.getRotate());
        savedInstanceState.putFloat("mScale", pictureView.getScale());
        String stringUri="";
        if (imageUri != null) stringUri=imageUri.toString();
        savedInstanceState.putString("imageUri", stringUri);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            pictureView.setColour(savedInstanceState.getInt("colour", Color.BLACK));
            pictureView.setColumns(savedInstanceState.getInt("columns", 1));
            pictureView.setRows(savedInstanceState.getInt("rows",1));
            pictureView.setPosX(savedInstanceState.getFloat("mPosX", 0f));
            pictureView.setPosY(savedInstanceState.getFloat("mPosY", 0f));
            pictureView.setRotate(savedInstanceState.getFloat("mRotate", 0f));
            pictureView.setScale(savedInstanceState.getFloat("mScale", 1.0f));
            imageUri = Uri.parse(savedInstanceState.getString("imageUri", ""));
            loadPicture();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (locked) return true;
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_grid_2x2) {
            pictureView.setRowsCols(2, 2);
            pictureView.invalidate();
            return true;
        } else if (id == R.id.action_grid_3x3) {
            pictureView.setRowsCols(3, 3);
            pictureView.invalidate();
            return true;
        }else if (id == R.id.action_grid_4x4) {
            pictureView.setRowsCols(4, 4);
            pictureView.invalidate();
            return true;
        }else if (id == R.id.action_grid_5x5) {
            pictureView.setRowsCols(5, 5);
            pictureView.invalidate();
            return true;
        }else if (id == R.id.action_grid_4x3) {
            pictureView.setRowsCols(4, 3);
            pictureView.invalidate();
            return true;
        }else if (id == R.id.action_grid_3x4) {
            pictureView.setRowsCols(3, 4);
            pictureView.invalidate();
            return true;
        } else if (id == R.id.action_grid_colour_black) {
            pictureView.setColour(Color.BLACK);
            pictureView.invalidate();
            return true;
        } else if (id == R.id.action_grid_colour_yellow) {
            pictureView.setColour(Color.YELLOW);
            pictureView.invalidate();
            return true;
        } else if (id == R.id.action_image_rotate_mode) {
            pictureView.setRotating(true);
            pictureView.invalidate();
            return true;
        } else if (id == R.id.action_image_scale_mode) {
            pictureView.setRotating(false);
            pictureView.invalidate();
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_gallery) {
            Intent selectImageIntent = new Intent(Intent.ACTION_PICK);
            selectImageIntent.setType("image/*");
            if (selectImageIntent.resolveActivity(getPackageManager()) != null) {
                // Bring up gallery to select a photo
                startActivityForResult(selectImageIntent, SELECT_PHOTO);
            }
           // Intent chooser = Intent.createChooser(selectImageIntent, "Choose Picture");
          //  startActivityForResult(chooser, SELECT_PHOTO);

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    final Uri _imageUri = imageReturnedIntent.getData();
                    imageUri = _imageUri;
                    loadPicture();
                }
        }
    }

    public void loadPicture() {
        try {
            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream).copy(Bitmap.Config.ARGB_8888, true);
            pictureView.setBitmap(bitmap);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void permissionCheck() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_PICS
                );
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show();
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_PICS
                );
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show();
        }
    }

    private Process launchLogcat() {
        try {
            File filename = new File(Environment.getExternalStorageDirectory() + "/gridthing-logfile.log");
            filename.createNewFile();
            String cmd = "logcat -d -f " + filename.getAbsolutePath();
            return Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return null;
        }
    }
}
