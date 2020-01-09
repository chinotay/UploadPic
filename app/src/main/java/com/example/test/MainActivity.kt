package com.example.test

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Intent
import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.View.OnClickListener
import android.webkit.MimeTypeMap
import android.widget.*
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.google.android.gms.auth.api.signin.internal.Storage
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.UploadTask.TaskSnapshot
import kotlinx.android.synthetic.main.activity_main.*

//declare to use glide
@GlideModule
class AppGlideModule : AppGlideModule()

class MainActivity : AppCompatActivity() {

    private var PICK_IMAGE_REQUEST = 1
    private var mButtonChooseImage: Button? = null
    private var mButtonUpload: Button? = null
    private var mTextViewShowUpload: TextView? = null
    private var mEditTextFileName: EditText? = null
    private var mImageView: ImageView? = null
    private var mProgressBar: ProgressBar? = null

    private var mImageUri: Uri? = null

    private var aStorageRef: StorageReference? = null
    private var aDatabaseReference: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //initialize variable
        aStorageRef = FirebaseStorage.getInstance().getReference("workout")
        aDatabaseReference = FirebaseDatabase.getInstance().getReference("workout")
        mButtonChooseImage = findViewById(R.id.button_choose_gif)
        mButtonUpload = findViewById((R.id.button_upload))
        mTextViewShowUpload = findViewById(R.id.text_view_show_uploads)
        mEditTextFileName = findViewById(R.id.edit_text_file_name)
        mImageView = findViewById(R.id.image_view)
        mProgressBar = findViewById(R.id.progress_bar)

        //when click choose file
        mButtonChooseImage!!.setOnClickListener {
            openFileChooser()
        }
        mButtonUpload!!.setOnClickListener {
            uploadFile()
        }
        mTextViewShowUpload!!.setOnClickListener {
            openImagesActivity()
        }
    }

    private fun openFileChooser() {
        //open a file chooser to allow user choose photo from file
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun getFileExtension(uri: Uri): String? {
        //get file extension from image(in our case is gif)
        var cR: ContentResolver = contentResolver
        var mime: MimeTypeMap = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR.getType(uri))
    }

    private fun uploadFile() {
        if (mImageUri != null) {
            var fileReference: StorageReference? = aStorageRef?.child(
                "." + System.currentTimeMillis() + this.getFileExtension(mImageUri!!) // the reference will be like 123456.gif
            )
            val uploadTask = fileReference?.putFile(mImageUri!!)                //declare upload task
            val urlTask = uploadTask?.continueWithTask { task ->                //declare a task for upload correct url
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                fileReference?.downloadUrl
            }?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result               //Url is download link

                    var upload: workout = workout(
                        mEditTextFileName?.text.toString().trim(),
                        downloadUri.toString()                          //download url is save to retireve next time
                    )
                    //it.uploadSessionUri.toString())
                    var uploadId = aDatabaseReference?.push()?.key
                    if (uploadId != null) {
                        aDatabaseReference?.child(uploadId)?.setValue(upload)
                    }
                }
            }
            uploadTask?.addOnSuccessListener {
                Toast.makeText(this@MainActivity, "Upload successfully", Toast.LENGTH_LONG).show()
                }
                ?.addOnFailureListener {
                    Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_SHORT).show()
                }
                ?.addOnProgressListener {
                    var progress = (100.0 * it.bytesTransferred / it.totalByteCount)
                    mProgressBar?.progress = progress.toInt()
                }

        } else {
            Toast.makeText(
                this@MainActivity, "No file selected.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            mImageUri = data.data
            //use picasso show image(but gif cannot move)
            //Picasso.get().load(mImageUri).into(mImageView)

            //use glide show gif
            Glide.with(this)
                .load(mImageUri)//Url of the image
                .into(mImageView)//show on image view
        }

    }

    private fun openImagesActivity() {
        startActivity(Intent(this, gifActivity::class.java))
    }
}
