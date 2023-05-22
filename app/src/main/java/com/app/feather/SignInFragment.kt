package com.app.feather

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.app.feather.databinding.FragmentSignInBinding
import com.google.android.material.textfield.TextInputLayout

class SignInFragment : Fragment() {
    private lateinit var binding: FragmentSignInBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)
        binding = FragmentSignInBinding.bind(view)

        setupEmailValidation()
        setupPasswordValidation()

        binding.signInButton.setOnClickListener { validateAndSignUp() }

        return view
    }

    override fun onResume() {
        super.onResume()
        showAccountSelectionDialog()
    }

    private fun validateAndSignUp() {
        fun showError(layout: TextInputLayout, errorMessageRes: Int) {
            layout.errorIconDrawable = null
            layout.error = getString(errorMessageRes)
        }

        val emailIsEmpty = binding.emailEditor.text.toString().isEmpty()
        val passwordIsEmpty = binding.passwordEditor.text.toString().isEmpty()

        if (emailIsEmpty) {
            showError(binding.emailLayout, R.string.emptyEditorError)
        }

        if (passwordIsEmpty) {
            showError(binding.passwordLayout, R.string.emptyEditorError)
        }

        val isEmailValid = binding.emailLayout.error == null
        val isPasswordValid = binding.passwordLayout.error == null

        if (isEmailValid && isPasswordValid) {
            APIManager(context).signIn(
                binding.emailEditor.text.toString(), binding.passwordEditor.text.toString()
            )
        }
    }

    private fun showAccountSelectionDialog() {
        val accountManager = AccountsManager(context)
        val accounts = accountManager.getAccounts()
        if (accounts.isEmpty()) return

        val builder = AlertDialog.Builder(context)
        val accountNames = accounts.map { it.name }.toTypedArray()

        builder.setTitle(getString(R.string.choosingAccountsHeader))
        builder.setItems(accountNames) { dialog: DialogInterface, which: Int ->
            val selectedAccount = accounts[which]

            val email = selectedAccount.name
            val password = accountManager.getPassword(selectedAccount)

            binding.emailEditor.setText(email)
            binding.passwordEditor.setText(password)

            dialog.dismiss()
        }

        builder.show()
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

    private fun clearError(layout: TextInputLayout) {
        layout.errorIconDrawable = null
        layout.error = null
    }

    private fun setError(layout: TextInputLayout, errorMessageRes: Int) {
        layout.errorIconDrawable = null
        layout.error = getString(errorMessageRes)
    }
}