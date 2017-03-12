package com.lightmsg.activity.msgdesign.prof;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lightmsg.LightMsg;
import com.lightmsg.R;
import com.lightmsg.service.CoreService;
import com.lightmsg.service.CoreService.Account;
import com.lightmsg.util.MediaFile;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class PersonalInfo extends FragmentActivity {

    private static final String TAG = PersonalInfo.class.getSimpleName();

    private LightMsg app = null;
    private CoreService xs = null;
    private ImageView pin;
    private TextView nick;
    private TextView account;
    private TextView gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate()... ");
        super.onCreate(savedInstanceState);
        app = (LightMsg)getApplication();
        xs = app.xs;

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.personal_info);

        Account acc = xs.getAccount();
        if (acc == null) {
            return;
        }
        
        pin = (ImageView) findViewById(R.id.portrait);
        if (acc.portrait != null) {
            pin.setImageBitmap(acc.portrait);
        }
        
        View lpin = findViewById(R.id.rl_portrait);
        lpin.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Log.v(TAG, "onClick(), "+v);
                showDialog0(0);
            }

        });

        nick = (TextView) findViewById(R.id.nick);
        nick.setText(acc.nick);
        View lnick = findViewById(R.id.rl_nick);
        lnick.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Log.v(TAG, "onClick(), "+v);
                showDialog0(1);
            }

        });

        account = (TextView) findViewById(R.id.account);
        account.setText(acc.account);
        View laccount = findViewById(R.id.rl_account);
        laccount.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Log.v(TAG, "onClick(), "+v);
                showDialog0(2);
            }

        });

        gender = (TextView) findViewById(R.id.gender);
        gender.setText(acc.gender);
        View lgender = findViewById(R.id.rl_gender);
        lgender.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Log.v(TAG, "onClick(), "+v);
                showDialog0(3);
            }

        });
    }

    public static final int IMAGE_BY_GALLERY = 10;
    public static final int IMAGE_BY_CAPTURE = 11;
    public static final int IMAGE_TO_CROP = 12;
    String CROPPED_IMAGE_LOC = "file:///sdcard/cropped.jpg";
    Uri croppedUri = Uri.parse(CROPPED_IMAGE_LOC);
    String CAPTURE_IMAGE_LOC = "file:///sdcard/captured.jpg";
    Uri imageUri = Uri.parse(CAPTURE_IMAGE_LOC);

    private void captureImage() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        startActivityForResult(i, IMAGE_BY_CAPTURE);
    }

    private void fetchImage() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");

        //i.putExtra("return-data", true);
        i.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        i.putExtra("return-data", false);

        i.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        i.putExtra("noFaceDetection", true);

        startActivityForResult(i, IMAGE_BY_GALLERY);
    }

    private void fetchAndCropImage() {
        fetchAndCropImage(croppedUri, 320, 320, IMAGE_TO_CROP);
    }

    private void fetchAndCropImage(Uri uri, int outputX, int outputY, int reqeustCode) {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);

        //i.setData(uri);
        i.setType("image/*");

        i.putExtra("crop", "true");
        i.putExtra("aspectX", 1);
        i.putExtra("aspectY", 1);
        i.putExtra("outputX", outputX);
        i.putExtra("outputY", outputY);
        i.putExtra("scale", true);

        //i.putExtra("return-data", true);
        i.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        i.putExtra("return-data", false);

        i.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        i.putExtra("noFaceDetection", true);

        startActivityForResult(i, reqeustCode);
    }

    private void cropImage(Uri inUri, Uri outUri, int outputX, int outputY, int reqeustCode) {
        //Intent i = new Intent(MediaStore.);
        //Intent i = new Intent("com.android.camera.action.CROP");
        Intent i = new Intent("com.android.action.CROP");

        i.setDataAndType(inUri, "image/*");
        i.putExtra("crop", "true");
        i.putExtra("aspectX", 1);
        i.putExtra("aspectY", 1);
        i.putExtra("outputX", outputX);
        i.putExtra("outputY", outputY);
        i.putExtra("scale", true);

        //i.putExtra("return-data", true);
        i.putExtra(MediaStore.EXTRA_OUTPUT, outUri);
        i.putExtra("return-data", false);

        i.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        i.putExtra("noFaceDetection", true);

        startActivityForResult(i, reqeustCode);
    }

    //File croppedImageFile = new File(getFilesDir(), "test.jpg");
    //Uri croppedImage = Uri.fromFile(croppedImageFile);
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(), "+requestCode+", "+resultCode+", "+data);
        if (resultCode != RESULT_OK) { // RESULT_CANCELED / ...
            return;
        }

        switch (requestCode) {
        case IMAGE_BY_CAPTURE:
            cropImage(imageUri, croppedUri, 320, 320, IMAGE_TO_CROP);
            // When the user is done picking a picture, let's start the CropImage Activity,
            // setting the output image file and size to 200x200 pixels square.
            //File croppedImageFile = new File(getFilesDir(), "test.jpg");
            //Uri croppedImage = Uri.fromFile(croppedImageFile);

            /*CropImageIntentBuilder cropImage = new CropImageIntentBuilder(320, 320, croppedUri);
            cropImage.setOutlineColor(0xFF03A9F4);
            cropImage.setSourceImage(imageUri);//(data.getData());

            startActivityForResult(cropImage.getIntent(this), IMAGE_TO_CROP);*/
            break;

        case IMAGE_BY_GALLERY:
            cropImage(data.getData(), croppedUri, 320, 320, IMAGE_TO_CROP);
            //cropImage(imageUri, croppedUri, 320, 320, IMAGE_TO_CROP);
            break;

        case IMAGE_TO_CROP:
            Bitmap bitmap = null;
            //if (data != null) { //("return-data", true)
            //	bitmap = data.getParcelableExtra("data");
            //} else { // ("return-data", false)
            bitmap = decodeUriToBitmap(croppedUri);
            //}

            ImageView portrait = (ImageView)findViewById(R.id.portrait);
            if (bitmap != null && portrait != null) {
                portrait.setImageBitmap(bitmap);
            }
            break;
        }
    }

    private Bitmap decodeUriToBitmap(Uri uri) {
        InputStream is = null;
        try {
            is = getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

        Bitmap bitmap = BitmapFactory.decodeStream(is);
        return bitmap;
    }

    private void showDialog0(int item) {
        switch (item) {

        // Portrait
        case 0:
            new AlertDialog.Builder(this)
            .setTitle("Set portrait")
            .setItems(new String[] {"Gallery", "Camera"}, 
                    new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    if (!MediaFile.isSdcardAvailable()) {
                        Toast.makeText(PersonalInfo.this, "Check SD Card, pls!",  Toast.LENGTH_LONG).show();
                        return;
                    }

                    switch (which) {
                    case 0:
                        //From Gallery
                        try {
                            fetchImage();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 1:
                        //From Camera
                        try {
                            captureImage();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                    }
                }
            }).show();
            break;
        
        // Nick
        case 1:
            final EditText et1 = new EditText(this);
            et1.setText(nick.getText());
            new AlertDialog.Builder(this)
            .setTitle("Set portrait")
            .setView(et1)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    // TODO Auto-generated method stub
                    if (et1.getText() != null) {
                        nick.setText(et1.getText());
                    }
                }
                
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    // TODO Auto-generated method stub
                    
                }
                
            })
            .show();
            break;
            
        // Account
        case 2:
            final EditText et2 = new EditText(this);
            et2.setText(account.getText());
            new AlertDialog.Builder(this)
            .setTitle("Set portrait")
            .setView(et2)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    // TODO Auto-generated method stub
                    if (et2.getText() != null) {
                        account.setText(et2.getText());
                    }
                }
                
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    // TODO Auto-generated method stub
                    
                }
                
            })
            .show();
            break;

        // Gender
        case 3:
            new AlertDialog.Builder(this)
            .setTitle("Set gender")
            .setItems(new String[] {"男", "女"}, 
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    TextView tv = (TextView)findViewById(R.id.gender);
                    switch (which) {
                    case 0:
                        tv.setText("男");
                        break;
                    case 1:
                        tv.setText("女");
                        break;
                    default:
                        break;
                    }
                }
            }).show();
            break;

        }
    }
}

