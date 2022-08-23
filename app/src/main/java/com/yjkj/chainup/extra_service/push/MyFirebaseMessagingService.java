package com.yjkj.chainup.extra_service.push;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

//继承FirebaseMessagingService
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private String mToken;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("我是启动服务","");

    }

    //获取到谷歌到token
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        sendRegistrationToServer(token);
    }

    //  回传给服务器操作
    void sendRegistrationToServer(String token) {
        mToken = token;


    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);


    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);

    }

    @Override
    public void onSendError(String s, Exception e) {
        super.onSendError(s, e);
    }
}
