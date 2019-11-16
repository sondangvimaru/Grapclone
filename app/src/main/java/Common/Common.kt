package Common

import Remote.IgoogleAPI
import Remote.RetrofitClient



class Common {
    companion object
    {
        val baseURL:String="https://maps.googleapis.com"

        fun getGoogleAPI(): IgoogleAPI
        {
            return RetrofitClient.getclient(baseURL).create(IgoogleAPI::class.java)

        }
    }
}