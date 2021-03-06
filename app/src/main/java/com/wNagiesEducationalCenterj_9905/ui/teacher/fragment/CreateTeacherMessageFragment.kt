package com.wNagiesEducationalCenterj_9905.ui.teacher.fragment


import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.wNagiesEducationalCenterj_9905.R
import com.wNagiesEducationalCenterj_9905.api.request.TeacherMessageRequest
import com.wNagiesEducationalCenterj_9905.base.BaseFragment
import com.wNagiesEducationalCenterj_9905.common.showAnyView
import com.wNagiesEducationalCenterj_9905.common.utils.InputValidationProvider
import com.wNagiesEducationalCenterj_9905.ui.teacher.viewmodel.TeacherViewModel
import com.wNagiesEducationalCenterj_9905.vo.Status
import kotlinx.android.synthetic.main.fragment_create_teacher_message.*
import timber.log.Timber
import javax.inject.Inject

class CreateTeacherMessageFragment : BaseFragment() {
    private lateinit var teacherViewModel: TeacherViewModel
    @Inject
    lateinit var validationProvider: InputValidationProvider
    private var isDeviceConnected = false
    private var isBusy = false
    private var snackBar: Snackbar? = null
    private var loadingIndicator: ProgressBar? = null
    private var dialog: AlertDialog.Builder? = null
    private var teacherMessageRequest: TeacherMessageRequest? = null
    private var studentNumber = ""
    private var studentName = ""
    private val studentNumberArr = ArrayList<String>()
    private val selectedItemListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (position <= 0) return
            studentName = parent?.getItemAtPosition(position).toString()
            studentNumber = studentNumberArr[position - 1]
            Timber.i("student ref: $studentNumber and student name: $studentName")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_teacher_message, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingIndicator = progressBar
        snackBar = Snackbar.make(root, "", Snackbar.LENGTH_LONG)

        spinner.onItemSelectedListener = selectedItemListener
        showLoadingDialog(false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        configureViewModel()
    }

    private fun configureViewModel() {
        teacherViewModel = ViewModelProvider(this, viewModelFactory)[TeacherViewModel::class.java]
        teacherViewModel.getUserToken()
        subscribeObservers()
    }

    private fun subscribeObservers() {
        getNetworkState()?.observe(viewLifecycleOwner, Observer { isNetworkAvail ->
            isDeviceConnected = isNetworkAvail
        })
        teacherViewModel.isSuccess.observe(viewLifecycleOwner, Observer {
            isBusy = when (it) {
                true -> {
                    showLoadingDialog(false)
                    showMessage(getString(R.string.sent_message_success))
                    clearMessage()
                    false
                }
                false -> {
                    showLoadingDialog(false)
                    showMessage(getString(R.string.sent_message_failed))
                    false
                }
            }
        })
        teacherViewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            showLoadingDialog(false)
            connectionErrorDialog(it)
            isBusy = false
        })


        teacherViewModel.userToken.observe(viewLifecycleOwner, Observer { token ->
            teacherViewModel.getClassStudent(token)
                .observe(viewLifecycleOwner, Observer { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            val studentNamesArr = ArrayList<String>()
                            studentNamesArr.add("To entire class")
                            resource.data?.forEach { student ->
                                studentNamesArr.add(student.studentName)
                                studentNumberArr.add(student.studentNo)
                            }
                            if (studentNamesArr.isNotEmpty()) {
                                val classAdapter = ArrayAdapter(
                                    context!!,
                                    android.R.layout.select_dialog_singlechoice,
                                    studentNamesArr
                                )
                                spinner.adapter = classAdapter
                                classAdapter.notifyDataSetChanged()
                            }
                            showLoadingDialog(false)
                        }
                        Status.ERROR -> {
                            showLoadingDialog(false)
                        }
                        Status.LOADING -> {
                            showLoadingDialog()
                        }
                    }
                })
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_send_message, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_send -> {
                Timber.i("selected item: $studentNumber and name: $studentName")
                if (!isBusy) {
                    val imm =
                        context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.hideSoftInputFromWindow(view?.windowToken, 0)
                    preparingToMessage()
                }
            }
            R.id.action_close -> {
                showCancelAlertDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCancelAlertDialog() {
        dialog = context?.let { AlertDialog.Builder(it) }
        dialog?.setTitle("Cancel Alert")
        dialog?.setMessage("Do you want to clear message ?")
        dialog?.setPositiveButton("yes") { dialog, _ ->
            clearMessage()
            dialog.dismiss()
        }
        dialog?.setNegativeButton("cancel", null)
        dialog?.setCancelable(false)
        dialog?.show()

    }


    private fun preparingToMessage() {
        when (isDeviceConnected) {
            true -> {
                sendMessage()
            }
            false -> {
                showMessage(getString(R.string.network_state_no_connection))
            }
        }

    }

    private fun sendMessage() {
        if (!validationProvider.isEditTextFilled(
                et_message_content,
                getString(R.string.message_field_empty_error)
            )
        ) {
            return
        }
        val content: String? = et_message_content.text.toString()
        teacherMessageRequest = content?.let { TeacherMessageRequest(it,studentName,studentNumber) }
        teacherMessageRequest?.let {
            isBusy = true
            showLoadingDialog()
            teacherViewModel.sendTeacherMessage(it)
        }
    }

    private fun showLoadingDialog(show: Boolean = true) {
        showAnyView(progressBar, null, null, show) { view, _, _, visible ->
            if (visible) {
                (view as ProgressBar).visibility = View.VISIBLE
            } else {
                (view as ProgressBar).visibility = View.GONE
            }
        }
    }

    private fun showMessage(message: String) {
        showAnyView(snackBar, message, null, true) { view, msg, _, visible ->
            if (visible) {
                (view as Snackbar).setText(msg!!).show()
            }
        }
    }

    private fun clearMessage() {
        et_message_content.text.clear()
    }

    private fun connectionErrorDialog(@StringRes error_message: Int?) {
        snackBar = error_message?.let { Snackbar.make(root, it, Snackbar.LENGTH_LONG) }
        snackBar?.show()
    }

}
