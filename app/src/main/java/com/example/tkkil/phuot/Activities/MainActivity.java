package com.example.tkkil.phuot.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tkkil.phuot.Interface.ItemClickListener;
import com.example.tkkil.phuot.Models.Group;
import com.example.tkkil.phuot.Models.User;
import com.example.tkkil.phuot.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int REQUEST_LOCATION_CODE = 99;
    private GoogleMap mMap;
    private GoogleApiClient mClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private Marker mCurrentLocationMarker;

    private static final int REQUEST_SELECT_PICTURE = 999;
    private StorageReference mStorage;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    NavigationView myNav;
    private DrawerLayout myDrawer;
    private ProgressDialog loading;
    private Uri filepath;
    Bitmap bitmap;
    RecyclerView nav_rcvListGroup;
    //Dialog
    EditText edtNameGroup, edtPwdGroup;
    //Header-Nav
    private CircleImageView nav_avatar;
    private TextView nav_name, nav_email;
    //Dialog avatar
    private CircleImageView dialog_avatar;
    private User user;
    FirebaseRecyclerAdapter adapterabc;
    ArrayList<String> keys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            checkLocationPermisstion();
        }
        initGoogleMap();
        mStorage = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference();
        loading = new ProgressDialog(this, R.style.MyDialogTheme);
        loading.setTitle("LOADING");
        loading.setMessage("Please wait...");

        init();
        initToolbar();
    }

    private boolean checkLocationPermisstion() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            return false;
        }else
            return  true;
    }

    protected synchronized void buildGoogleApiClient(){
        mClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mClient.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapterabc.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapterabc.stopListening();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_LOCATION_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED){
                        if(mClient == null){
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }else {
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED){
            LocationServices.FusedLocationApi.requestLocationUpdates(mClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        if(mCurrentLocationMarker != null){
            mCurrentLocationMarker.remove();
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        mCurrentLocationMarker = mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(10));

        if(mClient !=null){
            LocationServices.FusedLocationApi.removeLocationUpdates(mClient, this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED){
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    private void initGoogleMap(){
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragment);
        mapFragment.getMapAsync(this);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView txtvNameGroup, txtvQuantity, txtvFullname;
        Button btnDelete, btnEdit;
        ItemClickListener itemClickListener;

        ViewHolder(View itemView) {
            super(itemView);
            txtvNameGroup = itemView.findViewById(R.id.txtvNameGroup);
            txtvQuantity = itemView.findViewById(R.id.txtvQuantity);
            txtvFullname = itemView.findViewById(R.id.txtvFullname);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onClick(view, getAdapterPosition(), false);
        }

        @Override
        public boolean onLongClick(View view) {
            itemClickListener.onClick(view, getAdapterPosition(), true);
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        if (myDrawer.isDrawerOpen(GravityCompat.START)) {
            myDrawer.closeDrawer(GravityCompat.START);
        } else {
            AlertDialog.Builder ab_exit = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
            ab_exit.setTitle("EXIT");
            ab_exit.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            ab_exit.setNegativeButton("NO", null);
            final AlertDialog dialog_ab_exit = ab_exit.create();
            dialog_ab_exit.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    dialog_ab_exit.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorRed));
                }
            });
            dialog_ab_exit.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_group:
                addGroupDialog();
                break;
            case R.id.action_change_info:
                startActivity(new Intent(MainActivity.this, ChangeInformationActivity.class));
                break;
            case R.id.action_change_password:
                startActivity(new Intent(MainActivity.this, ChangePasswordActivity.class));
                break;
            case R.id.action_log_out:
                AlertDialog.Builder ab = new AlertDialog.Builder(this, R.style.MyDialogTheme);
                ab.setTitle("SIGN OUT");
                ab.setNegativeButton("NO", null);
                ab.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mAuth.signOut();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    }
                });
                final AlertDialog alertDialog = ab.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorRed));
                    }
                });
                alertDialog.show();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_PICTURE && resultCode == RESULT_OK && data != null) {
            filepath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filepath);
                dialog_avatar.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean Validate() {
        boolean isValidate = true;
        if (TextUtils.isEmpty(edtNameGroup.getText().toString().trim())) {
            edtNameGroup.setError("Please enter Name!");
            isValidate = false;
        }
        if (TextUtils.isEmpty(edtPwdGroup.getText().toString().trim())) {
            edtPwdGroup.setError("Please enter Password!");
            isValidate = false;
        } else {
            if (edtPwdGroup.getText().toString().trim().length() < 6) {
                edtPwdGroup.setError("Password is too short!");
                isValidate = false;
            }
        }
        return isValidate;
    }

    private void addGroupDialog() {
        AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_new_group, null);
        ab.setView(view);

        ab.setPositiveButton("CREATE", null);
        ab.setNegativeButton("CANCEL", null);

        edtNameGroup = view.findViewById(R.id.edtNameGroup);
        edtPwdGroup = view.findViewById(R.id.edtPwdGroup);

        final AlertDialog dialog = ab.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                Button Positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Positive.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("SimpleDateFormat")
                    @Override
                    public void onClick(View view) {
                        if (!Validate()) {
                            return;
                        }
                        dialog.dismiss();
                        Snackbar.make(myDrawer, "Success", Snackbar.LENGTH_SHORT).show();
                        Group group = new Group();
                        FirebaseUser mUser = mAuth.getCurrentUser();
                        if (mUser != null) {
                            group.setHost(mUser.getUid());
                            group.setName(edtNameGroup.getText().toString().trim());
                            group.setPass(edtPwdGroup.getText().toString().trim());
                            group.setTime(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Calendar.getInstance().getTime()));
                            myRef.child("Groups").push().setValue(group)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Snackbar.make(myDrawer, "Success", Snackbar.LENGTH_SHORT).show();
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Snackbar.make(myDrawer, "Error: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                });
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Home");
        myDrawer = findViewById(R.id.myDrawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, myDrawer, toolbar, R.string.openDrawer, R.string.closeDrawer);
        myDrawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private int onGetHeightStatus() {
        int result = 0;
        int resourceId = MainActivity.this.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void init() {
        myNav = findViewById(R.id.myNav);
        myNav.setPadding(0, onGetHeightStatus(), 0, 0);

        nav_avatar = findViewById(R.id.nav_avatar);
        nav_name = findViewById(R.id.nav_name);
        nav_email = findViewById(R.id.nav_email);

        keys = new ArrayList<>();
        myRef.child("Groups").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                s = dataSnapshot.getKey();
                keys.add(s);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Recycler
        /*rcvListGroup = findViewById(R.id.rcvListGroup);
        rcvListGroup.setHasFixedSize(true);
        rcvListGroup.setLayoutManager(new LinearLayoutManager(this));
        Query query = myRef.child("Groups");
        FirebaseRecyclerOptions<Group> options = new FirebaseRecyclerOptions.Builder<Group>().setQuery(query, Group.class).build();
        adapterabc = new FirebaseRecyclerAdapter<Group, ViewHolder>(options) {

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
                return new ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(ViewHolder holder, final int position, final Group model) {
                holder.txtvName.setText(model.getName());
                holder.txtvTime.setText(model.getTime());

                holder.btnDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        myRef.child("Users").child(model.getHost()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                User user = dataSnapshot.getValue(User.class);
                                if (user != null) {
                                    AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
                                    ab.setTitle("INFORMATION");
                                    View dialog_info_host_view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_info_host, null);
                                    ab.setView(dialog_info_host_view);
                                    TextView txtvName = dialog_info_host_view.findViewById(R.id.txtvFullname);
                                    TextView txtvBirthday = dialog_info_host_view.findViewById(R.id.txtvBirthday);
                                    TextView txtvPhone = dialog_info_host_view.findViewById(R.id.txtvPhone);
                                    TextView txtvEmail = dialog_info_host_view.findViewById(R.id.txtvEmail);
                                    txtvName.setText(user.getFullname());
                                    txtvBirthday.setText(user.getBirthday());
                                    txtvPhone.setText(user.getPhone());
                                    txtvEmail.setText(user.getEmail());
                                    AlertDialog alertDialog = ab.create();
                                    alertDialog.show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
                holder.btnJoin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mAuth.getCurrentUser().getUid().equalsIgnoreCase(model.getHost())) {
                            Toast.makeText(MainActivity.this, "Can't join!", Toast.LENGTH_SHORT).show();
                        } else {
                            AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
                            ab.setTitle("JOIN GROUP");
                            View dialog_join_group_view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_join_group, null);
                            ab.setView(dialog_join_group_view);
                            final EditText edtPwdGroup = dialog_join_group_view.findViewById(R.id.edtPwdGroup);
                            ab.setPositiveButton("JOIN", null);
                            ab.setNegativeButton("CANCEL", null);
                            final AlertDialog alertDialog = ab.create();
                            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialogInterface) {
                                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                                    Button Positive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                    Positive.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (TextUtils.isEmpty(edtPwdGroup.getText().toString().trim())) {
                                                edtPwdGroup.setError("Please enter Password!");
                                                return;
                                            }
                                            myRef.child("Groups").child(keys.get(position)).child("pass").addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.getValue(String.class).equalsIgnoreCase(edtPwdGroup.getText().toString().trim())) {
                                                        myRef.child("Groups").child(keys.get(position)).child("members").child(mAuth.getCurrentUser().getUid()).setValue("Joined");
                                                        alertDialog.dismiss();
                                                    } else {
                                                        Toast.makeText(MainActivity.this, "Wrong, can't join!", Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    });
                                }
                            });
                            alertDialog.setCanceledOnTouchOutside(false);
                            alertDialog.show();

                        }
                    }
                });
            }
        };
        rcvListGroup.setAdapter(adapterabc);*/

        nav_rcvListGroup = findViewById(R.id.nav_rcvListGroup);
        nav_rcvListGroup.setHasFixedSize(true);
        nav_rcvListGroup.setLayoutManager(new LinearLayoutManager(this));

        Query query = myRef.child("Groups").orderByChild("host").startAt(mAuth.getCurrentUser().getUid()).endAt(mAuth.getCurrentUser().getUid());
        FirebaseRecyclerOptions<Group> options = new FirebaseRecyclerOptions.Builder<Group>().setQuery(query, Group.class).build();
        adapterabc = new FirebaseRecyclerAdapter<Group, ViewHolder>(options) {

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
                return new ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(final ViewHolder holder, final int position, final Group model) {
                holder.txtvNameGroup.setText(model.getName());
                myRef.child("Users").child(model.getHost()).child("fullname").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        holder.txtvFullname.setText(dataSnapshot.getValue(String.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
                        ab.setTitle("DELETE");
                        ab.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                myRef.child("Groups").child(keys.get(position)).removeValue();
                            }
                        });
                        ab.setNegativeButton("NO", null);
                        AlertDialog alertDialog = ab.create();
                        alertDialog.show();
                    }
                });
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        if (isLongClick) {
                            Toast.makeText(MainActivity.this, "Long Click: " + model.getName(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Short Click: " + model.getHost(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        };

        nav_rcvListGroup.setAdapter(adapterabc);

        FirebaseUser mUser = mAuth.getCurrentUser();
        if (mUser != null) {
            myRef.child("Users/" + mUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    user = new User();
                    user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        if (TextUtils.isEmpty(user.getAvatar())) {
                            user.setAvatar("AAA");
                        }
                        Picasso.with(MainActivity.this)
                                .load(user.getAvatar())
                                .placeholder(R.drawable.defaut_avatar)
                                .centerCrop()
                                .fit()
                                .into(nav_avatar);
                        nav_name.setText(user.getFullname());
                        nav_email.setText(user.getEmail());
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        nav_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, nav_avatar, Gravity.END);
                popupMenu.getMenuInflater().inflate(R.menu.avatar, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.action_change_avatar) {
                            final AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
                            ab.setTitle("CHANGE AVATAR");
                            ab.setIcon(R.drawable.ic_account_box_green_24dp);
                            @SuppressLint("InflateParams")
                            View dialog_change_avatar = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_change_avatar, null);
                            ab.setView(dialog_change_avatar);
                            dialog_avatar = dialog_change_avatar.findViewById(R.id.dialog_avatar);
                            ab.setPositiveButton("CHANGE", null);
                            ab.setNeutralButton("SELECT PICTURE", null);
                            ab.setNegativeButton("NO", null);
                            final AlertDialog alertDialog = ab.create();
                            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialogInterface) {
                                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorBlue));
                                    Button b = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                                    Button c = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                    b.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent(Intent.ACTION_PICK);
                                            intent.setType("image/*");
                                            startActivityForResult(intent, REQUEST_SELECT_PICTURE);
                                        }
                                    });
                                    c.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (dialog_avatar.getDrawable() == null) {
                                                Toasty.error(MainActivity.this, "Please choose picture!", Snackbar.LENGTH_SHORT, true).show();
                                            } else {
                                                loading.show();
                                                StorageReference storageReference = mStorage.child("images").child(UUID.randomUUID().toString());
                                                storageReference.putFile(filepath)
                                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                            @Override
                                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                Uri downloadUri = taskSnapshot.getDownloadUrl();
                                                                myRef.child("Users/" + mAuth.getCurrentUser().getUid() + "/avatar").setValue(downloadUri);
                                                                loading.dismiss();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Snackbar.make(myDrawer, "Error: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                                                loading.dismiss();
                                                            }
                                                        });
                                                alertDialog.dismiss();
                                            }
                                        }
                                    });
                                }
                            });
                            alertDialog.setCanceledOnTouchOutside(false);
                            alertDialog.show();
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });
    }
}
