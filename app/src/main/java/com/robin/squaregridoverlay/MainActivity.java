package com.robin.squaregridoverlay;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
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
import android.util.Log;
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

    private int lockedColour = Color.RED;
    private int unLockedColour = Color.GREEN;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionCheck();
        try {

            process = launchLogcat();
            setContentView(R.layout.activity_main);
            final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);


            final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            pictureView = (PictureView) findViewById(R.id.pictureView);


            final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

            fab.setBackgroundTintList(ColorStateList.valueOf(unLockedColour));

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    locked = !locked;
                    if (locked) {
                        Snackbar.make(view, "Locked", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                        // orientation = getRequestedOrientation();
                        //  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
                        fab.setBackgroundTintList(ColorStateList.valueOf(lockedColour));

                    } else {
                        Snackbar.make(view, "Unlocked", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        //  setRequestedOrientation(orientation);
                        fab.setBackgroundTintList(ColorStateList.valueOf(unLockedColour));
                    }
                    pictureView.setStateLocked(locked);
                }
            });

            final FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);

            fab2.setImageResource(android.R.drawable.ic_menu_rotate);

            fab2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean isrotating = pictureView.getRotating();
                    pictureView.setRotating(!isrotating);
                    if (locked) return;
                    if (isrotating) {
                        fab2.setImageResource(android.R.drawable.ic_menu_rotate);
                    } else {
                        fab2.setImageResource(android.R.drawable.ic_menu_search);
                    }
                    pictureView.invalidate();
                }
            });


            final FloatingActionButton fab3 = (FloatingActionButton) findViewById(R.id.fab3);

            //set to gray the no Colour Colour
            fab3.setBackgroundTintList(ColorStateList.valueOf(PictureView.noColourColour));

            try {
                int col = pictureView.getNextColour();
                fab3.setBackgroundTintList(ColorStateList.valueOf(col));
            } catch (Exception ex) {
                Log.d("!","unable to preempt fab3 button");
            }

            fab3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //if (locked) return;
                    pictureView.incrementColourPointer();
                    int col = pictureView.getNextColour();
                    fab3.setBackgroundTintList(ColorStateList.valueOf(col));
                    pictureView.invalidate();
                }
            });

            Intent intent = getIntent();
            String action = intent.getAction();
            String type = intent.getType();

            if (Intent.ACTION_SEND.equals(action) && type != null) {
                if (type.startsWith("image/")) {
                    handleSendImage(intent); // Handle single image being sent
                }
            }


        } catch (Exception ex) {
            Toast.makeText(this, "error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("colour", pictureView.getColourPointer());
        savedInstanceState.putInt("columns", pictureView.getColumns());
        savedInstanceState.putInt("rows", pictureView.getRows());
        savedInstanceState.putFloat("mPosX", pictureView.getPosX());
        savedInstanceState.putFloat("mPosY", pictureView.getPosY());
        savedInstanceState.putFloat("mRotate", pictureView.getRotate());
        savedInstanceState.putFloat("mScale", pictureView.getScale());
        String stringUri = "";
        if (imageUri != null) stringUri = imageUri.toString();
        savedInstanceState.putString("imageUri", stringUri);

        savedInstanceState.putBoolean("longHeight", pictureView.isLongHeight());
        savedInstanceState.putBoolean("longWidth", pictureView.isLongWidth());
        savedInstanceState.putBoolean("square", pictureView.isSquare());

    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            pictureView.setColour(savedInstanceState.getInt("colour", 0));
            pictureView.setColumns(savedInstanceState.getInt("columns", 1));
            pictureView.setRows(savedInstanceState.getInt("rows", 1));
            pictureView.setPosX(savedInstanceState.getFloat("mPosX", 0f));
            pictureView.setPosY(savedInstanceState.getFloat("mPosY", 0f));
            pictureView.setRotate(savedInstanceState.getFloat("mRotate", 0f));
            pictureView.setScale(savedInstanceState.getFloat("mScale", 1.0f));
            imageUri = Uri.parse(savedInstanceState.getString("imageUri", ""));


            pictureView.setLongWidth(savedInstanceState.getBoolean("longWidth", false));
            pictureView.setLongHeight(savedInstanceState.getBoolean("longHeight", false));
            pictureView.setSquare(savedInstanceState.getBoolean("square", true));

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
        if (id == R.id.action_grid_2x2) {
            item.setChecked(true);
            pictureView.setRowsCols(2, 2);
            pictureView.invalidate();
            return true;
        } else if (id == R.id.action_grid_3x3) {
            item.setChecked(true);
            pictureView.setRowsCols(3, 3);
            pictureView.invalidate();
            return true;
        } else if (id == R.id.action_grid_4x4) {
            item.setChecked(true);
            pictureView.setRowsCols(4, 4);
            pictureView.invalidate();
            return true;
        } else if (id == R.id.action_grid_5x5) {
            item.setChecked(true);
            pictureView.setRowsCols(5, 5);
            pictureView.invalidate();
            return true;
        } else if (id == R.id.action_grid_8x8) {
            item.setChecked(true);
            pictureView.setRowsCols(8, 8);
            pictureView.invalidate();
            return true;
        } else if (id == R.id.action_grid_7x5) {
            item.setChecked(true);
            pictureView.setRowsCols(5, 7);
            pictureView.invalidate();
            return true;
        } else if (id == R.id.action_grid_5x7) {
            item.setChecked(true);
            pictureView.setRowsCols(7, 5);
            pictureView.invalidate();
            return true;
        } else if (id == R.id.action_grid_4x3) {
            item.setChecked(true);
            pictureView.setRowsCols(3, 4);
            pictureView.invalidate();
            return true;
        } else if (id == R.id.action_grid_3x4) {
            item.setChecked(true);
            pictureView.setRowsCols(4, 3);
            pictureView.invalidate();
            return true;
        } else if (id == R.id.action_reset) {
            pictureView.setRowsCols(0, 0);
            pictureView.setRotate(0.0f);
            pictureView.setScale(1.0f);
            pictureView.setPosX(0.0f);
            pictureView.setPosY(0.0f);
            pictureView.invalidate();
            return true;
        } else if (id == R.id.longheight) {
            item.setChecked(true);
            pictureView.setLongHeight(true);
            pictureView.setLongWidth(false);
            pictureView.setSquare(false);
            pictureView.invalidate();
            return true;
        } else if (id == R.id.longwidth) {
            item.setChecked(true);
            pictureView.setLongHeight(false);
            pictureView.setLongWidth(true);
            pictureView.setSquare(false);
            pictureView.invalidate();
            return true;
        } else if (id == R.id.square) {
            item.setChecked(true);
            pictureView.setLongHeight(false);
            pictureView.setLongWidth(false);
            pictureView.setSquare(true);
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

    private void handleSendImage(Intent intent) {
        imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            // Update UI to reflect image being shared
            loadPicture();
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
