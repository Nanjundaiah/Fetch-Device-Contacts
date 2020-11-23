package com.m3i.assignment;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.m3i.assignment.recycleradapters.ContactsRecyclerAdapter;
import com.m3i.assignment.model.ContactsList;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<ContactsList> contactslist;
    private RecyclerView recyclerView;
    private static final int PERMISSIONS_REQUEST_READ_WRITE_CONTACTS = 100;
    private ContactsRecyclerAdapter contactsRecyclerAdapter;
    private Dialog dialog;
    private TextView permissionTextview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeFields();
    }

    private void initializeFields() {
        contactslist = new ArrayList<>();
        FloatingActionButton floatingActionButton = findViewById(R.id.add_floating_button);
        permissionTextview = findViewById(R.id.permission_textview);
        permissionTextview.setVisibility(View.GONE);
        recyclerView = findViewById(R.id.contacts_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        AskPermission();
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
    }


    private void AskPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, PERMISSIONS_REQUEST_READ_WRITE_CONTACTS);
        } else {
            FetchContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_WRITE_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                AskPermission();
            } else {
                permissionTextview.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Please grant permission", Toast.LENGTH_LONG).show();
            }
        }
    }

   private void FetchContacts(){
        permissionTextview.setVisibility(View.GONE);
       Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            ContactsList contactsList = new ContactsList(name, number);
            contactslist.add(contactsList);
        }
        SetAdapter();
   }

   private void SetAdapter(){
        contactsRecyclerAdapter = new ContactsRecyclerAdapter(contactslist, this);
       recyclerView.setAdapter(contactsRecyclerAdapter);
       contactsRecyclerAdapter.notifyDataSetChanged();
   }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                contactsRecyclerAdapter.getFilter().filter(newText);
                return false;
            }
        });

        return true;

    }

    public void showDialog(){
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.save_contact_layout);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);

        final EditText name = (EditText) dialog.findViewById(R.id.name_edittext);
        final EditText number  = (EditText) dialog.findViewById(R.id.number_edittext);
        TextView save = (TextView) dialog.findViewById(R.id.save_textview);
        TextView cancel = (TextView) dialog.findViewById(R.id.cancel_textview);


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String namefromdialog = name.getText().toString();
                String numbwerfromdialog = number.getText().toString();
                addContact(namefromdialog, numbwerfromdialog);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void addContact(String given_name,  String mobile) {
        ArrayList<ContentProviderOperation> contact = new ArrayList<ContentProviderOperation>();
        contact.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        contact.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, given_name)
                .build());

        contact.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, mobile)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build());

        try {
            ContentProviderResult[] results = getContentResolver().applyBatch(ContactsContract.AUTHORITY, contact);
            Toast.makeText(getApplicationContext(), "Contact saved successfully...", Toast.LENGTH_SHORT).show();
            FetchContacts();
            dialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}