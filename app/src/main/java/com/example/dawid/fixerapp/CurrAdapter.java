package com.example.dawid.fixerapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class CurrAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<CurrencyItem> mCurrencyData = new ArrayList<CurrencyItem>();

    final private ListItemClickListener mOnClickListener;

    public interface ListItemClickListener{
        void onListItemClick(CurrencyItem curr_item);
    }

    public CurrAdapter(ArrayList<CurrencyItem> currencyData, ListItemClickListener listener) {
        mCurrencyData = currencyData;
        mOnClickListener = listener;
    }

    public void setCurrencyData(ArrayList<CurrencyItem> currencyData){
        mCurrencyData = currencyData;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

//        Check whether the viewholder is a date separator or a currency
        if (viewType == 0){
            View view0 = inflater.inflate(R.layout.date_list_item, viewGroup, false);
            DateViewHolder dViewHolder = new DateViewHolder(view0);
            return dViewHolder;

        }else{
            View view1 = inflater.inflate(R.layout.currency_list_item, viewGroup, false);
            CurrViewHolder cViewHolder = new CurrViewHolder(view1);
            return cViewHolder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

//        Check whether the viewholder is a date separator or a currency
        if (holder.getItemViewType() == 0){
            DateViewHolder viewHolder0 = (DateViewHolder) holder;
            CurrencyItem currentDate = mCurrencyData.get(position);
            viewHolder0.currDate.setText(currentDate.getDate());
        } else{
            CurrViewHolder viewHolder1 = (CurrViewHolder) holder;
            CurrencyItem currentItem = mCurrencyData.get(position);

            viewHolder1.currName.setText(currentItem.getCurrency());
            viewHolder1.currValue.setText(currentItem.getValue());
        }
    }

    @Override
    public int getItemCount() {
        return mCurrencyData.size();
    }

    @Override
    public int getItemViewType(int position) {

//        If the item has "DATE SEPARATOR" name the view type is DateViewHolder, else it's CurrViewHolder
        if (mCurrencyData.get(position).getCurrency() == "DATE SEPARATOR"){
            return 0;
        } else {
            return 1;
        }
    }

//    ViewHolder for currency item
    class CurrViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView currName;
        TextView currValue;
        TextView currDate;

        public CurrViewHolder(View itemView){
            super(itemView);

            currName = (TextView) itemView.findViewById(R.id.curr_name);
            currValue = (TextView) itemView.findViewById(R.id.curr_value);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view){
            int clickedPosition = getAdapterPosition();
            CurrencyItem clickedItem = mCurrencyData.get(clickedPosition);
            mOnClickListener.onListItemClick(clickedItem);
        }
    }

//    ViewHolder for date separator
    class DateViewHolder extends RecyclerView.ViewHolder{
        TextView currDate;

        public DateViewHolder(View itemView){
            super(itemView);
            currDate = (TextView) itemView.findViewById(R.id.curr_date);
        }
    }
}
