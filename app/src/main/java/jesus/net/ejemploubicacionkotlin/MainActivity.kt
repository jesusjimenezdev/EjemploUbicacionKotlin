package jesus.net.ejemploubicacionkotlin

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.provider.Settings
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AlertDialog
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.OnSuccessListener


class MainActivity : AppCompatActivity() {

    private val permisoFineLocation = Manifest.permission.ACCESS_FINE_LOCATION
    private val permisoCoarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION
    private val CODIGO_SOLICITUD_PERMISO = 100
    var fusedLocationClient: FusedLocationProviderClient? = null
    var locationRequest: LocationRequest? = null
    var callBack: LocationCallback? = null

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = FusedLocationProviderClient(this)
        inicializarLocationRequest()
    }

    override fun onStart() {
        super.onStart()

        if(validarPermisosUbicacion()) {
            obtenerUbicacion()
        } else {
            pedirPermisos()
        }
    }

    @SuppressLint("RestrictedApi")
    private fun inicializarLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest?.interval = 10000
        locationRequest?.fastestInterval = 5000
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun validarPermisosUbicacion(): Boolean {
        val hayUbicacionPrecisa = ActivityCompat.checkSelfPermission(this, permisoFineLocation) == PackageManager.PERMISSION_GRANTED
        val hayUbicacionOrdinaria = ActivityCompat.checkSelfPermission(this, permisoCoarseLocation) == PackageManager.PERMISSION_GRANTED

        return  hayUbicacionPrecisa && hayUbicacionOrdinaria
    }

    @SuppressLint("MissingPermission")
    private fun obtenerUbicacion() {
        // fusedLocationClient implementado de esta forma sirve para obtener la ultima localizacion
        /*fusedLocationClient?.lastLocation?.addOnSuccessListener(this, object : OnSuccessListener<Location> {

            override fun onSuccess(location: Location?) {
                if(location != null) {
                    Toast.makeText(this@MainActivity, "Latitud: ${location.latitude.toString()}\n Longitud: ${location.longitude.toString()}", Toast.LENGTH_LONG).show()
                }
            }

        })*/

        // fusedLocationClient implementado de esta forma sirve para obtener la localizacion cada cierto tiempo
        callBack = object : LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                for (ubicacion in locationResult?.locations!!) {
                    Toast.makeText(this@MainActivity, "Latutud: ${ubicacion.latitude.toString()} \n Longitud: ${ubicacion.longitude.toString()}", Toast.LENGTH_LONG).show()
                }
            }
        }

        fusedLocationClient?.requestLocationUpdates(locationRequest, callBack, null)
    }

    private fun pedirPermisos() {
        val deboProveerContexto = ActivityCompat.shouldShowRequestPermissionRationale(this, permisoFineLocation)
        if(deboProveerContexto) {
            //mandar un mensaje con explicacion adicional
            solicitudPermiso()
        } else {
            solicitudPermiso()
        }
    }

    private fun solicitudPermiso() {
        requestPermissions(arrayOf(permisoFineLocation, permisoCoarseLocation), CODIGO_SOLICITUD_PERMISO)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode) {
            CODIGO_SOLICITUD_PERMISO -> {
                if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // obtener la ultima ubicacion
                } else {
                    var layout = findViewById<ConstraintLayout>(R.id.layoutMain)
                    val alertDialog = AlertDialog.Builder(this@MainActivity)
                    alertDialog.setTitle("Permiso de ubicación")

                    alertDialog.setMessage("¿Quieres conceder el permiso de ubicación ahora?")

                    alertDialog.setPositiveButton("ACEPTAR"){dialog, which ->
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }


                    alertDialog.setNegativeButton("CANCELAR"){ dialog, which ->
                        dialog.dismiss()
                    }

                    val dialog: AlertDialog = alertDialog.create()
                    dialog.show()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()

        detenerAcualizacionUbicacion()
    }

    private fun detenerAcualizacionUbicacion() {
        fusedLocationClient?.removeLocationUpdates(callBack)
    }
}
