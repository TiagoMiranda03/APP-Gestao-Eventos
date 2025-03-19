package com.seuapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleExpandableListAdapter
import androidx.fragment.app.Fragment
import com.example.gestao_eventos.databinding.FragmentFAQBinding


class FAQFragment : Fragment() {

    private var _binding: FragmentFAQBinding? = null
    private val binding get() = _binding!!

    private lateinit var faqList: LinkedHashMap<String, List<String>>

    companion object{
        fun newInstance(): FAQFragment{
            return FAQFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFAQBinding.inflate(inflater, container, false)

        setupFAQ()
        return binding.root
    }

    private fun setupFAQ() {
        val expandableListView = binding.expandableListView

        faqList = linkedMapOf(
            "Lorem Ipsum" to listOf("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla tempus ipsum et urna aliquam euismod. Praesent molestie, nunc at ultricies scelerisque, mauris lorem gravida urna, id egestas nisl nisi ac lectus. Sed congue hendrerit risus, nec egestas sem ultricies sit amet. In volutpat nisi a sem tempor, ac dignissim velit luctus. Suspendisse ac dui egestas magna consectetur rhoncus quis non mi. Phasellus vestibulum odio et risus pharetra blandit. Mauris ante quam, dignissim vitae tortor eu, fermentum dictum orci. Etiam imperdiet eget purus ut aliquet. Pellentesque ut dolor quis odio convallis faucibus. Cras efficitur nisl aliquet lectus consequat, eget dignissim mi ultricies. Duis efficitur, leo sed condimentum interdum, orci turpis tristique urna, quis lacinia ipsum sapien quis nisl."),
            "Lorem Ipsum1" to listOf("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla tempus ipsum et urna aliquam euismod. Praesent molestie, nunc at ultricies scelerisque, mauris lorem gravida urna, id egestas nisl nisi ac lectus. Sed congue hendrerit risus, nec egestas sem ultricies sit amet. In volutpat nisi a sem tempor, ac dignissim velit luctus. Suspendisse ac dui egestas magna consectetur rhoncus quis non mi. Phasellus vestibulum odio et risus pharetra blandit. Mauris ante quam, dignissim vitae tortor eu, fermentum dictum orci. Etiam imperdiet eget purus ut aliquet. Pellentesque ut dolor quis odio convallis faucibus. Cras efficitur nisl aliquet lectus consequat, eget dignissim mi ultricies. Duis efficitur, leo sed condimentum interdum, orci turpis tristique urna, quis lacinia ipsum sapien quis nisl."),
            "Lorem Ipsum2" to listOf("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla tempus ipsum et urna aliquam euismod. Praesent molestie, nunc at ultricies scelerisque, mauris lorem gravida urna, id egestas nisl nisi ac lectus. Sed congue hendrerit risus, nec egestas sem ultricies sit amet. In volutpat nisi a sem tempor, ac dignissim velit luctus. Suspendisse ac dui egestas magna consectetur rhoncus quis non mi. Phasellus vestibulum odio et risus pharetra blandit. Mauris ante quam, dignissim vitae tortor eu, fermentum dictum orci. Etiam imperdiet eget purus ut aliquet. Pellentesque ut dolor quis odio convallis faucibus. Cras efficitur nisl aliquet lectus consequat, eget dignissim mi ultricies. Duis efficitur, leo sed condimentum interdum, orci turpis tristique urna, quis lacinia ipsum sapien quis nisl."),
        )

        val groupList = faqList.keys.toList()
        val childList = faqList.values.toList()

        val groupData = ArrayList<HashMap<String, String>>()
        for (group in groupList) {
            val map = HashMap<String, String>()
            map["groupName"] = group
            groupData.add(map)
        }

        val childData = ArrayList<ArrayList<HashMap<String, String>>>()
        for (children in childList) {
            val childrenMaps = ArrayList<HashMap<String, String>>()
            for (child in children) {
                val map = HashMap<String, String>()
                map["childName"] = child
                childrenMaps.add(map)
            }
            childData.add(childrenMaps)
        }

        val groupFrom = arrayOf("groupName")
        val groupTo = intArrayOf(android.R.id.text1)
        val childFrom = arrayOf("childName")
        val childTo = intArrayOf(android.R.id.text1)

        val adapter = SimpleExpandableListAdapter(
            requireContext(),
            groupData,
            android.R.layout.simple_expandable_list_item_1, groupFrom, groupTo,
            childData,
            android.R.layout.simple_list_item_1, childFrom, childTo
        )

        expandableListView.setAdapter(adapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
