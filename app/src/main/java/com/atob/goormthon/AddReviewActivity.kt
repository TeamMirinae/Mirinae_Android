package com.atob.goormthon

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.atob.goormthon.databinding.ActivityAddReviewBinding
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap


class AddReviewActivity : AppCompatActivity() {

    private var mBinding: ActivityAddReviewBinding? = null
    private val binding get() = mBinding!!

    private val REQUEST_CODE = 0 //갤러리
    private val PICK_FROM_CAMERA = 1 //카메라

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityAddReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 완료 버튼
        checkButton()
        // 감정 선택
        selectEmotion()
        // 카메라
        intentCamera()
        // 갤러리
        intentGallery()
        // 좋았던 점
        likes()
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

        @POST("/v0/marker/create/{userId}")
        fun createMarker(
            @Path("userId") userId : String,
            @Body body: HashMap<String, Any>,

        ): Call<MarkerDetailDto>
    }
    // timeout setting 해주기
    var okHttpClient = OkHttpClient().newBuilder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()


    val retrofit = Retrofit.Builder().baseUrl("https://mirinae-sbnbk.run.goorm.io").client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create()).build();
    val service = retrofit.create(RetrofitService::class.java);

    // 현재 표정
    private var nowFace : Int ?= null
    // 좋았던 점
    val categoryList = mutableListOf<Int>()

    // 좋았던 점
    private fun likes(){
        // 토글 버튼
        val text = binding.ThemedToggleButtonGroup.selectedButtons
        for (i in text) {
            var num = 0
            when (i.text) {
                resources.getString(R.string.emotion0) -> num = 0
                resources.getString(R.string.emotion1) -> num = 1
                resources.getString(R.string.emotion2) -> num = 2
                resources.getString(R.string.emotion3) -> num = 3
                resources.getString(R.string.emotion4) -> num = 4
                resources.getString(R.string.emotion5) -> num = 5
                resources.getString(R.string.emotion6) -> num = 6
                resources.getString(R.string.emotion7) -> num = 7
            }
            categoryList.add(num)
        }
    }

    // 보낼 값
    fun getMarkerDetail() {

        val ll = intent.getStringExtra("uLatitude")
        val lolo = intent.getStringExtra("uLongitude")
        //Log.d("??---", ll!!)
        //Log.d("??aaa", lolo!!)
        val hashmap = HashMap<String, Any>()
        hashmap["emotion"] = nowFace!!
        hashmap["atmosphere"] = if (categoryList.size == 1){ arrayOf(categoryList[0]) } else { arrayOf(categoryList[0],categoryList[1]) }
        hashmap["latitude"] = ll!!.toDouble()
        hashmap["longitude"] = lolo!!.toDouble()
        service.createMarker("1", hashmap).enqueue(object : Callback<MarkerDetailDto> {
            override fun onResponse(
                call: Call<MarkerDetailDto>,
                response: Response<MarkerDetailDto>
            ) {
                if (response.isSuccessful) {
                    var result: MarkerDetailDto? = response.body()
                    finish()

                    Log.d("111??","$result")
                    Log.d("test??", result.toString())
                    Log.d("222??","${response.code()}")


                } else {
                    if (response.code() == 403){
                        Toast.makeText(applicationContext, "이미 등록한 장소입니다. :)",Toast.LENGTH_SHORT).show()
                    }
                    Log.d("333??","${response.code()}")
                    Log.d("4444??","${response.errorBody()}")
                    Log.d("555??","${response.body()}")
                }
            }

            override fun onFailure(call: Call<MarkerDetailDto>, t: Throwable) {

                println("444?? ${t.message}")
                println("555?? $call")
            }
        })
    }

    // 리뷰 등록 버튼
    private fun checkButton() {
        binding.check.setOnClickListener {
            // 좋았던 점 가져오기
            likes()
            if (nowFace == null || categoryList.isEmpty() ){
                /*Log.d("???", "$nowFace")
                Log.d("????", "${categoryList}")*/
                Toast.makeText(this, "카테고리를 채워주세요 :)",Toast.LENGTH_SHORT).show()
            }else{
                getMarkerDetail()
            }
        }
    }

    // 감정 선택
    private fun selectEmotion() {
        binding.selectEmotion.setOnClickListener {
            val dialog = SelectEmotionDialog(this)
            dialog.showDialog()
            dialog.setOnClickListener(object : SelectEmotionDialog.OnDialogClickListener {
                override fun onClicked(number: Int) {
                    nowFace = number
                    var position = 0
                    when (number) {
                        0 -> position = R.drawable.emotion1
                        1 -> position = R.drawable.emotion2
                        2 -> position = R.drawable.emotion3
                        3 -> position = R.drawable.emotion4
                        4 -> position = R.drawable.emotion5
                        5 -> position = R.drawable.emotion6
                    }
                    binding.selectEmotion.setImageResource(position)
                }
            })
        }
    }

    // 카메라
    private fun intentCamera() {
        binding.camera.setOnClickListener {
            //사진촬영
            val cameraPermissionCheck = ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            )
            if (cameraPermissionCheck != PackageManager.PERMISSION_GRANTED) { // 권한이 없는 경우
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.CAMERA),
                    1000
                )
            } else { //권한이 있는 경우
                val REQUEST_IMAGE_CAPTURE = 1
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                    takePictureIntent.resolveActivity(packageManager)?.also {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                    }
                }
            }
        }
    }

    // 카메라 허용
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) { //거부
                //Toast.makeText(this, "거부", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // 갤러리
    private fun intentGallery() {
        binding.gallery.setOnClickListener {
            // 갤러리
            val intent = Intent()
            intent.type = "image/png"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, REQUEST_CODE)
        }
    }

    var bitmap: Bitmap? = null
    // 결과 가져오기
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val selectedImageUri: Uri? = data?.data
        // 갤러리
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    bitmap = selectedImageUri?.let {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            ImageDecoder.createSource(contentResolver,
                                it
                            )
                        } else {
                            // ("VERSION.SDK_INT < P")
                        }
                    }?.let { ImageDecoder.decodeBitmap(it as ImageDecoder.Source) }
                }catch (e: IOException){
                    e.printStackTrace()
                }
                binding.imageView.setImageURI(selectedImageUri)
            } else {
                //Toast.makeText(this, "Cancel", Toast.LENGTH_LONG).show()
            }
        } else if (requestCode == PICK_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            val image = data?.extras?.get("data") as Bitmap
            binding.imageView.setImageBitmap(image)
        } else {
            //Toast.makeText(this, "error! sorry :(", Toast.LENGTH_LONG).show()
        }
    }


    // 액티비티가 파괴될 때..
    override fun onDestroy() {
        // onDestroy 에서 binding class 인스턴스 참조를 정리해주어야 한다.
        mBinding = null
        super.onDestroy()
    }
}