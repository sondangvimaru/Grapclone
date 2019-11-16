package Remote

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class RetrofitClient {
    companion object {

        var retrofit:Retrofit?=null
     fun getclient(baseURL:String):Retrofit
        {
            if(retrofit==null)
            {
                retrofit= Retrofit.Builder().baseUrl(baseURL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build()
            }

            return retrofit!!
        }
    }

}