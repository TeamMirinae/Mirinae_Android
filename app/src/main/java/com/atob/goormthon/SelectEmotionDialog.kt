package com.atob.goormthon

import android.app.Dialog
import android.content.Context
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView

class SelectEmotionDialog (context: Context)
{
    private val dialog = Dialog(context)
    private lateinit var onClickListener: OnDialogClickListener

    fun setOnClickListener(listener: OnDialogClickListener)
    {
        onClickListener = listener
    }

    fun showDialog()
    {
        dialog.setContentView(R.layout.dialog_emotion_select)
        dialog.window!!.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.show()

        // val edit_name = dialog.findViewById<EditText>(R.id.name_edit)

        dialog.findViewById<ImageView>(R.id.imageView1).setOnClickListener {
            onClickListener.onClicked(0)
            dialog.dismiss()
        }
        dialog.findViewById<ImageView>(R.id.imageView2).setOnClickListener {
            onClickListener.onClicked(1)
            dialog.dismiss()
        }
        dialog.findViewById<ImageView>(R.id.imageView3).setOnClickListener {
            onClickListener.onClicked(2)
            dialog.dismiss()
        }
        dialog.findViewById<ImageView>(R.id.imageView4).setOnClickListener {
            onClickListener.onClicked(3)
            dialog.dismiss()
        }
        dialog.findViewById<ImageView>(R.id.imageView5).setOnClickListener {
            onClickListener.onClicked(4)
            dialog.dismiss()
        }
        dialog.findViewById<ImageView>(R.id.imageView6).setOnClickListener {
            onClickListener.onClicked(5)
            dialog.dismiss()
        }

        /*dialog.findViewById<Button>(R.id.finish_button).setOnClickListener {
            //onClickListener.onClicked(edit_name.text.toString())
            dialog.dismiss()
        }*/

    }

    interface OnDialogClickListener
    {
        fun onClicked(number: Int)
    }

}