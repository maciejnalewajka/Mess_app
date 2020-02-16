package pl.example.messenger.messenger.messages

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.example.messenger.messenger.R
import pl.example.messenger.messenger.models.ChatMessage
import pl.example.messenger.messenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()

    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_chat)

        recyclerView_chat.adapter = adapter

        toUser = intent.getParcelableExtra(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username

        listenForMessages()

        send_button_chat.setOnClickListener {
            performSendMessage()
        }

        recyclerView_chat.setOnClickListener{
            hideKeyboard()
        }

    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user_mess/$fromId/$toId")
        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return

                if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                    val currentUser = LatestMessagesActivity.currentUser
                            ?: return
                    adapter.add(ChatFromItem(chatMessage.text, currentUser, chatMessage.timestamp))
                } else {
                    adapter.add(ChatToItem(chatMessage.text, toUser!!, chatMessage.timestamp))
                }
                recyclerView_chat.scrollToPosition(adapter.itemCount -1)

            }

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })
    }

    @SuppressLint("SimpleDateFormat")
    private fun performSendMessage() {
        val text = editText_chat.text.toString()

        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user.uid
        if (fromId == null) return

        val ref = FirebaseDatabase.getInstance().getReference("/user_mess/$fromId/$toId").push()
        val toref = FirebaseDatabase.getInstance().getReference("/user_mess/$toId/$fromId").push()

        val mili = System.currentTimeMillis()
        val sdf = SimpleDateFormat("dd.MM.yy HH:mm")
        val resultdate = Date(mili)
        val date = sdf.format(resultdate)
        val chatMessage = ChatMessage(ref.key!!, text, fromId, toId, date)
        if(fromId != toId){
        ref.setValue(chatMessage)}
        toref.setValue(chatMessage)
                .addOnSuccessListener {
                    editText_chat.setText("")
                    recyclerView_chat.scrollToPosition(adapter.itemCount -1)
                }

        val fromlatestRef = FirebaseDatabase.getInstance().getReference("/latest_mess/$fromId/$toId")
        fromlatestRef.setValue(chatMessage)
        val tolatestRef = FirebaseDatabase.getInstance().getReference("/latest_mess/$toId/$fromId")
        tolatestRef.setValue(chatMessage)

    }

    private fun Activity.hideKeyboard() {
        hideKeyboard(if (currentFocus == null) View(this) else currentFocus)
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

class ChatFromItem(val text: String, private val user: User, private val time: String): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_from_chat.text = text
        viewHolder.itemView.textView_time_from.text = time
        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.imageView_from_chat
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, private val user: User, private val time: String): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_to_chat.text = text
        viewHolder.itemView.textView_time_to.text = time

        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.imageView_to_chat
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}

