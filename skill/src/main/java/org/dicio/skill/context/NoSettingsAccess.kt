package org.dicio.skill.context;

class NoSettingsAccess : SettingsAccess {
    override var wakeDeviceEnabled: Boolean = false
}
