package com.yjkj.chainup.extra_service.push;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

//继承FirebaseMessagingService
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private String mToken;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    //获取到谷歌到token
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        sendRegistrationToServer(token);
    }

    //  回传给服务器操作
    private void sendRegistrationToServer(String token) {

        mToken = token;


    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);


//
//        //这个应该可以看懂
//        if (remoteMessage.getNotification() != null && remoteMessage.getNotification().getBody() != null) {
//            sendNotification(getApplicationContext(), remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
//        } else {
//            sendNotification(getApplicationContext(), remoteMessage.getData().get("title"), remoteMessage.getData().get("body"));
//        }
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

//    private void sendNotification(Context iContext, String messageTitle, String messageBody) {
//
//
//        //跳转到你想要跳转到页面
//        Intent intent = new Intent(this, NotificationActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                PendingIntent.FLAG_ONE_SHOT);
//
//        String channelId = getString(R.string.default_notification_channel_id);
//        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        NotificationCompat.Builder notificationBuilder =
//                new NotificationCompat.Builder(this, channelId)
//                        .setTicker(messageTitle)//标题
//                        .setSmallIcon(R.mipmap.appicon)//你的推送栏图标
//                        .setContentTitle("notification")
//                        .setContentText(messageBody)//内容
//                        .setAutoCancel(true)
//                        .setSound(defaultSoundUri)
//                        .setContentIntent(pendingIntent);
//
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//
//        //判断版本
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(channelId,
//                    "notification",
//                    NotificationManager.IMPORTANCE_DEFAULT);
//            notificationManager.createNotificationChannel(channel);
//        }
//
//        //这里如果需要的话填写你自己项目的，可以在控制台找到，强转成int类型
//        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
//
//
//    }