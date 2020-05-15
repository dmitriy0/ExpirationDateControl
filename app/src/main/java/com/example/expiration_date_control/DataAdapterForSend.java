package com.example.expiration_date_control;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

class DataAdapterForSend extends RecyclerView.Adapter<DataAdapterForSend.ViewHolder> {

    private LayoutInflater inflater;
    private List<MyProductsForRecyclerView> products;

    private long time;
    private long date;
    long notificationDate;
    private Context context;

    private StorageReference mStorageRef;

    DataAdapterForSend(Context context, List<MyProductsForRecyclerView> products) {
        this.products = products;
        this.inflater = LayoutInflater.from(context);
    }
    @Override
    public DataAdapterForSend.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.list_item_for_send, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final DataAdapterForSend.ViewHolder holder, final int position) {

        final MyProductsForRecyclerView products = this.products.get(position);

        final SharedPreferences preferences = getDefaultSharedPreferences(products.getContext());

        context = products.getContext();
        time = products.getNotificationTime();
        date = products.getNotificationDate();
        notificationDate = products.getNotificationDate();

        String description = products.getName() + " " + products.getCountProd() + " " + products.getValue() + ". " + "до " + DateUtils.formatDateTime(products.getContext(), date, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR);

        holder.timeView.setText(DateUtils.formatDateTime(products.getContext(),
                time+notificationDate,
                DateUtils.FORMAT_SHOW_TIME));

        holder.descriptionView.setText(description);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        StorageReference riversRef = mStorageRef.child(products.getImagePath());

        holder.dateView.setText(DateUtils.formatDateTime(products.getContext(),
                notificationDate,
                DateUtils.FORMAT_SHOW_WEEKDAY|DateUtils.FORMAT_ABBREV_WEEKDAY|DateUtils.FORMAT_SHOW_DATE|DateUtils.FORMAT_NUMERIC_DATE|DateUtils.FORMAT_SHOW_YEAR));


        //отрисовка картинки
        riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(final Uri uri) {
                Picasso.get().load(uri).into(holder.imageView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {

                        @SuppressLint("StaticFieldLeak")
                        class CatTask extends AsyncTask<Void, Void, Void> {

                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();

                            }

                            @Override
                            protected Void doInBackground(Void... voids) {
                                @SuppressLint("WrongThread") Bitmap bm = ((BitmapDrawable) holder.imageView.getDrawable()).getBitmap();
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bm.compress(Bitmap.CompressFormat.PNG, 10, baos); //bm is the bitmap object
                                byte[] b = baos.toByteArray();

                                String encoded = Base64.encodeToString(b, Base64.DEFAULT);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("image"+position, encoded);
                                editor.apply();

                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                super.onPostExecute(aVoid);

                            }
                        }
                        CatTask catTask = new CatTask();
                        catTask.execute();
                    }

                    @Override
                    public void onError(Exception e) {

                    }

                });

            }
        });
        holder.checkBoxSend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(position+"",b);
                editor.apply();
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;
        final CheckBox checkBoxSend;
        final TextView descriptionView, timeView,dateView;
        ViewHolder(View view){
            super(view);
            checkBoxSend = view.findViewById(R.id.checkBoxSend);
            imageView = (ImageView)view.findViewById(R.id.productImage);
            descriptionView = (TextView) view.findViewById(R.id.description);
            timeView = (TextView) view.findViewById(R.id.notificationTime);
            dateView = (TextView) view.findViewById(R.id.notificationDate);
        }
    }



}
