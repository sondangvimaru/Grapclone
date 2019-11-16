package Remote

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url


interface IgoogleAPI {
    @GET

    fun  getPath(@Url url:String):Call<String>
}