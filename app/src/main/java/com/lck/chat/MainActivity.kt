package com.lck.chat

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.lck.chat.databinding.ActivityMainBinding
import com.livechatinc.inappchat.ChatWindowConfiguration
import com.livechatinc.inappchat.ChatWindowErrorType
import com.livechatinc.inappchat.ChatWindowView
import com.livechatinc.inappchat.models.NewMessageModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setWebView()

        //Проверяю в бесконечном цикле, если ориентация вертикальная, показываю чат, если горизотальная, скрываю
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT){
            lifecycleScope.launch(Dispatchers.IO) {
                while (true){
                    if (binding.chatWindow.isShown){
                        withContext(Dispatchers.Main) { binding.floatId.visibility = View.GONE }
                    } else {
                        withContext(Dispatchers.Main) { binding.floatId.visibility = View.VISIBLE }
                    }
                }
            }

            // проверяю по клику на лого чата, если он инициализирован был, то просто показываю его, если нет, инициализирую
            binding.floatId.setOnClickListener {
                if (binding.chatWindow.isInitialized){
                    binding.chatWindow.showChatWindow()
                } else {
                    startInitChat()
                }
            }
        }else binding.floatId.visibility = View.GONE
    }

    /**
     * Настройка WebView
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun setWebView(){
        binding.webViewId.webViewClient = WebViewClient()
        binding.webViewId.loadUrl("https://www.google.com.ua")
        binding.webViewId.settings.javaScriptEnabled = true
    }

    /**
     * Запускаю и настраиваю чат
     */
    private fun startInitChat(){
        //указываю номер лицензии
        val config = ChatWindowConfiguration.Builder().setLicenceNumber("14090775").build()
        binding.chatWindow.setUpWindow(config)
        //переопределяю слушатель ивентов, по факту для загрузки данных
        binding.chatWindow.setUpListener(object : ChatWindowView.ChatWindowEventsListener{
            override fun onChatWindowVisibilityChanged(visible: Boolean) {}

            //не понял зачем)
            override fun onNewMessage(
                message: NewMessageModel?,
                windowVisible: Boolean
            ) {}

            //для возможности загружать файлы
            override fun onStartFilePickerActivity(intent: Intent?, requestCode: Int) {
                startActivityForResult(intent, requestCode)
            }

            //по своему обработать ошибки
            override fun onError(
                errorType: ChatWindowErrorType?,
                errorCode: Int,
                errorDescription: String?
            ): Boolean { return false}

            //нестандартно обработать uri
            override fun handleUri(uri: Uri?): Boolean {return false}

        })
        binding.chatWindow.initialize()
        binding.chatWindow.showChatWindow()
    }

    /**
     * Обрабатываю ответ, для отправки файлов
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        binding.chatWindow.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}