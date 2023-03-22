package ru.netology.nework.activity

import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentNewJobBinding
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.viewmodel.PostViewModel
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*


@AndroidEntryPoint
class NewJobFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment,
    )

    private var fragmentBinding: FragmentNewJobBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_new_post, menu) // связка с созданным меню
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> {
                fragmentBinding?.let {
                    if (it.editName.text.toString().isEmpty()) {
                        Toast.makeText(
                            context,
                            getString(R.string.new_job_empty_name),
                            Toast.LENGTH_LONG
                        )
                            .show()
                        it.editName.requestFocus()
                    } else {
                        if (it.editPosition.text.toString().isEmpty()) {
                            Toast.makeText(
                                context,
                                getString(R.string.new_job_empty_position),
                                Toast.LENGTH_LONG
                            )
                                .show()
                            it.editPosition.requestFocus()
                        } else {
                            viewModel.changeJobStart(it.editStartDate.text.toString())
                            viewModel.changeJobFinish(it.editFinishDate.text.toString())
                            viewModel.changeNameJob(it.editName.text.toString())
                            viewModel.changePositionJob(it.editPosition.text.toString())
                            viewModel.changeLinkJob(it.editLink.text.toString())
                            viewModel.saveJob(viewModel.getCurrentUser())
                            AndroidUtils.hideKeyboard(requireView())
                        }
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewJobBinding.inflate(
            inflater,
            container,
            false
        )
        fragmentBinding = binding

        val editJob = viewModel.getEditJob()
        val cal = Calendar.getInstance()

        with(binding) {
            editStartDate.setText(
                SimpleDateFormat(dateFormat).format(
                    if (editJob?.start != "")
                        Date.from(
                            Instant.from(
                                DateTimeFormatter.ISO_INSTANT.parse(editJob?.start)
                            )
                        )
                    else
                        cal.time
                )
            )
            editFinishDate.setText(
                if (editJob?.finish != null )
                    SimpleDateFormat(dateFormat).format(
                        Date.from(
                            Instant.from(
                                DateTimeFormatter.ISO_INSTANT.parse(editJob?.finish)
                            )
                        )
                    )
                else
                    ""
            )
            editName.setText(editJob?.name)
            editPosition.setText(editJob?.position)
            editLink.setText(editJob?.link)
        }

        val dateStartSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
                binding.editStartDate.setText(sdf.format(cal.time))
            }

        binding.buttonChangeStartDate.setOnClickListener {
            DatePickerDialog(
                binding.root.context, dateStartSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val dateFinishSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
                binding.editFinishDate.setText(sdf.format(cal.time))
            }

        binding.buttonChangeFinishDate.setOnClickListener {
            DatePickerDialog(
                binding.root.context, dateFinishSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        viewModel.jobCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }
}