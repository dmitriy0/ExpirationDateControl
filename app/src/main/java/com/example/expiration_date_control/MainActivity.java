package com.example.expiration_date_control;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static androidx.core.content.ContextCompat.startActivity;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;

public class MainActivity extends AppCompatActivity {

    private NotificationManager mNotificationManager;
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

    private AppBarConfiguration mAppBarConfiguration;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,AddProduct.class);
                intent.putExtra("name","");
                intent.putExtra("category","Непродовольственные товары");
                intent.putExtra("countProd","0");
                intent.putExtra("value","шт");
                intent.putExtra("imagePath","");
                intent.putExtra("barcodeImagePath","");
                startActivity(intent);
            }
        });

        counterFor = 1;

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        SharedPreferences preferences = getDefaultSharedPreferences(this);
        final String phoneNumber = preferences.getString("phoneNumber","");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "name";
            String description = "description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("chanel", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        
        NavigationView navigationView = findViewById(R.id.nav_view);

        Fragment fragment = null;
        try {
            fragment = (Fragment) MyProducts.class.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        assert fragment != null;
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
        navigationView.setCheckedItem(R.id.nav_products);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment = null;

                int id = menuItem.getItemId();

                if (id == R.id.nav_tools) {
                    try {
                        fragment = (Fragment) ImportPDF.class.newInstance();
                        fab.hide();
                    } catch (IllegalAccessException | InstantiationException e) {
                        e.printStackTrace();
                    }
                }
                if (id == R.id.nav_products) {
                    try {
                        fragment = (Fragment) MyProducts.class.newInstance();
                        fab.show();
                    } catch (IllegalAccessException | InstantiationException e) {
                        e.printStackTrace();
                    }
                }
                if (id == R.id.nav_send) {
                    try {
                        fragment = (Fragment) Send.class.newInstance();
                        fab.hide();
                    } catch (IllegalAccessException | InstantiationException e) {
                        e.printStackTrace();
                    }
                }

                FragmentManager fragmentManager = getSupportFragmentManager();
                assert fragment != null;
                fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
                // Выделяем выбранный пункт меню в шторке
                menuItem.setChecked(true);

                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.container);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void onBackPressed() {
        // super.onBackPressed();
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "aaaa";
            String description = "gdfvdfv";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("chanel", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


}
