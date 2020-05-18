package com.e.shelter.adapers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.e.shelter.R;
import com.e.shelter.utilities.Contact;
import com.google.android.material.button.MaterialButton;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;

import org.bson.Document;

import java.util.ArrayList;

import static com.mongodb.client.model.Filters.eq;

public class ContactListAdapter extends ArrayAdapter<Contact> {

    private static final String TAG = "CustomListAdapter";

    private Context mContext;
    private int mResource;
    private int lastPosition = -1;
    private ContactListAdapter adapter;
    private ArrayList<Contact> cards;
    private String user_type;


    /**
     * Holds variables in a View
     */
    private static class ViewHolder {
        TextView name;
        TextView phoneNumber;
        MaterialButton callButton;
        MaterialButton removeButton;
        MaterialButton editButton;
    }

    /**
     * Default constructor for the PersonListAdapter
     * @param context
     * @param resource
     * @param objects
     */
    public ContactListAdapter(Context context, int resource, ArrayList<Contact> objects, String userType) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
        adapter = this;
        cards = objects;
        user_type = userType;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final View result;
        final ContactListAdapter.ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);
            holder = new ContactListAdapter.ViewHolder();
            holder.name = convertView.findViewById(R.id.contactCardContactName);
            holder.phoneNumber = convertView.findViewById(R.id.contactCardContactPhone);
            holder.callButton = convertView.findViewById(R.id.contactCardCallButton);
            holder.removeButton = convertView.findViewById(R.id.contactCardRemoveButton);
            holder.editButton = convertView.findViewById(R.id.contactCardEditButton);
            result = convertView;
            convertView.setTag(holder);
        } else {
            holder = (ContactListAdapter.ViewHolder) convertView.getTag();
            result = convertView;
        }

//        Animation animation = AnimationUtils.loadAnimation(mContext,
//                (position > lastPosition) ? R.anim.load_down_anim : R.anim.load_up_anim);
//        result.startAnimation(animation);
        lastPosition = position;

        final String name = getItem(position).getName();
        final String phoneNumber = getItem(position).getPhoneNumber();
        Log.i(TAG, "Contact name: " + name + ", Phone: " + phoneNumber);
        holder.name.setText(name);
        holder.phoneNumber.setText(phoneNumber);

        if (user_type.equals("simpleUser")) {
            holder.removeButton.setVisibility(View.INVISIBLE);
            holder.editButton.setVisibility(View.INVISIBLE);
        } else {
            if (name.equals("משטרת ישראל | Israel Police") || name.equals("מגן דוד אדום | Magen David Adom")
                    || name.equals("מכבי אש | Fire Department") || name.equals("פיקוד העורף | Home Front Command")) {
                holder.removeButton.setVisibility(View.INVISIBLE);
                holder.editButton.setVisibility(View.INVISIBLE);
            }
        }

        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeSelectedContactFromContactsList(position);
                adapter.notifyDataSetChanged();
            }
        });
        holder.callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                mContext.startActivity(intent);
            }
        });
        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });


        return convertView;
    }

    public void removeSelectedContactFromContactsList(int position) {
        //TODO : FIX
        MongoClient mongoClient = new MongoClient("10.0.2.2", 27017);
        MongoDatabase database = mongoClient.getDatabase("SafeZone_DB");
        MongoCollection<Document> mongoCollection = database.getCollection("FavoriteShelters");
        Document shelterToRemove = new Document()
                .append("shelter_name", getItem(position).getName())
                .append("address", getItem(position).getPhoneNumber());

        mongoCollection.updateOne(eq("name", getItem(position).getName()), Updates.pull("favorite_shelters", shelterToRemove));

        cards.remove(position);

        Toast.makeText(mContext, "Removed from contacts list", Toast.LENGTH_LONG).show();
    }
}
