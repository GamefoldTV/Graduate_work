package ru.netology.nework.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.activity.FeedJobsFragment.Companion.userId
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.viewmodel.AuthViewModel
import javax.inject.Inject

val coordinatesMoscow = Point(55.7522200, 37.6155600)
val dateFormat = "dd.MM.yyyy"
val timeFormat = "HH:mm"


@AndroidEntryPoint
class AppActivity : AppCompatActivity(R.layout.activity_app) {
    @Inject
    lateinit var auth: AppAuth
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authViewModel.data.observe(this){
            invalidateOptionsMenu()
            if (it.id == 0L) {
                findNavController(R.id.nav_host_fragment)
                    .navigate(R.id.action_feedFragment_to_loginFragment)
            } else {
                val welcome = getString(R.string.welcome)
                Toast.makeText(this@AppActivity,"$welcome ${it.name}", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.wall -> {
                findNavController(R.id.nav_host_fragment).navigateUp()
                findNavController(R.id.nav_host_fragment)
                    .navigate(R.id.action_feedFragment_to_wallFragment)
                true
            }
            R.id.posts -> {
                findNavController(R.id.nav_host_fragment).navigateUp()
                true
            }
            R.id.events -> {
                findNavController(R.id.nav_host_fragment).navigateUp()
                findNavController(R.id.nav_host_fragment)
                    .navigate(R.id.action_feedFragment_to_feedEventFragment)
                true
            }
            R.id.jobs -> {
                findNavController(R.id.nav_host_fragment).navigateUp()
           //     findNavController(R.id.nav_host_fragment).navigate(R.id.action_feedFragment_to_feedJobsFragment)
                findNavController(R.id.nav_host_fragment).navigate(R.id.action_feedFragment_to_feedJobsFragment,
                    Bundle().apply {
                        userId = auth.authStateFlow.value.id
                    })


                true
            }
            R.id.signout -> {
                findNavController(R.id.nav_host_fragment).navigateUp()
                auth.removeAuth()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}