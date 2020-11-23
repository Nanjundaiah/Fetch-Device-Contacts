package com.m3i.assignment.recycleradapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.m3i.assignment.R;
import com.m3i.assignment.model.ContactsList;

import java.util.ArrayList;
import java.util.List;

public class ContactsRecyclerAdapter extends RecyclerView.Adapter<ContactsRecyclerAdapter.ViewHolder > {

    private List<ContactsList> contactslist = new ArrayList<>();
    private Context context;
    private List<ContactsList> searchcontactlist;

    public ContactsRecyclerAdapter(List<ContactsList> contactslist, Context context) {
        this.contactslist = contactslist;
        this.context = context;
        searchcontactlist = new ArrayList<>(contactslist);
    }

    @NonNull
    @Override
    public ContactsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.contacts_list_layout, parent, false);
        return new ContactsRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsRecyclerAdapter.ViewHolder holder, int position) {
        ContactsList contactsList = contactslist.get(position);
        holder.name.setText(contactsList.getName());
        holder.number.setText(contactsList.getNumber());

    }

    @Override
    public int getItemCount() {
        return contactslist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView name, number;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name_textview);
            number = itemView.findViewById(R.id.number_textview);

        }
    }


    public Filter getFilter() {
        return filter;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<ContactsList> filteredlist = new ArrayList<>();
            if (charSequence == null || charSequence.length() == 0) {
                filteredlist.addAll(searchcontactlist);
            } else {
                String filterpattern = charSequence.toString().toLowerCase().trim();
                for (ContactsList item : searchcontactlist) {
                    if (item.getName().toLowerCase().contains(filterpattern)) {
                        filteredlist.add(item);
                    }

                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredlist;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            contactslist.clear();
            contactslist.addAll((List) filterResults.values);
            notifyDataSetChanged();

        }
    };
}
