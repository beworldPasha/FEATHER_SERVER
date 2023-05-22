package com.app.feather

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.app.feather.databinding.FragmentSignUpBinding
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout

class SignUpFragment : Fragment() {
    private lateinit var binding: FragmentSignUpBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)
        binding = FragmentSignUpBinding.bind(view)

        setupEmailValidation()
        setupPasswordValidation()
        setupRepeatedPasswordValidation()

        binding.signUpButton.setOnClickListener {
            validateAndSignUp()
        }
        return view
    }

    private fun scrollToSignIn(duration: Long) {
        val pager = activity?.findViewById<ViewPager2>(R.id.pager)
        val currentPosition = pager?.currentItem
        val nextPage = currentPosition?.minus(1)

        if (nextPage != null) {
            if (nextPage < (pager.adapter?.itemCount ?: 0)) {
                val animator = ValueAnimator.ofInt(currentPosition, nextPage)
                animator.duration = duration

                animator.addUpdateListener { animation ->
                    val position = animation.animatedValue as Int
                    pager.setCurrentItem(position, true)
                }
                animator.start()
            }
        }
    }

    private fun setupEmailValidation() {
        val checker = ValidChecker()
        binding.emailEditor.doAfterTextChanged {
            val emailText = binding.emailEditor.text.toString()
            val isCorrectEmail = checker.checkEmailValid(emailText)
            val isEmpty = emailText.isEmpty()

            if (isCorrectEmail || isEmpty) {
                clearError(binding.emailLayout)
            } else {
                setError(binding.emailLayout, R.string.emailError)
            }
        }
    }

    private fun setupPasswordValidation() {
        val checker = ValidChecker()
        binding.passwordEditor.doAfterTextChanged {
            val isCorrectPassword = checker.checkPassword(binding.passwordEditor.text.toString())

            if (isCorrectPassword) {
                clearError(binding.passwordLayout)
            } else {
                setError(binding.passwordLayout, R.string.passwordLengthError)
            }
        }
    }

    private fun setupRepeatedPasswordValidation() {
        binding.repeatPasswordEditor.doAfterTextChanged {
            val password = binding.passwordEditor.text.toString()
            val repeatedPassword = binding.repeatPasswordEditor.text.toString()

            if (password == repeatedPassword || repeatedPassword.isEmpty()) {
                clearError(binding.repeatPasswordLayout)
            } else {
                setError(binding.repeatPasswordLayout, R.string.repeatedPasswordError)
            }
        }
    }

    private fun validateAndSignUp() {
        fun showError(layout: TextInputLayout, errorMessageRes: Int) {
            layout.errorIconDrawable = null
            layout.error = getString(errorMessageRes)
        }

        val emailIsEmpty = binding.emailEditor.text.toString().isEmpty()
        val passwordIsEmpty = binding.passwordEditor.text.toString().isEmpty()
        val repeatedPasswordIsEmpty = binding.repeatPasswordEditor.text.toString().isEmpty()

        if (emailIsEmpty) {
            showError(binding.emailLayout, R.string.emptyEditorError)
        }

        if (passwordIsEmpty) {
            showError(binding.passwordLayout, R.string.emptyEditorError)
        }

        if (repeatedPasswordIsEmpty) {
            showError(
                binding.repeatPasswordLayout,
                R.string.emptyEditorError
            )
        }

        if (!binding.policyCheckBox.isChecked) {
            binding.policyCheckBox.error = getString(R.string.errorPolicy)
        } else {
            binding.policyCheckBox.error = null
        }

        val isEmailValid = binding.emailLayout.error == null
        val isPasswordValid =
            binding.passwordLayout.error == null && binding.repeatPasswordLayout.error == null
        val isPrivacyChecked = binding.policyCheckBox.isChecked

        if (isEmailValid && isPasswordValid && isPrivacyChecked) {
            APIManager(context).signUp(
                binding.emailEditor.text.toString(), binding.passwordEditor.text.toString()
            )
            showSaveCredentialsSnackBar()
        }
    }

    private fun showSaveCredentialsSnackBar() {
        val saveCredentialsSnackBar =
            Snackbar.make(
                binding.root,
                getString(R.string.saveCredentialsQuestion), Snackbar.LENGTH_LONG
            )

        saveCredentialsSnackBar.setAction("Да") {
            val email = binding.emailEditor.text.toString()
            val password = binding.passwordEditor.text.toString()
            AccountsManager(context).addAccount(email, password)

            scrollToSignIn(500)
        }
        saveCredentialsSnackBar.addCallback(
            object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    scrollToSignIn(0)
                }
            })

        saveCredentialsSnackBar.show()
    }

    private fun clearError(layout: TextInputLayout) {
        layout.errorIconDrawable = null
        layout.error = null
    }

    private fun setError(layout: TextInputLayout, errorMessageRes: Int) {
        layout.errorIconDrawable = null
        layout.error = getString(errorMessageRes)
    }
}