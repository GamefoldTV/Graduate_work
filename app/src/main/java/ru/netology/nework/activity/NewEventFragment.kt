package ru.netology.nework.activity

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.activity.MapsNewMarkerFragment.Companion.latArg
import ru.netology.nework.activity.MapsNewMarkerFragment.Companion.longArg
import ru.netology.nework.databinding.FragmentNewEventBinding
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.view.load
import ru.netology.nework.viewmodel.EventViewModel
import java.util.*


@AndroidEntryPoint
class NewEventFragment : Fragment() {
    private val viewModel: EventViewModel by viewModels(
        ownerProducer = ::requireParentFragment,
    )

    private var fragmentBinding: FragmentNewEventBinding? = null

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
                    viewModel.changeDateTime(
                        it.editTextDate.text.toString(),
                        it.editTextTime.text.toString()
                    )
                    viewModel.changeContent(it.editContent.text.toString())
                    viewModel.changeLink(it.editLink.text.toString())
                    viewModel.changeCoords(
                        viewModel.coords.value?.lat,
                        viewModel.coords.value?.long
                    )
                    viewModel.changeSpeakers(it.editSpeakers.text.toString())
                    viewModel.saveEvent()
                    AndroidUtils.hideKeyboard(requireView())
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
        val binding = FragmentNewEventBinding.inflate(
            inflater,
            container,
            false
        )
        fragmentBinding = binding


        val editPost = viewModel.getEditEvent()
        binding.editContent.setText(editPost?.content)
        binding.editLink.setText(editPost?.link)
        val lat = editPost?.coords?.lat
        val long = editPost?.coords?.long
        if (lat != null && long != null)
            viewModel.changeCoordsFromMap(lat, long)
        val attachment = editPost?.attachment
        if (attachment != null) viewModel.changePhoto(Uri.parse(attachment.url), null)
        if (attachment?.url != null) {
            binding.AttachmentImage.load(attachment.url)
            binding.AttachmentContainer.visibility = View.VISIBLE
        }

        var cal = Calendar.getInstance()
        binding.editTextDate.setText(SimpleDateFormat("dd.MM.yyyy").format(System.currentTimeMillis()))
        binding.editTextTime.setText(SimpleDateFormat("HH:mm").format(System.currentTimeMillis()))

        val dateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val myFormat = "dd.MM.yyyy" // mention the format you need
                val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
                binding.editTextDate.setText(sdf.format(cal.time))
            }

        val timeSetListener = TimePickerDialog.OnTimeSetListener { view, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            //   DateFormat.is24HourFormat(binding.root.context)
            val myFormat = "HH:mm" // mention the format you need
            val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
            binding.editTextTime.setText(sdf.format(cal.time))
        }

        binding.buttonChangeDate.setOnClickListener {
            DatePickerDialog(
                binding.root.context, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.buttonChangeTime.setOnClickListener {
            TimePickerDialog(
                binding.root.context, timeSetListener,
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(binding.root.context)
            ).show()
        }

        binding.editContent.requestFocus()

        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(it.data),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    Activity.RESULT_OK -> {
                        val uri: Uri? = it.data?.data
                        viewModel.changePhoto(uri, uri?.toFile())
                    }
                }
            }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.GALLERY)
                .galleryMimeTypes(
                    arrayOf(
                        "image/png",
                        "image/jpeg",
                    )
                )
                .createIntent(pickPhotoLauncher::launch)
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.CAMERA)
                .createIntent(pickPhotoLauncher::launch)
        }

        binding.removePhoto.setOnClickListener {
            viewModel.changePhoto(null, null)
        }

        binding.buttonLocationOn.setOnClickListener {
            findNavController().navigate(R.id.action_newEventFragment_to_mapsNewMarkerFragment,
                Bundle().apply {
                    latArg = viewModel.coords.value?.lat?.toDouble() ?: coordinatesMoscow.latitude
                    longArg =
                        viewModel.coords.value?.long?.toDouble() ?: coordinatesMoscow.longitude
                })
        }

        binding.buttonLocationOff.setOnClickListener {
            viewModel.changeCoordsFromMap("", "")
            viewModel.changeCoords("", "")
        }


        viewModel.eventCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        viewModel.coords.observe(viewLifecycleOwner) {
            binding.textCoordLat.text = viewModel.coords.value?.lat
            binding.textCoordLong.text = viewModel.coords.value?.long
        }

        viewModel.photo.observe(viewLifecycleOwner) {
            val url = viewModel.getEditEvent()?.attachment?.url
            if (it.uri == null) {
                binding.AttachmentContainer.visibility = View.GONE
                return@observe
            }
            // загрузили новый аттач
            binding.AttachmentContainer.visibility = View.VISIBLE
            binding.AttachmentImage.setImageURI(it.uri)
        }

        return binding.root
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }
}