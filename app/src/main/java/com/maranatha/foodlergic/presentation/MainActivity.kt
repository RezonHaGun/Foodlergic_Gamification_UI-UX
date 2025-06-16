package com.maranatha.foodlergic.presentation

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.analytics.FirebaseAnalytics
import com.maranatha.foodlergic.R
import com.maranatha.foodlergic.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        // Log app launch event
        val bundle = Bundle()
        bundle.putString("message", "MainActivity opened")
        firebaseAnalytics.logEvent("app_launch", bundle)

        // Set up the Toolbar as the ActionBar
        setSupportActionBar(binding.toolbar)

        // Set up Navigation Controller
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val drawable = ContextCompat.getDrawable(this, R.drawable.arrow_back)
        drawable?.setTint(ContextCompat.getColor(this, R.color.white)) // <- warna custom
            binding.toolbar.navigationIcon=drawable

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.splashFragment,
                R.id.onBoardingFragment,
                R.id.loginFragment,
                R.id.registerFragment,
                R.id.predictResultFragment,
                R.id.leaderboardFragment,
                R.id.userProfileFragment,
                R.id.predictCameraFragment,
            ) // Specify top-level destinations
        )
        // Setup ActionBar with Navigation
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNavigationView.setupWithNavController(navController)

        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.menu.getItem(2).isEnabled = false

        val fragmentsWithBottomNav = setOf(
            R.id.homeFragment,
            R.id.userProfileFragment,
            R.id.leaderboardFragment
        )

        binding.fab.setOnClickListener {
            binding.navHostFragment.findNavController().navigate(R.id.predictCameraFragment)
        }
        // Add a listener to manage the visibility of the Toolbar (ActionBar)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.splashFragment,
                R.id.predictCameraFragment,
                R.id.onBoardingFragment,
                R.id.homeFragment,
                R.id.loginFragment,
                R.id.registerFragment,
                R.id.predictResultFragment,
                R.id.userProfileFragment,
                R.id.leaderboardFragment,
                R.id.predictFragment -> {
                    supportActionBar?.hide()
                }

                else -> {
                    supportActionBar?.show()
                }
            }
            if (destination.id in fragmentsWithBottomNav) {
                binding.bottomAppBar.visibility = View.VISIBLE
                binding.bottomNavigationView.visibility = View.VISIBLE
                binding.fab.visibility = View.VISIBLE
            } else {
                binding.bottomAppBar.visibility = View.GONE
                binding.bottomNavigationView.visibility = View.GONE
                binding.fab.visibility = View.GONE
            }
        }

        // Log fragment navigation event
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val fragmentName = resources.getResourceEntryName(destination.id)
            val navEventBundle = Bundle().apply {
                putString("fragment_name", fragmentName)
            }
            firebaseAnalytics.logEvent("navigation_event", navEventBundle)
        }

    }

    override fun onBackPressed() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val isAtRoot = navController.currentDestination?.id == navController.graph.startDestinationId

        if (isAtRoot) {
            finish()
        } else {
            navController.popBackStack()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
