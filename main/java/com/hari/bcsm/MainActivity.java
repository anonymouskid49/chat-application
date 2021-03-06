package com.hari.bcsm;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private androidx.appcompat.widget.Toolbar  mToolbar;
    private ViewPager myviewpager;
    private TabLayout mytablayout;
    private TabsAccessorAdapter mytabsaccessoradapter;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserID;



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();

        mToolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("BCSM");

        myviewpager = (ViewPager) findViewById(R.id.maintabs_pager);
        mytabsaccessoradapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myviewpager.setAdapter(mytabsaccessoradapter);

        mytablayout = (TabLayout) findViewById(R.id.maintabs);
        mytablayout.setupWithViewPager(myviewpager);
    }

    @Override
    protected void onStart()
    {

        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null)
        {
            sendUserToLoginActivity();
        }
        else
        {
            updateUserStatus("online");
            VerifyUserExistance();
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null)
        {
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
        {
            updateUserStatus("offline");
        }
    }

    private void VerifyUserExistance()
    {
     String currentUserID = mAuth.getCurrentUser().getUid();
     RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
         @Override
         public void onDataChange(@NonNull DataSnapshot dataSnapshot)
         {
           if ((dataSnapshot.child("name").exists()))
           {
               Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
           }
           else
           {
               sendUserToSettingsActivity();
           }
         }

         @Override
         public void onCancelled(@NonNull DatabaseError databaseError) {

         }
     });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_logout_option)
        {
            updateUserStatus("offline");
            mAuth.signOut();
            sendUserToLoginActivity();
        }
        if (item.getItemId() == R.id.main_settings_option)
        {
        sendUserToSettingsActivity();
        }
        if (item.getItemId() == R.id.main_create_group_option)
        {
            RequestNewGroup();
        }
        if (item.getItemId() == R.id.main_find_friends_option)
        {
            sendUserToFindFriendsActivity();

        }
        return true;
    }

    private void RequestNewGroup()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name:");

        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g Friends , Family");
        builder.setView(groupNameField);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String groupName = groupNameField.getText().toString();
                if (TextUtils.isEmpty(groupName))
                {
                    Toast.makeText(MainActivity.this, "Please Enter Group Name ....", Toast.LENGTH_SHORT).show();
                }
                else
                {
                   CreateNewGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
              dialog.cancel();
            }
        });
        builder.show();

    }

    private void CreateNewGroup(final String groupName)
    {
        RootRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                      if (task.isSuccessful())
                      {
                          Toast.makeText(MainActivity.this, groupName+"Group is Created", Toast.LENGTH_SHORT).show();
                      }
                    }
                });
    }

    private void sendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();

    }
    private void sendUserToSettingsActivity()
    {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);

    }
    private void sendUserToFindFriendsActivity()
    {
        Intent findFriendIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findFriendIntent);

    }

    private void updateUserStatus(String State)
    {
        String saveCurrentTime, saveCurrentDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd MMM, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap= new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime );
        onlineStateMap.put("date", saveCurrentDate );
        onlineStateMap.put("state", State );

        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);

    }
}
