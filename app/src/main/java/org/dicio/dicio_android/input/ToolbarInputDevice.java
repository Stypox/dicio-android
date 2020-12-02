package org.dicio.dicio_android.input;

import android.view.MenuItem;

import androidx.appcompat.widget.SearchView;

public class ToolbarInputDevice extends InputDevice {
    private MenuItem textInputItem;

    public void setTextInputItem(final MenuItem textInputItem) {
        this.textInputItem = textInputItem;
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

    @Override
    public void tryToGetInput() {
        textInputItem.expandActionView();
    }
}
