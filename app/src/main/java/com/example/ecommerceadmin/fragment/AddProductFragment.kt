package com.example.ecommerceadmin.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ecommerceadmin.R
import com.example.ecommerceadmin.adapter.AddProductImageAdapter
import com.example.ecommerceadmin.databinding.FragmentAddProductBinding
import com.example.ecommerceadmin.databinding.FragmentProductBinding
import com.example.ecommerceadmin.models.AddProductModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID


class AddProductFragment : Fragment() {

    private lateinit var binding:FragmentAddProductBinding
    private lateinit var list:ArrayList<Uri>
    private lateinit var listImages:ArrayList<String>
    private lateinit var adapter: AddProductImageAdapter
    private var coverImage:Uri? = null
    private lateinit var dialog:Dialog
    private var coverImgUrl:String? = ""
    private lateinit var categoryList:ArrayList<String>



    private var launchGalleryActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == Activity.RESULT_OK){
            coverImage = it.data!!.data
            binding.productCoverImg.setImageURI(coverImage)
            binding.productCoverImg.visibility = View.VISIBLE
        }
    }

    private var imageUrl: Uri? = null

    private var launchProductActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == Activity.RESULT_OK){
            imageUrl = it.data!!.data
            list.add(imageUrl!!)
            adapter.notifyDataSetChanged()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddProductBinding.inflate(layoutInflater)

        list = ArrayList()
        listImages = ArrayList()

        binding.selectCoverImgBtn.setOnClickListener {
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type  = "image/*"
            launchGalleryActivity.launch(intent)
        }

        binding.productCoverImgBtn.setOnClickListener {
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"
            launchProductActivity.launch(intent)
        }
        setProductCategory()

        adapter = AddProductImageAdapter(list)
        binding.prodcutImgRecyclerView.adapter = adapter

        binding.submitBtn.setOnClickListener {
            validateData()
        }
        return binding.root
    }

    private fun validateData() {
        if(binding.ProductNameEt.text.toString().isEmpty()){
            binding.ProductNameEt.requestFocus()
            binding.ProductNameEt.error = "Empty Field"
        }
        else if(binding.ProductDescriptionEt.text.toString().isEmpty()){
            binding.ProductDescriptionEt.requestFocus()
            binding.ProductDescriptionEt.error = "Empty Field"
        }else if(binding.productMrpEt.text.toString().isEmpty()){
            binding.productMrpEt.requestFocus()
            binding.productMrpEt.error = "Empty Field"
        }else if(binding.productSpEt.text.toString().isEmpty()){
            binding.productSpEt.requestFocus()
            binding.productSpEt.error = "Empty Field"
        }
        else if(coverImage == null){
            Toast.makeText(requireContext(),"Please Select cover Image",Toast.LENGTH_SHORT).show()
        }else if(list.size < 1){
            Toast.makeText(requireContext(),"Please Select product Images",Toast.LENGTH_SHORT).show()
        }else{
            uploadImage()
        }

    }

    private fun uploadImage() {
        val filename = UUID.randomUUID().toString()+".jpg"

        val firebaseStorage = FirebaseStorage.getInstance().reference.child("products/$filename")
        firebaseStorage.putFile(coverImage!!)
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener {image->
                    Toast.makeText(requireContext(),"successful uploaded",Toast.LENGTH_SHORT).show()
                    coverImgUrl = image.toString()

                    uploadProductImage()
                }
            }
            .addOnFailureListener{
                Toast.makeText(requireContext(),"something went wrong before store",Toast.LENGTH_SHORT).show()
            }
    }

    private var i = 0
    private fun uploadProductImage() {
        val filename = UUID.randomUUID().toString()+".jpg"

        val firebaseStorage = FirebaseStorage.getInstance().reference.child("products/$filename")
        firebaseStorage.putFile(list[i])
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener {image->
                    Toast.makeText(requireContext(),"successful uploaded",Toast.LENGTH_SHORT).show()
                    listImages.add(image!!.toString())

                    if(list.size == listImages.size){
                        saveToDatabase()
                    }else
                    {
                        i += 1
                        uploadProductImage()
                    }

                }
            }
            .addOnFailureListener{
                Toast.makeText(requireContext(),"something went wrong before store",Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveToDatabase() {
        val db = FirebaseDatabase.getInstance().getReference("products")
        val Key = db.push().key

        val map = HashMap<String,Any>()

        val  data = AddProductModel(
            binding.ProductNameEt.text.toString(),
            binding.ProductDescriptionEt.text.toString(),
            coverImgUrl.toString(),
            categoryList[binding.productSpinner.selectedItemPosition],
            Key,
            binding.productMrpEt.text.toString(),
            binding.productSpEt.text.toString(),
            listImages

        )
        map["productModel"] = data
            Key?.let{
            db.child(it).setValue(map).addOnCompleteListener{dbtask->
                if(dbtask.isSuccessful)
                {
                    Toast.makeText(requireContext(),"Product Added data saved successfully on db",Toast.LENGTH_SHORT).show()
                    binding.ProductNameEt.text = null
                    binding.ProductDescriptionEt.text = null
                    binding.productSpEt.text = null
                    binding.productMrpEt.text = null
                }
                else{
                    Toast.makeText(requireContext(),"data is not saved successfully",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setProductCategory() {
        categoryList = ArrayList()

        val dbref = FirebaseDatabase.getInstance().getReference("Categories").child("items")
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    for(data in snapshot.children)
                    {
                        val catData = data.child("cat").getValue(String::class.java)
                        categoryList.add(catData!!)
                    }

                    categoryList.add(0,"Select Category")

                    val arrayAdapter = ArrayAdapter(requireContext(),R.layout.dropdown_item_layout,categoryList)
                    binding.productSpinner.adapter = arrayAdapter
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
}