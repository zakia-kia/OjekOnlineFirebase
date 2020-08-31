package com.zakia.idn.ojekonlinefirebase.ui.home

import android.annotation.SuppressLint
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.app.ProgressDialog.show
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.zakia.idn.ojekonlinefirebase.R
import com.zakia.idn.ojekonlinefirebase.model.Booking
import com.zakia.idn.ojekonlinefirebase.model.ResultRoute
import com.zakia.idn.ojekonlinefirebase.model.RoutesItem
import com.zakia.idn.ojekonlinefirebase.network.NetworkModule
import com.zakia.idn.ojekonlinefirebase.network.RequestNotification
import com.zakia.idn.ojekonlinefirebase.utils.ChangeFormat
import com.zakia.idn.ojekonlinefirebase.utils.Constan
import com.zakia.idn.ojekonlinefirebase.utils.GPSTrack
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.fragment_home.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment(), OnMapReadyCallback {

    var map: GoogleMap? = null
    var tanggal: String? = null
    var latAwal: Double? = null
    var latAhir: Double? = null
    var lonAwal: Double? = null
    var lonAkhir: Double? = null

    var jarak: String? = null
    var dialog: Dialog? = null

    var keyy: String? = null
    private var auth: FirebaseAuth? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    //menapilkan maps ke fragment
    override fun onMapReady(p0: GoogleMap?) {
        map = p0
        map?.uiSettings?.isMyLocationButtonEnabled = false
        map?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(-6.3088652, 106.682188), 12f
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //meng-inisialisasi dari mapsView
        map_view.onCreate(savedInstanceState)
        map_view.getMapAsync { this }

        showPermission()
        visibleView(false)
//        keyy.let { bookingHistroryUser(it) }

        tv_home_awal?.onClick { takeLocation(1) }
        tv_home_tujuan.onClick { takeLocation(2) }
        btn_home_next?.onClick {
            if (tv_home_awal?.text?.isNotEmpty()!! && tv_home_tujuan.text.isNotEmpty()) {
                insertServer()
            } else {
                toast("tidak boleh kosong").show()
                view.let { Snackbar.make(it, "tidak boleh kosong", Snackbar.LENGTH_SHORT)
                    .show()
                }
            }
        }
    }

    private fun showPermission() {
        showGps()
        if (activity?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED) {

            if (activity?.let {
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        it, android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                }!!) {
                showGps()
            } else {
                requestPermissions(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ), 1
                )
            }
        }
    }


    //insert data booking ke realtime database
    private fun insertServer() {
        val currentTime = Calendar.getInstance().time
        tanggal = currentTime.toString()
        insertRequest(
            currentTime.toString(), auth?.uid.toString(),
            tv_home_awal.text.toString(), latAwal, lonAwal,
            tv_home_tujuan.text.toString(), latAhir, lonAkhir,
            tv_home_price.text.toString(), jarak.toString()
        )
    }

    private fun insertRequest(
        tanggal: String,
        uid: String,
        lokasiAwal: String,
        latAwal: Double?,
        lonAwal: Double?,
        lokasiTujuan: String,
        latTujuan: Double?,
        lonTujuan: Double?,
        harga: String,
        jarak: String
    ): Boolean {
        val booking = Booking()
        booking.tanggal = tanggal
        booking.uid = uid
        booking.lokasiAwal = lokasiAwal
        booking.latAwal = latAwal
        booking.lonAwal = lonAwal
        booking.lokasiTujuan = lokasiTujuan
        booking.latTujuan = latTujuan
        booking.lonTujuan = lonTujuan
        booking.jarak = jarak
        booking.harga = harga
        booking.status = 1
        booking.driver = ""

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(Constan.tb_booking)
        keyy = database.reference.push().key
        val k = keyy

        pushNotif(booking)
//        k?.let { bookingHistroryUser(it) }
//        myRef.child(keyy ?: "").setValue(booking)

        return true
    }

    private fun pushNotif(booking: Booking) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("Driver")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (issue in snapshot.children) {
                    val token = issue.child("token").getValue(String::class.java)

                    println(token.toString())
                    val request = RequestNotification()
                    request.token = token
                    request.sendNotificationModel = booking
                }
            }
        })
    }

    //menampilkan lokasi user berdasarkan gps device
    private fun showGps() {
        val gps = context?.let { GPSTrack(it) }

        if (gps?.canGetLocation()!!) {
            latAwal = gps.latitude
            lonAwal = gps.latitude

            showMarker(latAwal ?: 0.0, lonAwal ?: 0.0, "My Location")
            val name = showName(latAwal ?: 0.0, lonAwal ?: 0.0)
            tv_home_awal.text = name

        } else gps.showSettingGPS()
    }

    @SuppressLint("CheckResult")
    private fun route() {
        val origin = latAwal.toString() + "," + lonAwal.toString()
        val dest = latAhir.toString() + "," + lonAkhir.toString()

        NetworkModule.getService().actionRoute(origin, dest, Constan.API_KEY)
            .observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
            .subscribe({ t: ResultRoute? ->
                showData(t?.routes)
            }, {})

    }

    //menampilkan harga
    private fun showData(routes: List<RoutesItem?>?) {
        visibleView(true)

        if (routes != null) {
            val point = routes[0]?.overviewPolyline?.points
            jarak = routes[0]?.legs?.get(0)?.distance?.text
            val jarakValue = routes[0]?.legs?.get(0)?.distance?.value
            val waktu = routes[0]?.legs?.get(0)?.duration?.text

            tv_home_waktu_distance.text = waktu + "(" + jarak + ")"
            val pricex = jarakValue?.toDouble()?.let { Math.round(it) }
            val price = pricex?.div(1000.0)?.times(2000.0)
            val price2 = ChangeFormat.toRupiahFormat2(price.toString())

            tv_home_price.text = "Rp" + price2
        } else {
            alert { message = "data route null" }
                .show()
        }
    }

    private fun visibleView(status: Boolean) {
        if (status) {
            btn_home?.visibility = View.VISIBLE
            btn_home_next?.visibility = View.VISIBLE
        } else {
            btn_home?.visibility = View.GONE
            btn_home_next?.visibility = View.GONE
        }
    }

    //auto-complete google maps
    fun takeLocation(status: Int) {
        try {
            context?.applicationContext?.let {
                Places.initialize(it, Constan.API_KEY)
            }
            val field = arrayListOf(
                Place.Field.ID, Place.Field.NAME,
                Place.Field.LAT_LNG, Place.Field.ADDRESS
            )
            val intent = context?.applicationContext?.let {
                Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, field)
                    .build(it)
            }

            startActivityForResult(intent, status)

        } catch (e: GooglePlayServicesRepairableException) {

        } catch (e: GooglePlayServicesNotAvailableException) {

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                val place = data?.let { Autocomplete.getPlaceFromIntent(it) }

                latAwal = place?.latLng?.latitude
                lonAwal = place?.latLng?.longitude

                tv_home_awal.text = place?.address.toString()
                showMainMarker(
                    latAwal ?: 0.0, lonAwal ?: 0.0,
                    place?.address.toString()
                )

                Log.i("location", "place: " + place?.name)
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status = data?.let { Autocomplete.getStatusFromIntent(it) }

                Log.i("locations", status?.statusMessage)
            } else if (resultCode == RESULT_CANCELED) {

            }

        } else {
            if (resultCode == RESULT_OK) {
                val place = data?.let { Autocomplete.getPlaceFromIntent(it) }

                latAhir = place?.latLng?.latitude
                lonAkhir = place?.latLng?.longitude

                tv_home_tujuan.text = place?.address.toString()
                showMarker(latAhir ?: 0.0, lonAwal ?: 0.0, place?.address.toString())

                route()
                Log.i("loctions", "place" + place?.name)
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status = data?.let { Autocomplete.getStatusFromIntent(it) }
                Log.i("locations", status?.statusMessage)
            } else if (resultCode == RESULT_CANCELED) {

            }
        }
    }

    //GEOCODER = Menerjemahkana koOrdinat jadi nama lokasi
    private fun showName(lat: Double, lon: Double): String? {
        var name = ""
        var geocoder = Geocoder(context, Locale.getDefault())

        try {
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (addresses.size > 0) {
                val fetchedAddress = addresses.get(0)
                val startAddress = StringBuilder()

                for (i in 0..fetchedAddress.maxAddressLineIndex) {
                    name = startAddress.append(fetchedAddress.getAddressLine(i))
                        .append("").toString()
                }
            }
        } catch (e: Exception) {

        }
        return name
    }

    //untuk menampilkan lokasi (marker origin)
    private fun showMainMarker(lat: Double, lon: Double, msg: String) {
        val res = context?.resources
        val marker1 = BitmapFactory.decodeResource(res, R.drawable.placeholder)
        val smallmarker = Bitmap.createScaledBitmap(marker1, 80, 120, false)
        val coordinate = LatLng(lat, lon)

        //membuat pin baru di android
        map?.addMarker(
            MarkerOptions().position(coordinate).title(msg)
                .icon(BitmapDescriptorFactory.fromBitmap(smallmarker))
        )
        //mengatur zoom camera
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 16f))
        //agar posisi marker selalu di tengah
        map?.moveCamera(CameraUpdateFactory.newLatLng(coordinate))
    }

    //marker destination
    private fun showMarker(lat: Double, lon: Double, msg: String) {
        val coordinate = LatLng(lat, lon)

        map?.addMarker(MarkerOptions().position(coordinate).title(msg))
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 16f))
        map?.moveCamera(CameraUpdateFactory.newLatLng(coordinate))
    }

    override fun onResume() {
        super.onResume()
//        keyy?.let { bookingHistroryUser(it) }
        map_view?.onResume()
    }

    override fun onPause() {
        super.onPause()
        map_view?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        map_view?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        map_view?.onLowMemory()
    }

//    private fun bookingHistroryUser(it: String): Any {
//
//    }

}