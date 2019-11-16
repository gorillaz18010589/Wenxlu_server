package tw.org.iii.appps.androidwenxlufood.Common;
//createBitmap(int width, int height, @NonNull Bitmap.Config config):(回傳值Bitmap )
//Bitmap.createBitmap(int width, int height, @NonNull Bitmap.Config config):(回傳值Bitmap )
//Bitmap.getWidth():取得視圖寬
//Bitmap.getHeight()://取得試圖高
//Matrix.setScale(float sx, float sy, float px, float py)://設置Matrix以pivaotX,pivaoty為軸心進行縮放，scaleX,scaleY控制X,Y方向上的缩放比例；

//Canvas.Canvas(@NonNull Bitmap bitmap)://設定圖紙為()
//Canvas.setMatrix(@Nullable Matrix matrix)://設定縮放為()
//Canvas.drawBitmap(@NonNull Bitmap bitmap, float left, float top, @Nullable Paint paint)://開始畫圖(1.像素,2.0,3.0,4畫筆)
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import tw.org.iii.appps.androidwenxlufood.Model.Request;
import tw.org.iii.appps.androidwenxlufood.Model.User;
import tw.org.iii.appps.androidwenxlufood.Remote.IGeoCoordinates;
import tw.org.iii.appps.androidwenxlufood.Remote.RetrofitClient;

public class Common {
    public static User currentUser;
    public static Request currentRequest;
    public static final String UPDATE = "Update";
    public static final String DELETE = "Delete";

    public static final int PICK_IMAGE_REQUEST = 71;
    public static final int CAMERA_REQUEST_CODE = 1;//自己設定的相機回應馬

    public static final String baseUrl = "https://maps.googleapis.com";


    //改變訂單狀方法
    public static String convertCodeToStatus(String code) {
        if (code.equals("0")) {

            return "Placed";

        } else if (code.equals("1")) {

            return "On My Way";

        } else {

            return "Shipped";
        }
    }

    // <T> T create(final Class<T> service):創建伺服器(泛型資料結構伺服器)(回傳泛型資料)
    //取得GeocodeService方法
    public static IGeoCoordinates getGeocodeService() {
        return RetrofitClient
                .getClient(baseUrl)//自己寫的取得Client方法
                .create(IGeoCoordinates.class);//創建

    }


    //畫線辦法
    public static Bitmap scaleBitmap(Bitmap bitmap ,int newWidth, int newHeight){
        //圖紙像素
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth,newHeight,Bitmap.Config.ARGB_8888);//設定畫圖紙(1.圖寬,2.圖高,3像素種類)

        //線
        float scaleX = newWidth / (float)bitmap.getWidth(); //x點連成線
        float scaleY = newHeight / (float)bitmap.getHeight();//y點連成線
        float pivaotX =0 , pivaoty=0 ; //以0,0為縮放中心位置

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX,scaleY,pivaotX,pivaoty);//設置Matrix以pivaotX,pivaoty為軸心進行縮放，scaleX,scaleY控制X,Y方向上的缩放比例；

        //畫圖設定
        Canvas canvas = new Canvas(scaledBitmap); //設定圖紙為(scaleBitmap)
        canvas.setMatrix(scaleMatrix);//設定縮放為scaleMatrix
        canvas.drawBitmap(bitmap,0,0,new Paint(Paint.FILTER_BITMAP_FLAG));//開始畫圖(1.像素,2.0,3.0,4畫筆)
        return  scaledBitmap;
    }
}



