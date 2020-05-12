package com.example.expiration_date_control;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

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
                        products.add(new MyProductsForRecyclerView(name,countProd,value,imagePath,notificationTime,notificationDate, validUntilDate, productionDate, category,i,getContext(),getActivity()));                                DataAdapter adapter = new DataAdapter(getContext(), products);
                        recyclerView.setAdapter(adapter);


                    }

                    adapter = new DataAdapterForSend(getContext(), products);
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
                filename = editFileName.getText().toString();
                FILE = Environment.getExternalStorageDirectory()+File.separator+filename+".pdf";
                generatePdf();
            }
        });

        return rootView;
    }
    private void generatePdf() {


        try {
            final Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(FILE));
            document.open();

            final int size = adapter.getItemCount();
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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

                        for(int i = 0;i<size;i++){
                            final boolean isCheckBoxChecked = preferences.getBoolean(i + "", false);
                            if (isCheckBoxChecked) {
                                numbers.add(i);
                            }
                        }

                        for (int i = 0; i < size; i++) {
                            final boolean isCheckBoxChecked = preferences.getBoolean(i + "", false);
                            if (isCheckBoxChecked) {

                                String codeImageUri = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("imagePath").getValue(String.class)+"barcode";
                                final String name = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("name").getValue(String.class);
                                final String countProd = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("count").getValue(String.class);
                                final String value = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("value").getValue(String.class);
                                final Long validUntilDate = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("validUntilDate").getValue(Long.class);

                                assert codeImageUri != null;
                                StorageReference riversRef = mStorageRef.child(codeImageUri);
                                final int finalI = i;
                                riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final ByteArrayOutputStream stream = new ByteArrayOutputStream();

                                        Picasso.get().load(uri).into(new Target() {
                                            @Override
                                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                                if (bitmap == null) {
                                                } else {
                                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                                    try {
                                                        imageFromWeb = Image.getInstance(stream.toByteArray());

                                                        PdfPCell cell = new PdfPCell();
                                                        cell.addElement(imageFromWeb);
                                                        table.addCell(cell);


                                                        PdfPCell cell1 = new PdfPCell(new Phrase(name, font));
                                                        table.addCell(cell1);

                                                        PdfPCell cell2 = new PdfPCell(new Phrase(DateUtils.formatDateTime(getContext(),
                                                                validUntilDate,
                                                                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR), font));
                                                        table.addCell(cell2);



                                                        PdfPCell cell3 = new PdfPCell(new Phrase(countProd + " " + value, font));
                                                        table.addCell(cell3);

                                                        if(finalI == numbers.get(numbers.size()-1)){
                                                            document.add(table);
                                                            document.close();
                                                            Toast.makeText(getContext(), "Файл успешно создан, путь - "+FILE, Toast.LENGTH_LONG).show();
                                                        }


                                                    } catch (IOException | DocumentException e) {
                                                        Toast.makeText(getContext(), e + "", Toast.LENGTH_LONG).show();
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                                            }

                                            @Override
                                            public void onPrepareLoad(Drawable placeHolderDrawable) {

                                            }
                                        });


                                    }
                                });

                            }

                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Failed to read value
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
