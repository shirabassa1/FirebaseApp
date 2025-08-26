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

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        view = inflater.inflate(R.layout.shopping_list_lv, viewGroup, false);

        TextView txtName = (TextView) view.findViewById(R.id.listTxtName),
                txtQuantity = (TextView) view.findViewById(R.id.listTxtQuantity),
                txtPrice = (TextView) view.findViewById(R.id.listTxtPrice),
                txtBought = (TextView) view.findViewById(R.id.listTxtBought);
        Item item = list.get(i);

        txtName.setText(item.getName());
        txtQuantity.setText(String.valueOf(item.getQuantity()));
        txtPrice.setText(String.valueOf(item.getPrice()));
        txtBought.setText(String.valueOf(item.isBought()));

        return view;
    }
}
