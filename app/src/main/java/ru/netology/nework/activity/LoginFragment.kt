package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.databinding.FragmentLoginBinding
import ru.netology.nework.util.AndroidUtils.hideKeyboard
import ru.netology.nework.view.afterTextChanged
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.PostViewModel

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var fragmentBinding: FragmentLoginBinding? = null

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment,
    )

    private val viewModelAuth: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentLoginBinding.inflate(
            inflater,
            container,
            false
        )
        fragmentBinding = binding

        with(binding) {
            login.requestFocus()

            checkBoxRegister.setOnClickListener {
                name.isVisible = checkBoxRegister.isChecked
            }

            login.afterTextChanged {
                viewModelAuth.loginDataChanged(
                    login.text.toString(),
                    password.text.toString()
                )
            }

            password.afterTextChanged {
                viewModelAuth.loginDataChanged(
                    login.text.toString(),
                    password.text.toString()
                )
            }

            button.setOnClickListener {
                hideKeyboard(requireView())

                if (checkBoxRegister.isChecked) {
                    viewModelAuth.userRegistration(
                        // "netology",1
                        binding.login.text.toString(),
                        // "secret",
                        binding.password.text.toString(),
                        // Куликова Ольга Ивановна
                        binding.name.text.toString()
                    )
                } else {
                    viewModelAuth.userAuthentication(
                        // "netology",1
                        binding.login.text.toString(),
                        // "secret",
                        binding.password.text.toString()
                    )
                }
            }

            viewModelAuth.loginFormState.observe(viewLifecycleOwner) { state ->
                button.isEnabled = state.isDataValid
                loading.isVisible = state.isLoading
                if (state.isError) {
                    Toast.makeText(context, "Ошибка при авторизации", Toast.LENGTH_LONG)
                        .show()
                }
            }

            viewModelAuth.data.observe(viewLifecycleOwner) {
                if (it.id != 0L) findNavController().navigateUp()
            }
            return root
        }
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }
}


