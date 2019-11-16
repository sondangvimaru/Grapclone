@file:Suppress("DEPRECATION")

package com.cnt57cl.grapclonfordriver

import Common.Common
import Remote.IgoogleAPI
import android.animation.ValueAnimator

import android.content.pm.PackageManager
import android.graphics.Color

import android.location.Location

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock

import android.util.Log

import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.core.app.ActivityCompat


import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil

import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status


import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient

import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_home.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.lang.Exception
import java.util.*

import kotlin.collections.ArrayList


@Suppress("DEPRECATION", "CAST_NEVER_SUCCEEDS")
class Home : AppCompatActivity(), OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,
    com.google.android.gms.location.LocationListener {
    override fun onLocationChanged(location: Location?) {
    this.location=location
        displayLocation()

    }



    override fun onConnected(p0: Bundle?) {

        displayLocation()
        StartLocationUpdate()

    }

    override fun onConnectionSuspended(p0: Int) {
        apiclient?.connect()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
     lateinit var mMap: GoogleMap
    private   val MY_Pemission_code=7000
    private  val request_service_code=7001
    private  var locationRequest: LocationRequest?=null
    private  var apiclient:GoogleApiClient?=null
    private  var location:Location?=null
    private  val Update_Interval=5000
    private  val Fatest_Interval=3000
    private  val DispLacement=10
    var driver:DatabaseReference?=null

    var geofire:GeoFire?=null
    var Mcurrent:Marker?=null
    var mapFragment:SupportMapFragment?=null
    //car animation
    private  var linelist:List<LatLng>?=null
    private var  carMarker:Marker?=null
    private var v:Float?=null
    private var handler:Handler?=null
    private var startposition:LatLng?=null
    private var endposition:LatLng?=null
    private var currentposition:LatLng?=null
    private var index:Int=0
    private var next:Int=0
    private var noiden:String=" "
    private var polylineOptions:PolylineOptions?=null
    private  var blackpolylineOptions:PolylineOptions?=null
    private var polyline_black:Polyline?=null
    private var polyline_grey:Polyline?=null
    private var mServices:IgoogleAPI?=null
    private var lng:Double?=null
    private var lat:Double?=null
    var place: AutocompleteSupportFragment?=null
    val runnable:Runnable = Runnable {
       runOnUiThread() {

           handler_animator()

            handler?.postDelayed(Runnable {

                runOnUiThread()
                {
                 handler_animator()
                }
            },3000)
        }

    }
    private  fun handler_animator()
    {
        if(index<linelist!!.size-1)
        {
            index++
            next=index+1

        }
        if(index<linelist!!.size)
        {
            startposition=linelist!!.get(index)
            endposition=linelist!!.get(next)

        }
        val animator:ValueAnimator= ValueAnimator.ofInt(0,1)
        animator.duration=3000
        animator.interpolator= LinearInterpolator()
        animator.addUpdateListener {
                animation ->

            v=animation.animatedFraction
            lng=v!!*endposition!!.longitude+(1-v!!)*startposition!!.longitude
            lat=v!!*endposition!!.latitude+(1-v!!)*startposition!!.latitude
            val newPos:LatLng= LatLng(lat!!,lng!!)
            carMarker?.position=newPos
            carMarker?.setAnchor(0.5f,0.5f)
            carMarker?.rotation=getBearing(startposition!!,newPos)
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(

                CameraPosition.Builder().target(newPos).zoom(15.5f).build()
            ))

        }

        animator.start()
    }

    private fun getBearing(start: LatLng, end: LatLng): Float {
        val lat_g:Double = Math.abs(start.latitude-end.latitude)
        val lng_g:Double=Math.abs(start.longitude-end.longitude)
        if(start.latitude<end.latitude && start.longitude<end.longitude)
        return Math.toDegrees(Math.atan(lng_g/lat_g)).toFloat()
        else if(start.latitude>=end.latitude && start.longitude<end.longitude)
            return (90-Math.toDegrees(Math.atan(lng_g/lat_g))+90).toFloat()
        else if(start.latitude>=end.latitude && start.longitude>=end.longitude)
            return (Math.toDegrees(Math.atan(lng_g/lat_g))+180).toFloat()
        else if(start.latitude<end.latitude && start.longitude>=end.longitude)
            return (90-Math.toDegrees(Math.atan(lng_g/lat_g))+270).toFloat()

        return -1.0f

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
         mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment!!.getMapAsync(this)

        Places.initialize(applicationContext,"AIzaSyAWXelgWb7ztLu4yn0FWj2yKHGJ-9XJdxQ")
        val placesClient:PlacesClient  = Places.createClient(this);
        location_home.setOnCheckedChangeListener { isonline ->

            if(isonline)
            {
                StartLocationUpdate()
                displayLocation()
                Snackbar.make(mapFragment?.view!!,"You are online",Snackbar.LENGTH_SHORT).show()


            }else

            {

                StopLocationUpdate()
                Mcurrent?.remove()
                mMap.clear()
                handler?.removeCallbacks(runnable)
                Snackbar.make(mapFragment?.view!!,"You are offline",Snackbar.LENGTH_SHORT).show()

            }
            }
            driver=FirebaseDatabase.getInstance().getReference("Drivers")

        linelist=ArrayList()

        place=  supportFragmentManager.findFragmentById(R.id.place_autocomplete_fragment) as AutocompleteSupportFragment
        place?.setPlaceFields(arrayListOf(Place.Field.ID,Place.Field.NAME,Place.Field.PRICE_LEVEL,Place.Field.PHONE_NUMBER,Place.Field.PRICE_LEVEL))
        place?.setOnPlaceSelectedListener(object : PlaceSelectionListener
        {
            override fun onError(p0: Status) {
                Log.d("erro",p0.toString())

            }

            override fun onPlaceSelected(p0: Place) {
                if(location_home.isChecked)
                {
                    noiden=p0.address.toString()
                    noiden=noiden.replace(" ","+")
                    noiden="101 nguyen van linh da nang"

                    noiden=noiden.replace(" ","+")
                    getDirection()
                }
                else
                    Toast.makeText(applicationContext,"please change your status to Online",Toast.LENGTH_LONG).show()            }
        })


        geofire= GeoFire(driver)

        setUplocation()
        mServices=Common.getGoogleAPI()
    }

    //xac dinh phuong huong
    private fun  getDirection()
    {
        currentposition= LatLng(location!!.latitude,location!!.longitude)
        var requestAPI:String?=null
        try {

            requestAPI="https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin="+currentposition?.latitude+","+currentposition?.longitude+"&" +
                    "destination="+noiden+"&" +
                    "key="+resources.getString(R.string.google_direction_api)
            Log.d("Request_API",requestAPI)
           mServices?.getPath(requestAPI)
   ?.enqueue(object :retrofit2.Callback<String>
   {
       override fun onResponse(call: Call<String>, response: Response<String>) {

       try {
           val jsonObject:JSONObject = JSONObject(response.body().toString())

           val jsonArray:JSONArray=jsonObject.getJSONArray("routes")

           for (i in 0 until jsonArray.length())
           {
               val route:JSONObject=jsonArray.getJSONObject(i)
               val poly:JSONObject=route.getJSONObject("overview_polyline")
               val polyline:String=poly.getString("points")
               linelist=decodePoly(polyline)
           }
           val buider:LatLngBounds.Builder =LatLngBounds.Builder()

           for (latIng in linelist!!)
            buider.include(latIng)
           val bounds:LatLngBounds= buider.build()
           val cameraupdate:CameraUpdate= CameraUpdateFactory.newLatLngBounds(bounds,2)

           mMap.animateCamera(cameraupdate)

           polylineOptions = PolylineOptions()
           polylineOptions?.color(Color.RED)
           polylineOptions?.width(5f)
           polylineOptions?.startCap(SquareCap())
           polylineOptions?.endCap(SquareCap())
           polylineOptions?.jointType(JointType.ROUND)
           polylineOptions?.addAll(linelist)
           polyline_grey= mMap.addPolyline(polylineOptions)

           blackpolylineOptions= PolylineOptions()
           blackpolylineOptions?.color(Color.BLACK)
            blackpolylineOptions?.width(5f)
           blackpolylineOptions?.startCap(SquareCap())
           blackpolylineOptions?.endCap(SquareCap())
           blackpolylineOptions?.jointType(JointType.ROUND)
           polyline_black =mMap.addPolyline(blackpolylineOptions)
           mMap.addMarker(MarkerOptions().position(linelist!!.get(linelist!!.size-1))
               .title("Điểm Đến"))

           //animation

           val animaton: ValueAnimator =ValueAnimator.ofInt(0,100)
           animaton.duration=2000
           animaton.interpolator= LinearInterpolator()
           animaton.addUpdateListener { animation ->

            val list:List<LatLng> = polyline_grey!!.points
               val percentValue:Int= animation.animatedValue as Int
               val size: Int=list.size
                val newPoints:Int= (size*(percentValue/100.0f)).toInt()
               val p:List<LatLng> = list.subList(0,newPoints)
               polyline_black?.points=p

           }

           animaton.start()
           carMarker = mMap.addMarker(MarkerOptions().position(currentposition!!).flat(true)
               .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)))

           handler= Handler()
           index=1
           next=1
           handler?.postDelayed(runnable,3000)
       }catch (e:   JSONException)
       {
           e.printStackTrace()
       }


       }

       override fun onFailure(call: Call<String>, t: Throwable) {

        Toast.makeText(applicationContext,""+t.message,Toast.LENGTH_LONG).show()
       }
   })
        }catch (e:Exception)
        {
            e.printStackTrace()

        }

    }
    private fun decodePoly(encoded: String): List<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            poly.add(LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5))
        }

        return poly
    }
    private fun setUplocation() {
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {


            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,

                android.Manifest.permission.ACCESS_FINE_LOCATION),MY_Pemission_code)
        }
        else
        {
            if (checkPlayService())
            {
                buildGoogleApiclient()
                createLocationRequest()
                if(location_home.isChecked)
                {
                    displayLocation()
                }
            }

        }

    }

    private fun createLocationRequest() {
        locationRequest= LocationRequest()
        locationRequest?.interval= Update_Interval.toLong()
        locationRequest?.fastestInterval=Fatest_Interval.toLong()
        locationRequest?.priority=LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest?.smallestDisplacement=DispLacement.toFloat()

    }

    private fun buildGoogleApiclient() {

        apiclient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
        apiclient?.connect()

    }

    private fun checkPlayService(): Boolean {

        val result_code= GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)

        if(result_code!=ConnectionResult.SUCCESS)
        {
            if(GooglePlayServicesUtil.isUserRecoverableError(result_code))
            {
                GooglePlayServicesUtil.getErrorDialog(result_code,this,request_service_code).show()

            }else {
                Toast.makeText(this, "thiet bi khong duoc ho tro", Toast.LENGTH_LONG).show()

                finish()
            }
            return false
        }
        return true
    }

    private fun StopLocationUpdate() {
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {
            return
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(apiclient,this)
    }

    private fun displayLocation() {


        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {
            return
        }
        location=LocationServices.FusedLocationApi.getLastLocation(apiclient)
        if(location!=null)
        {
            if(location_home.isChecked)
            {
                val latiude:Double=location!!.latitude
                val longitude=location!!.longitude

                geofire?.setLocation(FirebaseAuth.getInstance().currentUser?.uid, GeoLocation(latiude,longitude), GeoFire.CompletionListener(
                function = fun(key:String?,error:DatabaseError?) {

                    if (Mcurrent!=null)
                        Mcurrent?.remove()

                        Mcurrent=mMap.addMarker(MarkerOptions()

                            .icon
                     (BitmapDescriptorFactory.fromResource(R.drawable.car))
                            .position(LatLng(latiude,longitude)

                            ).title("You")
                        )


                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latiude,longitude),15.0f))


                }


                ))

            }

        }
        else
        {
            Log.d("ERRO","cannot get your location")
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode)
        {
            MY_Pemission_code->
                if (grantResults.size>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    if (checkPlayService())
                    {
                        buildGoogleApiclient()
                        createLocationRequest()
                        if(location_home.isChecked)
                        {
                            displayLocation()
                        }
                    }
                }
        }

    }
    private fun roateMaker(mcurrent: Marker?, i: Int, mMap: GoogleMap) {

        val handler:Handler= Handler()
        val start =SystemClock.uptimeMillis()
        val startRotaion:Float =Mcurrent!!.rotation
        val duration=1000
        val interpolator:Interpolator = LinearInterpolator()
        handler.post(Runnable {
            val elapsed=SystemClock.uptimeMillis()-start
            val t = interpolator.getInterpolation((elapsed/duration).toFloat())
            val rot= t*i+(1-t)*startRotaion

            val result:Float
            if(-rot>180)result=rot/2 else result=rot
            Mcurrent!!.rotation=rot

            if (t<1.0)
            {
               handler.postDelayed(Runnable {

                   val elapsed=SystemClock.uptimeMillis()-start
                   val t = interpolator.getInterpolation((elapsed/duration).toFloat())
                   val rot= t*i+(1-t)*startRotaion

                   val result:Float
                   if(-rot>180)result=rot/2 else result=rot
                   Mcurrent!!.rotation=rot
               },16)
            }
        })
    }

    private fun StartLocationUpdate() {

    if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
    {
        return
    }
        LocationServices.FusedLocationApi.requestLocationUpdates(apiclient, locationRequest,this)

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType=GoogleMap.MAP_TYPE_NORMAL
        mMap.isTrafficEnabled=false
        mMap.isIndoorEnabled=false
        mMap.isBuildingsEnabled=false
        mMap.uiSettings.isZoomControlsEnabled=true


    }
}
