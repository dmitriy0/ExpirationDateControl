package com.example.expiration_date_control;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static androidx.core.content.PermissionChecker.checkSelfPermission;

public class ImportPDF extends Fragment {

    private static StorageReference mStorageRef;
    private static Image imageFromWeb;
    LinearLayout pdf_layout;

    boolean canClose;

    private int counterFor;
    private int count;
    private String countProd;

    private long notificationDate;
    private long notificationTime;
    private long validUntilDate;
    private long productionDate;

    private String value;
    private String name;
    private String imagePath;
    private String phoneNumber;
    String category;

    private List<MyProductsForRecyclerView> products;
    private RecyclerView recyclerView;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("Users");

    private static String FILE;
    public static final String FONT = "/res/font/rubik.ttf";

    DataAdapterForSend adapter;

    Font font;
    SharedPreferences preferences;

    String filename;

    Bitmap bitmap;

    int permissionStorage;

    ArrayList<Target> targets;
    ArrayList<String> urls;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_import_pdf, container, false);
        pdf_layout = rootView.findViewById(R.id.pdf_layout);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        permissionStorage = checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionStorage != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        }


        final EditText editFileName = ((EditText) rootView.findViewById(R.id.editFileName));

        preferences = getDefaultSharedPreferences(getContext());
        phoneNumber = preferences.getString("phoneNumber","");

        BaseFont bf= null;
        try {
            bf = BaseFont.createFont(FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
        font =new Font(bf,16,Font.NORMAL);

        products  = new ArrayList<>();
        counterFor = 1;
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                try {
                    count = dataSnapshot.child(phoneNumber).child("allProducts").child("count").getValue(Integer.class);

                    for (int i = 0; i < count; i++) {

                        name = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("name").getValue(String.class);
                        countProd = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("count").getValue(String.class);
                        value = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("value").getValue(String.class);
                        imagePath = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("imagePath").getValue(String.class);
                        notificationDate = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("notificationDate").getValue(Long.class);
                        notificationTime = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("notificationTime").getValue(Long.class);

                        validUntilDate = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("validUntilDate").getValue(Long.class);
                        productionDate = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("productionDate").getValue(Long.class);
                        category = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("category").getValue(String.class);
                        products.add(new MyProductsForRecyclerView(name,countProd,value,imagePath+"barcode",notificationTime,notificationDate, validUntilDate, productionDate, category,i,getContext(),getActivity()));                                DataAdapter adapter = new DataAdapter(getContext(), products);
                        recyclerView.setAdapter(adapter);


                    }

                    adapter = new DataAdapterForSend(getContext(), products);
                    LinearLayoutManager layoutManager=new LinearLayoutManager(getContext());
                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.setNestedScrollingEnabled(false);
                    recyclerView.setHasFixedSize(false);
                    recyclerView.setAdapter(adapter);

                    for (int i = 0;i<adapter.getItemCount();i++){
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean(i+"",false);
                        editor.apply();
                    }




                } catch (Exception e) {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
            }
        });

        Button createPdf = rootView.findViewById(R.id.create);
        createPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                permissionStorage = checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permissionStorage != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

                }
                else{
                    filename = editFileName.getText().toString();
                    if (!filename.equals("")){

                        File dir = new File(Environment.getExternalStorageDirectory(),"folderTest");
                        if(! dir.exists()){
                            dir.mkdir();
                        }
                        FILE = dir+File.separator+filename+".pdf";
                        generatePdf();
                    }else{
                        Toast.makeText(getContext(),"не все поля заполнены",Toast.LENGTH_LONG).show();
                    }
                }


            }
        });

        return rootView;
    }
    private void generatePdf() {

        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Создание");
        progressDialog.show();
        progressDialog.setCancelable(false);
        try {
            final Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(FILE));
            document.open();

            final int size = products.size();

            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    try {
                        final PdfPTable table = new PdfPTable(4);

                        PdfPCell c1 = new PdfPCell(new Phrase("Штрихкод", font));
                        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                        table.addCell(c1);

                        c1 = new PdfPCell(new Phrase("Название", font));
                        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                        table.addCell(c1);

                        c1 = new PdfPCell(new Phrase("Годен до", font));
                        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                        table.addCell(c1);

                        c1 = new PdfPCell(new Phrase("Количество", font));
                        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                        table.addCell(c1);

                        final ArrayList<Integer> numbers = new ArrayList<Integer>();
                        for (int i=0;i<size;i++){
                            final boolean isCheckBoxChecked = preferences.getBoolean(i+"",false);
                            if(isCheckBoxChecked){
                                numbers.add(i);
                            }
                        }
                        for (int i=0;i<size;i++) {

                            final boolean isCheckBoxChecked = preferences.getBoolean(i + "", false);
                            if (isCheckBoxChecked) {
                                String codeImageUri = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("imagePath").getValue(String.class) + "barcode";
                                final String name = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("name").getValue(String.class);
                                final String countProd = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("count").getValue(String.class);
                                final String value = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("value").getValue(String.class);

                                final Long validUntilDate = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("validUntilDate").getValue(Long.class);

                                String encoded = preferences.getString("image"+i,"");
                                if (!encoded.equals("")){
                                    byte[] imageAsBytes = Base64.decode(encoded, Base64.DEFAULT);
                                    bitmap = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
                                }

                                final ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                if (bitmap != null){
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 10, stream);
                                }
                                try {
                                    if (bitmap != null){
                                        imageFromWeb = Image.getInstance(stream.toByteArray());

                                        PdfPCell cell = new PdfPCell();
                                        cell.addElement(imageFromWeb);
                                        table.addCell(cell);
                                    }else{
                                        PdfPCell cell = new PdfPCell(new Phrase("что-то не так"));
                                        table.addCell(cell);
                                    }



                                    PdfPCell cell1 = new PdfPCell(new Phrase(name, font));
                                    table.addCell(cell1);
                                    if (validUntilDate != null) {
                                        PdfPCell cell2 = new PdfPCell(new Phrase(DateUtils.formatDateTime(getContext(),
                                                validUntilDate,
                                                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR), font));
                                        table.addCell(cell2);
                                    }



                                    PdfPCell cell3 = new PdfPCell(new Phrase(countProd + " " + value, font));
                                    table.addCell(cell3);


                                    if (i == numbers.get(numbers.size() - 1)) {
                                        document.add(table);
                                        document.close();
                                        Toast.makeText(getContext(), "Файл успешно создан, путь - " + FILE, Toast.LENGTH_LONG).show();
                                        progressDialog.dismiss();
                                    }


                                } catch (DocumentException e) {
                                    Toast.makeText(getContext(), e + "", Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }

                            }
                        }


                        progressDialog.dismiss();


                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Что-то пошло не так , попробуйте еще раз" + FILE, Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Failed to read value
                    Toast.makeText(getContext(), "Что-то пошло не так , попробуйте еще раз" + FILE, Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(),e+"",Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        }
    }


}
