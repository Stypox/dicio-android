package org.stypox.dicio.settings.skill;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.stypox.dicio.R;
import org.stypox.dicio.skills.SkillHandler;
import org.stypox.dicio.util.ShareUtils;
import org.dicio.skill.SkillContext;

public class SkillsFragment extends Fragment {

    private static final String DICIO_NUMBERS_URL = "https://github.com/Stypox/dicio-numbers";

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.pref_skills, container, false);
        final RecyclerView skillRecyclerView = view.findViewById(R.id.skillRecyclerView);
        final TextView numberLibraryTextView = view.findViewById(R.id.numberLibraryTextView);

        skillRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        skillRecyclerView.setAdapter(new SkillsAdapter(
                this, inflater, SkillHandler.getAllSkillInfoList()));

        // the skill context should always already have been initialized
        final SkillContext skillContext = SkillHandler.getSkillContext();
        numberLibraryTextView.setVisibility(skillContext != null
                && skillContext.getNumberParserFormatter() == null ? View.VISIBLE : View.GONE);
        numberLibraryTextView.setOnClickListener(
                v -> ShareUtils.openUrlInBrowser(requireContext(), DICIO_NUMBERS_URL));
        numberLibraryTextView.setOnLongClickListener(v -> {
            ShareUtils.copyToClipboard(requireContext(), DICIO_NUMBERS_URL);
            return true;
        });

        return view;
    }
}
