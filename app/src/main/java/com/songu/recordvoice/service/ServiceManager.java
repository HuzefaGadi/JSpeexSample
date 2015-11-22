package com.songu.recordvoice.service;

import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.samsung.sample.jspeex.JSpeexSampleActivity;
import com.samsung.sample.jspeex.LoginActivity;
import com.samsung.sample.jspeex.RegisterActivity;
import com.songu.recordvoice.doc.Globals;
import com.songu.recordvoice.model.UserModel;
import com.songu.recordvoice.model.VoiceModel;
import com.songu.recordvoice.util.HttpUtil;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Administrator on 10/16/2015.
 */
public class ServiceManager {

    public static void onLoginUser(String name,String password,String lat,String lon,final LoginActivity activity)
    {
        String url = Globals.m_baseUrl + Globals.m_loginUrl
                + "?user=" + name
                + "&password=" + password
                + "&lat=" + lat
                + "&lon=" + lon;

        HttpUtil.get(url, new AsyncHttpResponseHandler() {
            public void onFailure(Throwable paramThrowable) {
                String s = "fail";
            }

            public void onFinish() {
                String s = "finish";
            }

            public void onSuccess(String paramString) {  //that is return when success..
                try {
                    JSONObject localJSONObject1 = new JSONObject(paramString);
                    if (localJSONObject1.has("response"))
                    {
                        int code = localJSONObject1.getInt("response");
                        if (code == 200)
                        {
                            UserModel model = new UserModel();
                            model.mId = localJSONObject1.getString("id");
                            model.mName = localJSONObject1.getString("name");
                            model.mEmail = localJSONObject1.getString("email");
                            model.mPassword = localJSONObject1.getString("password");
                            model.mContacts = localJSONObject1.getString("contact");
                            model.mCalendar = localJSONObject1.getString("date");
                            Globals.mAccount = model;
                            activity.onSuccessLogin();
                        }
                        else
                        {

                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    /**
     * 
     */
    public static void onLogoutUser(String uid)
    {
    	String url = Globals.m_baseUrl + Globals.m_logoutUrl
                + "?uid=" + uid;
    	
        HttpUtil.get(url, new AsyncHttpResponseHandler() {
            public void onFailure(Throwable paramThrowable) {
                String s = "fail";
            }

            public void onFinish() {
                String s = "finish";
            }

            public void onSuccess(String paramString) {  //that is return when success..
                try {
                    JSONObject localJSONObject1 = new JSONObject(paramString);
                    if (localJSONObject1.has("response"))
                    {
                        
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void onRegisterUser(UserModel model,final RegisterActivity activity)
    {
        /*String url = Globals.m_baseUrl + Globals.m_registerUserUrl
                + "?user=" + model.mName
                + "&password=" + model.mPassword
                + "&email=" + model.mEmail
                + "&contact=" + model.mContacts
                + "&lat=" + model.mLat
                + "&long=" + model.mLong
                + "&size=" + String.valueOf(Globals.lstContact.size())
                + "&image=" + model.mImage;*/
         
    	String url = Globals.m_baseUrl + Globals.m_registerUserUrl;
        
        
        RequestParams req = new RequestParams();
    	req.put("user",model.mName);
    	req.put("password", model.mPassword);
    	req.put("email", model.mEmail);
    	req.put("cont",model.mContacts);
    	req.put("lat",model.mLat);
    	req.put("long", model.mLong);
    	req.put("image", model.mImage);
    	req.put("size", String.valueOf(Globals.lstContact.size()));
    	for (int i = 0;i < Globals.lstContact.size();i++)
    	{
    		/*url = url + "&cname" + String.valueOf(i) + "=" + Globals.lstContact.get(i).mName;
    		url = url + "&cphone" + String.valueOf(i) + "=" + Globals.lstContact.get(i).mPhone;
    		url = url + "&cemail" + String.valueOf(i) + "=" + Globals.lstContact.get(i).mEmail;*/
    		
    		req.put("cname" + String.valueOf(i), Globals.lstContact.get(i).mName);
    		req.put("cphone" + String.valueOf(i), Globals.lstContact.get(i).mPhone);
    		req.put("cemail" + String.valueOf(i), Globals.lstContact.get(i).mEmail);
    	}        
    	//HttpUtil.get(url, new AsyncHttpResponseHandler() {
    	HttpUtil.post(url,req, new AsyncHttpResponseHandler() {
            public void onFailure(Throwable paramThrowable) {
                String s = "fail";
            }

            public void onFinish() {
                String s = "finish";
            }

            public void onSuccess(String paramString) {  //that is return when success..
                try {
                    JSONObject localJSONObject1 = new JSONObject(paramString);
                    if (localJSONObject1.has("response"))
                    {
                        int code = localJSONObject1.getInt("response");
                        if (code == 200)
                        {
                            Toast.makeText(activity,"Register Success",Toast.LENGTH_SHORT).show();
                            activity.finish();
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
	
    public static  void onProfileImageSave(UserModel profile,final RegisterActivity activity)
    {
        if (profile.mImage.equals(""))
        {
            onRegisterUser(profile,activity);
        }
        else
        {
            PostImage m = new PostImage(profile,activity);
            m.postHttp();
        }
    }
    public static void onVoiceUpload(VoiceModel profile,String uid,final JSpeexSampleActivity activity)
    {
    	if (profile.mPath.equals(""))
        {
    		onRegisterData(profile,uid,activity);
        }
        else
        {
            PostVoice m = new PostVoice(profile,uid,activity);
            m.postHttp();
        }
    }
    public static void onRegisterData(VoiceModel model,String uid,final JSpeexSampleActivity activity)
    {
    	String url = Globals.m_baseUrl + Globals.m_registerUrl 
                + "?path=" + model.mPath
                + "&lat=" + model.mLat
                + "&log=" + model.mLong
    			+ "&uid=" + uid;
    	
    	
    	HttpUtil.get(url, new AsyncHttpResponseHandler() {    	
            public void onFailure(Throwable paramThrowable) {
                String s = "fail";
            }

            public void onFinish() {
                String s = "finish";
            }

            public void onSuccess(String paramString) {  //that is return when success..
                try {
                    JSONObject localJSONObject1 = new JSONObject(paramString);
                    if (localJSONObject1.has("response"))
                    {
                        int code = localJSONObject1.getInt("response");
                        if (code == 200)
                        {



                            Toast.makeText(activity,"Upload Success",Toast.LENGTH_SHORT).show();
                            String path = localJSONObject1.getString("answer");
                            activity.playAnswer(path);
                            activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    activity.mDlg.dismiss();
                                }
                            });

                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /*public static void onLoadComment(final CommentFragment fragment)
    {
        String url = Constants.c_loadCommentUrl;


        HttpUtil.get(url, new AsyncHttpResponseHandler() {
            public void onFailure(Throwable paramThrowable) {
                String s = "fail";
            }

            public void onFinish() {
                String s = "finish";
            }

            public void onSuccess(String paramString) {  //that is return when success..
                try {
                    JSONObject localJSONObject1 = new JSONObject(paramString);
                    if (localJSONObject1.has("response"))
                    {
                        int code = localJSONObject1.getInt("response");
                        if (code == 200) {
                            JSONArray commentArray = localJSONObject1.getJSONArray("comment");
                            Globals.lstComments.clear();
                            for (int i = 0; i < commentArray.length(); i++) {
                                JSONObject objectComment = commentArray.getJSONObject(i);
                                String name = objectComment.getString("name");
                                String content = objectComment.getString("content");
                                String date = objectComment.getString("date");
                                String image = objectComment.getString("image");

                                CommentModel model = new CommentModel();
                                model.mContent = content;
                                model.mName = name;
                                model.mDate = date;
                                model.mImage = image;
                                Globals.lstComments.add(model);
                                fragment.setData();
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public static  void onSendComment(CommentModel model,CommentFragment fragment)
    {
        String url = Constants.c_sendCommentUrl
                + "?user=" + model.mName
                + "&content=" + model.mContent
                + "&image=" + model.mImage;

        HttpUtil.get(url, new AsyncHttpResponseHandler() {
            public void onFailure(Throwable paramThrowable) {
                String s = "fail";
            }

            public void onFinish() {
                String s = "finish";
            }

            public void onSuccess(String paramString) {  //that is return when success..
                try {
                    JSONObject localJSONObject1 = new JSONObject(paramString);
                    if (localJSONObject1.has("response"))
                    {
                        int code = localJSONObject1.getInt("response");
                        if (code == 200)
                        {

                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }*/
}
