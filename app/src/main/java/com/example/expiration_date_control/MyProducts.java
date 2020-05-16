package com.example.expiration_date_control;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;


public class MyProducts extends Fragment {

    private List<MyProductsForRecyclerView> products;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("Users");

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

    ImageView imageNonFood;
    ImageView imageCars;
    ImageView imageGarden;
    ImageView imageBabyFood;
    ImageView imageDrinks;
    ImageView imageCosmetics;
    ImageView imageFish;
    ImageView imageMeat;
    ImageView imageNuts;
    ImageView imageHotDrinks;
    ImageView imageConfectionery;
    ImageView imageConservation;
    ImageView imageGrocery;
    ImageView imageCooking;
    ImageView imageDairy;
    ImageView imageFrozen;


    private LinearLayout layoutNonFood;
    private LinearLayout layoutCars;
    private LinearLayout layoutGarden;
    private LinearLayout layoutBabyFood;
    private LinearLayout layoutDrinks;
    private LinearLayout layoutCosmetics;
    private LinearLayout layoutFish;
    private LinearLayout layoutMeat;
    private LinearLayout layoutNuts;
    private LinearLayout layoutHotDrinks;
    private LinearLayout layoutConfectionery;
    private LinearLayout layoutConservation;
    private LinearLayout layoutGrocery;
    private LinearLayout layoutCooking;
    private LinearLayout layoutDairy;
    private LinearLayout layoutFrozen;
    private LinearLayout layoutAllProducts;

    private String productCategory;

    private RecyclerView recyclerView;

    private StorageReference mStorageRef;

    boolean first;

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_my_products, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        SharedPreferences preferences = getDefaultSharedPreferences(getContext());
        phoneNumber = preferences.getString("phoneNumber","");

        products  = new ArrayList<>();
        first = true;


        productCategory = "allProducts";

        displayProducts();

        final TextView textViewCategories = rootView.findViewById(R.id.textViewCategories);

        imageNonFood = rootView.findViewById(R.id.imageViewNonFood);
        imageCars = rootView.findViewById(R.id.imageViewCars);
        imageGarden = rootView.findViewById(R.id.imageViewGarden);
        imageBabyFood = rootView.findViewById(R.id.imageViewBabyFood);
        imageDrinks = rootView.findViewById(R.id.imageViewDrinks);
        imageCosmetics = rootView.findViewById(R.id.imageViewCosmetics);
        imageFish = rootView.findViewById(R.id.imageViewFish);
        imageMeat = rootView.findViewById(R.id.imageViewMeat);
        imageNuts = rootView.findViewById(R.id.imageViewNuts);
        imageHotDrinks = rootView.findViewById(R.id.imageViewHotDrinks);
        imageConfectionery = rootView.findViewById(R.id.imageViewConfectionery);
        imageConservation = rootView.findViewById(R.id.imageViewConservation);
        imageGrocery = rootView.findViewById(R.id.imageViewGrocery);
        imageCooking = rootView.findViewById(R.id.imageViewCooking);
        imageDairy = rootView.findViewById(R.id.imageViewDairy);
        imageFrozen = rootView.findViewById(R.id.imageViewFrozen);

        layoutNonFood = rootView.findViewById(R.id.layoutNonFood);
        layoutCars = rootView.findViewById(R.id.layoutCars);
        layoutGarden = rootView.findViewById(R.id.layoutGarden);
        layoutBabyFood = rootView.findViewById(R.id.layoutBabyFood);
        layoutDrinks = rootView.findViewById(R.id.layoutDrinks);
        layoutCosmetics = rootView.findViewById(R.id.layoutCosmetics);
        layoutFish = rootView.findViewById(R.id.layoutFish);
        layoutMeat = rootView.findViewById(R.id.layoutMeat);
        layoutNuts = rootView.findViewById(R.id.layoutNuts);
        layoutHotDrinks = rootView.findViewById(R.id.layoutHotDrinks);
        layoutConfectionery = rootView.findViewById(R.id.layoutConfectionery);
        layoutConservation = rootView.findViewById(R.id.layoutConservation);
        layoutGrocery = rootView.findViewById(R.id.layoutGrocery);
        layoutCooking = rootView.findViewById(R.id.layoutCooking);
        layoutDairy = rootView.findViewById(R.id.layoutDairy);
        layoutFrozen = rootView.findViewById(R.id.layoutFrozen);
        layoutAllProducts = rootView.findViewById(R.id.layoutAllProducts);

        layoutNonFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productCategory = "Непродовольственные товары";
                textViewCategories.setText(productCategory);
                displayProducts();
            }
        });
        layoutCars.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productCategory = "Автотовары";
                textViewCategories.setText(productCategory);
                displayProducts();
            }
        });
        layoutGarden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productCategory = "Сад и огород";
                textViewCategories.setText(productCategory);
                displayProducts();
            }
        });
        layoutBabyFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productCategory = "Детское питание";
                textViewCategories.setText(productCategory);
                displayProducts();
            }
        });
        layoutDrinks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productCategory = "Напитки и алкоголь";
                textViewCategories.setText(productCategory);
                displayProducts();
            }
        });
        layoutCosmetics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productCategory = "Косметика и бытовая химия";
                textViewCategories.setText(productCategory);
                displayProducts();
            }
        });
        layoutFish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productCategory = "Рыбный отдел";
                textViewCategories.setText(productCategory);
                displayProducts();
            }
        });
        layoutMeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productCategory = "Мясной отдел";
                textViewCategories.setText(productCategory);
                displayProducts();
            }
        });
        layoutNuts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productCategory = "Орехи и сухофрукты";
                textViewCategories.setText(productCategory);
                displayProducts();
            }
        });
        layoutHotDrinks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productCategory = "Горячие напитки";
                textViewCategories.setText(productCategory);
                displayProducts();
            }
        });
        layoutConfectionery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productCategory = "Кондитерские изделия";
                textViewCategories.setText(productCategory);
                displayProducts();
            }
        });
        layoutConservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productCategory = "Консервация";
                textViewCategories.setText(productCategory);
                displayProducts();
            }
        });
        layoutGrocery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productCategory = "Бакалея";
                textViewCategories.setText(productCategory);
                displayProducts();
            }
        });
        layoutCooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productCategory = "Кулинария";
                textViewCategories.setText(productCategory);
                displayProducts();
            }
        });
        layoutDairy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productCategory = "Молочный отдел";
                textViewCategories.setText(productCategory);
                displayProducts();
            }
        });
        layoutFrozen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productCategory = "Замороженные продукты";
                textViewCategories.setText(productCategory);
                displayProducts();
            }
        });
        layoutAllProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productCategory = "allProducts";
                textViewCategories.setText("Все продукты");
                displayProducts();
            }
        });

        return rootView;
    }

    private void displayProducts(){

        products  = new ArrayList<>();
        counterFor = 1;
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if (counterFor == 1) {

                    //try {
                        if (first){
                            mStorageRef = FirebaseStorage.getInstance().getReference();

                            count = dataSnapshot.child(phoneNumber).child("allProducts").child("count").getValue(Integer.class);

                            for (int i = 0; i < count; i++) {
                                if (dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("category").getValue(String.class).equals("Непродовольственные товары")){
                                    StorageReference riversRef = mStorageRef.child(Objects.requireNonNull(dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("imagePath").getValue(String.class)));
                                    riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Picasso.get().load(uri).into(imageNonFood);
                                        }
                                    });
                                }

                                if (dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("category").getValue(String.class).equals("Автотовары")){
                                    StorageReference riversRef1 = mStorageRef.child(Objects.requireNonNull(dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("imagePath").getValue(String.class)));
                                    riversRef1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Picasso.get().load(uri).into(imageCars);
                                        }
                                    });
                                }

                                if (dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("category").getValue(String.class).equals("Сад и огород")){
                                    StorageReference riversRef2 = mStorageRef.child(Objects.requireNonNull(dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("imagePath").getValue(String.class)));
                                    riversRef2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Picasso.get().load(uri).into(imageGarden);
                                        }
                                    });
                                }

                                if (dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("category").getValue(String.class).equals("Детское питание")){
                                    StorageReference riversRef3 = mStorageRef.child(Objects.requireNonNull(dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("imagePath").getValue(String.class)));
                                    riversRef3.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Picasso.get().load(uri).into(imageBabyFood);
                                        }
                                    });
                                }

                                if (dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("category").getValue(String.class).equals("Напитки и алкоголь")){
                                    StorageReference riversRef4 = mStorageRef.child(Objects.requireNonNull(dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("imagePath").getValue(String.class)));
                                    riversRef4.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Picasso.get().load(uri).into(imageDrinks);
                                        }
                                    });
                                }

                                if (dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("category").getValue(String.class).equals("Косметика и бытовая химия")){
                                    StorageReference riversRef6 = mStorageRef.child(Objects.requireNonNull(dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("imagePath").getValue(String.class)));
                                    riversRef6.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Picasso.get().load(uri).into(imageCosmetics);
                                        }
                                    });
                                }

                                if (dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("category").getValue(String.class).equals("Рыбный отдел")){
                                    StorageReference riversRef7 = mStorageRef.child(Objects.requireNonNull(dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("imagePath").getValue(String.class)));
                                    riversRef7.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Picasso.get().load(uri).into(imageFish);
                                        }
                                    });
                                }

                                if (dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("category").getValue(String.class).equals("Мясной отдел")){
                                    StorageReference riversRef8 = mStorageRef.child(Objects.requireNonNull(dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("imagePath").getValue(String.class)));
                                    riversRef8.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Picasso.get().load(uri).into(imageMeat);
                                        }
                                    });
                                }

                                if (dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("category").getValue(String.class).equals("Орехи и сухофрукты")){
                                    StorageReference riversRef9 = mStorageRef.child(Objects.requireNonNull(dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("imagePath").getValue(String.class)));
                                    riversRef9.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Picasso.get().load(uri).into(imageNuts);
                                        }
                                    });
                                }

                                if (dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("category").getValue(String.class).equals("Горячие напитки")){
                                    StorageReference riversRef0 = mStorageRef.child(Objects.requireNonNull(dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("imagePath").getValue(String.class)));
                                    riversRef0.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Picasso.get().load(uri).into(imageHotDrinks);
                                        }
                                    });
                                }

                                if (dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("category").getValue(String.class).equals("Кондитерские изделия")){
                                    StorageReference riversRef01 = mStorageRef.child(Objects.requireNonNull(dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("imagePath").getValue(String.class)));
                                    riversRef01.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Picasso.get().load(uri).into(imageConfectionery);
                                        }
                                    });
                                }

                                if (dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("category").getValue(String.class).equals("Консервация")){
                                    StorageReference riversRef02 = mStorageRef.child(Objects.requireNonNull(dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("imagePath").getValue(String.class)));
                                    riversRef02.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Picasso.get().load(uri).into(imageConservation);
                                        }
                                    });
                                }

                                if (dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("category").getValue(String.class).equals("Бакалея")){
                                    StorageReference riversRef03 = mStorageRef.child(Objects.requireNonNull(dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("imagePath").getValue(String.class)));
                                    riversRef03.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Picasso.get().load(uri).into(imageGrocery);
                                        }
                                    });
                                }

                                if (dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("category").getValue(String.class).equals("Кулинария")){
                                    StorageReference riversRef04 = mStorageRef.child(Objects.requireNonNull(dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("imagePath").getValue(String.class)));
                                    riversRef04.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Picasso.get().load(uri).into(imageCooking);
                                        }
                                    });
                                }

                                if (dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("category").getValue(String.class).equals("Молочный отдел")){
                                    StorageReference riversRef05 = mStorageRef.child(Objects.requireNonNull(dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("imagePath").getValue(String.class)));
                                    riversRef05.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Picasso.get().load(uri).into(imageDairy);
                                        }
                                    });
                                }

                                if (dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("category").getValue(String.class).equals("Замороженные продукты")){
                                    StorageReference riversRef06 = mStorageRef.child(Objects.requireNonNull(dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("imagePath").getValue(String.class)));
                                    riversRef06.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Picasso.get().load(uri).into(imageFrozen);
                                        }
                                    });

                                }

                            }

                            first = false;
                        }


                        if (productCategory.equals("allProducts")){
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
                                products.add(new MyProductsForRecyclerView(name,countProd,value,imagePath,notificationTime,notificationDate, validUntilDate, productionDate, category,i,getContext(),getActivity()));


                            }
                        }else{
                            count = dataSnapshot.child(phoneNumber).child("allProducts").child("count").getValue(Integer.class);

                            for (int i = 0; i < count; i++) {

                                if(productCategory.equals(dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("category").getValue(String.class))){
                                    name = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("name").getValue(String.class);
                                    countProd = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("count").getValue(String.class);
                                    value = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("value").getValue(String.class);
                                    imagePath = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("imagePath").getValue(String.class);
                                    notificationDate = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("notificationDate").getValue(Long.class);
                                    notificationTime = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("notificationTime").getValue(Long.class);
                                    validUntilDate = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("validUntilDate").getValue(Long.class);
                                    productionDate = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("productionDate").getValue(Long.class);
                                    category = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(i)).child("category").getValue(String.class);
                                    products.add(new MyProductsForRecyclerView(name,countProd,value,imagePath,notificationTime,notificationDate, validUntilDate, productionDate, category,i,getContext(),getActivity()));

                                }
                            }
                        }
                        Collections.sort(products, new Comparator<MyProductsForRecyclerView>() {
                            @Override
                            public int compare(MyProductsForRecyclerView myProductsForRecyclerView, MyProductsForRecyclerView t1) {
                                return (myProductsForRecyclerView.validUntilDate+notificationTime+"").compareTo(t1.validUntilDate+notificationTime+"");
                            }

                        });
                        DataAdapter adapter = new DataAdapter(getContext(), products);
                        recyclerView.setAdapter(adapter);




                    //} catch (Exception e) {
                        //Toast.makeText(getContext(),e+"",Toast.LENGTH_LONG).show();
                    //}
                    counterFor = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
            }
        });
    }

}
