package com.atob.goormthon

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.atob.goormthon.databinding.ActivityMarkerInfoBinding
import com.bumptech.glide.Glide
import com.google.gson.annotations.SerializedName
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

class MarkerInfoActivity : AppCompatActivity() {

    private var mBinding: ActivityMarkerInfoBinding? = null
    private val binding get() = mBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMarkerInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 해당 마커의 상세 정보 가져오기
        getMarkerDetail()

    }

    data class MarkerDetailDto(
        @SerializedName("id")
        val id: Int,

        @SerializedName("atmosphere")
        val atmosphere: Array<Int>,

        @SerializedName("emotion")
        val emotion: Int,

        @SerializedName("imageUrl")
        val imageUrl: String,

        @SerializedName("count")
        val count: Int,

        @SerializedName("standard")
        val standard: String,

        @SerializedName("dialect")
        val dialect: String,
    )


    interface RetrofitService {
        @GET("/v0/marker/{markerId}")
        fun getMarkerDetail(@Path("markerId") markerId: String): Call<MarkerDetailDto>
    }

    private val retrofit: Retrofit = Retrofit.Builder().baseUrl("https://mirinae-sbnbk.run.goorm.io")
        .addConverterFactory(GsonConverterFactory.create()).build()
    private val service: RetrofitService = retrofit.create(RetrofitService::class.java)

    fun getMarkerDetail() {
        val itemName = intent.getStringExtra("itemName")
        service.getMarkerDetail("$itemName").enqueue(object : Callback<MarkerDetailDto> {
            override fun onResponse(
                call: Call<MarkerDetailDto>,
                response: Response<MarkerDetailDto>
            ) {
                if (response.isSuccessful) {
                    var result: MarkerDetailDto? = response.body();
                    Log.d("111????" ,"$result")
                    // 내 감정
                    when (result!!.emotion) {
                        0 -> binding.selectEmotion.setImageResource(R.drawable.emotion1)
                        1 -> binding.selectEmotion.setImageResource(R.drawable.emotion2)
                        2 -> binding.selectEmotion.setImageResource(R.drawable.emotion3)
                        3 -> binding.selectEmotion.setImageResource(R.drawable.emotion4)
                        4 -> binding.selectEmotion.setImageResource(R.drawable.emotion5)
                        5 -> binding.selectEmotion.setImageResource(R.drawable.emotion6)
                    }

                    // 내 리뷰평
                    if (result.atmosphere.size == 2) {
                        var str2 = ""
                        when (result.atmosphere[1]) {
                            0 -> str2 = resources.getString(R.string.emotion0)
                            1 -> str2 = resources.getString(R.string.emotion1)
                            2 -> str2 = resources.getString(R.string.emotion2)
                            3 -> str2 = resources.getString(R.string.emotion3)
                            4 -> str2 = resources.getString(R.string.emotion4)
                            5 -> str2 = resources.getString(R.string.emotion5)
                            6 -> str2 = resources.getString(R.string.emotion6)
                            7 -> str2 = resources.getString(R.string.emotion7)
                        }
                        binding.like2.text = str2
                    } else {
                        binding.like2.visibility = View.GONE
                    }

                    var str1 = ""
                    when (result.atmosphere[0]) {
                        0 -> str1 = resources.getString(R.string.emotion0)
                        1 -> str1 = resources.getString(R.string.emotion1)
                        2 -> str1 = resources.getString(R.string.emotion2)
                        3 -> str1 = resources.getString(R.string.emotion3)
                        4 -> str1 = resources.getString(R.string.emotion4)
                        5 -> str1 = resources.getString(R.string.emotion5)
                        6 -> str1 = resources.getString(R.string.emotion6)
                        7 -> str1 = resources.getString(R.string.emotion7)
                    }
                    binding.like1.text = str1

                    // 200 내 리뷰, 2001 별이 된 리뷰
                    when(response.code()){
                        200->{
                            binding.scrollView.visibility = View.VISIBLE
                            binding.textView3.visibility = View.GONE
                            binding.ThemedToggleButtonGroup.visibility = View.GONE
                            binding.textView1.text = "아직 이름이 지어지지 않았어요."
                            binding.textView1.setTextColor(ContextCompat.getColor(this@MarkerInfoActivity, R.color.white))
                            binding.textView2.text = "아기별이 ${5-result.count}명을 더 기다려요"
                            Log.d("????","${result.count}")
                        }
                        201->{
                            binding.scrollView.visibility = View.VISIBLE
                            binding.textView1.text = result.dialect
                            binding.textView2.text = result.standard
                        }
                    }
                    // 사진 넣기
                    val defaultImage = R.drawable.ic_launcher_foreground
                    val url = result.imageUrl
                    Glide.with(this@MarkerInfoActivity)
                        .load(url) // 불러올 이미지 url
                        .placeholder(defaultImage) // 이미지 로딩 시작하기 전 표시할 이미지
                        .error(defaultImage) // 로딩 에러 발생 시 표시할 이미지
                        .fallback(defaultImage) // 로드할 url 이 비어있을(null 등) 경우 표시할 이미지
                        .circleCrop() // 동그랗게 자르기
                        .into(binding.imageView) // 이미지를 넣을 뷰

                    //println("222 ${response.code()}");

                } else {
                    //println("333 ${response.code()}")
                }
            }

            override fun onFailure(call: Call<MarkerDetailDto>, t: Throwable) {
                //println("444 ${t.message}")
                //println("555 $call")
            }
        })
    }


    // 액티비티가 파괴될 때..
    override fun onDestroy() {
        // onDestroy 에서 binding class 인스턴스 참조를 정리해주어야 한다.
        mBinding = null
        super.onDestroy()
    }

}