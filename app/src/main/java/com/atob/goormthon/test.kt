/*
package com.atob.goormthon

import android.util.Log
import android.util.Log.*
import com.google.gson.annotations.SerializedName
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

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
    @GET("/v0/marker/{markerId}")
    fun getMarkerDetail(@Path("markerId") markerId: String): Call<MarkerDetailDto>

    @GET("/v0/marker/contribution/{markerId}")
    fun getMarkerContribution(@Path("markerId") markerId: String): Call<Array<MarkerContributionDto>>

    @Multipart
    @POST("/v0/marker/create/{userId}")
    fun createMarker(
        @Path("userId") userId: Int = 1,
        @Part("emotion") emotion: RequestBody,
        @Part("atmosphere") atmosphere: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("image") image: RequestBody,
        ): Call<MarkerDetailDto>
}

object Test {

    val retrofit = Retrofit.Builder().baseUrl("https://mirinae-sbnbk.run.goorm.io")
        .addConverterFactory(GsonConverterFactory.create()).build();
    val service = retrofit.create(RetrofitService::class.java);

    fun getMarkerDetail() {
        */
/*var input = HashMap<String, String>()
        input["college"] = "TEST"*//*


        service.createMarker("1").enqueue(object : Callback<MarkerDetailDto> {
            override fun onResponse(
                call: Call<MarkerDetailDto>,
                response: Response<MarkerDetailDto>
            ) {
                if (response.isSuccessful) {
                    var result: MarkerDetailDto? = response.body()
                    println("111 $result")
                    println("test ${result.toString()}")

                    println("222 ${response.code()}")


                    */
/* println("111 $result");
                    println("222 ${response.code()}");*//*


                } else {
                    println("333 ${response.code()}")
                }
            }

            override fun onFailure(call: Call<MarkerDetailDto>, t: Throwable) {
                println("444 ${t.message}")
                println("555 $call")
            }
        })
    }

}

fun main() {
    val test = Test;

    test.getMarkerDetail();
}*/
