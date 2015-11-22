package com.songu.recordvoice.service;


import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import com.samsung.sample.jspeex.RegisterActivity;
import com.songu.recordvoice.doc.Globals;
import com.songu.recordvoice.model.UserModel;

@TargetApi(Build.VERSION_CODES.CUPCAKE) public class PostImage {  //Image Post Class for Attachment
    public boolean isRunning = false;
    public int l_m;
    public TimerTask task;
    public  Timer mTimer;
    public PostClass posting;
    public RegisterActivity activity;
    public String file;
    public UserModel data1;
    public String ff;
    public String url;
    public String name;
    public int mode; //0:Personal,1:Group

    public PostImage(UserModel profile, RegisterActivity activity)
    {
        this.data1 = profile;
        this.activity = activity;
        mode = 0;
        url = profile.mImage;
        name = profile.mName;
    }
    public void postHttp()
    {
        isRunning = true;

        posting =  new PostClass();
        posting.execute("");
    }


    public class PostClass extends AsyncTask<String, Void, Boolean> {
        String responseString = "";
        boolean recording = false;
        @Override
        protected void onPreExecute() {

            super.onPreExecute();

        }
        public int uploadFile(String sourceFileUri) {
            int serverResponseCode = 0;
            String serverUrl = Globals.m_baseUrl + "profileupload.php";
            String fileName = name;

            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            File sourceFile = new File(sourceFileUri);

            if (!sourceFile.isFile()) {

                Toast.makeText(activity, "File does not exist", Toast.LENGTH_SHORT).show();
                return 0;
            }
            else
            {
                try {
                    String filenameArray[] = sourceFileUri.split("\\.");
                    String extension = filenameArray[filenameArray.length-1];
                    fileName = fileName + "." +extension;
                    ff = fileName;
                    // open a URL connection to the Servlet
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL(serverUrl);

                    // Open a HTTP  connection to  the URL
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    conn.setRequestProperty("uploaded_file", fileName);
                    dos = new DataOutputStream(conn.getOutputStream());
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                            + fileName + "\"" + lineEnd);

                    dos.writeBytes(lineEnd);

                    // create a buffer of  maximum size
                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {

                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    }

                    // send multipart form data necesssary after file data...
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    // Responses from the server (code and message)
                    serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();
                    String ss1 = (String) conn.getContent();
                    int i = 1;

                    if(serverResponseCode == 200){

                    }

                    //close the streams //
                    fileInputStream.close();
                    dos.flush();
                    dos.close();

                } catch (MalformedURLException ex) {

                    ex.printStackTrace();
                } catch (Exception e) {


                }
                return serverResponseCode;

            } // End else block
        }
        @Override
        protected Boolean doInBackground(String... urls) {
            uploadFile(url);
            return true;
        }
        @Override
        protected void onPostExecute(Boolean isSuccess) {
            if(isSuccess){
                    data1.mImage = Globals.m_baseUrl + "image/" + ff;
                    ServiceManager.onRegisterUser(data1, activity);
            }
        }
    }
}
