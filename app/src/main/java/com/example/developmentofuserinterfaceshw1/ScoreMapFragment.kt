package com.example.developmentofuserinterfaceshw1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.util.Locale

class ScoreMapFragment : Fragment() {

    private lateinit var mapStatusText: TextView
    private lateinit var mapWebView: WebView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_score_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapStatusText = view.findViewById(R.id.mapStatusText)
        mapWebView = view.findViewById(R.id.mapWebView)
        showNoLocation()

        parentFragmentManager.setFragmentResultListener(
            ScoreSelectionContract.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            if (!bundle.getBoolean(ScoreSelectionContract.KEY_HAS_LOCATION)) {
                showNoLocation()
                return@setFragmentResultListener
            }

            showLocation(
                latitude = bundle.getDouble(ScoreSelectionContract.KEY_LATITUDE),
                longitude = bundle.getDouble(ScoreSelectionContract.KEY_LONGITUDE)
            )
        }
    }

    override fun onDestroyView() {
        mapWebView.destroy()
        super.onDestroyView()
    }

    @Suppress("SetJavaScriptEnabled")
    private fun showLocation(latitude: Double, longitude: Double) {
        val latitudeText = String.format(Locale.US, "%.6f", latitude)
        val longitudeText = String.format(Locale.US, "%.6f", longitude)
        mapStatusText.text = getString(R.string.map_location_format, latitudeText, longitudeText)
        mapWebView.visibility = View.VISIBLE
        mapWebView.settings.javaScriptEnabled = true
        mapWebView.loadUrl(
            "https://www.openstreetmap.org/?mlat=$latitudeText&mlon=$longitudeText#map=16/$latitudeText/$longitudeText"
        )
    }

    private fun showNoLocation() {
        mapStatusText.text = getString(R.string.no_location_for_score)
        mapWebView.visibility = View.INVISIBLE
        mapWebView.loadUrl("about:blank")
    }
}
