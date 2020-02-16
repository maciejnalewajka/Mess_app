package pl.example.messenger.messenger.messages

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.example.messenger.messenger.R
import pl.example.messenger.messenger.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class NewMessageActivity : AppCompatActivity() {

    companion object {
        const val USER_KEY = "USER_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_new_message)
        fetchUsers()
    }

    private fun fetchUsers(){
        val ref = FirebaseDatabase.getInstance().getReference("/users/")

        ref.addListenerForSingleValueEvent(object: ValueEventListener{


            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()
                p0.children.forEach {
                    val user = it.getValue(User::class.java)
                    adapter.add(UserItem(user!!))

                    adapter.setOnItemClickListener { item, view ->

                        val userItem = item as UserItem

                        val intent = Intent(view.context, ChatActivity::class.java)
                        intent.putExtra(USER_KEY, userItem.user)
                        startActivity(intent)
                        finish()
                    }


                }
                recyclerview_newmessage.adapter = adapter
            }


            override fun onCancelled(p0: DatabaseError) {
            }

        })
    }
}

class UserItem(val user: User): Item<ViewHolder>(){

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_userRowNewMessage.text = user.username

        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.imageView_userRowNewMessage)
    }

    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }

}