package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.android.politicalpreparedness.BuildConfig
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.google.android.material.snackbar.Snackbar
import java.util.Locale
import java.util.function.Consumer

class DetailFragment : Fragment(), LocationListener {

    companion object {
        const val LOCATION_PERMISSION = 100
    }

    private val viewModel = RepresentativeViewModel()

    lateinit var binding: FragmentRepresentativeBinding

    private var snackbar: Snackbar? = null

    private val locationManager by lazy {
        requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = FragmentRepresentativeBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        val adapter = RepresentativeListAdapter()
        binding.representativeRv.adapter = adapter

        viewModel.representatives.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

        binding.buttonLocation.setOnClickListener {
            hideKeyboard()
            checkLocationPermissions()
        }

        return binding.root
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            getLocation()
        } else {
            snackbar = Snackbar.make(binding.root, R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.settings) {
                        if (isAdded) {
                            startActivity(Intent().apply {
                                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            })
                        }
                    }

            snackbar?.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        snackbar?.dismiss()
    }

    private fun checkLocationPermissions() {
        return if (isPermissionGranted()) {
            getLocation()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION)
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun getLocation() {
        val isNetworkProviderEnabled =
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        val isGPSProviderEnabled =
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        when {
            isNetworkProviderEnabled -> {
                moveToCurrentLocation(LocationManager.NETWORK_PROVIDER)
            }
            isGPSProviderEnabled -> {
                moveToCurrentLocation(LocationManager.GPS_PROVIDER)
            }
            else -> {
                AlertDialog.Builder(requireContext())
                        .setMessage(R.string.location_required_error)
                        .setPositiveButton(R.string.ok) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    private fun moveToCurrentLocation(provider: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (isPermissionGranted()) {
                locationManager.getCurrentLocation(provider, null, requireContext().mainExecutor, Consumer {
                    geoCodeLocation(it)
                })
            }
        } else {
            locationManager.requestSingleUpdate(provider, this, Looper.getMainLooper())
        }
    }

    override fun onLocationChanged(p0: Location) {
        viewModel.getGeolocationAddress(geoCodeLocation(p0))
    }

    private fun geoCodeLocation(location: Location): Address {
        val geocoder = Geocoder(context, Locale.getDefault())
        return geocoder.getFromLocation(location.latitude, location.longitude, 1)
                .map { address ->
                    Address(address.thoroughfare, address.subThoroughfare, address.locality, address.adminArea, address.postalCode)
                }
                .first()
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
    }
}