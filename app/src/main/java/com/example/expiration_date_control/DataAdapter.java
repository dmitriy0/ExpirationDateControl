package com.example.expiration_date_control;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.provider.MediaStore.Images.Thumbnails.getThumbnail;

class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private List<MyProductsForRecyclerView> products;

    private long time;
    private long date;
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

    @Override
    public void onBindViewHolder(final DataAdapter.ViewHolder holder, int position) {

        final MyProductsForRecyclerView products = this.products.get(position);

        context = products.getContext();
        time = products.getNotificationTime();
        date = products.getNotificationDate();

        String description = products.getName() + "" + products.getCountProd() + "" + products.getValue() + "." + "до " + DateUtils.formatDateTime(products.getContext(), date, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR);

        holder.timeView.setText(DateUtils.formatDateTime(products.getContext(),
                time,
                DateUtils.FORMAT_SHOW_TIME));

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
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;
        final TextView descriptionView, timeView;
        ViewHolder(View view){
            super(view);
            imageView = (ImageView)view.findViewById(R.id.productImage);
            descriptionView = (TextView) view.findViewById(R.id.description);
            timeView = (TextView) view.findViewById(R.id.notificationTime);
        }
    }



}