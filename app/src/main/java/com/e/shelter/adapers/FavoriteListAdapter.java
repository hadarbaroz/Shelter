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

import com.e.shelter.MapViewActivity;
import com.e.shelter.R;
import com.e.shelter.utilities.FavoriteCard;
import com.google.android.material.button.MaterialButton;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;

import org.bson.Document;

import java.util.ArrayList;

import static com.mongodb.client.model.Filters.eq;

public class FavoriteListAdapter extends ArrayAdapter<FavoriteCard> {

    private static final String TAG = "CustomListAdapter";

    private Context mContext;
    private int mResource;
    private int lastPosition = -1;
    private String mUserEmail;
    FavoriteListAdapter adapter;
    ArrayList<FavoriteCard> cards;

    /**
     * Holds variables in a View
     */
    private static class ViewHolder {
        TextView name;
        TextView address;
        MaterialButton navigateButton;
        MaterialButton removeButton;
    }

    /**
     * Default constructor for the PersonListAdapter
     * @param context
     * @param resource
     * @param objects
     */
    public FavoriteListAdapter(Context context, int resource, ArrayList<FavoriteCard> objects, String userEmail) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
        mUserEmail = userEmail;
        adapter = this;
        cards = objects;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final View result;
        final ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);
            holder = new ViewHolder();
            holder.name = convertView.findViewById(R.id.favCardShelterName);
            holder.address = convertView.findViewById(R.id.favCardAddress);
            holder.navigateButton = convertView.findViewById(R.id.favCardNavigateButton);
            holder.removeButton = convertView.findViewById(R.id.favCardRemoveButton);
            result = convertView;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        Animation animation = AnimationUtils.loadAnimation(mContext,
                (position > lastPosition) ? R.anim.load_down_anim : R.anim.load_up_anim);
        result.startAnimation(animation);
        lastPosition = position;

        String name = getItem(position).getName();
        String address = getItem(position).getAddress();
        Log.i(TAG, "Shelter name: " + name + ", Address: " + address);
        holder.name.setText(name);
        holder.address.setText(address);
        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeSelectedShelterFromFavorites(position);
                adapter.notifyDataSetChanged();
            }
        });
        holder.navigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri navigationIntentUri = Uri.parse("google.navigation:q=" + getItem(position).getAddress());//creating intent with latlng
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, navigationIntentUri).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);;
                mapIntent.setPackage("com.google.android.apps.maps");
                mContext.startActivity(mapIntent);
            }
        });

        return convertView;
    }

    public void removeSelectedShelterFromFavorites(int position) {
        MongoClient mongoClient = new MongoClient("10.0.2.2", 27017);
        MongoDatabase database = mongoClient.getDatabase("SafeZone_DB");
        MongoCollection<Document> mongoCollection = database.getCollection("FavoriteShelters");
        Document shelterToRemove = new Document()
                .append("shelter_name", getItem(position).getName())
                .append("address", getItem(position).getAddress())
                .append("lat", getItem(position).getLatitude())
                .append("lon", getItem(position).getLongitude());

        //removing shelter from DB
        mongoCollection.updateOne(eq("user_email", mUserEmail), Updates.pull("favorite_shelters", shelterToRemove));

        cards.remove(position);

        Toast.makeText(mContext, "Removed from favorites", Toast.LENGTH_LONG).show();
    }
}
