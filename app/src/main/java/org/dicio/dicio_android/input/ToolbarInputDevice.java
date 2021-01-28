package org.dicio.dicio_android.input;

import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;

public class ToolbarInputDevice extends InputDevice {
    @Nullable private MenuItem textInputItem = null;

    public void setTextInputItem(@Nullable final MenuItem textInputItem) {
        if (this.textInputItem != null) {
            ((SearchView) this.textInputItem.getActionView()).setOnQueryTextListener(null);
        }

        this.textInputItem = textInputItem;
        if (textInputItem != null) {
            final SearchView textInputView = (SearchView) textInputItem.getActionView();
            textInputView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(final String query) {
                    notifyInputReceived(query);
                    textInputItem.collapseActionView();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(final String newText) {
                    return true;
                }
            });
        }
    }

    @Override
    public void load() {
    }

    @Override
    public void tryToGetInput() {
        if (textInputItem != null) {
            textInputItem.expandActionView();
        }
    }
}
