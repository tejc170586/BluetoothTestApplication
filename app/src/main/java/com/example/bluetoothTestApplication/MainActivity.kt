package com.example.bluetoothTestApplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.os.RemoteException
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.altbeacon.beacon.*
import org.altbeacon.beacon.Region
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity(), BeaconConsumer {

    companion object{
        const val PERMISSIONS_REQUEST_CODE = 1000
    }
    private lateinit var beaconManager: BeaconManager
    private var isScanning = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermission()

        beaconManager = BeaconManager.getInstanceForApplication(this)
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconUtil.IBEACON_FORMAT))
    }

    // Check Permission.
//    private fun checkAndRequestPermissions() {
//        val permissions = arrayOf(
//                Manifest.permission.ACCESS_COARSE_LOCATION,
//                Manifest.permission.ACCESS_FINE_LOCATION
//        )
//        val permissionsNotGranted =
//            permissions
//                .filter {
//                    ActivityCompat.checkSelfPermission(this, it) ==
//                            PackageManager.PERMISSION_GRANTED
//                }.toTypedArray()
//        ActivityCompat.requestPermissions(this, permissionsNotGranted, PERMISSIONS_REQUEST_CODE)
//    }

    private fun checkPermission(){
        if((ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)   != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_CODE)
        }
    }


    private fun externalStoragePath(): String {
        return Environment.getExternalStorageDirectory().absolutePath
    }


    // Start Service.
    override fun onResume(){
        super.onResume()
        beaconManager.bind(this)
        isScanning = true
    }


    // Service termination.
    override fun onPause(){
        super.onPause()
        beaconManager.unbind(this)
    }


    override fun onBeaconServiceConnect(){

        beaconManager.addMonitorNotifier(object : MonitorNotifier {

            override fun didEnterRegion(region: Region) {
                Log.d("iBeacon", "Enter Region")
                beaconManager.startRangingBeaconsInRegion(region)
            }

            override fun didExitRegion(region: Region) {
                beaconManager.stopRangingBeaconsInRegion(region)
            }

            override fun didDetermineStateForRegion(i: Int, region: Region) {
                Log.d("MainActivity", "Determine State $i")
            }
        })

        // 表示用データクラス
        data class BeaconDetail(val id1: Identifier, val id2: Identifier, val id3: Identifier, val distance: Double){
//            val uuid  = id1.toString()
//            val major = id2.toString()
//            val minor = id3.toString()
//            val distance = ((distance * 100).roundToInt() / 100).toString()
//            val category = if(distance < 1) "NEAR" else "FAR"
        }

        val beaconDetails = mutableListOf<BeaconDetail>()
        val list = mutableListOf<String>()
        beaconManager.addRangeNotifier { beacons, region ->
            if(beacons.count() > 0){
                beacons
                        .map{
                            val beaconDetail = BeaconDetail(it.id1, it.id2, it.id3, it.distance)
                            beaconDetails.add(beaconDetail)
                            it
                        }
                        .map { "UUID:" + it.id1 + " major:" + it.id2 + " minor:" + it.id3 + " RSSI:" + it.rssi + " Distance:" + it.distance + " txPower" + it.txPower }
                        .forEach { Log.d("iBeacon", it)}
                Log.d("iBeacon", "beacon available")
                //ListView設定
                val listView = ListView(this)
                setContentView(listView)
                val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, beaconDetails)
                listView.adapter = arrayAdapter
            }else{
                Log.d("iBeacon", "No beacon available")
            }
        }




        val major: Identifier = Identifier.parse("0")
        val mRegion: Region = Region("iBeacon", null, major, null)


        try{
            Log.d("Debug", "Start Monitoring.")
            beaconManager.startMonitoringBeaconsInRegion(mRegion)

        }catch (e: RemoteException){
            e.printStackTrace()
        }

    }
}

