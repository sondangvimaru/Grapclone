package com.cnt57cl.grapclonfordriver

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.rengwuxian.materialedittext.MaterialEditText
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import android.graphics.Color.DKGRAY
import cc.cloudist.acplibrary.ACProgressConstant
import cc.cloudist.acplibrary.ACProgressFlower
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T




class MainActivity : AppCompatActivity() {

        var auth:FirebaseAuth?=null
         var data:FirebaseDatabase?=null
        var users:DatabaseReference?=null
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CalligraphyConfig.initDefault(CalligraphyConfig.Builder().
            setDefaultFontPath("fonts/Arkhip_font.ttf").
            setFontAttrId(R.attr.fontPath).
            build())
        setContentView(R.layout.activity_main)
        tv_title.text="Thanh son app \n for Driver"
        auth=FirebaseAuth.getInstance()
        data= FirebaseDatabase.getInstance()
        users=data?.getReference("Users")

        btn_singup.setOnClickListener(View.OnClickListener {

            showsignup()

        })
        btn_login.setOnClickListener( View.OnClickListener {
        showsingin()

        })
    }

    fun  showsignup()
    {
        val alert: AlertDialog.Builder =AlertDialog.Builder(this)
        alert.setTitle("Register")
        alert.setMessage("Please use email to register")
        val view:View= LayoutInflater.from(this).inflate(R.layout.signup,null,false)

        val editEmail= view.findViewById<MaterialEditText>(R.id.ed_Email)
        val editpassword= view.findViewById<MaterialEditText>(R.id.ed_password)
        val edrepassword=view.findViewById<MaterialEditText>(R.id.ed_repassword)
        alert.setView(view)
        alert.setPositiveButton("Register",DialogInterface.OnClickListener { dialog, which ->

            dialog.dismiss()
            if(TextUtils.isEmpty(editEmail.text.toString()))
            {

                Snackbar.make(root_view,"vui lòng nhập Email",Snackbar.LENGTH_SHORT).show()


            }
            if(TextUtils.isEmpty(editpassword.text.toString()))
            {

                Snackbar.make(root_view,"vui lòng nhập password",Snackbar.LENGTH_SHORT).show()

            }
            if(TextUtils.isEmpty(edrepassword.text.toString()))
            {

                Snackbar.make(root_view,"vui lòng xác nhận password",Snackbar.LENGTH_SHORT).show()

            }
            if(!editpassword.text.toString().trim().equals(edrepassword.text.toString().trim()))
            {
                Snackbar.make(root_view,"mật khẩu xác nhận không trùng nhau",Snackbar.LENGTH_SHORT).show()

            }
            if(!TextUtils.isEmpty(editEmail.text.toString()) && !TextUtils.isEmpty(editpassword.text.toString()) && !TextUtils.isEmpty(edrepassword.text.toString()) && edrepassword.text.toString().trim().equals(editpassword.text.toString().trim()))
            {

                auth?.createUserWithEmailAndPassword(editEmail.text.toString(),editpassword.editableText.toString())
                    ?.addOnSuccessListener { authResult ->

                        val us:user = user(editEmail.text.toString(),editpassword.text.toString())

                        users?.child(FirebaseAuth.getInstance().currentUser!!.uid)
                            ?.setValue(us)
                            ?.addOnSuccessListener { void ->
                                Snackbar.make(root_view,"Đăng ký thành công!",Snackbar.LENGTH_SHORT).show()


                            }
                            ?.addOnFailureListener { exception ->
                                Snackbar.make(root_view,"Lỗi:"+exception.message,Snackbar.LENGTH_SHORT).show()

                                Log.d("loi",exception.message)
                            }
                    }
                    ?.addOnFailureListener { exception ->
                        Snackbar.make(root_view,"Lỗi:"+exception.message,Snackbar.LENGTH_SHORT).show()
                        Log.d("loi",exception.message)

                    }
            }



        })
        alert.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->


            dialog.dismiss()

        })


        alert.create().show()
    }

    fun showsingin()
    {
        val alert: AlertDialog.Builder =AlertDialog.Builder(this)
        alert.setTitle("Signin")
        alert.setMessage("Please use email to signin")
        val view:View= LayoutInflater.from(this).inflate(R.layout.login,null,false)

        val editEmail= view.findViewById<MaterialEditText>(R.id.ed_Email)
        val editpassword= view.findViewById<MaterialEditText>(R.id.ed_password)
        alert.setView(view)
        alert.setPositiveButton("Login",DialogInterface.OnClickListener { dialog, which ->

            btn_login.isEnabled=false
            dialog.dismiss()
            if(TextUtils.isEmpty(editEmail.text.toString()))
            {

                Snackbar.make(root_view,"vui lòng nhập Email",Snackbar.LENGTH_SHORT).show()


            }
            if(TextUtils.isEmpty(editpassword.text.toString()))
            {

                Snackbar.make(root_view,"vui lòng nhập password",Snackbar.LENGTH_SHORT).show()

            }


            if(!TextUtils.isEmpty(editEmail.text.toString()) && !TextUtils.isEmpty(editpassword.text.toString()) ) {


                val di = ACProgressFlower.Builder(this)
                    .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                    .themeColor(Color.WHITE)
                    .text("Vui lòng chờ")
                    .fadeColor(Color.DKGRAY).build()

                di.show()
                auth?.signInWithEmailAndPassword(editEmail.text.toString(),editpassword.text.toString())
                    ?.addOnSuccessListener { authResult ->
                        di.dismiss()
                        startActivity( Intent(this,Home::class.java))
                        Snackbar.make(root_view,"Đăng nhập thành công",Snackbar.LENGTH_SHORT).show()
                        Log.d("login","thanh cong")
                        finish()
                    }
                    ?.addOnFailureListener { exception ->

                        di.dismiss()
                        btn_login.isEnabled=true
                        Snackbar.make(root_view,"Lỗi:"+exception.message,Snackbar.LENGTH_SHORT).show()


                    }

            }



        })
        alert.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->


            dialog.dismiss()

        })


        alert.show()
    }
}
