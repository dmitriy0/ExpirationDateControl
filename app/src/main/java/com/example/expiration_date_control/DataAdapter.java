package com.example.expiration_date_control;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static android.provider.MediaStore.Images.Thumbnails.getThumbnail;
import static androidx.core.content.ContextCompat.startActivity;

class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private List<MyProductsForRecyclerView> products;

    private long time;
    private long date;
    private long notificationDate;
    private Context context;

    private StorageReference mStorageRef;

    DataAdapter(Context context, List<MyProductsForRecyclerView> products) {
        this.products = products;
        this.inflater = LayoutInflater.from(context);
    }
    @Override
    public DataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    private static ArrayList<LinearLayout> cardViewArrayList = new ArrayList<>();

    @Override
    public void onBindViewHolder(final DataAdapter.ViewHolder holder, final int position) {

        final MyProductsForRecyclerView products = this.products.get(position);

        final SharedPreferences preferences = getDefaultSharedPreferences(products.getContext());
        final String phoneNumber = preferences.getString("phoneNumber","");

        context = products.getContext();
        time = products.getNotificationTime();
        date = products.getValidUntilDate();
        notificationDate = products.getNotificationDate();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, (int) (time/3600000));
        calendar.set(Calendar.MINUTE, (int) ((time%3600000)/60000));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        String description = products.getName() + " " + products.getCountProd() + " " + products.getValue() + ". " + "до " + DateUtils.formatDateTime(products.getContext(), date, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR);

        holder.timeView.setText(DateUtils.formatDateTime(products.getContext(),
                calendar.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_TIME));
        holder.dateView.setText(DateUtils.formatDateTime(products.getContext(),
                notificationDate,
                DateUtils.FORMAT_SHOW_WEEKDAY|DateUtils.FORMAT_ABBREV_WEEKDAY|DateUtils.FORMAT_SHOW_DATE|DateUtils.FORMAT_NUMERIC_DATE|DateUtils.FORMAT_SHOW_YEAR));

        holder.descriptionView.setText(description);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        StorageReference riversRef = mStorageRef.child(products.getImagePath());



        //отрисовка картинки
        riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(holder.imageView);
            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("Users");
        holder.imageDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(products.getActivity());
                builder.setTitle("Вы уверенны");  // заголовок
                builder.setMessage("Удалить эту запись?"); // сообщение
                builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                int countProducts = dataSnapshot.child(phoneNumber).child("allProducts").child("count").getValue(Integer.class);
                                myRef.child(phoneNumber).child("allProducts").child("count").setValue(countProducts - 1);
                                myRef.child(phoneNumber).child("allProducts").child(String.valueOf(products.getNumber())).removeValue();
                                for (int j = products.getNumber()+1; j < countProducts;j++) {
                                    String name = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(j)).child("name").getValue(String.class);
                                    myRef.child(phoneNumber).child("allProducts").child(String.valueOf(j-1)).child("name").setValue(name);

                                    String category = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(j)).child("category").getValue(String.class);
                                    myRef.child(phoneNumber).child("allProducts").child(String.valueOf(j-1)).child("category").setValue(category);

                                    String count = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(j)).child("count").getValue(String.class);
                                    myRef.child(phoneNumber).child("allProducts").child(String.valueOf(j-1)).child("count").setValue(count);

                                    String imagePath = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(j)).child("imagePath").getValue(String.class);
                                    myRef.child(phoneNumber).child("allProducts").child(String.valueOf(j-1)).child("imagePath").setValue(imagePath);

                                    String value = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(j)).child("value").getValue(String.class);
                                    myRef.child(phoneNumber).child("allProducts").child(String.valueOf(j-1)).child("value").setValue(value);

                                    Long productionDate = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(j)).child("productionDate").getValue(Long.class);
                                    myRef.child(phoneNumber).child("allProducts").child(String.valueOf(j-1)).child("productionDate").setValue(productionDate);

                                    Long notificationDate = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(j)).child("notificationDate").getValue(Long.class);
                                    myRef.child(phoneNumber).child("allProducts").child(String.valueOf(j-1)).child("notificationDate").setValue(notificationDate);

                                    Long validUntilDate = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(j)).child("validUntilDate").getValue(Long.class);
                                    myRef.child(phoneNumber).child("allProducts").child(String.valueOf(j-1)).child("validUntilDate").setValue(validUntilDate);

                                    Long notificationTime = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(j)).child("notificationTime").getValue(Long.class);
                                    myRef.child(phoneNumber).child("allProducts").child(String.valueOf(j-1)).child("notificationTime").setValue(notificationTime);

                                    myRef.child(phoneNumber).child("allProducts").child(String.valueOf(j)).removeValue();

                                }


                                Intent intent = new Intent(products.getActivity(),MainActivity.class);
                                startActivity(products.getContext(),intent,null);


                            }

                            @Override
                            public void onCancelled(DatabaseError error) {

                            }
                        });
                        Toast.makeText(products.getContext(), "Запись успешно удалена",
                                Toast.LENGTH_LONG).show();
                    }
                });

                builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
                builder.setCancelable(true);
                builder.create();
                builder.show();
            }
        });

        holder.imageEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(products.getActivity(),AddProduct.class);
                intent.putExtra("name",products.getName());
                intent.putExtra("productionDate",products.getProductionDate());
                intent.putExtra("validUntilDate",products.getValidUntilDate());
                intent.putExtra("category",products.getCategory());
                intent.putExtra("countProd",products.getCountProd());
                intent.putExtra("value",products.getValue());
                intent.putExtra("notificationDate",products.getNotificationDate());
                intent.putExtra("notificationTime",products.getNotificationTime());
                intent.putExtra("imagePath",products.getImagePath());
                intent.putExtra("barcodeImagePath",products.getImagePath()+"barcode");
                //Toast.makeText(products.getContext(),"barcode"+products.getImagePath(),Toast.LENGTH_LONG).show();
                intent.putExtra("number",products.getNumber());
                startActivity(products.getActivity(),intent,null);
                
            }
        });

    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView, imageDelete,imageEdit;
        final TextView descriptionView, timeView,dateView;
        ViewHolder(View view){
            super(view);
            dateView = (TextView) view.findViewById(R.id.notificationDate);
            imageView = (ImageView)view.findViewById(R.id.productImage);
            imageDelete = (ImageView)view.findViewById(R.id.imageViewDelete);
            imageEdit = (ImageView)view.findViewById(R.id.imageViewEdit);
            descriptionView = (TextView) view.findViewById(R.id.description);
            timeView = (TextView) view.findViewById(R.id.notificationTime);
        }
    }



}