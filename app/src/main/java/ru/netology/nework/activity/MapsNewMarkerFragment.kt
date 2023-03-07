package ru.netology.nework.activity

import android.content.pm.PackageManager
import android.graphics.PointF
import android.os.Bundle
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.*
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.Runtime.getApplicationContext
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentMapNewMarkerBinding
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.util.DoubleArg
import ru.netology.nework.viewmodel.PostViewModel

@AndroidEntryPoint
class MapsNewMarkerFragment : Fragment(), UserLocationObjectListener, CameraListener,
    InputListener {

    companion object {
        var Bundle.latArg: Double by DoubleArg
        var Bundle.longArg: Double by DoubleArg
    }

    val requestPermissionLocation = 1
    var marker: PlacemarkMapObject? = null
    private var mapView: MapView? = null
    private var userLocationLayer: UserLocationLayer? = null
    private var permissionLocation = false
    private var routeStartLocation = Point(0.0, 0.0)
    private var followUserLocation = false

    @OptIn(ExperimentalCoroutinesApi::class)
    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment,
    )

    private var fragmentBinding: FragmentMapNewMarkerBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_new_marker, menu)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.save -> {
                fragmentBinding?.let {
                    if (it.editLatitude.text.isEmpty() ||
                        it.editLongitude.text.isEmpty()
                    ) {
                        Snackbar.make(it.root, "Data id empty", Snackbar.LENGTH_LONG)
                            .show()
                    } else {
                        viewModel.changeCoordsPosts(
                            it.editLatitude.text.toString(),
                            it.editLongitude.text.toString()
                        )
                        AndroidUtils.hideKeyboard(requireView())
                        findNavController().navigateUp()
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMapNewMarkerBinding.inflate(
            inflater,
            container,
            false
        )
        fragmentBinding = binding

        mapView = binding.mapview
        mapView?.map?.addInputListener(this)

        val markerLatitude = (arguments?.latArg ?: 0).toDouble()
        val markerLongitude = (arguments?.longArg ?: 0).toDouble()
        routeStartLocation = Point(markerLatitude, markerLongitude)

        followUserLocation = if (markerLatitude != 0.0 && markerLongitude != 0.0) {
            binding.editLatitude.setText(markerLatitude.toString())
            binding.editLongitude.setText(markerLongitude.toString())
            createMarker(routeStartLocation)
            false
        } else {
            true
        }

        mapView?.map?.move(
            CameraPosition(routeStartLocation, 15.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 2F),
            null
        )

        checkPermission()

        fragmentBinding?.userLocationFab?.setOnClickListener {
            if (permissionLocation) {
                cameraUserPosition()
                followUserLocation = true
            } else {
                checkPermission()
            }
        }
        return binding.root
    }

    override fun onStop() {
        mapView?.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView?.onStart()
    }

    override fun onDestroyView() {
        fragmentBinding = null
        mapView = null
        userLocationLayer = null
        super.onDestroyView()
    }

    override fun onObjectAdded(userLocationView: UserLocationView) {
        if (followUserLocation) setAnchor()
    }

    override fun onObjectRemoved(p0: UserLocationView) {}

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {}

    private fun onMapReady() {
        val mapKit = MapKitFactory.getInstance()
        userLocationLayer = mapView?.let { mapKit.createUserLocationLayer(it.mapWindow) }
        userLocationLayer?.isVisible = true
        userLocationLayer?.isHeadingEnabled = true
        userLocationLayer?.setObjectListener(this)
        mapView?.map?.addCameraListener(this)
        permissionLocation = true
    }

    private fun createMarker(point: Point) {
        marker.let {
            if (it != null) {
                mapView?.map?.mapObjects?.remove(it)
            }
        }

        lifecycleScope.launch {
            marker = mapView?.map?.mapObjects?.addPlacemark(
                Point(point.latitude, point.longitude),
                ImageProvider.fromResource(
                    getApplicationContext(),
                    android.R.drawable.btn_star_big_on
                )
            )
        }
        marker?.opacity = 0.5f
    }

    private fun cameraUserPosition() {
        val start = userLocationLayer?.cameraPosition()?.target
        if (start != null) routeStartLocation = start

        mapView?.map?.move(
            CameraPosition(routeStartLocation, 16f, 0f, 0f),
            Animation(Animation.Type.SMOOTH, 1f),
            null
        )
    }

    private fun checkPermission() {
        val permissionLocation = ContextCompat.checkSelfPermission(
            getApplicationContext(),
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                requestPermissionLocation
            )
        } else {
            onMapReady()
        }
    }

    private fun setAnchor() {
        val x = (mapView?.width?.times(0.5.toFloat()) ?: 0).toFloat()
        val y1 = (mapView?.height?.times(0.5.toFloat()) ?: 0).toFloat()
        val y2 = (mapView?.height?.times(0.83.toFloat()) ?: 0).toFloat()
        userLocationLayer?.setAnchor(
            PointF(x, y1), PointF(x, y2)
        )
        fragmentBinding?.userLocationFab?.setImageResource(R.drawable.ic_baseline_my_location_24)
        followUserLocation = false
    }

    private fun noAnchor() {
        userLocationLayer?.resetAnchor()
        fragmentBinding?.userLocationFab?.setImageResource(R.drawable.ic_baseline_location_searching_24)
    }

    override fun onCameraPositionChanged(
        p0: Map,
        p1: CameraPosition,
        p2: CameraUpdateReason,
        finish: Boolean
    ) {
        if (finish) {
            if (followUserLocation) {
                setAnchor()
            }
        } else {
            if (!followUserLocation) {
                noAnchor()
            }
        }
    }

    override fun onMapTap(p0: Map, p1: Point) {
        fragmentBinding?.editLatitude?.setText(String.format("%.6f", p1.latitude))
        fragmentBinding?.editLongitude?.setText(String.format("%.6f", p1.longitude))
        createMarker(p1)
    }

    override fun onMapLongTap(p0: Map, p1: Point) {}
}