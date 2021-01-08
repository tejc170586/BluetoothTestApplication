package com.example.bluetoothTestApplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.os.RemoteException
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.altbeacon.beacon.*
import org.altbeacon.beacon.Region as Region1

import java.util.Collection


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
        val mRegion: Region1 = Region1("iBeacon", null, null, null)

        beaconManager.addMonitorNotifier(object : MonitorNotifier {

            override fun didEnterRegion(region: Region1) {
                Log.d("iBeacon", "Enter Region")
                beaconManager.startRangingBeaconsInRegion(region)
            }

            override fun didExitRegion(region: Region1) {
                beaconManager.stopRangingBeaconsInRegion(region)
            }

            override fun didDetermineStateForRegion(i: Int, region: Region1) {
                Log.d("MainActivity", "Determine State $i")
            }
        })

        Log.d("Test", "Loaded")

        beaconManager.addRangeNotifier { beacons, region ->
            if(beacons.count() > 0){
                beacons
                        .map { "UUID:" + it.id1 + " major:" + it.id2 + " minor:" + it.id3 + " RSSI:" + it.rssi + " Distance:" + it.distance + " txPower" + it.txPower }
                        .forEach { Log.d("iBeacon", it) }
            }else{
                Log.d("iBeacon", "No beacon available")
            }
        }

        Log.d("Test2", "Loaded")

        try{
            Log.d("Debug", "Start Monitoring.")
            beaconManager.startMonitoringBeaconsInRegion(mRegion)

        }catch (e: RemoteException){
            e.printStackTrace()
        }

    }
}


fun didRangeBeaconsInRegion(beacons: Collection<Beacon>, region: Region1){
    for (beacon in beacons) {
        Log.d("MyActivity", "UUID:" + beacon.id1 + ", major:"
                + beacon.id2 + ", minor:" + beacon.id3 + ", RSSI:"
                + beacon.rssi + ", TxPower:" + beacon.txPower
                + ", Distance:" + beacon.distance)
    }
}

