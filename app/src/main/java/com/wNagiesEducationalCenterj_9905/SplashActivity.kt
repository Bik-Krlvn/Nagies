package com.wNagiesEducationalCenterj_9905

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.wNagiesEducationalCenterj_9905.base.BaseActivity
import com.wNagiesEducationalCenterj_9905.common.LOGIN_ROLE_OPTIONS
import com.wNagiesEducationalCenterj_9905.common.USER_INFO
import com.wNagiesEducationalCenterj_9905.common.UserAccount
import com.wNagiesEducationalCenterj_9905.common.delegate.lazyDeferred
import com.wNagiesEducationalCenterj_9905.data.db.Entities.UserEntity
import com.wNagiesEducationalCenterj_9905.ui.auth.RoleActivity
import com.wNagiesEducationalCenterj_9905.ui.auth.viewmodel.AuthViewModel
import com.wNagiesEducationalCenterj_9905.ui.parent.ParentNavigationActivity
import com.wNagiesEducationalCenterj_9905.ui.teacher.TeacherNavigationActivity
import com.wNagiesEducationalCenterj_9905.vo.AuthResource
import com.wNagiesEducationalCenterj_9905.vo.AuthStatus
import kotlinx.coroutines.launch
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast
import timber.log.Timber
import java.util.ArrayList
import javax.inject.Inject

class SplashActivity : BaseActivity() {
    @Inject
    lateinit var authViewModel: AuthViewModel
    private var userAccount: UserAccount? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configViewModel()
        skipLoginPage()
    }

    private fun subscribeObserver() {
        authViewModel.cachedUser.observe(this, Observer { resource ->
            when (resource.status) {
                AuthStatus.LOADING -> {
                    Timber.i("loading...")
                }
                AuthStatus.AUTHENTICATED -> {
                    loadAccountDashboard(resource)
                }
                AuthStatus.ERROR -> {
                    Timber.i(resource.message)
                    toast("${resource.message}")
                }
                AuthStatus.LOG_OUT -> {
                }
            }
        })
    }

    private fun loadAccountDashboard(resource: AuthResource<UserEntity>) {
        userAccount = when (resource.data?.role) {
            LOGIN_ROLE_OPTIONS[0].toLowerCase() -> {
                UserAccount.PARENT
            }
            LOGIN_ROLE_OPTIONS[1].toLowerCase() -> {
                UserAccount.TEACHER
            }
            else -> null
        }
        val userInfo = arrayListOf<String?>()
        userInfo.add(resource.data?.username)
        userInfo.add(resource.data?.photo)
        startDashboard(userInfo, userAccount)
    }

    private fun startDashboard(userInfo: ArrayList<String?>, userAccount: UserAccount?) {
        userAccount?.let { account ->
            when (account) {
                UserAccount.PARENT -> {
                    Timber.i("starting parent dashboard")
                    startActivity(intentFor<ParentNavigationActivity>(USER_INFO to userInfo))
                    finish()
                }
                UserAccount.TEACHER -> {
                    Timber.i("starting teachers dashboard")
                    startActivity(intentFor<TeacherNavigationActivity>(USER_INFO to userInfo))
                    finish()
                }
            }
        }
    }

    private fun skipLoginPage() = launch {
        val getLoginStatus by lazyDeferred {
            preferenceProvider.getUserLoginStatus()
        }
        when (getLoginStatus.await()) {
            true -> {
                Timber.i("user login status ${getLoginStatus.await()} ")
                authViewModel.authenticateWithToken()
            }
            false -> {
                Timber.i("user login status ${getLoginStatus.await()} ")
                startActivity(intentFor<RoleActivity>())
                finish()
            }
        }
    }

    private fun configViewModel() {
        authViewModel = ViewModelProviders.of(this, viewModelFactory)[AuthViewModel::class.java]
        subscribeObserver()
    }
}
