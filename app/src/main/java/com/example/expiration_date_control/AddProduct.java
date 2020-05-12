package com.example.expiration_date_control;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.BreakIterator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;


import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class AddProduct extends AppCompatActivity {

    long validUntilDate;
    long notificationTime;
    long productionDate;
    long notificationDate;
    Calendar date = Calendar.getInstance();

    private StorageReference mStorageRef;

    SharedPreferences preferences;

    private DatabaseReference myRef;

    private Uri selectedImage;
    private Uri selectedBarCodeImage;

    private TextView prodDate;
    private TextView validUntil;
    TextView textNotificationDate;
    TextView textNotificationTime;

    RadioGroup radioValidUntil_ProdDate;

    Spinner spinnerDaysBeforeNotification;
    Spinner spinnerShelfLife;
    Spinner spinnerCategories;
    Spinner spinnerValues;

    LinearLayout layoutValidUntil;
    LinearLayout layoutShelfLife;

    ImageView setValidUntil;
    ImageView setProdDate;
    ImageView setPhoto;
    ImageView setBarCodePhoto;

    Button addNewProduct;
    Button cancel;

    EditText editTextShelfLife;
    EditText editName;
    EditText editCode;
    EditText editCount;

    String phoneNumber;
    String imagePath;
    String codeImagePath;

    int permissionCamera;
    int permissionStorage;

    String productOrBarCode;



    int counterFor;

    private static final int CAMERA_REQUEST = 0;
    private Uri file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddProduct.this,MainActivity.class);
                startActivity(intent);
            }
        });

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users");

        preferences = getDefaultSharedPreferences(getBaseContext());
        phoneNumber = preferences.getString("phoneNumber","");

        prodDate = findViewById(R.id.prodDate);
        validUntil = findViewById(R.id.validUntil);

        radioValidUntil_ProdDate = (RadioGroup) findViewById(R.id.radioValidUntil_ProdDate);

        spinnerDaysBeforeNotification = findViewById(R.id.daysBeforeNotification);
        spinnerShelfLife = findViewById(R.id.spinnerShelfLife);
        spinnerCategories = findViewById(R.id.spinnerCategories);
        spinnerValues = findViewById(R.id.spinnerValues);

        layoutValidUntil = findViewById(R.id.layoutValidUntil);
        layoutShelfLife = findViewById(R.id.layoutShelfLife);

        textNotificationDate = (TextView) findViewById(R.id.notificationDate);
        textNotificationTime = (TextView) findViewById(R.id.setNotificationTime);

        editTextShelfLife = findViewById(R.id.shelfLife);
        editName = findViewById(R.id.name);
        editCount = findViewById(R.id.count);

        setValidUntil = (ImageView) findViewById(R.id.setValidUntil);
        setProdDate = (ImageView) findViewById(R.id.setProdDate);
        setPhoto = (ImageView) findViewById(R.id.setProductPhoto);
        setBarCodePhoto = (ImageView) findViewById(R.id.setBarCodePhoto);

        addNewProduct = (Button) findViewById(R.id.addNewProduct);
        cancel = (Button) findViewById(R.id.cancel);

        final Intent intent = getIntent();


        editName.setText(intent.getStringExtra("name"));

        if(intent.getLongExtra("productionDate",-1) != -1){
            productionDate = intent.getLongExtra("productionDate",-1);
            prodDate.setText(DateUtils.formatDateTime(AddProduct.this,
                    intent.getLongExtra("productionDate",-1),
                    DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
        }

        if(intent.getLongExtra("validUntilDate",-1) != -1){
            validUntilDate = intent.getLongExtra("validUntilDate",-1);
            validUntil.setText(DateUtils.formatDateTime(AddProduct.this,
                    intent.getLongExtra("validUntilDate",-1),
                    DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
        }

        spinnerCategories.setSelection(new ArrayList(Arrays.asList(getResources().getStringArray(R.array.categories))).indexOf(intent.getStringExtra("category")));
        editCount.setText(intent.getStringExtra("countProd"));
        spinnerValues.setSelection(new ArrayList(Arrays.asList(getResources().getStringArray(R.array.values))).indexOf(intent.getStringExtra("value")));

        if (intent.getLongExtra("notificationDate",-1) != -1) {
            notificationDate = intent.getLongExtra("notificationDate", -1);
            textNotificationDate.setText(DateUtils.formatDateTime(AddProduct.this,
                    intent.getLongExtra("notificationDate", -1),
                    DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
        }

        if (intent.getLongExtra("notificationTime",-1) != -1){
            notificationTime = intent.getLongExtra("notificationTime",-1);
            textNotificationTime.setText(DateUtils.formatDateTime(AddProduct.this,
                    intent.getLongExtra("notificationTime",-1),
                    DateUtils.FORMAT_SHOW_TIME));
        }

        mStorageRef = FirebaseStorage.getInstance().getReference();
        if(!intent.getStringExtra("imagePath").equals("")){
            StorageReference riversRef = mStorageRef.child(intent.getStringExtra("imagePath"));
            //отрисовка картинки
            riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(setPhoto);
                }
            });
        }
        if(!intent.getStringExtra("barcodeImagePath").equals("")){
            StorageReference riversRef = mStorageRef.child(intent.getStringExtra("barcodeImagePath"));
            //отрисовка картинки
            riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(setBarCodePhoto);
                }
            });
        }




        mStorageRef = FirebaseStorage.getInstance().getReference();

        permissionCamera = checkSelfPermission(Manifest.permission.CAMERA);
        permissionStorage = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCamera != PackageManager.PERMISSION_GRANTED || permissionStorage != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        }

        radioValidUntil_ProdDate.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == R.id.radioValidUntil) {
                    layoutValidUntil.setVisibility(View.VISIBLE);
                    layoutShelfLife.setVisibility(View.INVISIBLE);
                    setNotificationDate();
                } else {
                    layoutShelfLife.setVisibility(View.VISIBLE);
                    layoutValidUntil.setVisibility(View.INVISIBLE);
                    setNotificationDate();
                }

            }

        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddProduct.this,MainActivity.class);
                startActivity(intent);
            }
        });
        setProdDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new DatePickerDialog(AddProduct.this, datePicker,
                        date.get(Calendar.YEAR),
                        date.get(Calendar.MONTH),
                        date.get(Calendar.DAY_OF_MONTH))
                        .show();


            }

            DatePickerDialog.OnDateSetListener datePicker = new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    date.set(Calendar.YEAR, year);
                    date.set(Calendar.MONTH, monthOfYear);
                    date.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    setInitialDateTime();
                }
            };

            private void setInitialDateTime() {

                prodDate.setText(DateUtils.formatDateTime(AddProduct.this,
                        date.getTimeInMillis(),
                        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
                productionDate = date.getTimeInMillis();

                setNotificationDate();
            }

        });


        setValidUntil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new DatePickerDialog(AddProduct.this, datePicker,
                        date.get(Calendar.YEAR),
                        date.get(Calendar.MONTH),
                        date.get(Calendar.DAY_OF_MONTH))
                        .show();


            }

            DatePickerDialog.OnDateSetListener datePicker = new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    date.set(Calendar.YEAR, year);
                    date.set(Calendar.MONTH, monthOfYear);
                    date.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    validUntilDate = date.getTimeInMillis();

                    setInitialDateTime();
                }
            };


            private void setInitialDateTime() {

                setNotificationDate();

                validUntil.setText(DateUtils.formatDateTime(AddProduct.this,
                        date.getTimeInMillis(),
                        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
            }

        });


        textNotificationTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new TimePickerDialog(AddProduct.this, timePicker,
                        date.get(Calendar.HOUR_OF_DAY),
                        date.get(Calendar.MINUTE), true)
                        .show();


            }

            TimePickerDialog.OnTimeSetListener timePicker = new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    date.set(Calendar.MINUTE, minute);

                    notificationTime = date.getTimeInMillis();

                    setInitialDateTime();
                }
            };


            private void setInitialDateTime() {

                textNotificationTime.setText(DateUtils.formatDateTime(AddProduct.this,
                        date.getTimeInMillis(),
                        DateUtils.FORMAT_SHOW_TIME));
            }

        });


        spinnerDaysBeforeNotification.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,
                                       View itemSelected, int selectedItemPosition, long selectedId) {

                setNotificationDate();

            }
            public void onNothingSelected(AdapterView<?> parent) { }
        });



        editTextShelfLife.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {

                setNotificationDate();

            }
        });
        spinnerShelfLife.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,
                                       View itemSelected, int selectedItemPosition, long selectedId) {

                setNotificationDate();

            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        setBarCodePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                permissionCamera = checkSelfPermission(Manifest.permission.CAMERA);
                permissionStorage = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permissionCamera == PackageManager.PERMISSION_GRANTED && permissionStorage == PackageManager.PERMISSION_GRANTED) {

                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    file = FileProvider.getUriForFile(AddProduct.this, BuildConfig.APPLICATION_ID + ".provider",getOutputMediaFile());
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,  file);
                    productOrBarCode = "barCode";
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);

                }
                else{
                    ActivityCompat.requestPermissions(AddProduct.this, new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }

            }

        });

        setPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                permissionCamera = checkSelfPermission(Manifest.permission.CAMERA);
                permissionStorage = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permissionCamera == PackageManager.PERMISSION_GRANTED && permissionStorage == PackageManager.PERMISSION_GRANTED) {

                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    file = FileProvider.getUriForFile(AddProduct.this, BuildConfig.APPLICATION_ID + ".provider",getOutputMediaFile());
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,  file);
                    productOrBarCode = "product";
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);

                }
                else{
                    ActivityCompat.requestPermissions(AddProduct.this, new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }

            }

        });

        addNewProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                final String name = editName.getText().toString();
                final String category = spinnerCategories.getSelectedItem().toString();
                final String value = spinnerValues.getSelectedItem().toString();
                final String countProd = editCount.getText().toString();

                imagePath = "gs://expiration-date-control-af24d.appspot.com/"+phoneNumber+name; // путь до обложки
                codeImagePath = "gs://expiration-date-control-af24d.appspot.com/"+phoneNumber+name+"barcode"; // путь до обложки

                if(name.equals("") || category.equals("") || value.equals("") || countProd.equals("0") || productionDate==0 || validUntilDate == 0 || editTextShelfLife.getText().toString().equals("") || notificationTime == 0 ){
                    Toast.makeText(getBaseContext(),"Не все поля заполнены, пожалуйста, повторите попытку",Toast.LENGTH_LONG).show();
                }
                else{

                    counterFor = 1;
                    myRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (counterFor == 1){
                                // This method is called once with the initial value and again
                                // whenever data at this location is updated.
                                int count = dataSnapshot.child(phoneNumber).child("allProducts").child("count").getValue(Integer.class);
                                if (intent.getIntExtra("number",-1) != -1){
                                    count = intent.getIntExtra("number",-1);
                                }else{
                                    myRef.child(phoneNumber).child("allProducts").child("count").setValue(count+1);
                                }

                                if (!dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(count)).child("name").getValue(String.class).equals(name)){
                                    imagePath = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(count)).child("imagePath").getValue(String.class);
                                    codeImagePath = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(count)).child("imagePath").getValue(String.class)+"barcode";
                                }


                                if (selectedImage != null) {

                                    try {
                                        uploadFile(imagePath, selectedImage);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (selectedBarCodeImage != null) {

                                    try {
                                        uploadFile(codeImagePath, selectedBarCodeImage);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                myRef.child(phoneNumber).child("allProducts").child(String.valueOf(count)).child("name").setValue(name);
                                //myRef.child(phoneNumber).child("allProducts").child(String.valueOf(count)).child("barCode").setValue(code);
                                myRef.child(phoneNumber).child("allProducts").child(String.valueOf(count)).child("productionDate").setValue(productionDate);
                                myRef.child(phoneNumber).child("allProducts").child(String.valueOf(count)).child("validUntilDate").setValue(validUntilDate);
                                myRef.child(phoneNumber).child("allProducts").child(String.valueOf(count)).child("notificationTime").setValue(notificationTime);
                                myRef.child(phoneNumber).child("allProducts").child(String.valueOf(count)).child("notificationDate").setValue(notificationDate);
                                myRef.child(phoneNumber).child("allProducts").child(String.valueOf(count)).child("category").setValue(category);
                                myRef.child(phoneNumber).child("allProducts").child(String.valueOf(count)).child("count").setValue(countProd);
                                myRef.child(phoneNumber).child("allProducts").child(String.valueOf(count)).child("value").setValue(value);
                                myRef.child(phoneNumber).child("allProducts").child(String.valueOf(count)).child("imagePath").setValue(imagePath);


                                counterFor = 0;

                            }


                        }

                        @Override
                        public void onCancelled(DatabaseError error) {

                        }
                    });
                    Intent intent = new Intent(AddProduct.this,MainActivity.class);
                    startActivity(intent);

                }



            }
        });



    }

    @Override
    protected void onActivityResult( int requestCode,  int resultCode,  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                if (productOrBarCode.equals("product")){
                    selectedImage = file;
                    setPhoto.setImageURI(file);
                }
                if (productOrBarCode.equals("barCode")){
                    selectedBarCodeImage = file;
                    setBarCodePhoto.setImageURI(file);
                }
            }
        }
    }

    public void setNotificationDate(){

        int daysBeforeNotification = Integer.parseInt(spinnerDaysBeforeNotification.getSelectedItem().toString());

        if (radioValidUntil_ProdDate.getCheckedRadioButtonId() == R.id.radioProdDate){

            String[] arrayShelfLife = getResources().getStringArray(R.array.shelfLife);
            String[] valueDays = getResources().getStringArray(R.array.days);

            if (!editTextShelfLife.getText().toString().equals("")){

                long sLife = Long.parseLong(editTextShelfLife.getText().toString());


                String valueShelfLife = spinnerShelfLife.getSelectedItem().toString();

                if (valueShelfLife.equals("часов")){

                    notificationDate = productionDate+(sLife*3600*1000 - daysBeforeNotification*24*3600*1000);
                    textNotificationDate.setText(DateUtils.formatDateTime(AddProduct.this,
                            productionDate+(sLife*3600*1000 - daysBeforeNotification*24*3600*1000),
                            DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
                    validUntilDate = productionDate+sLife*3600*1000;
                    validUntil.setText(DateUtils.formatDateTime(AddProduct.this,
                            productionDate+sLife*3600*1000,
                            DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));

                }
                if (valueShelfLife.equals("суток")){
                    notificationDate = productionDate+(sLife*24*3600*1000 - daysBeforeNotification*24*3600*1000);
                    textNotificationDate.setText(DateUtils.formatDateTime(AddProduct.this,
                            productionDate+sLife*24*3600*1000 - daysBeforeNotification*24*3600*1000,
                            DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
                    validUntilDate = productionDate+sLife*24*3600*1000;
                    validUntil.setText(DateUtils.formatDateTime(AddProduct.this,
                            productionDate+sLife*24*3600*1000,
                            DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
                }
                if (valueShelfLife.equals("месяцев")){
                    notificationDate = productionDate+(sLife*365*2*3600*1000 - daysBeforeNotification*24*3600*1000);
                    textNotificationDate.setText(DateUtils.formatDateTime(AddProduct.this,
                            productionDate+sLife*365*2*3600*1000 - daysBeforeNotification*24*3600*1000,
                            DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
                    validUntilDate = productionDate+sLife*365*2*3600*1000;
                    validUntil.setText(DateUtils.formatDateTime(AddProduct.this,
                            productionDate+sLife*365*2*3600*1000,
                            DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
                }
                if (valueShelfLife.equals("лет")){
                    notificationDate = productionDate+(sLife*365*24*3600*1000 - daysBeforeNotification*24*3600*1000);
                    textNotificationDate.setText(DateUtils.formatDateTime(AddProduct.this,
                            productionDate+sLife*365*24*3600*1000 - daysBeforeNotification*24*3600*1000,
                            DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
                    validUntilDate = productionDate+sLife*365*24*3600*1000;
                    validUntil.setText(DateUtils.formatDateTime(AddProduct.this,
                            productionDate+sLife*365*24*3600*1000,
                            DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));

                }
            }
        }else{
            notificationDate = validUntilDate - daysBeforeNotification * 24 * 3600 * 1000;
            textNotificationDate.setText(DateUtils.formatDateTime(AddProduct.this,
                    validUntilDate - daysBeforeNotification * 24 * 3600 * 1000,
                    DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));

        }


    }
    private void uploadFile(String path, Uri pathOfFile) throws IOException {
        //if there is a file to upload

        StorageReference riversRef = mStorageRef.child(path);

        final ProgressDialog progressDialog = new ProgressDialog(AddProduct.this);
        progressDialog.setTitle("Uploading");
        progressDialog.show();
        progressDialog.setCancelable(false);

        Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), pathOfFile);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos);
        byte[] data = baos.toByteArray();
        //uploading the image
        riversRef.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        Intent intent = new Intent(AddProduct.this,MainActivity.class);
                        startActivity(intent);
                        Toast.makeText(AddProduct.this, "File Uploaded ", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        progressDialog.dismiss();

                        //and displaying error message

                        //* заменить на активити
                        //Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_LONG).show();

                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        //calculating progress percentage
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                        //displaying percentage in progress dialog
                        progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                    }
                });
    }
    private static File getOutputMediaFile( ){
        File mediaStorageDir =  new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES),  "CameraDemo");

        if ( !mediaStorageDir.exists( ) ){
            if ( !mediaStorageDir.mkdirs( ) ){
                return null;
            }
        }

        String timeStamp =  new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date( ) );
        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+  timeStamp + ".jpg");
    }
}
