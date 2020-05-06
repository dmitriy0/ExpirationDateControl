package com.example.expiration_date_control;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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

import org.w3c.dom.Text;

import java.io.File;
import java.text.BreakIterator;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

    Button addNewProduct;
    Button cancel;

    EditText editTextShelfLife;
    EditText editName;
    EditText editCode;
    EditText editCount;

    String phoneNumber;

    int permissionCamera;
    int permissionStorage;

    int counterFor;

    private static final int CAMERA_REQUEST = 0;
    private Uri file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

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
        editCode = findViewById(R.id.barCode);
        editCount = findViewById(R.id.count);

        setValidUntil = (ImageView) findViewById(R.id.setValidUntil);
        setProdDate = (ImageView) findViewById(R.id.setProdDate);
        setPhoto = (ImageView) findViewById(R.id.setProductPhoto);

        addNewProduct = (Button) findViewById(R.id.addNewProduct);
        cancel = (Button) findViewById(R.id.cancel);

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

        setPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                permissionCamera = checkSelfPermission(Manifest.permission.CAMERA);
                permissionStorage = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permissionCamera == PackageManager.PERMISSION_GRANTED || permissionStorage == PackageManager.PERMISSION_GRANTED) {

                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    file = FileProvider.getUriForFile(AddProduct.this, BuildConfig.APPLICATION_ID + ".provider",getOutputMediaFile());
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,  file);
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
                final String code = editCode.getText().toString();
                final String category = spinnerCategories.getSelectedItem().toString();
                final String value = spinnerValues.getSelectedItem().toString();
                final String countProd = editCount.getText().toString();

                final String imagePath = "gs://expiration-date-control-af24d.appspot.com/"+phoneNumber+code; // путь до обложки

                if(name.equals("") || code.equals("") || category.equals("") || value.equals("") || countProd.equals("0") || productionDate==0 || validUntilDate == 0 || selectedImage == null || editTextShelfLife.getText().toString().equals("")){
                    Toast.makeText(getBaseContext(),"Не все поля заполнены, пожалуйста, повторите попытку",Toast.LENGTH_LONG).show();
                }
                else{
                    if (selectedImage != null) {

                        uploadFile(imagePath, selectedImage);
                    }
                    counterFor = 1;
                    myRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (counterFor == 1){
                                // This method is called once with the initial value and again
                                // whenever data at this location is updated.
                                int count = dataSnapshot.child(phoneNumber).child("allProducts").child("count").getValue(Integer.class);
                                myRef.child(phoneNumber).child("allProducts").child("count").setValue(count+1);
                                myRef.child(phoneNumber).child("allProducts").child(String.valueOf(count)).child("name").setValue(name);
                                myRef.child(phoneNumber).child("allProducts").child(String.valueOf(count)).child("barCode").setValue(code);
                                myRef.child(phoneNumber).child("allProducts").child(String.valueOf(count)).child("productionDate").setValue(productionDate);
                                myRef.child(phoneNumber).child("allProducts").child(String.valueOf(count)).child("validUntilDate").setValue(validUntilDate);
                                myRef.child(phoneNumber).child("allProducts").child(String.valueOf(count)).child("category").setValue(category);
                                myRef.child(phoneNumber).child("allProducts").child(String.valueOf(count)).child("count").setValue(countProd);
                                myRef.child(phoneNumber).child("allProducts").child(String.valueOf(count)).child("value").setValue(value);
                                myRef.child(phoneNumber).child("allProducts").child(String.valueOf(count)).child("photoPath").setValue(imagePath);

                                int countCategory = dataSnapshot.child(phoneNumber).child("categories").child(category).child("count").getValue(Integer.class);
                                myRef.child(phoneNumber).child("categories").child(category).child("count").setValue(countCategory+1);
                                myRef.child(phoneNumber).child("categories").child(category).child(String.valueOf(countCategory)).child("name").setValue(name);
                                myRef.child(phoneNumber).child("categories").child(category).child(String.valueOf(countCategory)).child("barCode").setValue(code);
                                myRef.child(phoneNumber).child("categories").child(category).child(String.valueOf(countCategory)).child("productionDate").setValue(productionDate);
                                myRef.child(phoneNumber).child("categories").child(category).child(String.valueOf(countCategory)).child("validUntilDate").setValue(validUntilDate);
                                myRef.child(phoneNumber).child("categories").child(category).child(String.valueOf(countCategory)).child("category").setValue(category);
                                myRef.child(phoneNumber).child("categories").child(category).child(String.valueOf(countCategory)).child("count").setValue(countProd);
                                myRef.child(phoneNumber).child("categories").child(category).child(String.valueOf(countCategory)).child("value").setValue(value);
                                myRef.child(phoneNumber).child("categories").child(category).child(String.valueOf(countCategory)).child("photoPath").setValue(imagePath);
                                counterFor = 0;
                            }


                        }

                        @Override
                        public void onCancelled(DatabaseError error) {

                        }
                    });
                }



            }
        });



    }

    @Override
    protected void onActivityResult( int requestCode,  int resultCode,  Intent data) {
        if (requestCode ==  0) {
            if (resultCode == RESULT_OK) {
                selectedImage = file;
                Toast.makeText(getBaseContext(),file+"",Toast.LENGTH_LONG).show();
                setPhoto.setImageURI(file);
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

            textNotificationDate.setText(DateUtils.formatDateTime(AddProduct.this,
                    validUntilDate - daysBeforeNotification * 24 * 3600 * 1000,
                    DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));

        }


    }
    private void uploadFile(String path, Uri pathOfFile) {
        //if there is a file to upload
        StorageReference riversRef = mStorageRef.child(path);

        final ProgressDialog progressDialog = new ProgressDialog(AddProduct.this);
        progressDialog.setTitle("Uploading");
        progressDialog.show();
        riversRef.putFile(pathOfFile)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();

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
