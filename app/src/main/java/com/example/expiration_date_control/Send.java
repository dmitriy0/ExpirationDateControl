package com.example.expiration_date_control;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;


public class Send extends Fragment {
    private int countProducts;
    private int counterFor;
    private int count;
    private int countProductsInCategories;
    private String countProd;

    private boolean isProductAlreadyExists;

    private long notificationDate;
    private long notificationTime;
    private long validUntilDate;
    private long productionDate;

    private String value;
    private String name;
    private String imagePath;
    private String phoneNumber;
    private String category;

    private List<MyProductsForRecyclerView> products;

    private RecyclerView recyclerView;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("Users");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView =  inflater.inflate(R.layout.fragment_send, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        final SharedPreferences preferences = getDefaultSharedPreferences(getContext());
        phoneNumber = preferences.getString("phoneNumber","");

        products  = new ArrayList<>();

        counterFor = 1;
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if (counterFor == 1) {

                    try {
                        count = dataSnapshot.child(phoneNumber).child("allProducts").child("count").getValue(Integer.class);

                        for (int i = 0; i < count; i++) {

                            name = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("name").getValue(String.class);
                            countProd = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("count").getValue(String.class);
                            value = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("value").getValue(String.class);
                            imagePath = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("imagePath").getValue(String.class);
                            notificationDate = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("notificationDate").getValue(Long.class);
                            validUntilDate = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("notificationDate").getValue(Long.class);
                            notificationTime = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("notificationTime").getValue(Long.class);
                            productionDate = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("productionDate").getValue(Long.class);
                            category = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("category").getValue(String.class);

                            products.add(new MyProductsForRecyclerView(name, countProd, value, imagePath, notificationTime, notificationDate,validUntilDate, productionDate, category, i, getContext(), getActivity()));


                        }
                        DataAdapterForSend adapter = new DataAdapterForSend(getContext(), products);
                        recyclerView.setAdapter(adapter);
                        for (int i = 0;i<adapter.getItemCount();i++){
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean(i+"",false);
                            editor.apply();
                        }




                    } catch (Exception e) {

                    }
                    counterFor = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
            }
        });

        Button send = rootView.findViewById(R.id.buttonSend);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editPhone = rootView.findViewById(R.id.editPhoneNumber);
                final String phone = editPhone.getText().toString();

                counterFor = 1;
                //Toast.makeText(getContext(), finalI +"",Toast.LENGTH_LONG).show();
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (counterFor == 1){
                            String currentCategory = "";
                            try{
                                dataSnapshot.child(phone).child("allProducts").child("count").getValue(Integer.class);
                                countProducts = dataSnapshot.child(phone).child("allProducts").child("count").getValue(Integer.class);
                                int countMyProducts = dataSnapshot.child(phoneNumber).child("allProducts").child("count").getValue(Integer.class);
                                for (int i = 0; i < countMyProducts; i++) {

                                    final MyProductsForRecyclerView sendProducts = products.get(i);
                                    final boolean isCheckBoxChecked = preferences.getBoolean(i+"",false);


                                    //Toast.makeText(getContext(),i+""+isCheckBoxChecked,Toast.LENGTH_LONG).show();
                                    if (isCheckBoxChecked){
                                        //Toast.makeText(getContext(),i+"",Toast.LENGTH_LONG).show();
                                        isProductAlreadyExists = false;

                                        for (int j=0;j<countProducts;j++){
                                            if (sendProducts.getName().equals(dataSnapshot.child(phone).child("allProducts").child(j+"").child("name").getValue(String.class))){
                                                isProductAlreadyExists = true;
                                            }
                                        }
                                        //Toast.makeText(getContext(),isCheckBoxChecked+""+ finalI +"",Toast.LENGTH_LONG).show();
                                        if(!isProductAlreadyExists){
                                            myRef.child(phone).child("allProducts").child("count").setValue(countProducts+1);
                                            myRef.child(phone).child("allProducts").child(String.valueOf(countProducts)).child("name").setValue(sendProducts.getName());
                                            //myRef.child(phoneNumber).child("allProducts").child(String.valueOf(count)).child("barCode").setValue(code);
                                            myRef.child(phone).child("allProducts").child(String.valueOf(countProducts)).child("productionDate").setValue(sendProducts.getProductionDate());
                                            myRef.child(phone).child("allProducts").child(String.valueOf(countProducts)).child("validUntilDate").setValue(sendProducts.getValidUntilDate());
                                            myRef.child(phone).child("allProducts").child(String.valueOf(countProducts)).child("notificationTime").setValue(sendProducts.getNotificationTime());
                                            myRef.child(phone).child("allProducts").child(String.valueOf(countProducts)).child("notificationDate").setValue(sendProducts.getNotificationDate());
                                            myRef.child(phone).child("allProducts").child(String.valueOf(countProducts)).child("category").setValue(sendProducts.getCategory());
                                            myRef.child(phone).child("allProducts").child(String.valueOf(countProducts)).child("count").setValue(sendProducts.getCountProd());
                                            myRef.child(phone).child("allProducts").child(String.valueOf(countProducts)).child("value").setValue(sendProducts.getValue());
                                            myRef.child(phone).child("allProducts").child(String.valueOf(countProducts)).child("imagePath").setValue(sendProducts.getImagePath());

                                            countProducts++;
                                        }

                                    }

                                }
                                Toast.makeText(getContext(),"Записи о продуктах успешно отправлены",Toast.LENGTH_LONG).show();
                                counterFor = 0;
                            } catch (Exception e) {
                                Toast.makeText(getContext(),"Пользователя с данным номером телефона не существует",Toast.LENGTH_LONG).show();
                            }



                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });


            }
        });

        return rootView;
    }

}
