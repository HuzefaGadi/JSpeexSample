package com.samsung.sample.jspeex;

import com.songu.recordvoice.db.DBManager;
import com.songu.recordvoice.doc.Globals;
import com.songu.recordvoice.service.ServiceManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class LoginActivity extends Activity implements View.OnClickListener, LocationListener{
	
	
	public EditText editUser;
	public EditText editPassword;
	public Button btnLogin;
	public Button btnRegister;
	
	
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
		SharedPreferences sp = this.getSharedPreferences("login", MODE_PRIVATE);
		boolean isLogin = sp.getBoolean("login1", false);
		if (isLogin)
		{
			Intent m = new Intent(this,JSpeexSampleActivity.class);
			this.startActivity(m);
			finish();
			return;
		}
		setContentView(R.layout.activity_login);
		//Globals.g_dbManager = new DBManager(this);
		initView();
	}
	
	public void initView()
	{
		editUser = (EditText) this.findViewById(R.id.email);
		editPassword = (EditText) this.findViewById(R.id.password);
		btnLogin = (Button) this.findViewById(R.id.btnLogin);
		btnRegister = (Button) this.findViewById(R.id.btnLinkToRegisterScreen);
		
		btnLogin.setOnClickListener(this);
		btnRegister.setOnClickListener(this);
		
	}
	public void onSuccessLogin()
	{
		SharedPreferences sp = this.getSharedPreferences("login", MODE_PRIVATE);
		String user = this.editUser.getText().toString();
		String pass = this.editPassword.getText().toString();
		sp.edit().putBoolean("login1", true).apply();
		sp.edit().putString("user", user).apply();
		sp.edit().putString("pass", pass).apply();		
		sp.edit().putString("uid", Globals.mAccount.mId).apply();
		Intent m = new Intent(this,JSpeexSampleActivity.class);
		this.startActivity(m);
		finish();
	}
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch(arg0.getId())
		{
			case R.id.btnLogin:
				String user = this.editUser.getText().toString();
				String pass = this.editPassword.getText().toString();
				if (!user.equals("") && !pass.equals(""))
				{
					Location m = this.getLocation();
					String lat = "";
					String lon = "";
					if (m != null)
			        {
				        lat = String.valueOf(m.getLatitude());
				        lon = String.valueOf(m.getLongitude());
			        }
					ServiceManager.onLoginUser(user, pass,lat,lon, this);
				}
				break;
			case R.id.btnLinkToRegisterScreen:
				Intent m = new Intent(this,RegisterActivity.class);
				this.startActivity(m);
				break;
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
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
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, LoginActivity.this);
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
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, LoginActivity.this);
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
	

}
