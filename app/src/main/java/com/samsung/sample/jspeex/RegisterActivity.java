package com.samsung.sample.jspeex;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.songu.recordvoice.adapter.AdapterCountry;
import com.songu.recordvoice.doc.Globals;
import com.songu.recordvoice.model.ContactModel;
import com.songu.recordvoice.model.UserModel;
import com.songu.recordvoice.service.ServiceManager;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.telephony.TelephonyManager;
import android.view.View;

public class RegisterActivity extends Activity implements View.OnClickListener, LocationListener
{
	
	public EditText editUser;
	public EditText editEmail;
	public EditText editPassword;
	
	public ImageView imgProfile;
	
	public Button btnRegister;
	public Button btnLogin;
	
	public Spinner spCountry;
	public AdapterCountry adapterCountry;
	
	public String imagePath = "";
	
	 // flag for GPS status
    boolean isGPSEnabled = false;
 
    // flag for network status
    boolean isNetworkEnabled = false;
 
    // flag for GPS status
    boolean canGetLocation = false;
 
    Location location; // location
    double latitude; // latitude
    double longitude; // longitude
    
    protected LocationManager locationManager;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
	
	
	
	@Override
	public void onCreate(final Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		initView();
		getContacts();
	}
	public void initView()
	{
		editUser = (EditText) this.findViewById(R.id.editRegisterName);
		editEmail = (EditText) this.findViewById(R.id.editRegisterEmail);
		editPassword = (EditText) this.findViewById(R.id.editRegisterPassword);
		this.spCountry = (Spinner) this.findViewById(R.id.spLoginCountry);
		imgProfile = (ImageView) this.findViewById(R.id.imgProfile);		
		btnRegister = (Button) this.findViewById(R.id.btnRegister);
		btnLogin = (Button) this.findViewById(R.id.btnLinkToLoginScreen);
		
		btnRegister.setOnClickListener(this);
		btnLogin.setOnClickListener(this);
		imgProfile.setOnClickListener(this);
				
		adapterCountry = new AdapterCountry();
		this.spCountry.setAdapter(adapterCountry);
		
		this.spCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() 
		{
			
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});
		adapterCountry.update(Globals.lstCountryCode);
	}
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {  //Get Image for Profile.. if you select image from gallery
            if (data != null) {
                Uri uri = data.getData();
                Bitmap bmp = Globals.setImageScale(this,uri);
                this.imgProfile.setImageBitmap(bmp);  //set bitmap to imageview
                imagePath = Globals.getRealPathFromURI(this,uri);  //image path saved
            }
        }
    }
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.imgProfile:
			Intent intent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(
                    Intent.createChooser(intent, "Select File"),1);
			break;
		case R.id.btnRegister:
			String name = this.editUser.getText().toString();
			String pass = this.editPassword.getText().toString();
			String email = this.editEmail.getText().toString();
			//String location = Globals.lstCountryCode.get(this.spCountry.getSelectedItemPosition()).mName;
			
			if (!name.equals("") && !pass.equals("") && !email.equals(""))
			{
				UserModel model = new UserModel();
				model.mName = name;
				model.mPassword = pass;
				model.mEmail = email;
				model.mContacts = getPhoneNumber();
				//model.mLocation = location;
				Location m = getLocation();
		        if (m != null)
		        {
			        model.mLat = String.valueOf(m.getLatitude());
			        model.mLong = String.valueOf(m.getLongitude());
		        }
				model.mImage = imagePath;
				ServiceManager.onProfileImageSave(model, this);
				//ServiceManager.onRegisterUser(model, this);
			}
			break;
		case R.id.btnLinkToLoginScreen:
			finish();
			break;
		}
	}
	@Override
 	protected Dialog onCreateDialog(int id) {
 	   // TODO Auto-generated method stub
 	   if (id == 999) {
 	      return new DatePickerDialog(this, myDateListener, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
 	   }
 	   return null;
 	}
	private DatePickerDialog.OnDateSetListener myDateListener 
 	= new DatePickerDialog.OnDateSetListener() {
 	   @Override
 	   public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
 		  SimpleDateFormat formatter = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");//isNotEmpty
		  Date currentTime = new Date ( );
		  int month = currentTime.getMonth();
		  int day = currentTime.getDate();
		  int year = currentTime.getYear();
		  year = year - 100 + 2000;		  
		  String m_currentDate = String.valueOf(arg1) + "-" + String.valueOf(arg2 + 1) + "-" + String.valueOf(arg3);
 	   }
 	};
 	

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}
	public String getPhoneNumber()
	{
		TelephonyManager tMgr = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
		if (tMgr != null)
		{
			String mPhoneNumber = tMgr.getLine1Number();
			return mPhoneNumber;
		}
		return "";
	}
	public Location getLocation() {
        try {
            locationManager = (LocationManager) this
                    .getSystemService(LOCATION_SERVICE);
 
            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
 
            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
 
            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, RegisterActivity.this);
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, RegisterActivity.this);
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }
 
        } catch (Exception e) {
            e.printStackTrace();
        }
 
        return location;
    }
	public void getContacts()
	{
		Globals.lstContact.clear();
		ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if (cur.getCount() > 0) {
	    while (cur.moveToNext()) {
	    	ContactModel model = new ContactModel();
	        String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
	        String name = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
	        model.mName = name;
	        
	        if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) 
	        {
	        	Cursor pCur = cr.query(
	         		    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
	         		    null, 
	         		    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", 
	         		    new String[]{id}, null);
	         		    pCur.moveToNext();
	        			String phone = pCur.getString(
	        					pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
	        			model.mPhone = phone;
	         	        pCur.close();
	         	        Cursor emailCur = cr.query(
	                            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
	                            null,
	                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
	                            new String[]{id}, null);
	         	       if (emailCur.getCount() > 0)
	         	       {
		         	       emailCur.moveToNext();
		         	       String email = emailCur.getString(
	                               emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
		         	       String emailType = emailCur.getString(
	                               emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
		         	       model.mEmail = email;
	         	       }
	                   emailCur.close();
 	        }
	        Globals.lstContact.add(model);
        }
	    
 	}
	}
}
