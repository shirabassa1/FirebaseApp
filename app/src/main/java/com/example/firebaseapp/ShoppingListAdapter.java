package com.example.firebaseapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ShoppingListAdapter extends BaseAdapter
{
    private Context contex;
    private ArrayList<Item> list;
    private LayoutInflater inflater;

    public ShoppingListAdapter(Context contex, ArrayList<Item> list)
    {
        this.contex = contex;
        this.list = list;
        inflater = LayoutInflater.from(contex);
    }

    public void updateList(ArrayList<Item> newList)
    {
        this.list = newList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount()
    {
        return list.size();
    }

    @Override
    public Object getItem(int i)
    {
        return list.get(i);
    }

    @Override
    public long getItemId(int i)
    {
        return i;
    }

    static class ViewHolder {
        TextView txtName;
        TextView txtQuantity;
        TextView txtPrice;
        TextView txtBought;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;

        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.shopping_list_lv, parent, false);

            holder = new ViewHolder();
            holder.txtName = convertView.findViewById(R.id.listTxtName);
            holder.txtQuantity = convertView.findViewById(R.id.listTxtQuantity);
            holder.txtPrice = convertView.findViewById(R.id.listTxtPrice);
            holder.txtBought = convertView.findViewById(R.id.listTxtBought);

            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        Item item = list.get(position);
        holder.txtName.setText(item.getName());
        holder.txtQuantity.setText(String.valueOf(item.getQuantity()));
        holder.txtPrice.setText(String.valueOf(item.getPrice()));
        holder.txtBought.setText(String.valueOf(item.isBought()));

        return convertView;
    }
}
