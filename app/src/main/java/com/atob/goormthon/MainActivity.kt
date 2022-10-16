package com.atob.goormthon

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.VectorDrawable
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.contains
import com.atob.goormthon.databinding.ActivityMainBinding
import com.google.gson.annotations.SerializedName
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapPolyline
import net.daum.mf.map.api.MapView
import okhttp3.Call
import okhttp3.Response
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/***
 * 인터넷 끊겼을 때 error android W/libEGL: EGLNativeWindowType 0x7b12132010 disconnect failed
 *
 */
class MainActivity : AppCompatActivity() {

    private var mBinding: ActivityMainBinding? = null
    private val binding get() = mBinding!!

    private val ACCESS_FINE_LOCATION = 1000     // Request Code

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // 화면 세팅
        initView()
        // 내 위치추적 버튼
        myPositionButton()
        // 리뷰 추가 버틑
        reviewButton()
        // 마커 클릭 이벤트
        markerClickButton()
    }

    data class MarkerContributionDto(
        @SerializedName("id")
        val id: Int,

        @SerializedName("latitude")
        val latitude: Double,

        @SerializedName("longitude")
        val longitude: Double,

        @SerializedName("count")
        val count: Int
    )

    interface RetrofitService {
        @GET("/v0/marker/contribution/{userId}")
        fun getMarkerContribution(@Path("userId") userId: String): retrofit2.Call<Array<MarkerContributionDto>>
    }


    val retrofit = Retrofit.Builder().baseUrl("https://mirinae-sbnbk.run.goorm.io")
        .addConverterFactory(GsonConverterFactory.create()).build();
    val service = retrofit.create(RetrofitService::class.java);

    // 마커 데이터 받아오기
    fun getMarkerDetail() {
        service.getMarkerContribution("1")
            .enqueue(object : Callback<Array<MarkerContributionDto>> {
                override fun onResponse(
                    call: retrofit2.Call<Array<MarkerContributionDto>>,
                    response: retrofit2.Response<Array<MarkerContributionDto>>
                ) {
                    if (response.isSuccessful) {
                        var result: Array<MarkerContributionDto>? = response.body()

                        // overlay 선긋기
                        val polyline = MapPolyline()
                        polyline.tag = 1000
                        polyline.lineColor = Color.argb(128, 255, 51, 0)

                        if (result != null) {
                            for (i in result) {
                                //Log.d("???", "$i")
                                val marker = MapPOIItem()
                                marker.apply {
                                    itemName = i.id.toString()   // 마커 이름
                                    mapPoint =
                                        MapPoint.mapPointWithGeoCoord(i.latitude, i.longitude)
                                    markerType =
                                        MapPOIItem.MarkerType.CustomImage    // 마커타입을 커스텀 마커로 지정

                                    // 다섯개 이상이면 별, 아니면 흑색
                                    customImageResourceId = if (i.count >= 5){
                                        R.drawable.start
                                    }else{
                                        R.drawable.notstar
                                    }
                                    // customSelectedImageResourceId = R.drawable.ic_launcher_foreground       // 클릭 시 커스텀 마커 이미지
                                    isCustomImageAutoscale =
                                        false // hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.
                                }
                                binding.mapView.addPOIItem(marker)

                                // Polyline 컬러 포인트 지정
                                polyline.addPoint(MapPoint.mapPointWithGeoCoord(i.latitude,  i.longitude))
                            }

                            binding.mapView.addPolyline(polyline)
                        }

                        println("6666 ${response.code()}")
                        //content = result as Nothing?;
                    } else {
                        println("1111, ${response.code()}")
                    }
                }

                override fun onFailure(
                    call: retrofit2.Call<Array<MarkerContributionDto>>,
                    t: Throwable
                ) {
                    println("2222 ${t.message}")
                    println("3333 $call")
                }

            })
    }


    // 초기 화면 id
    private fun initView() {
        // 정보 가져오기
        getMarkerDetail()

        this.window?.apply {
            this.statusBarColor = Color.TRANSPARENT
            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }

        //초기 지도 설정
        binding.mapView.setMapCenterPoint(
            MapPoint.mapPointWithGeoCoord(33.3616666, 126.5291666),
            true
        )
        binding.mapView.setZoomLevel(8, true)

    }


    // 내위치
    private fun myPositionButton() {
        binding.myPosition.setOnClickListener {
            if (checkLocationService()) {
                // GPS가 켜져있을 경우
                permissionCheck()
                binding.mapView.setZoomLevel(1, true)
            } else {
                // GPS가 꺼져있을 경우
                Toast.makeText(this, "GPS를 켜주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // 리뷰 버튼
    @SuppressLint("MissingPermission")
    private fun reviewButton() {
        binding.reviewButton.setOnClickListener {
            // 현재 위도, 경도 가져오기
            val lm: LocationManager =
                getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val userNowLocation: Location =
                lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)!!
            val uLatitude = userNowLocation.latitude       // 위도
            val uLongitude = userNowLocation.longitude      // 경도

            val intent = Intent(this, AddReviewActivity::class.java)
            intent.putExtra("uLatitude", "$uLatitude")
            intent.putExtra("uLongitude", "$uLongitude")

            startActivity(intent)
        }
    }


    // 마커 클릭 이벤트 리스너
    private val eventListener = MarkerEventListener(this)
    private fun markerClickButton() {
        // 마커 클릭 이벤트 리스너
        binding.mapView.setPOIItemEventListener(eventListener)
    }


    // 마커 클릭 이벤트 리스너
    class MarkerEventListener(private val context: Context) : MapView.POIItemEventListener {
        override fun onPOIItemSelected(mapView: MapView?, poiItem: MapPOIItem?) {
            // 마커 클릭 시
        }

        override fun onCalloutBalloonOfPOIItemTouched(mapView: MapView?, poiItem: MapPOIItem?) {
            // 말풍선 클릭 시 (Deprecated)
            // 이 함수도 작동하지만 그냥 아래 있는 함수에 작성하자
        }

        override fun onCalloutBalloonOfPOIItemTouched(
            mapView: MapView?,
            poiItem: MapPOIItem?,
            buttonType: MapPOIItem.CalloutBalloonButtonType?
        ) {
            // 말풍선 클릭 시
            val intent = Intent(context, MarkerInfoActivity::class.java)
            // poiItem.itemName 이름 식별자
            intent.putExtra("itemName", poiItem!!.itemName)
            context.startActivity(intent)
        }

        override fun onDraggablePOIItemMoved(
            mapView: MapView?,
            poiItem: MapPOIItem?,
            mapPoint: MapPoint?
        ) {
            // 마커의 속성 중 isDraggable = true 일 때 마커를 이동시켰을 경우
        }
    }


    // 위치 권한 확인
    private fun permissionCheck() {
        val preference = getPreferences(MODE_PRIVATE)
        val isFirstCheck = preference.getBoolean("isFirstPermissionCheck", true)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 권한이 없는 상태
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                // 권한 거절 (다시 한 번 물어봄)
                val builder = AlertDialog.Builder(this)
                builder.setMessage("현재 위치를 확인하시려면 위치 권한을 허용해주세요.")
                builder.setPositiveButton("확인") { _, _ ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        ACCESS_FINE_LOCATION
                    )
                }
                builder.setNegativeButton("취소") { _, _ ->

                }
                builder.show()
            } else {
                if (isFirstCheck) {
                    // 최초 권한 요청
                    preference.edit().putBoolean("isFirstPermissionCheck", false).apply()
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        ACCESS_FINE_LOCATION
                    )
                } else {
                    // 다시 묻지 않음 클릭 (앱 정보 화면으로 이동)
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage("현재 위치를 확인하시려면 설정에서 위치 권한을 허용해주세요.")
                    builder.setPositiveButton("설정으로 이동") { _, _ ->
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:$packageName")
                        )
                        startActivity(intent)
                    }
                    builder.setNegativeButton("취소") { _, _ ->

                    }
                    builder.show()
                }
            }
        } else {
            // 권한이 있는 상태
            startTracking()
        }
    }

    // 권한 요청 후 행동
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ACCESS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 요청 후 승인됨 (추적 시작)
                Toast.makeText(this, "위치 권한이 승인되었습니다", Toast.LENGTH_SHORT).show()
                startTracking()
            } else {
                // 권한 요청 후 거절됨 (다시 요청 or 토스트)
                Toast.makeText(this, "위치 권한이 거절되었습니다", Toast.LENGTH_SHORT).show()
                permissionCheck()
            }
        }
    }

    // GPS가 켜져있는지 확인
    private fun checkLocationService(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    // 위치추적 시작
    private fun startTracking() {
        binding.mapView.currentLocationTrackingMode =
            MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
    }

    // 액티비티가 파괴될 때..
    override fun onDestroy() {
        // onDestroy 에서 binding class 인스턴스 참조를 정리해주어야 한다.
        mBinding = null
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        // 정보 가져오기
        getMarkerDetail()
    }

}
