package org.dicio.skill.skill

import androidx.annotation.StringRes

sealed interface Permission {
    /**
     * A string resource with a short description of the permission.
     */
    @get:StringRes
    val name: Int

    /**
     * Normal permission, which can be requested easily from within the app.
     */
    data class NormalPermission(
        @StringRes override val name: Int,

        /**
         * The ID of the permission, used to check if it's granted or not, and to launch the
         * permission requester.
         */
        val id: String,
    ) : Permission

    /**
     * Secure permission, which requires the user to manually grant it by going to settings.
     */
    data class SecurePermission(
        @StringRes override val name: Int,

        /**
         * The ID of the permission, used to check if it's granted or not through
         * `Settings.Secure.getString()`.
         */
        val id: String,

        /**
         * The action for an intent that brings the user to the correct settings screen.
         */
        val settingsAction: String,
    ) : Permission
}
