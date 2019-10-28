package tw.org.iii.appps.androidwenxlufood;
//1.new google map
//2.在Res複製網址https://console.developers.google.com/flows/enableapi?apiid=maps_android_backend&keyType=CLIENT_SIDE_ANDROID&r=AA:95:0D:09:E3:53:0B:CC:DA:8E:AA:37:41:A1:F1:A7:3F:AC:E1:27%3Btw.org.iii.appps.androidwenxlufood
//3.建立專案選擇了app_service
//4.在OrderStatus設置Intent到這頁,並且創一個Common.currentRequest
//5.實作OnMapReadyCallback,當地圖讀取時
//6.在實作三個GoogleApiClient.ConnectionCallbacks(客戶端連線回呼),GoogleApiClient.OnConnectionFailedListener(客戶端線失敗監聽),LocationListener(Gps監聽)
//7.加入location api
//implementation 'com.google.android.gms:play-services-maps:17.0.0' //googlemap api
//implementation 'com.google.android.gms:play-services-location:17.0.0' //location api
//8.檔案總管家上gps權限
//做到13可以連線抓到你的gps後
//我們將會取得你訂單的地址經緯度,並且畫線移動到目標地
//將使用Retrofit to http 客戶端
//加入api
//創建IGeo介面,跟RetofitClient.Class
//到Common的新增
//<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />精準gps權限
//<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>粗略gps權限


//getMapAsync(OnMapReadyCallback var1):
//addMarker(MarkerOptions var1)://設定標記(MarkerOptions())(回傳Marker)
//position(LatLng var1)://設定位置(經緯度)(回傳MarkerOptions)
//moveCamera(CameraUpdate var1)://移動地圖(CameraUpdateFactory)
//newLatLng(LatLng var0):設定經度緯度(經緯度物件)(回傳值CameraUpdate )
//title(@Nullable String var1):設定標記標題(回傳值MarkerOptions)
//LatLng(@Param(id = 2) double var1, @Param(id = 3) double var3)://設定經度緯度(經度,緯度)

//isGooglePlayServicesAvailable(Context var0):檢查用戶的google是否有支援(回傳值static int)
//Dialog getErrorDialog(int var0, Activity var1, int var2)
//ConnectionResult.SUCCESS://連線結果物件實體.成功型別
//isUserRecoverableError(int var0)://判斷使用者連線的狀態(支援的Request int型別)(回傳boolean)
//getErrorDialog(int var0, Activity var1, int var2)://取得錯誤的訊息用Dialog方式(1.錯誤訊息瑪,2.頁面3.回應瑪)(回傳值Dialog)

//addConnectionCallbacks(GoogleApiClient.ConnectionCallbacks var1)://新增一個客戶端連線回應(回傳值GoogleApiClient.Builder )
//addOnConnectionFailedListener(@NonNull GoogleApiClient.OnConnectionFailedListener var1)://新增一個客戶端連線失敗監聽(回傳值GoogleApiClient.Builder )
//addApi(Api<? extends NotRequiredOptions> var1)://新增api(LocationServices.API這邊設定為gps伺服器端api )(回傳值GoogleApiClient.Builder)

//LocationRequest setInterval(long var1)://設定讀取位置資訊的間隔時間為一（1000ms）
//LocationRequest setFastestInterval(long var1):// 設定讀取位置資訊最快的間隔時間為5秒（5000ms）
//LocationRequest setPriority(int var1)://設定優先讀取高精確度的位置資訊（GPS）(LocationRequest.PRIORITY_HIGH_ACCURACY)
//LocationRequest.PRIORITY_HIGH_ACCURACY//高精確度的位置資訊
//LocationRequest setSmallestDisplacement(float var1):設定位移

//Location getLastLocation(GoogleApiClient var1)://取得最後gps位置
//getLatitude()://取得緯度(回傳值double )
//getLongitude(): //取得經度(回傳值double )


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tw.org.iii.appps.androidwenxlufood.Common.Common;
import tw.org.iii.appps.androidwenxlufood.Common.DirectionJSONParser;
import tw.org.iii.appps.androidwenxlufood.Remote.IGeoCoordinates;


public class TrackingOrderMapsActivity extends FragmentActivity implements OnMapReadyCallback ,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private GoogleMap mMap; //地圖顯示物件

    private final static int LOCATION_PERMISSION_REQUEST = 1001; //gps權限回應馬自己定義為1001
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation; //gps最後連線位置

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest; //gps回應請求

    private static int UPDATE_INTERVAL = 1000;//1秒
    private static int FATEST_INTERVAL = 5000;//5秒
    private static int DISPLACEMENT = 10;//位移10

    private IGeoCoordinates mService;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_order_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        //14.取得IGeoCoordinates Service
        mService = Common.getGeocodeService(); //取得Service


        //2.初始化時詢問gps權限,如果ACCESS_FINE_LOCATION,ACCESS_COARSE_LOCATION都沒有開那就去要
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {//沒有開權限的話,去要
            requestRuntimePermission();
        }else
            {//有權限的話

            if (checkPlayService()) { //如果使用者有google Service
                buildGoogleApiClient(); //創建一個跟使用者gps連線的客戶端
                crateLocationRequest();
            }
        }

        displayLocation();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()//取得協助Fragment經理人,轉型成SupportMapFragment
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);//取得地圖實作

    }

    //FusedLocationApi:(回傳值FusedLocationProviderApi )

    //8.取得用戶的Gps經度緯度,並設定標記,跟移動地圖zoon to
    private void displayLocation() {
        //詢問gps權限,如果ACCESS_FINE_LOCATION,ACCESS_COARSE_LOCATION都沒有開那就去要
       if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {//沒有開權限的話,去要
            requestRuntimePermission();
        }else {//有權限的話
           mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);//取得最後的gps位置(客戶的gps)
           if(mLastLocation!= null) //如果客戶GPS有近來
           {
               double latitude =  mLastLocation.getLatitude(); //取得緯度
               double longitude = mLastLocation.getLongitude(); //取得經度

               LatLng yourLocation = new LatLng(latitude,longitude);//設定緯度精度
               mMap.addMarker(new MarkerOptions().position(yourLocation).title("Your Location"));//創造一個新標記位置是你的經緯度,設定標題
               mMap.moveCamera(CameraUpdateFactory.newLatLng(yourLocation));//移動地圖到你設定的經緯度
               mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));

               //add Maker for your Location , and Maker for your Order and draw loute
               drawRoute(yourLocation,Common.currentRequest.getAddress());//畫地圖方法(1.你的gps位置,2.Oders的訂單地址)



           }else{//沒有連上客戶端的gps
                Toast.makeText(this,"Couldn't get location",Toast.LENGTH_SHORT).show();
           }
        }
    }

    //15.畫地圖方法
    private void drawRoute(final LatLng yourLocation , String address){
        mService.getGeoCode(address).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try{

                    JSONObject jsonObject = new JSONObject(response.body().toString());

                    String lat = ((JSONArray)jsonObject.get("results"))
                            .getJSONObject(0)
                            .getJSONObject("geometry")
                            .getJSONObject("location")
                            .get("lat").toString();

                    String lng = ((JSONArray)jsonObject.get("results"))
                            .getJSONObject(0)
                            .getJSONObject("geometry")
                            .getJSONObject("location")
                            .get("lng").toString();


                    //設定經緯度
                    LatLng orderLocation = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));

                    //設定圖片
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.home);
                    bitmap = Common.scaleBitmap(bitmap,70,70);

                    // MarkerOptions.icon(@Nullable BitmapDescriptor var1)
                    //設定Marke
                    MarkerOptions maker = new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(bitmap))//設定圖片為(bitmap Home圖案)
                            .title("Oder of " +Common.currentRequest.getPhone())//設定標題名為(用戶的電話)
                            .position(orderLocation);//設定位置為(用戶的地址經緯度)
                    mMap.addMarker(maker);//地圖設置標記

                    //畫線
                    mService.getDirections(yourLocation.latitude+"," +yourLocation.longitude,
                                            orderLocation.latitude+"," + orderLocation.longitude)
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    new ParserTask().execute(response.body().toString());
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {

                                }
                            });

                }catch (Exception e){
                    Log.v("brad","錯誤例外" + e.toString());
                }
                Log.v("brad","有標記家畫線");
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    //7.建立Location請求物件
    private void crateLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);//設定讀取位置資訊的間隔時間為一（1000ms）
        mLocationRequest.setFastestInterval(FATEST_INTERVAL); // 設定讀取位置資訊最快的間隔時間為一秒（1000ms）
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //設定優先讀取高精確度的位置資訊（GPS）
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);//設定最小位移
    }

    //6.創建一個連線客戶端gps方法
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)//新增一個客戶端連線回應
                .addOnConnectionFailedListener(this)//新增一個客戶端連線失敗監聽
                .addApi(LocationServices.API).build();//新增api(LocationServices.API這邊設定為gps伺服器端api )
        mGoogleApiClient.connect();//客戶端連線

    }

    //5.檢查使用者伺服器是否支援Google Service
    private boolean checkPlayService() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this); //檢查用戶的google是否有支援(this頁面)(回傳支援結果int)
            if(resultCode !=ConnectionResult.SUCCESS)//如果支援的結果不是成功的
            {
                //如果使用者沒有支援Google Service的話
                if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))//判斷使用者連線的狀態(支援的Request int型別)(回傳boolean)
                {   //顯示錯誤的訊息用Dialog方式
                    GooglePlayServicesUtil.getErrorDialog(resultCode, this,PLAY_SERVICES_RESOLUTION_REQUEST).show();
                }
                else {
                    Toast.makeText(this,"this is drvice not support",Toast.LENGTH_SHORT).show();
                    finish();
                }
                return  false;
            }
        return true;
    }



    //3.要權限方法
    private void requestRuntimePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST);
    }

    //4.當權限回應結果馬一樣時,近來處理
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case  LOCATION_PERMISSION_REQUEST:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){//如果權限果確實有且權限地0個且權限授予的話
                    if (checkPlayService()){
                        buildGoogleApiClient();
                        crateLocationRequest();

                        displayLocation();
                    }
                }
                break;
        }

    }

    //1.當地圖讀取時(OnMapReadyCallback實作方法)
  @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        LatLng sydney = new LatLng(-34, 151); //設定經度緯度
//        mMap.addMarker(new MarkerOptions() //設定標記(MarkerOptions())
//                .position(sydney)//設定位置(經緯度)
//                .title("Marker in Sydney"));//設定標記標題
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));//移動地圖視野(CameraUpdateFactory.的物件)
        Log.v("brad","onMapReady=> " +"GoogleMap:" + googleMap);

    }
    //9.已經連線到Google Services 啟動用戶gps的經緯度 (GoogleApiClient.ConnectionCallbacks實做方法)
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();//啟動用戶gps的經緯度
        startLoactionUpdates();
        Log.v("brad","onConnected=> bundle:" +bundle);
    }

    //PendingResult<Status> requestLocationUpdates(GoogleApiClient var1, LocationRequest var2, LocationListener var3):(回傳值PendingResult<Status>)
    private void startLoactionUpdates() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {//沒有開權限的話直接結束
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);;
    }


    //10.Google Services連線中斷時 (GoogleApiClient.ConnectionCallbacks實做方法)
    @Override
    public void onConnectionSuspended(int i) { //int參數是連線中斷的代號
        mGoogleApiClient.connect();//繼續連線
        Log.v("brad","onConnectionSuspended,連線錯誤是i:" + i);
    }

    //Google Services連線失敗(OnConnectionFailedListener實做方法)
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {//ConnectionResult參數是連線失敗的資訊

    }

    //11.當位置改變時(LocationListener實做方法)
    @Override
    public void onLocationChanged(Location location) {// Location參數是目前的位置
        mLastLocation = location;
        displayLocation();
        Log.v("brad","=>onLocationChanged,location:" + mLastLocation);
    }

    //(LocationListener實做方法)    @Override
    public void onProviderEnabled(String s) {

    }


    //13.onResume()時checkPlayService();
    @Override
    protected void onResume() {
        super.onResume();
        checkPlayService();
        Log.v("brad","onResume");
    }

    //12.當開始時檢查是否有客戶端連線,有的話連線
    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient != null){ //如果客戶端連線有近來時
            mGoogleApiClient.connect();//連線
            Log.v("brad","=>onStart,客戶端有連線");

        }
    }

    //16.AsyncTask<Params, Progress, Result
    private class ParserTask extends AsyncTask<String,Integer, List<List<HashMap<String,String>>>>{

        ProgressDialog mDialog = new ProgressDialog(TrackingOrderMapsActivity.this);
        //當
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Pleas wait...");
            mDialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jObject;
            List<List<HashMap<String,String>>> routes = null;
            try {
                jObject = new JSONObject(strings[0]);
                DirectionJSONParser parser = new DirectionJSONParser();

                routes = parser.parse(jObject);
            }catch (Exception e){
                Log.v("brad","e:"+ e.toString());
            }
            return  routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            mDialog.dismiss();

            ArrayList points = null;
            PolylineOptions lineOptions = null;

            for(int i=0; i<lists.size(); i++){
                points = new ArrayList();
                lineOptions = new PolylineOptions();

                List<HashMap<String,String>> path = lists.get(i);

                for(int j =0; j<path.size(); j++){
                    HashMap<String,String> point = path.get(j);

                   double lat = Double.parseDouble(point.get("lat"));
                   double lng = Double.parseDouble(point.get("lng"));
                   LatLng position = new LatLng(lat,lng);

                   points.add(position);

                }

                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.BLUE);
                lineOptions.geodesic(true);
            }
            mMap.addPolyline(lineOptions);


        }
    }
}
