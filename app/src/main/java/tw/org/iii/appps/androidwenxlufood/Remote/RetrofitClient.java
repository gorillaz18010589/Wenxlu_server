package tw.org.iii.appps.androidwenxlufood.Remote;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;

    //Retrofit.Builder():
    //baseUrl(String baseUrl)://設置baseUrl即要連的網站(回傳值Builder )
    //addConverterFactory(Converter.Factory factory)://新增一個轉換工廠(用Scalars作為資料處理)(回傳值Builder)
    //build():(回傳值Retrofit)

    //1.取得Client方法,準備創造Service
    public static Retrofit getClient(String baseUrl){//baseUrl即要連的網站參數
        if(retrofit == null){
            retrofit = new Retrofit.Builder()//建立Retrofit物件實體
                    .baseUrl(baseUrl)//設置baseUrl即要連的網站
                    .addConverterFactory(ScalarsConverterFactory.create())//新增一個轉換工廠(用Scalars作為資料處理)
                    .build();
        }
            return  retrofit;
    }
}
