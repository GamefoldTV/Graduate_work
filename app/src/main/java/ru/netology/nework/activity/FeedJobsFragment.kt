package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.activity.MapsPreviewFragment.Companion.doubleArg1
import ru.netology.nework.adapter.JobAdapter
import ru.netology.nework.adapter.OnInteractionJobListener
import ru.netology.nework.databinding.FragmentJobsFeedBinding
import ru.netology.nework.dto.Job
import ru.netology.nework.util.LongArg
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.PostViewModel

@AndroidEntryPoint
class FeedJobsFragment : Fragment() {

    companion object{
        var Bundle.userId: Long by LongArg
    }

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment,
    )

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentJobsFeedBinding.inflate(inflater, container, false)

        val userId = (arguments?.userId ?: 0).toLong()
        viewModel.loadJobs(viewModel.getCurrentUser())

        val adapter = JobAdapter(object : OnInteractionJobListener {
            override fun onEdit(job: Job) {
                viewModel.editJob(job)
                findNavController().navigate(R.id.action_feedJobsFragment_to_newJobFragment)
            }

            override fun onRemove(job: Job) {
                viewModel.removeJobById(job.id)

            }
        })

        binding.list.adapter = adapter

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swiperefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.loadEvents() }
                    .show()
            }
        }
        viewModel.dataMyJobs.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.jobs)
            binding.emptyText.isVisible = state.empty
        }

        binding.swiperefresh.setOnRefreshListener {
        //    if (authViewModel.data.value != null)
          //      viewModel.refreshJobs(authViewModel.data.value!!.id)

            viewModel.refreshJobs(userId)
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedJobsFragment_to_newJobFragment)
        }

        return binding.root
    }
}

