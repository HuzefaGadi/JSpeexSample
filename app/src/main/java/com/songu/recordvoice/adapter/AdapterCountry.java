package com.songu.recordvoice.adapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;




import com.samsung.sample.jspeex.R;
import com.songu.recordvoice.model.CountryCodeModel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class AdapterCountry extends BaseAdapter
{
	  List<CountryCodeModel> m_lstEvent;
	  
	  public AdapterCountry()
	  {
		  super();		  
	  }
	  public int getCount()
	  {
	    if (this.m_lstEvent == null)
	      return 0;
	    return this.m_lstEvent.size();
		  
	  }

	  public CountryCodeModel getItem(int paramInt)
	  {
	    return this.m_lstEvent.get(paramInt);
	  }

	  public long getItemId(int paramInt)
	  {
	    return 0L;
	  }

	  public View getView(int paramInt, View paramView, ViewGroup paramViewGroup)
	  {	    
		  
		View localView = paramView;
		ViewHolder localViewHolder = null;
		
		if (localView == null)
	    {
	      localView = LayoutInflater.from(paramViewGroup.getContext()).inflate(R.layout.item_textview, null);
	     
	    }
	    else 
	    {
	    	localViewHolder = (ViewHolder) localView.getTag();
	    }
	    if (localViewHolder == null)
	    {
	    	  localViewHolder = new ViewHolder();
		      localViewHolder.txtCode = ((TextView)localView.findViewById(R.id.itemSingleText));		      
		            
		      localView.setTag(localViewHolder);
	    }
	    
	    
	    localViewHolder.txtCode.setText(this.m_lstEvent.get(paramInt).mName);
	    localView.setTag(localViewHolder);
	    return localView;
	    
	  }

	  public void update(List<CountryCodeModel> tickets)
	  {
		  this.m_lstEvent = tickets;
		  notifyDataSetChanged();
	  }

	  static class ViewHolder
	  {			  
		  public TextView txtCode;		 
	  }
	
}
