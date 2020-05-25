package com.example.whatsapp.api;

import com.example.whatsapp.model.DataNotification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface NotificationService {
    @Headers({
            "Authorization:key=AAAAoGQJS1w:APA91bFJSaw9mlKMfv_RxnK8ZBfL3x2QuRTbPwzLD85CEI54AI1nvHdhosjAZZhqD13NDclUVbtkRy0KkynqqJbO5yPh7wk8Za5K8iBouHA3TmrhwXQuGLUudwU3TONyGO-geoVYrzWw",
            "Content-Type:application/json"
    })
    @POST("send")
    Call<DataNotification> saveNotification(@Body DataNotification dataNotification);
}
