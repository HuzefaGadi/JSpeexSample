package com.songu.recordvoice.doc;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import com.songu.recordvoice.db.DBManager;
import com.songu.recordvoice.model.ContactModel;
import com.songu.recordvoice.model.CountryCodeModel;
import com.songu.recordvoice.model.UserModel;

public class Globals {
    //public static String m_baseUrl = "http://hellome.ddns.net/";
    //public static String m_baseUrl = "http://192.168.1.15/";
	public static String m_baseUrl = "http://70.166.96.29/test/";
	//public static String m_baseUrl = "http://ywc1209.xicp.net:88/record/";
	public static String m_uploadUrl = "voiceupload.php";
	public static String m_registerUrl = "registerData.php";
	public static String m_registerUserUrl = "useradd.php";
	public static String m_logoutUrl = "logout.php";
	public static String m_loginUrl = "login.php";
	
	public static List<CountryCodeModel> lstCountryCode = new ArrayList<CountryCodeModel>();
	//public static DBManager g_dbManager;
	public static UserModel mAccount = new UserModel();
	public static List<ContactModel> lstContact = new ArrayList<ContactModel>();
	
	public static String getRealPathFromURI(Context act, Uri contentURI)  //that is get Real path in Phone....
    {
        Cursor cursor = act.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }

    public static Bitmap setImageScale(Context activity,Uri imageUri)  //Set Image Scale for Bitmap
    {
        String path = Globals.getRealPathFromURI(activity,imageUri);
        File file = new File(path);
        Bitmap bitmap = decodeFile(file);
        Bitmap bt=Bitmap.createScaledBitmap(bitmap, 400, 300, false);
        return bt;
    }

    public static Bitmap decodeFile(File f)  //Decode File
    {
        Bitmap b = null;
        int IMAGE_MAX_WIDTH = 800;
        int IMAGE_MAX_HEIGHT = 600;
        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        FileInputStream fis;
        try
        {
            //fis = new FileInputStream(f);
            try
            {
                BitmapFactory.decodeFile(f.getAbsolutePath(), o);

                //fis.close();
                //fis = null;
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            int scale = 1;

            if (o.outHeight > IMAGE_MAX_WIDTH || o.outWidth > IMAGE_MAX_WIDTH)
            {
                int maxwh = Math.max(o.outWidth,o.outHeight);
                while(maxwh / scale > IMAGE_MAX_WIDTH)
                    scale *= 2;
            }


            //Decode with inSampleSize

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            o2.inJustDecodeBounds = false;
            o2.inPurgeable = true;

            //fis = new FileInputStream(f);

            try
            {
                b = BitmapFactory.decodeFile(f.getAbsolutePath(), o2);

                //fis.close();
                //fis = null;
            }
            catch (Exception e)
            {
                //	TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return b;
    }
}
