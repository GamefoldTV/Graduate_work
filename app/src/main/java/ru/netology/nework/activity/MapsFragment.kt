package ru.netology.nework.activity

import android.R
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.*
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.Runtime.getApplicationContext
import com.yandex.runtime.image.ImageProvider
import com.yandex.runtime.ui_view.ViewProvider
import ru.netology.nework.databinding.FragmentMapBinding
import ru.netology.nework.util.CompanionArg.Companion.doubleArg1
import ru.netology.nework.util.CompanionArg.Companion.doubleArg2
import ru.netology.nework.util.CompanionArg.Companion.textArg

class MapsFragment : Fragment() {
    private var mapView: MapView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMapBinding.inflate(layoutInflater)

        mapView = binding.mapview

        val markerLatitude = requireArguments().doubleArg1
        val markerLongitude = requireArguments().doubleArg2


        mapView?.getMap()?.move(
            CameraPosition(Point(markerLatitude, markerLongitude), 15.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 2F),
            null
        )


        val marker = mapView?.map?.mapObjects?.addPlacemark(
            Point(markerLatitude, markerLongitude),
            ImageProvider.fromResource(getApplicationContext(), R.drawable.btn_star_big_on)
        )
        marker?.opacity = 0.5f

        val textView = TextView(getApplicationContext())
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        mapView?.map?.mapObjects?.addPlacemark(
            Point(markerLatitude - 0.0005, markerLongitude),
        )
        binding.fabBack.setOnClickListener {
            findNavController().navigateUp()
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
}